#!/bin/bash

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
ENV_FILE="$SCRIPT_DIR/../.env"

# .env 로드
if [ ! -f "$ENV_FILE" ]; then
  echo ".env 파일을 찾을 수 없습니다: $ENV_FILE"
  exit 1
fi

DB_USERNAME=$(grep '^DB_USERNAME=' "$ENV_FILE" | cut -d'=' -f2 | tr -d '\r')
DB_PASSWORD=$(grep '^DB_PASSWORD=' "$ENV_FILE" | cut -d'=' -f2 | tr -d '\r')
DB_NAME=$(grep '^DB_NAME='     "$ENV_FILE" | cut -d'=' -f2 | tr -d '\r')

cleanup() {
  docker exec dabjeongneo-mysql \
    mysql -u"$DB_USERNAME" -p"$DB_PASSWORD" "$DB_NAME" \
    -e "DELETE FROM users WHERE username LIKE 'loadtest%';" 2>/dev/null
  echo "테스트 유저 삭제 완료"
}

parse_result() {
  local output="$1"
  AVG=$(echo "$output"  | grep 'http_req_duration' | grep -v expected | awk '{for(i=1;i<=NF;i++) if($i~/^avg=/) print $i}' | cut -d= -f2)
  MED=$(echo "$output"  | grep 'http_req_duration' | grep -v expected | awk '{for(i=1;i<=NF;i++) if($i~/^med=/) print $i}' | cut -d= -f2)
  P90=$(echo "$output"  | grep 'http_req_duration' | grep -v expected | awk '{for(i=1;i<=NF;i++) if($i~/^p\(90\)=/) print $i}' | cut -d= -f2)
  P95=$(echo "$output"  | grep 'http_req_duration' | grep -v expected | awk '{for(i=1;i<=NF;i++) if($i~/^p\(95\)=/) print $i}' | cut -d= -f2)
  MAX=$(echo "$output"  | grep 'http_req_duration' | grep -v expected | awk '{for(i=1;i<=NF;i++) if($i~/^max=/) print $i}' | cut -d= -f2)
  FAIL=$(echo "$output" | grep 'http_req_failed' | grep -v '{' | awk '{print $2}')
  RPS=$(echo "$output"  | grep 'http_reqs' | grep -v '{' | awk '{print $3}')
}

echo "======================================"
echo "  Refresh Token Store 부하테스트 비교"
echo "======================================"

# ── DB 테스트 ──
echo ""
echo "[1/2] application.yml 에서 store: db 로 설정 후 서버를 재시작하고 엔터를 누르세요..."
read -r

echo "DB 테스트 실행 중..."
DB_OUTPUT=$(docker compose --profile loadtest run --rm k6 run \
  --out influxdb=http://influxdb:8086/k6 \
  /scripts/refresh-token-test.js 2>&1)
echo "$DB_OUTPUT"

cleanup

# ── Redis 테스트 ──
echo ""
echo "[2/2] application.yml 에서 store: redis 로 설정 후 서버를 재시작하고 엔터를 누르세요..."
read -r

echo "Redis 테스트 실행 중..."
REDIS_OUTPUT=$(docker compose --profile loadtest run --rm k6 run \
  --out influxdb=http://influxdb:8086/k6 \
  /scripts/refresh-token-test.js 2>&1)
echo "$REDIS_OUTPUT"

cleanup

# ── 결과 비교 ──
parse_result "$DB_OUTPUT"
DB_AVG=$AVG; DB_MED=$MED; DB_P90=$P90; DB_P95=$P95; DB_MAX=$MAX; DB_FAIL=$FAIL; DB_RPS=$RPS

parse_result "$REDIS_OUTPUT"
REDIS_AVG=$AVG; REDIS_MED=$MED; REDIS_P90=$P90; REDIS_P95=$P95; REDIS_MAX=$MAX; REDIS_FAIL=$FAIL; REDIS_RPS=$RPS

echo ""
echo "======================================"
echo "           결과 비교"
echo "======================================"
printf "%-10s %-15s %-15s\n" "지표" "DB" "Redis"
echo "----------------------------------------------"
printf "%-10s %-15s %-15s\n" "avg"  "$DB_AVG"  "$REDIS_AVG"
printf "%-10s %-15s %-15s\n" "med"  "$DB_MED"  "$REDIS_MED"
printf "%-10s %-15s %-15s\n" "p90"  "$DB_P90"  "$REDIS_P90"
printf "%-10s %-15s %-15s\n" "p95"  "$DB_P95"  "$REDIS_P95"
printf "%-10s %-15s %-15s\n" "max"  "$DB_MAX"  "$REDIS_MAX"
printf "%-10s %-15s %-15s\n" "실패율" "$DB_FAIL" "$REDIS_FAIL"
printf "%-10s %-15s %-15s\n" "RPS"  "$DB_RPS"  "$REDIS_RPS"
echo "======================================"
