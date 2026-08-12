#!/bin/bash
# Trigger shared codegen, then copy Java files into springboot sources
set -euo pipefail

SHARED_DIR="$(cd "$(dirname "$0")/../../saas-identity-platform-shared" && pwd)"
echo "[gen-shared] rebuilding shared TypeSpec artifacts..."
(cd "$SHARED_DIR" && npm run build)

echo "[gen-shared] copying Java DTO + Controller into springboot src/main/java..."
GEN_JAVA="$SHARED_DIR/generated/java/src/main/java"
DEST="$(cd "$(dirname "$0")/.." && pwd)/src/main/java"

# Copy DTOs and API interfaces — these get overwritten on each rebuild
cp -r "$GEN_JAVA/saas/identity/shared/dto/." "$DEST/saas/identity/shared/dto/"
cp -r "$GEN_JAVA/saas/identity/shared/api/." "$DEST/saas/identity/platform/api/"
echo "[gen-shared] OK"