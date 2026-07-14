#!/bin/bash
set -e

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
FRONTEND_DIR="$(dirname "$SCRIPT_DIR")"
BACKEND_DIR="$(dirname "$FRONTEND_DIR")/backend"

export SAGEMAKER=1
export NEXT_PUBLIC_BASE_PATH="/codeeditor/default/ports/3000"
export SAGEMAKER_BASE_PATH="/codeeditor/default/ports/3000"

PROXY_PORT=3000
NEXT_PORT=3001

echo "=== Stopping existing processes ==="
fuser -k ${PROXY_PORT}/tcp 2>/dev/null || true
fuser -k ${NEXT_PORT}/tcp 2>/dev/null || true
sleep 1

echo "=== Building Next.js ==="
cd "$FRONTEND_DIR"
npx next build

echo "=== Starting Next.js (port ${NEXT_PORT}) ==="
npx next start -H 127.0.0.1 -p ${NEXT_PORT} &
NEXT_PID=$!
sleep 2

echo "=== Starting SageMaker proxy (port ${PROXY_PORT}) ==="
PROXY_PORT=${PROXY_PORT} NEXT_PORT=${NEXT_PORT} SAGEMAKER_BASE_PATH=${SAGEMAKER_BASE_PATH} \
  node "$SCRIPT_DIR/sagemaker-proxy.mjs" &
PROXY_PID=$!

echo ""
echo "=========================================="
echo " Frontend ready!"
echo " PORTS tab -> port ${PROXY_PORT} -> globe button"
echo " URL: https://<domain>/codeeditor/default/ports/3000/"
echo "=========================================="
echo ""
echo "PID: next=$NEXT_PID proxy=$PROXY_PID"

wait
