#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
SQL_FILE="${SCRIPT_DIR}/sql/create-javaapi-database.sql"

echo "Creating MySQL database JavaApi and app user 'javaapi' (requires sudo on Ubuntu)..."
sudo mysql < "${SQL_FILE}"
sudo mysql -e "SHOW DATABASES LIKE 'JavaApi';"
echo "Done."
