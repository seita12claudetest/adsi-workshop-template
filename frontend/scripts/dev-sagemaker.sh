#!/bin/bash
set -e

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
FRONTEND_DIR="$(dirname "$SCRIPT_DIR")"
BACKEND_DIR="$(dirname "$FRONTEND_DIR")/backend"

export SAGEMAKER=1
export NEXT_PUBLIC_BASE_PATH="/absports/3000"

PORT=3000

echo "=== Stopping existing processes ==="
fuser -k ${PORT}/tcp 2>/dev/null || true
sleep 1

echo "=== Building Next.js ==="
cd "$FRONTEND_DIR"
npx next build

echo "=== Starting Next.js (port ${PORT}) ==="
npx next start -p ${PORT} &
NEXT_PID=$!

echo ""
echo "=========================================="
echo " Frontend ready!"
echo " PORTS tab -> port ${PORT} -> globe button"
echo " Then replace 'ports' with 'absports'"
echo "=========================================="
echo ""
echo "PID: next=$NEXT_PID"

wait
