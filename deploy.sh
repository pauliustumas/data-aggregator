#!/bin/bash
set -e

# ─── Config ───────────────────────────────────────────────────────────────────
REMOTE_HOST="192.168.66.61"
REMOTE_USER="paulius"
REMOTE_HOME="/home/paulius"
REPO_DIR="${REMOTE_HOME}/data-aggregator-build"
JAR_NAME="aggregate-db-0.0.1-SNAPSHOT.jar"
SERVICE_NAME="aggregate.service"
JAVA_HOME="${REMOTE_HOME}/jdk-21.0.10+7"
MAVEN_HOME="${REMOTE_HOME}/apache-maven-3.9.9"

echo "═══════════════════════════════════════════════════"
echo "  Data Aggregator - Deploy"
echo "═══════════════════════════════════════════════════"

# ─── Step 1: Pull latest code ─────────────────────────────────────────────────
echo ""
echo "[1/4] Pulling latest code..."
ssh ${REMOTE_USER}@${REMOTE_HOST} "
  if [ -d ${REPO_DIR} ]; then
    cd ${REPO_DIR} && git pull
  else
    git clone https://github.com/pauliustumas/data-aggregator.git ${REPO_DIR}
  fi
"

# ─── Step 2: Build with Maven ─────────────────────────────────────────────────
echo ""
echo "[2/4] Building with Maven..."
ssh ${REMOTE_USER}@${REMOTE_HOST} "
  export JAVA_HOME=${JAVA_HOME}
  export PATH=\${JAVA_HOME}/bin:${MAVEN_HOME}/bin:\${PATH}
  cd ${REPO_DIR}
  mvn package -DskipTests -q
"
echo "  Build complete."

# ─── Step 3: Backup and copy jar ──────────────────────────────────────────────
echo ""
echo "[3/4] Deploying jar..."
ssh ${REMOTE_USER}@${REMOTE_HOST} "
  cp ${REMOTE_HOME}/${JAR_NAME} ${REMOTE_HOME}/${JAR_NAME}.bak 2>/dev/null || true
  cp ${REPO_DIR}/target/${JAR_NAME} ${REMOTE_HOME}/${JAR_NAME}
"
echo "  Jar deployed. Old jar backed up."

# ─── Step 4: Restart service ──────────────────────────────────────────────────
echo ""
echo "[4/4] Restarting service..."
ssh ${REMOTE_USER}@${REMOTE_HOST} "sudo systemctl restart ${SERVICE_NAME}"

sleep 5

# ─── Verify ───────────────────────────────────────────────────────────────────
echo ""
STATUS=$(ssh ${REMOTE_USER}@${REMOTE_HOST} "systemctl is-active ${SERVICE_NAME}")
if [ "$STATUS" = "active" ]; then
  echo "✅ ${SERVICE_NAME} is running."
else
  echo "❌ ${SERVICE_NAME} failed to start. Status: ${STATUS}"
  echo "   Check logs: ssh ${REMOTE_USER}@${REMOTE_HOST} journalctl -u ${SERVICE_NAME} -n 30"
  exit 1
fi

echo ""
echo "═══════════════════════════════════════════════════"
echo "  Deploy complete!"
echo "═══════════════════════════════════════════════════"
