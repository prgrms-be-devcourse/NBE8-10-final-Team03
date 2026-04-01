#!/bin/bash

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
ENV_FILE="$SCRIPT_DIR/../.env"

if [ ! -f "$ENV_FILE" ]; then
  echo ".env 파일을 찾을 수 없습니다: $ENV_FILE"
  exit 1
fi

# CRLF 대응: grep으로 값만 추출 후 \r 제거
DB_USERNAME=$(grep '^DB_USERNAME=' "$ENV_FILE" | cut -d'=' -f2 | tr -d '\r')
DB_PASSWORD=$(grep '^DB_PASSWORD=' "$ENV_FILE" | cut -d'=' -f2 | tr -d '\r')
DB_NAME=$(grep '^DB_NAME='     "$ENV_FILE" | cut -d'=' -f2 | tr -d '\r')

if [ -z "$DB_PASSWORD" ]; then
  echo "DB_PASSWORD를 .env에서 읽지 못했습니다."
  exit 1
fi

echo "테스트 유저 삭제 중... (DB: $DB_NAME, USER: $DB_USERNAME)"

docker exec dabjeongneo-mysql \
  mysql -u"$DB_USERNAME" -p"$DB_PASSWORD" "$DB_NAME" \
  -e "DELETE FROM users WHERE username LIKE 'loadtest%'; SELECT ROW_COUNT() AS deleted_count;"

echo "완료"
