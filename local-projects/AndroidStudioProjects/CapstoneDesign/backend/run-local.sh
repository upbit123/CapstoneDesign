#!/bin/zsh
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
SECRETS_ENV="$SCRIPT_DIR/secrets/mail.env"

if [[ ! -f "$SECRETS_ENV" ]]; then
  echo "Missing $SECRETS_ENV"
  echo "Copy secrets/mail.env.example to secrets/mail.env and fill in your mail credentials."
  exit 1
fi

source "$SECRETS_ENV"

required_vars=(
  FIREBASE_SERVICE_ACCOUNT_PATH
  SPRING_MAIL_USERNAME
  SPRING_MAIL_PASSWORD
  APP_MAIL_FROM
)

for var_name in "${required_vars[@]}"; do
  if [[ -z "${(P)var_name:-}" ]]; then
    echo "Missing required env var: $var_name"
    exit 1
  fi
done

exec ./gradlew bootRun
