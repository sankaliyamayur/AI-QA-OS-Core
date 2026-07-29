#!/usr/bin/env bash
# DX-1: AI-QA-OS CLI Launcher Script (Bash)

COMMAND="${1:-help}"
GATEWAY_URL="${AIQAOS_GATEWAY_URL:-http://localhost:8082}"

case "$COMMAND" in
  doctor)
    echo "======================================================="
    echo "            AI-QA-OS System Diagnostics (CLI)"
    echo "======================================================="
    echo "  Shell           : Bash ($BASH_VERSION)"
    echo "  Operating System: $(uname -s) $(uname -m)"
    echo "  Gateway URL     : $GATEWAY_URL"
    if command -v java >/dev/null 2>&1; then
      echo "  Java Runtime    : [OK] $(java -version 2>&1 | head -n 1)"
    else
      echo "  Java Runtime    : [FAIL] Java not found in PATH"
    fi
    echo "======================================================="
    echo "  Status: Diagnostic check complete."
    echo ""
    ;;
  version)
    echo ""
    echo "AI-QA-OS CLI v1.0.0-SNAPSHOT"
    echo "Target: AI-QA-OS Enterprise Platform (JDK 21+ / Spring Boot 3.3.0)"
    echo "Gateway Endpoint: $GATEWAY_URL"
    echo ""
    ;;
  *)
    JAR_PATH="./ai-qa-os-gateway/target/ai-qa-os-gateway-1.0.0-SNAPSHOT.jar"
    if [ -f "$JAR_PATH" ]; then
      java -jar "$JAR_PATH" qaos "$@"
    else
      java -cp "./ai-qa-os-gateway/target/classes" com.aiqaos.gateway.cli.QaOsCommandRunner qaos "$@" 2>/dev/null
    fi
    ;;
esac
