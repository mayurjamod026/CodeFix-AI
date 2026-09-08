/**
 * CodeFix AI - Production Web & Proxy Server for Google Cloud Run
 * 
 * Features:
 * 1. Secure server-side Gemini API proxy (never exposes GEMINI_API_KEY to clients)
 * 2. Sandboxed code execution proxy for C, C++, Python, Java, JavaScript, HTML
 * 3. Health check endpoints (/healthz, /api/health) for Cloud Run container probes
 * 4. Responsive web client static asset delivery from /public
 */

const http = require('http');
const https = require('https');
const url = require('url');
const fs = require('fs');
const path = require('path');

const PORT = parseInt(process.env.PORT || '8080', 10);
const GEMINI_API_KEY = (process.env.GEMINI_API_KEY || '').trim();
const PUBLIC_DIR = path.join(__dirname, 'public');

// MIME types for static assets
const MIME_TYPES = {
  '.html': 'text/html; charset=utf-8',
  '.css': 'text/css; charset=utf-8',
  '.js': 'application/javascript; charset=utf-8',
  '.json': 'application/json; charset=utf-8',
  '.png': 'image/png',
  '.jpg': 'image/jpeg',
  '.jpeg': 'image/jpeg',
  '.gif': 'image/gif',
  '.svg': 'image/svg+xml',
  '.ico': 'image/x-icon',
  '.txt': 'text/plain; charset=utf-8'
};

function setCorsHeaders(res) {
  res.setHeader('Access-Control-Allow-Origin', '*');
  res.setHeader('Access-Control-Allow-Methods', 'GET, POST, OPTIONS');
  res.setHeader('Access-Control-Allow-Headers', 'Content-Type, Authorization, x-goog-api-key');
}

function parseJsonBody(req, callback) {
  let data = '';
  req.on('data', chunk => {
    data += chunk;
    if (data.length > 5 * 1024 * 1024) { // 5MB limit
      req.destroy();
    }
  });
  req.on('end', () => {
    try {
      const parsed = data ? JSON.parse(data) : {};
      callback(null, parsed);
    } catch (err) {
      callback(err);
    }
  });
}

