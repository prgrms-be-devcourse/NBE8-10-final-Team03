# 🚀 k6 부하테스트 사용 가이드

## 📌 개요

본 프로젝트는 **k6 + InfluxDB + Grafana**를 이용하여
부하 테스트를 수행하고, 결과를 시각화합니다.

* k6: 부하 테스트 실행
* InfluxDB: 테스트 결과 저장
* Grafana: 결과 시각화

---

## 🧱 사전 준비

### 1. Docker 실행

Docker Desktop이 실행 중이어야 합니다.

### 2. Spring Boot 서버 실행

```bash
./gradlew bootRun
```

---

## ⚙️ 실행 방법

### 1️⃣ 전체 서비스 실행

```bash
docker compose up -d
```

실행되는 서비스:

* MySQL
* Redis
* InfluxDB
* Grafana

> Grafana 데이터소스(InfluxDB)와 대시보드는 자동으로 프로비저닝됩니다. 별도 설정이 필요 없습니다.

---

### 2️⃣ Grafana 접속

```
http://localhost:3001
```

기본 계정:

* ID: `admin`
* PW: `admin`

---

## 🧪 부하 테스트 실행

### 기본 실행 형식

```bash
docker compose --profile loadtest run --rm k6 run --out influxdb=http://influxdb:8086/k6 파일명.js
```

> k6 컨테이너의 working_dir이 `/scripts`로 설정되어 있어 파일명만 입력하면 됩니다.

---

### 📌 테스트 스크립트 목록

| 파일명 | 설명 | 테스트 후 정리 필요 |
|--------|------|:---:|
| `setup-users.js` | 부하테스트용 유저 50명 사전 생성 (1회 실행) | ✅ |
| `refresh-token-test.js` | 로그인 → reissue 3회 → 로그아웃 | ❌ |
| `ranking-test.js` | 랭킹 조회 (all / weekly / monthly 분산) | ❌ |

---

### 실행 예시

```bash
# 1. 테스트 유저 생성 (최초 1회만 실행)
docker compose --profile loadtest run --rm k6 run setup-users.js

# 2. Refresh Token 부하테스트
docker compose --profile loadtest run --rm k6 run --out influxdb=http://influxdb:8086/k6 refresh-token-test.js

# 3. 랭킹 조회 부하테스트
docker compose --profile loadtest run --rm k6 run --out influxdb=http://influxdb:8086/k6 ranking-test.js
```

---

## 🧹 테스트 데이터 정리

`setup-users.js`로 생성한 테스트 유저 50명을 삭제합니다.
테스트 완료 후 아래 명령어로 삭제해 주세요.

```bash
bash k6/cleanup.sh
```

---

## 📊 결과 확인

1. Grafana 접속 (`http://localhost:3001`)
2. Dashboards → **부하테스트** 선택
3. 우측 상단 시간 범위 설정 (Last 5m / Last 15m 추천)

---

## 📈 주요 지표 설명

| 지표 | 설명 |
|------|------|
| RPS | 초당 요청 수. 높을수록 처리량이 많음 |
| avg | 평균 응답 시간 |
| p90 / p95 | 상위 90% / 95% 응답 시간. p95가 사용자 체감 지연 기준 |
| 실패율 | HTTP 요청 실패 비율. 0에 가까울수록 안정적 |
| VU | 동시 가상 사용자 수 |

**threshold (통과 기준):**
- 실패율 < 1%
- p95 응답시간 < 500ms

---

## 🔄 Refresh token DB vs Redis 비교 방법

`application.yml`에서 저장소를 전환한 뒤 서버를 재시작하고 테스트를 실행합니다.

```yaml
custom:
  refresh-token:
    store: db     # "db" 또는 "redis"
```

Grafana에서 시간 범위를 조정하면 두 결과를 나란히 비교할 수 있습니다.

---

## 🧩 테스트 스크립트 작성 방법

```javascript
import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
    stages: [
        { duration: '30s', target: 50 },
        { duration: '1m',  target: 50 },
        { duration: '30s', target: 0  },
    ],
    thresholds: {
        http_req_failed:   ['rate<0.01'],
        http_req_duration: ['p(95)<500'],
    },
};

export default function () {
    const res = http.get('http://host.docker.internal:8080/api/v1/example', {
        tags: { name: 'API 이름' },  // Grafana에서 API별 분석 가능
    });

    check(res, { 'status 200': (r) => r.status === 200 });
    sleep(1);
}
```

> **URL 규칙:**
> - Spring Boot 서버 (로컬 실행): `http://host.docker.internal:8080`
> - Docker 서비스 간 통신 (InfluxDB 등): 서비스명 사용 (`http://influxdb:8086`)

---

## 👥 팀 규칙 (권장)

* 테스트 스크립트는 `k6/scripts/`에 추가
* 파일명: `기능명-test.js`
* PR 시 테스트 결과 캡처 첨부

---

## 🔥 한 줄 요약

👉 **k6 실행 → InfluxDB 저장 → Grafana 확인**
