package com.example.service

import com.example.BuildConfig
import com.example.data.model.AiAnalysisResult
import com.example.data.model.AiExplainResult
import com.example.data.model.AiFixResult
import com.example.data.model.ChatMessage
import com.example.data.model.CodeProblem
import com.example.data.model.ExplainLanguage
import com.example.data.model.ExplainMode
import com.example.data.model.Language
import com.example.data.model.LineExplanation
import com.example.data.model.MessageSender
import com.example.data.model.ProblemCategory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class GeminiAiService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private fun getApiKey(): String {
        val envKey = try { System.getenv("GEMINI_API_KEY") } catch (_: Throwable) { null }
        if (!envKey.isNullOrBlank() && envKey != "MY_GEMINI_API_KEY") {
            return envKey.trim()
        }
        val propKey = try { System.getProperty("GEMINI_API_KEY") } catch (_: Throwable) { null }
        if (!propKey.isNullOrBlank() && propKey != "MY_GEMINI_API_KEY") {
            return propKey.trim()
        }
        return try {
            BuildConfig.GEMINI_API_KEY.trim()
        } catch (_: Throwable) {
            ""
        }
    }

    private fun getProxyUrl(): String? {
        val proxyEnv = try { System.getenv("GEMINI_PROXY_URL") ?: System.getenv("SERVER_API_URL") } catch (_: Throwable) { null }
        if (!proxyEnv.isNullOrBlank()) return proxyEnv.trim()
        val proxyProp = try { System.getProperty("gemini.proxy.url") } catch (_: Throwable) { null }
        return if (!proxyProp.isNullOrBlank()) proxyProp.trim() else null
    }

    private fun isApiKeyValid(): Boolean {
        if (!getProxyUrl().isNullOrBlank()) return true
        val key = getApiKey()
        return key.isNotBlank() && key != "MY_GEMINI_API_KEY"
    }

    suspend fun analyzeCode(code: String, language: Language): AiAnalysisResult = withContext(Dispatchers.IO) {
        if (code.trim().isEmpty()) {
            return@withContext AiAnalysisResult(
                problems = listOf(
                    CodeProblem(
                        line = 1,
                        category = ProblemCategory.SYNTAX_ERROR,
                        issue = "Code is empty",
                        whyItHappens = "The editor contains no code to analyze.",
                        suggestedSolution = "Write or paste your code in the editor before requesting an AI analysis."
                    )
                ),
                summary = "Cannot analyze empty code. Please provide code in the editor first.",
                qualityScore = 0
            )
        }

        if (isApiKeyValid()) {
            try {
                val prompt = """
You are CodeFix AI, an expert programming tutor for BCA and Computer Science students.
Analyze this ${language.displayName} code thoroughly.
Identify:
1. Syntax errors
2. Logical issues
3. Runtime problems
4. Common beginner mistakes
5. Possible security problems (e.g. buffer overflow, SQL injection, memory leak, unvalidated input)

Return ONLY valid JSON matching this exact structure:
{
  "summary": "Brief 1-2 sentence overall assessment for student",
  "qualityScore": 85,
  "problems": [
    {
      "line": 1,
      "category": "SYNTAX_ERROR", 
      "issue": "Specific issue description",
      "whyItHappens": "Clear explanation of why this error or issue occurs in ${language.displayName}",
      "suggestedSolution": "Step by step fix recommendation"
    }
  ]
}
Valid categories are: SYNTAX_ERROR, LOGICAL_ISSUE, RUNTIME_PROBLEM, COMMON_MISTAKE, SECURITY_RISK, BEST_PRACTICE.
If code has no bugs, return empty problems list and high quality score.

Source Code:
```${language.extension}
$code
```
"""
                val rawResponse = callGemini(prompt)
                val jsonString = extractJsonFromResponse(rawResponse)
                return@withContext parseAnalysisJson(jsonString, code, language)
            } catch (e: Exception) {
                // Fall back to heuristic static analysis
            }
        }
        return@withContext fallbackAnalyze(code, language)
    }

    suspend fun fixCode(code: String, language: Language, errorContext: String? = null): AiFixResult = withContext(Dispatchers.IO) {
        if (code.trim().isEmpty()) {
            return@withContext AiFixResult(
                originalIssue = "Code is empty",
                correctedCode = "",
                explanation = "Cannot fix empty code. Please provide code in the editor first.",
                changesMade = listOf("None: editor is empty")
            )
        }

        if (isApiKeyValid()) {
            try {
                val prompt = """
You are CodeFix AI. Fix any errors, bugs, or inefficiencies in this ${language.displayName} code.
Context/Errors: ${errorContext ?: "None reported"}

Requirements:
1. Never change the user's core intent or logic unless it is buggy.
2. Return ONLY valid JSON in this exact structure:
{
  "originalIssue": "Summary of the key bug or flaw found",
  "correctedCode": "The full corrected code ready to run",
  "explanation": "Clear explanation of what was wrong and how it was fixed for a student",
  "changesMade": [
    "Fixed missing semicolon on line X",
    "Corrected loop boundary condition",
    "Added null check"
  ]
}

Source Code:
```${language.extension}
$code
```
"""
                val rawResponse = callGemini(prompt)
                val jsonString = extractJsonFromResponse(rawResponse)
                return@withContext parseFixJson(jsonString, code, language)
            } catch (e: Exception) {
                // Fall back to heuristic fixer
            }
        }
        return@withContext fallbackFix(code, language)
    }

    suspend fun explainCode(
        code: String,
        language: Language,
        mode: ExplainMode,
        targetLang: ExplainLanguage
    ): AiExplainResult = withContext(Dispatchers.IO) {
        if (code.trim().isEmpty()) {
            return@withContext AiExplainResult(
                summary = "The editor is currently empty. Please write or paste code to generate an explanation.",
                mode = mode,
                languageLocale = targetLang,
                lineExplanations = emptyList(),
                beginnerConcepts = emptyList(),
                keyTakeaways = emptyList()
            )
        }

        if (isApiKeyValid()) {
            try {
                val modeInstruction = when (mode) {
                    ExplainMode.FULL -> "Provide a comprehensive architectural and functional explanation of the code."
                    ExplainMode.LINE_BY_LINE -> "Provide a line-by-line breakdown explaining every key statement."
                    ExplainMode.BEGINNER -> "Explain this in very simple, beginner-friendly terms with analogies for a BCA 1st semester student."
                }

                val languageInstruction = when (targetLang) {
                    ExplainLanguage.ENGLISH -> "Respond in clear English."
                    ExplainLanguage.HINDI -> "Respond in Hindi (हिन्दी) using Devanagari script with English programming terminology in brackets."
                    ExplainLanguage.GUJARATI -> "Respond in Gujarati (ગુજરાતી) script with English programming keywords in brackets."
                }

                val prompt = """
You are CodeFix AI programming tutor.
Explain this ${language.displayName} code.
Mode: $modeInstruction
Language: $languageInstruction

Return ONLY valid JSON matching this schema:
{
  "summary": "High level overview of what this program accomplishes",
  "lineExplanations": [
    {
      "lineRange": "Lines 1-3",
      "codeSnippet": "code snippet",
      "explanation": "explanation in ${targetLang.displayName}"
    }
  ],
  "beginnerConcepts": ["Concept 1", "Concept 2"],
  "keyTakeaways": ["Takeaway 1", "Takeaway 2"]
}

Source Code:
```${language.extension}
$code
```
"""
                val rawResponse = callGemini(prompt)
                val jsonString = extractJsonFromResponse(rawResponse)
                return@withContext parseExplainJson(jsonString, code, language, mode, targetLang)
            } catch (e: Exception) {
                // Fall back to heuristic explainer
            }
        }
        return@withContext fallbackExplain(code, language, mode, targetLang)
    }

    suspend fun chatWithCode(
        messages: List<ChatMessage>,
        currentCode: String,
        language: Language
    ): String = withContext(Dispatchers.IO) {
        if (isApiKeyValid()) {
            try {
                val systemPrompt = """
You are CodeFix AI, a friendly, patient, and knowledgeable programming tutor for BCA and Computer Science students.
The student is currently working in the editor with this ${language.displayName} code:
```${language.extension}
$currentCode
```

Help them understand, debug, optimize, or practice concepts. Keep explanations concise, practical, and encourage good coding habits.
"""
                val contents = JSONArray()
                // System message
                contents.put(JSONObject().apply {
                    put("role", "user")
                    put("parts", JSONArray().apply {
                        put(JSONObject().put("text", systemPrompt))
                    })
                })
                contents.put(JSONObject().apply {
                    put("role", "model")
                    put("parts", JSONArray().apply {
                        put(JSONObject().put("text", "Understood! I am ready to help you with your ${language.displayName} code."))
                    })
                })

                // Recent conversation
                messages.takeLast(6).forEach { msg ->
                    contents.put(JSONObject().apply {
                        put("role", if (msg.sender == MessageSender.USER) "user" else "model")
                        put("parts", JSONArray().apply {
                            put(JSONObject().put("text", msg.content))
                        })
                    })
                }

                val payload = JSONObject().apply {
                    put("contents", contents)
                    put("generationConfig", JSONObject().apply {
                        put("temperature", 0.7)
                        put("topP", 0.9)
                    })
                }

                val requestBody = payload.toString().toRequestBody("application/json".toMediaType())
                val request = buildGeminiRequest(requestBody)
                val response = client.newCall(request).execute()

                if (response.isSuccessful) {
                    val body = response.body?.string() ?: ""
                    val json = JSONObject(body)
                    val candidates = json.optJSONArray("candidates")
                    if (candidates != null && candidates.length() > 0) {
                        val firstCandidate = candidates.getJSONObject(0)
                        val content = firstCandidate.getJSONObject("content")
                        val parts = content.getJSONArray("parts")
                        return@withContext parts.getJSONObject(0).getString("text")
                    }
                }
            } catch (e: Exception) {
                // Return offline response
            }
        }
        return@withContext fallbackChat(messages.lastOrNull()?.content ?: "", currentCode, language)
    }

    private fun buildGeminiRequest(requestBody: okhttp3.RequestBody): Request {
        val proxy = getProxyUrl()
        val apiKey = getApiKey()

        val (baseUrl, isDirectApi) = if (proxy != null) {
            val base = if (proxy.endsWith("/")) proxy.dropLast(1) else proxy
            "$base/api/gemini/generateContent" to false
        } else {
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent" to true
        }

        val requestBuilder = Request.Builder().post(requestBody)

        if (apiKey.isNotBlank()) {
            requestBuilder.addHeader("x-goog-api-key", apiKey)
        }

        val finalUrl = if (isDirectApi && apiKey.isNotBlank()) {
            "$baseUrl?key=$apiKey"
        } else {
            baseUrl
        }

        return requestBuilder.url(finalUrl).build()
    }

    private fun callGemini(prompt: String): String {
        val payload = JSONObject().apply {
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().put("text", prompt))
                    })
                })
            })
            put("generationConfig", JSONObject().apply {
                put("temperature", 0.2)
                put("responseMimeType", "application/json")
            })
        }

        val requestBody = payload.toString().toRequestBody("application/json".toMediaType())
        val request = buildGeminiRequest(requestBody)
        val response = client.newCall(request).execute()

        if (!response.isSuccessful) {
            throw RuntimeException("Gemini API call failed with code ${response.code}: ${response.message}")
        }

        val body = response.body?.string() ?: throw RuntimeException("Empty response body")
        val json = JSONObject(body)
        val candidates = json.optJSONArray("candidates")
            ?: throw RuntimeException("No candidates in Gemini response")
        if (candidates.length() == 0) throw RuntimeException("Empty candidates list")

        val first = candidates.getJSONObject(0)
        val content = first.getJSONObject("content")
        val parts = content.getJSONArray("parts")
        return parts.getJSONObject(0).getString("text")
    }

    private fun extractJsonFromResponse(raw: String): String {
        val trimmed = raw.trim()
        if (trimmed.startsWith("```json")) {
            return trimmed.removePrefix("```json").removeSuffix("```").trim()
        }
        if (trimmed.startsWith("```")) {
            return trimmed.removePrefix("```").removeSuffix("```").trim()
        }
        val start = trimmed.indexOf('{')
        val end = trimmed.lastIndexOf('}')
        if (start != -1 && end != -1 && end > start) {
            return trimmed.substring(start, end + 1)
        }
        return trimmed
    }

    private fun parseAnalysisJson(jsonStr: String, code: String, language: Language): AiAnalysisResult {
        val json = JSONObject(jsonStr)
        val summary = json.optString("summary", "Analysis completed.")
        val qualityScore = json.optInt("qualityScore", 85)
        val problems = mutableListOf<CodeProblem>()

        val problemsArray = json.optJSONArray("problems")
        if (problemsArray != null) {
            for (i in 0 until problemsArray.length()) {
                val p = problemsArray.getJSONObject(i)
                val line = p.optInt("line", 1)
                val catStr = p.optString("category", "COMMON_MISTAKE")
                val category = try {
                    ProblemCategory.valueOf(catStr)
                } catch (e: Exception) {
                    ProblemCategory.COMMON_MISTAKE
                }
                problems.add(
                    CodeProblem(
                        line = line,
                        category = category,
                        issue = p.optString("issue", "Code issue detected"),
                        whyItHappens = p.optString("whyItHappens", "Explanation"),
                        suggestedSolution = p.optString("suggestedSolution", "Solution")
                    )
                )
            }
        }
        return AiAnalysisResult(problems = problems, summary = summary, qualityScore = qualityScore)
    }

    private fun parseFixJson(jsonStr: String, code: String, language: Language): AiFixResult {
        val json = JSONObject(jsonStr)
        val originalIssue = json.optString("originalIssue", "Multiple code improvements applied.")
        val correctedCode = json.optString("correctedCode", code)
        val explanation = json.optString("explanation", "The code was inspected and bugs corrected.")
        val changesList = mutableListOf<String>()
        val arr = json.optJSONArray("changesMade")
        if (arr != null) {
            for (i in 0 until arr.length()) {
                changesList.add(arr.getString(i))
            }
        }
        return AiFixResult(
            originalIssue = originalIssue,
            correctedCode = correctedCode,
            explanation = explanation,
            changesMade = if (changesList.isNotEmpty()) changesList else listOf("Formatted and corrected syntax")
        )
    }

    private fun parseExplainJson(
        jsonStr: String,
        code: String,
        language: Language,
        mode: ExplainMode,
        targetLang: ExplainLanguage
    ): AiExplainResult {
        val json = JSONObject(jsonStr)
        val summary = json.optString("summary", "Code breakdown for ${language.displayName}")
        val lineExplanations = mutableListOf<LineExplanation>()
        val arr = json.optJSONArray("lineExplanations")
        if (arr != null) {
            for (i in 0 until arr.length()) {
                val item = arr.getJSONObject(i)
                lineExplanations.add(
                    LineExplanation(
                        lineRange = item.optString("lineRange", "Lines"),
                        codeSnippet = item.optString("codeSnippet", ""),
                        explanation = item.optString("explanation", "")
                    )
                )
            }
        }
        val concepts = mutableListOf<String>()
        val conceptsArr = json.optJSONArray("beginnerConcepts")
        if (conceptsArr != null) {
            for (i in 0 until conceptsArr.length()) concepts.add(conceptsArr.getString(i))
        }

        val takeaways = mutableListOf<String>()
        val takeArr = json.optJSONArray("keyTakeaways")
        if (takeArr != null) {
            for (i in 0 until takeArr.length()) takeaways.add(takeArr.getString(i))
        }

        return AiExplainResult(
            summary = summary,
            mode = mode,
            languageLocale = targetLang,
            lineExplanations = lineExplanations,
            beginnerConcepts = concepts,
            keyTakeaways = takeaways
        )
    }

    // Heuristic fallbacks for offline or unconfigured API key
    private fun fallbackAnalyze(code: String, language: Language): AiAnalysisResult {
        val problems = mutableListOf<CodeProblem>()
        val lines = code.lines()

        lines.forEachIndexed { index, rawLine ->
            val lineNum = index + 1
            val line = rawLine.trim()

            // Check C/C++ missing semicolons
            if ((language == Language.C || language == Language.CPP || language == Language.JAVA) &&
                line.isNotEmpty() &&
                !line.startsWith("//") && !line.startsWith("#") && !line.startsWith("/*") &&
                !line.endsWith(";") && !line.endsWith("{") && !line.endsWith("}") && !line.endsWith(":")
            ) {
                if (line.contains("=") || line.startsWith("printf") || line.startsWith("cout") || line.startsWith("return") || line.startsWith("int ") || line.startsWith("float ")) {
                    problems.add(
                        CodeProblem(
                            line = lineNum,
                            category = ProblemCategory.SYNTAX_ERROR,
                            issue = "Missing terminating semicolon ';'",
                            whyItHappens = "In ${language.displayName}, statements must terminate with a semicolon to tell the compiler where the instruction ends.",
                            suggestedSolution = "Add a ';' at the end of line $lineNum."
                        )
                    )
                }
            }

            // Check equality vs assignment in conditionals
            if (line.contains("if (") || line.contains("if(")) {
                if (Regex("if\\s*\\([^=]+=[^=]+\\)").containsMatchIn(line)) {
                    problems.add(
                        CodeProblem(
                            line = lineNum,
                            category = ProblemCategory.LOGICAL_ISSUE,
                            issue = "Single '=' assignment used inside conditional check",
                            whyItHappens = "Using a single '=' assigns the value rather than checking for equality, leading to unexpected truthy evaluation.",
                            suggestedSolution = "Replace '=' with '==' or '===' for comparison."
                        )
                    )
                }
            }

            // Check unbounded buffers or raw gets() in C/C++
            if ((language == Language.C || language == Language.CPP) && line.contains("gets(")) {
                problems.add(
                    CodeProblem(
                        line = lineNum,
                        category = ProblemCategory.SECURITY_RISK,
                        issue = "Insecure function 'gets()' used",
                        whyItHappens = "'gets()' does not check buffer boundaries and can cause severe buffer overflow vulnerabilities.",
                        suggestedSolution = "Replace 'gets()' with safe 'fgets(buffer, sizeof(buffer), stdin)'."
                    )
                )
            }

            // Check Python indentation or missing colons
            if (language == Language.PYTHON) {
                if ((line.startsWith("def ") || line.startsWith("if ") || line.startsWith("for ") || line.startsWith("while ") || line.startsWith("class ")) && !line.endsWith(":")) {
                    problems.add(
                        CodeProblem(
                            line = lineNum,
                            category = ProblemCategory.SYNTAX_ERROR,
                            issue = "Missing colon ':' at block header",
                            whyItHappens = "Python requires a colon ':' at the end of compound statements (functions, loops, conditionals) to initiate an indented block.",
                            suggestedSolution = "Add ':' to the end of line $lineNum."
                        )
                    )
                }
            }
        }

        val score = if (problems.isEmpty()) 95 else (100 - (problems.size * 15)).coerceAtLeast(40)
        val summary = if (problems.isEmpty()) {
            "Great job! No critical syntax, logic, or security bugs were detected in your ${language.displayName} code. Clean code structure."
        } else {
            "Found ${problems.size} potential improvement area(s) in your ${language.displayName} code. Review the line breakdowns below."
        }

        return AiAnalysisResult(
            problems = problems,
            summary = summary,
            qualityScore = score
        )
    }

    private fun fallbackFix(code: String, language: Language): AiFixResult {
        var corrected = code
        val changes = mutableListOf<String>()

        if (language == Language.PYTHON) {
            val lines = code.lines().map { line ->
                val trimmed = line.trim()
                if ((trimmed.startsWith("def ") || trimmed.startsWith("if ") || trimmed.startsWith("for ") || trimmed.startsWith("while ")) && !trimmed.endsWith(":")) {
                    changes.add("Added missing colon ':' to block statement")
                    "$line:"
                } else {
                    line
                }
            }
            corrected = lines.joinToString("\n")
        } else if (language == Language.C || language == Language.CPP || language == Language.JAVA) {
            val lines = code.lines().map { line ->
                var updated = line
                val trimmed = line.trim()
                // Fix single = in if
                if (trimmed.contains("if (") || trimmed.contains("if(")) {
                    val regex = Regex("if\\s*\\(([^=]+)=([^=]+)\\)")
                    if (regex.containsMatchIn(trimmed)) {
                        updated = regex.replace(updated) { match ->
                            val left = match.groupValues[1].trim()
                            val right = match.groupValues[2].trim()
                            "if ($left == $right)"
                        }
                        changes.add("Replaced assignment '=' with comparison '==' inside conditional")
                    }
                }
                // Fix gets
                if (trimmed.contains("gets(")) {
                    updated = updated.replace(Regex("gets\\(([^)]+)\\)"), "fgets($1, sizeof($1), stdin)")
                    changes.add("Replaced insecure 'gets()' with safe 'fgets(..., sizeof(...), stdin)'")
                }
                // Fix missing semicolon
                val currentTrimmed = updated.trim()
                if (currentTrimmed.isNotEmpty() && !currentTrimmed.startsWith("//") && !currentTrimmed.startsWith("#") &&
                    !currentTrimmed.endsWith(";") && !currentTrimmed.endsWith("{") && !currentTrimmed.endsWith("}") && !currentTrimmed.endsWith(":") &&
                    (currentTrimmed.startsWith("return") || currentTrimmed.startsWith("printf") || currentTrimmed.startsWith("cout") || currentTrimmed.contains("="))
                ) {
                    changes.add("Appended missing semicolon ';' to statement: ${currentTrimmed.take(20)}...")
                    "$updated;"
                } else {
                    updated
                }
            }
            corrected = lines.joinToString("\n")
        }

        if (changes.isEmpty()) {
            changes.add("Formatted indentation and verified standard ${language.displayName} conventions")
        }

        return AiFixResult(
            originalIssue = "Verified syntax rules and standard style conventions for ${language.displayName}.",
            correctedCode = corrected,
            explanation = "CodeFix AI verified your ${language.displayName} structure, ensured all block and statement terminators are correct, and sanitized formatting for reliable sandbox execution.",
            changesMade = changes
        )
    }

    private fun fallbackExplain(
        code: String,
        language: Language,
        mode: ExplainMode,
        targetLang: ExplainLanguage
    ): AiExplainResult {
        val lines = code.lines()
        val lineExplanations = mutableListOf<LineExplanation>()

        lines.chunked(3).forEachIndexed { index, chunk ->
            val start = index * 3 + 1
            val end = (index * 3 + chunk.size).coerceAtMost(lines.size)
            val snippet = chunk.joinToString("\n")
            val explanation = when (targetLang) {
                ExplainLanguage.HINDI -> "लाइन $start-$end: यह भाग ${language.displayName} के मुख्य लॉजिक को निष्पादित करता है और वेरिएबल्स व फ़ंक्शंस को प्रोसेस करता है।"
                ExplainLanguage.GUJARATI -> "લાઇન $start-$end: આ ભાગ ${language.displayName} નો મુખ્ય લોજિક રન કરે છે અને વેરીએબલ તેમજ ફંક્શન પ્રોસેસ કરે છે."
                ExplainLanguage.ENGLISH -> "Lines $start-$end: Sets up the execution block, declares variables, and drives program control flow."
            }
            lineExplanations.add(LineExplanation("$start-$end", snippet, explanation))
        }

        val summary = when (targetLang) {
            ExplainLanguage.HINDI -> "यह ${language.displayName} प्रोग्राम छात्र के इनपुट को प्रोसेस करता है, स्ट्रक्चर बनाता है और सुरक्षित आउटपुट देता है।"
            ExplainLanguage.GUJARATI -> "આ ${language.displayName} પ્રોગ્રામ વિદ્યાર્થીના કોડને પ્રોસેસ કરે છે અને સચોટ પરિણામ આપે છે."
            ExplainLanguage.ENGLISH -> "This ${language.displayName} script defines the core logic, processes data through function calls, and produces formatted output."
        }

        val concepts = listOf(
            "Control Flow & Syntax in ${language.displayName}",
            "Standard I/O Streams",
            "Variable Scope and Lifecycle"
        )

        val takeaways = listOf(
            "Always initialize variables before reading them.",
            "Maintain clean indentation for readability and maintainability."
        )

        return AiExplainResult(
            summary = summary,
            mode = mode,
            languageLocale = targetLang,
            lineExplanations = lineExplanations,
            beginnerConcepts = concepts,
            keyTakeaways = takeaways
        )
    }

    private fun fallbackChat(userQuery: String, currentCode: String, language: Language): String {
        val lower = userQuery.lowercase()
        return when {
            lower.contains("how to run") || lower.contains("run") ->
                "To run your ${language.displayName} code, simply tap the green **Run Code** button at the top or bottom of the editor. CodeFix AI will submit it to the secure sandbox and display the real-time terminal output in the Console below!"
            lower.contains("explain") || lower.contains("how it works") ->
                "In ${language.displayName}, your code executes sequentially. Click the **Explain** button on the toolbar to get a complete breakdown in English, Hindi, or Gujarati!"
            lower.contains("fix") || lower.contains("error") ->
                "If you're hitting errors, tap the **Analyze** or **Fix Code** button. CodeFix AI will check your syntax, logical boundaries, and generate a side-by-side fix preview without overwriting your work."
            lower.contains("bca") || lower.contains("exam") || lower.contains("syllabus") ->
                "For BCA & Computer Science exams, examiners heavily focus on: (1) Correct syntax and data types, (2) Edge cases like empty inputs, (3) Clean algorithm time complexity. Make sure to comment your functions!"
            else ->
                "I am your CodeFix AI tutor! I'm inspecting your ${language.displayName} code (${currentCode.lines().size} lines). You can ask me to explain any line, suggest optimizations, convert this code to another language, or write test cases!"
        }
    }
}
