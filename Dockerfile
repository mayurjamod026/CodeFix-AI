# Production Dockerfile for Google Cloud Run Deployment
FROM node:20-alpine

# Set working directory
WORKDIR /app

# Set production environment
ENV NODE_ENV=production
ENV PORT=8080

# Copy server code and public web client
COPY server.js ./
COPY public ./public
COPY metadata.json ./

# Cloud Run expects the container to listen on 0.0.0.0:$PORT
EXPOSE 8080

# Healthcheck for container orchestration
HEALTHCHECK --interval=30s --timeout=5s --start-period=5s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:${PORT}/healthz || exit 1

# Start production server
CMD ["node", "server.js"]