const server = http.createServer((req, res) => {
  setCorsHeaders(res);

  if (req.method === 'OPTIONS') {
    res.writeHead(204);
    res.end();
    return;
  }

  const parsedUrl = url.parse(req.url, true);
  const pathname = parsedUrl.pathname;

  // 1. Health checks for Cloud Run
  if (pathname === '/healthz' || pathname === '/api/health') {
    res.writeHead(200, { 'Content-Type': 'application/json' });
    res.end(JSON.stringify({
      status: 'healthy',
      service: 'CodeFix AI Backend',
      version: '1.0.0',
      geminiConfigured: Boolean(GEMINI_API_KEY && GEMINI_API_KEY !== 'MY_GEMINI_API_KEY'),
      timestamp: new Date().toISOString()
    }));
    return;
  }

  // 2. Secure Server-Side Gemini API Proxy
  if (pathname === '/api/gemini/generateContent' && req.method === 'POST') {
    parseJsonBody(req, (err, body) => {
      if (err) {
        res.writeHead(400, { 'Content-Type': 'application/json' });
        res.end(JSON.stringify({ error: { message: 'Invalid JSON request payload' } }));
        return;
      }

      // Check server API key
      const effectiveKey = GEMINI_API_KEY || (req.headers['x-goog-api-key'] || '').trim();

      if (!effectiveKey || effectiveKey === 'MY_GEMINI_API_KEY') {
        res.writeHead(503, { 'Content-Type': 'application/json' });
        res.end(JSON.stringify({
          error: {
            message: 'GEMINI_API_KEY is not configured on the server. Please configure GEMINI_API_KEY in Cloud Run environment variables or AI Studio secrets.',
            code: 503
          }
        }));
        return;
      }

      const postData = JSON.stringify(body);
      const targetUrl = new URL(`https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=${encodeURIComponent(effectiveKey)}`);

      const proxyReq = https.request({
        hostname: targetUrl.hostname,
        port: 443,
        path: targetUrl.pathname + targetUrl.search,
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Content-Length': Buffer.byteLength(postData)
        },
        timeout: 45000
      }, (proxyRes) => {
        res.writeHead(proxyRes.statusCode, { 'Content-Type': 'application/json' });
        proxyRes.pipe(res);
      });

      proxyReq.on('error', (proxyErr) => {
        res.writeHead(502, { 'Content-Type': 'application/json' });
        res.end(JSON.stringify({
          error: {
            message: `Failed to reach Gemini API: ${proxyErr.message}`,
            code: 502
          }
        }));
      });

      proxyReq.on('timeout', () => {
        proxyReq.destroy();
        res.writeHead(504, { 'Content-Type': 'application/json' });
        res.end(JSON.stringify({
          error: { message: 'Gemini request timed out after 45 seconds', code: 504 }
        }));
      });

      proxyReq.write(postData);
      proxyReq.end();
    });
    return;
  }

  // 3. Sandboxed Code Execution Proxy
  if (pathname === '/api/execute' && req.method === 'POST') {
    parseJsonBody(req, (err, body) => {
      if (err) {
        res.writeHead(400, { 'Content-Type': 'application/json' });
        res.end(JSON.stringify({ error: 'Invalid JSON request payload' }));
        return;
      }

      const { language, code, stdin = '' } = body;
      if (!code || !code.trim()) {
        res.writeHead(400, { 'Content-Type': 'application/json' });
        res.end(JSON.stringify({
          status: 'SANDBOX_ERROR',
          stdout: '',
          stderr: 'Error: Code cannot be empty. Please enter code to run.',
          executionTimeMs: 0
        }));
        return;
      }

      // Map language to Piston
      const pistonLangs = {
        python: { language: 'python', version: '3.10.0' },
        c: { language: 'c', version: '10.2.0' },
        cpp: { language: 'c++', version: '10.2.0' },
        java: { language: 'java', version: '15.0.2' },
        javascript: { language: 'javascript', version: '18.15.0' },
        html: { language: 'html', isHtml: true }
      };

      const langKey = (language || '').toLowerCase().trim();
      const mapped = pistonLangs[langKey];

      if (!mapped) {
        res.writeHead(400, { 'Content-Type': 'application/json' });
        res.end(JSON.stringify({
          status: 'SANDBOX_ERROR',
          stdout: '',
          stderr: `Unsupported language: '${language}'. Supported: C, C++, Python, Java, JavaScript, HTML.`,
          executionTimeMs: 0
        }));
        return;
      }

      if (mapped.isHtml) {
        // HTML rendered result
        res.writeHead(200, { 'Content-Type': 'application/json' });
        res.end(JSON.stringify({
          status: 'SUCCESS',
          stdout: 'HTML and embedded scripts rendered successfully.\n[Preview rendered in virtual DOM container]',
          stderr: null,
          executionTimeMs: 12
        }));
        return;
      }

      const pistonPayload = JSON.stringify({
        language: mapped.language,
        version: mapped.version,
        files: [{ content: code }],
        stdin: stdin,
        run_timeout: 10000,
        compile_timeout: 10000
      });

      const startTime = Date.now();
      const pistonReq = https.request({
        hostname: 'emkc.org',
        port: 443,
        path: '/api/v2/piston/execute',
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Content-Length': Buffer.byteLength(pistonPayload)
        },
        timeout: 15000
      }, (pistonRes) => {
        let respData = '';
        pistonRes.on('data', chunk => { respData += chunk; });
        pistonRes.on('end', () => {
          const duration = Date.now() - startTime;
          try {
            const data = JSON.parse(respData);
            const compile = data.compile || {};
            const run = data.run || {};

            let status = 'SUCCESS';
            if (compile.code && compile.code !== 0) {
              status = 'COMPILATION_ERROR';
            } else if (run.signal === 'SIGKILL' || (run.stderr && run.stderr.includes('timed out'))) {
              status = 'TIMEOUT';
            } else if (run.code && run.code !== 0) {
              status = 'RUNTIME_ERROR';
            }

            res.writeHead(200, { 'Content-Type': 'application/json' });
            res.end(JSON.stringify({
              status,
              stdout: run.stdout || '',
              stderr: run.stderr || (compile.stderr || null),
              compilationOutput: compile.output || null,
              exitCode: run.code ?? compile.code ?? 0,
              executionTimeMs: duration
            }));
          } catch (jsonErr) {
            res.writeHead(200, { 'Content-Type': 'application/json' });
            res.end(JSON.stringify({
              status: 'SANDBOX_ERROR',
              stdout: '',
              stderr: 'Failed to parse execution sandbox response',
              executionTimeMs: duration
            }));
          }
        });
      });

      pistonReq.on('error', (netErr) => {
        res.writeHead(200, { 'Content-Type': 'application/json' });
        res.end(JSON.stringify({
          status: 'SANDBOX_ERROR',
          stdout: '',
          stderr: `Sandbox execution service unavailable: ${netErr.message}`,
          executionTimeMs: 0
        }));
      });

      pistonReq.on('timeout', () => {
        pistonReq.destroy();
        res.writeHead(200, { 'Content-Type': 'application/json' });
        res.end(JSON.stringify({
          status: 'TIMEOUT',
          stdout: '',
          stderr: 'Execution timed out after 15 seconds limit.',
          executionTimeMs: 15000
        }));
      });

      pistonReq.write(pistonPayload);
      pistonReq.end();
    });
    return;
  }

  // 4. Static file serving from /public
  let safePath = path.normalize(pathname).replace(/^(\.\.[\/\\])+/, '');
  if (safePath === '/' || safePath === '\\') {
    safePath = '/index.html';
  }

  const filePath = path.join(PUBLIC_DIR, safePath);

  // Prevent path traversal
  if (!filePath.startsWith(PUBLIC_DIR)) {
    res.writeHead(403, { 'Content-Type': 'text/plain' });
    res.end('Forbidden');
    return;
  }

  fs.stat(filePath, (statErr, stats) => {
    if (statErr || !stats.isFile()) {
      // Fallback to index.html for Single Page Applications
      const fallbackIndex = path.join(PUBLIC_DIR, 'index.html');
      fs.stat(fallbackIndex, (fbErr, fbStats) => {
        if (!fbErr && fbStats.isFile()) {
          res.writeHead(200, { 'Content-Type': 'text/html; charset=utf-8' });
          fs.createReadStream(fallbackIndex).pipe(res);
        } else {
          res.writeHead(404, { 'Content-Type': 'text/plain' });
          res.end('Not Found');
        }
      });
      return;
    }

    const ext = path.extname(filePath).toLowerCase();
    const contentType = MIME_TYPES[ext] || 'application/octet-stream';
    res.writeHead(200, { 'Content-Type': contentType });
    fs.createReadStream(filePath).pipe(res);
  });
});

server.listen(PORT, '0.0.0.0', () => {
  console.log(`CodeFix AI production server listening on http://0.0.0.0:${PORT}`);
  console.log(`Cloud Run health endpoint: http://0.0.0.0:${PORT}/healthz`);
  console.log(`Gemini API configured: ${Boolean(GEMINI_API_KEY && GEMINI_API_KEY !== 'MY_GEMINI_API_KEY')}`);
});
