/**
 * lobby-scenario-test.js
 *
 * 실제 유저 행동 패턴을 재현하는 복합 시나리오 부하테스트입니다.
 *
 * [시나리오 1] browser (60 VU) - 로비 브라우저
 *   로그인 → 방 목록 조회 3회 → 랭킹 조회 → 로그아웃
 *
 * [시나리오 2] creator (40 VU) - 콘텐츠 제작자
 *   로그인 → 퀴즈셋 생성 → 퀴즈 3개 추가 → 방 생성 → 방 퇴장(방장) → 퀴즈셋 삭제(cascade 정리) → 로그아웃
 *
 * 측정 포인트:
 *   - GET /api/v1/rooms          : findAll() 페이징 없는 전체 조회 성능
 *   - POST /api/v1/quizsets      : DB 쓰기 + 트랜잭션 처리
 *   - POST /api/v1/rooms         : quizSet 조회 + gameSession 저장
 *   - DELETE /api/v1/rooms/{id}/leave : 방장 퇴장 → WebSocket broadcast + gameSession END
 *   - GET /api/v1/rankings       : 캐시 히트율 확인
 *
 * 사전 준비: setup-users.js 로 테스트 유저 50명 생성 필요
 */

import http from 'k6/http';
import { check, sleep } from 'k6';

const BASE_URL = 'http://host.docker.internal:8080';
const TOTAL_USERS = 50;
const JSON_HEADERS = { 'Content-Type': 'application/json' };

export const options = {
    scenarios: {
        browser: {
            executor: 'ramping-vus',
            startVUs: 0,
            stages: [
                { duration: '30s', target: 30 },
                { duration: '1m',  target: 30 },
                { duration: '30s', target: 0  },
            ],
            env: { SCENARIO: 'browser' },
            tags: { scenario: 'browser' },
        },
        creator: {
            executor: 'ramping-vus',
            startVUs: 0,
            stages: [
                { duration: '30s', target: 20 },
                { duration: '1m',  target: 20 },
                { duration: '30s', target: 0  },
            ],
            env: { SCENARIO: 'creator' },
            tags: { scenario: 'creator' },
        },
    },
    thresholds: {
        // 전체 기준
        // EC2 목표: rate<0.01 / p(95)<1000
        'http_req_failed':                        ['rate<0.05'],
        'http_req_duration':                      ['p(95)<3000'],
        // 시나리오별 기준
        // EC2 목표: browser p(95)<1000 / creator p(95)<2000
        'http_req_duration{scenario:browser}':    ['p(95)<3000'],
        'http_req_duration{scenario:creator}':    ['p(95)<3000'],
        // API별 기준
        // EC2 목표: room-list p(95)<1000 / ranking p(95)<500 / 쓰기 p(95)<2000
        'http_req_duration{api:room-list}':       ['p(95)<3000'],
        'http_req_duration{api:ranking}':         ['p(95)<2000'],
        'http_req_duration{api:quizset-create}':  ['p(95)<3000'],
        'http_req_duration{api:room-create}':     ['p(95)<3000'],
    },
};

// ─────────────────────────────────────────────
// 공통 헬퍼
// ─────────────────────────────────────────────

function getUser() {
    const idx = (__VU - 1) % TOTAL_USERS;
    return {
        username: `loadtest${String(idx).padStart(4, '0')}`,
        password: 'Test1234a',
    };
}

function login() {
    const user = getUser();
    const jar = http.cookieJar();

    const res = http.post(
        `${BASE_URL}/api/v1/auth/login`,
        JSON.stringify({ username: user.username, password: user.password }),
        { jar, headers: JSON_HEADERS, tags: { api: 'login' } }
    );

    const ok = check(res, { '로그인 200': (r) => r.status === 200 });
    if (!ok) {
        console.error(`[VU ${__VU}] 로그인 실패: ${res.status} ${res.body}`);
        return { jar, ok: false };
    }

    // 서버 쿠키가 Secure:true 로 설정되어 있어 HTTP 환경에서 자동 전송 안 됨
    // → 토큰 값을 꺼내 Secure 없이 jar에 재등록
    const accessToken  = res.cookies['accessToken']  && res.cookies['accessToken'][0].value;
    const refreshToken = res.cookies['refreshToken'] && res.cookies['refreshToken'][0].value;

    if (accessToken)  jar.set(BASE_URL, 'accessToken',  accessToken,  { secure: false });
    if (refreshToken) jar.set(BASE_URL, 'refreshToken', refreshToken, { secure: false });

    return { jar, ok: true };
}

function logout(jar) {
    http.post(`${BASE_URL}/api/v1/auth/logout`, null, {
        jar,
        tags: { api: 'logout' },
    });
}

// ─────────────────────────────────────────────
// 시나리오 1: 로비 브라우저 (read-heavy)
// ─────────────────────────────────────────────

function browserScenario() {
    const { jar, ok } = login();
    if (!ok) return;

    sleep(1);

    // 방 목록 3회 조회 (findAll() 성능 측정 핵심)
    for (let i = 0; i < 3; i++) {
        const res = http.get(`${BASE_URL}/api/v1/rooms`, {
            jar,
            tags: { scenario: 'browser', api: 'room-list' },
        });
        check(res, { '방 목록 200': (r) => r.status === 200 });
        sleep(1);
    }

    // 랭킹 조회 (캐시 히트율 측정)
    const rankRes = http.get(`${BASE_URL}/api/v1/rankings?period=weekly`, {
        jar,
        tags: { scenario: 'browser', api: 'ranking' },
    });
    check(rankRes, { '랭킹 200': (r) => r.status === 200 });

    sleep(1);
    logout(jar);
}

// ─────────────────────────────────────────────
// 시나리오 2: 콘텐츠 제작자 (write-heavy)
// ─────────────────────────────────────────────

function creatorScenario() {
    const { jar, ok } = login();
    if (!ok) return;

    sleep(1);

    // ── Step 1: 퀴즈셋 생성 ──────────────────────
    const quizSetRes = http.post(
        `${BASE_URL}/api/v1/quizsets`,
        JSON.stringify({
            title: `부하테스트 퀴즈셋 ${__VU}-${Date.now()}`,
            description: '부하테스트용 퀴즈셋입니다.',
        }),
        { jar, headers: JSON_HEADERS, tags: { scenario: 'creator', api: 'quizset-create' } }
    );

    const quizSetOk = check(quizSetRes, { '퀴즈셋 생성 201': (r) => r.status === 201 });
    if (!quizSetOk) {
        logout(jar);
        return;
    }

    // Location 헤더에서 quizSetId 추출: /api/v1/quizsets/{id}
    const quizSetId = quizSetRes.headers['Location'].split('/').pop();

    // ── Step 2: 퀴즈 3개 추가 (방 생성 시 maxQuizzes 충족을 위해 먼저 추가) ──
    const quizzes = [
        {
            questionType: 'TEXT',
            answerType: 'MULTIPLE_CHOICE',
            content: '다음 중 Java의 특징이 아닌 것은?',
            answer: '1',
            choice1: '포인터 직접 조작',
            choice2: '플랫폼 독립성',
            choice3: '객체지향',
            choice4: '자동 메모리 관리',
        },
        {
            questionType: 'TEXT',
            answerType: 'MULTIPLE_CHOICE',
            content: 'HTTP 상태코드 404의 의미는?',
            answer: '2',
            choice1: '서버 내부 오류',
            choice2: '리소스를 찾을 수 없음',
            choice3: '권한 없음',
            choice4: '요청 성공',
        },
        {
            questionType: 'TEXT',
            answerType: 'MULTIPLE_CHOICE',
            content: 'Spring Boot의 기본 내장 서버는?',
            answer: '3',
            choice1: 'Jetty',
            choice2: 'Undertow',
            choice3: 'Tomcat',
            choice4: 'Netty',
        },
    ];

    for (const quiz of quizzes) {
        const quizRes = http.post(
            `${BASE_URL}/api/v1/quizsets/${quizSetId}/quizzes`,
            JSON.stringify(quiz),
            { jar, headers: JSON_HEADERS, tags: { scenario: 'creator', api: 'quiz-create' } }
        );
        check(quizRes, { '퀴즈 생성 201': (r) => r.status === 201 });
        sleep(0.3);
    }

    // ── Step 3: 방 생성 ───────────────────────────
    const roomRes = http.post(
        `${BASE_URL}/api/v1/rooms`,
        JSON.stringify({
            roomName: `테스트방-VU${__VU}`,
            quizSetId: parseInt(quizSetId),
            maxQuizzes: 3,
            maxPlayers: 4,
        }),
        { jar, headers: JSON_HEADERS, tags: { scenario: 'creator', api: 'room-create' } }
    );

    const roomOk = check(roomRes, { '방 생성 201': (r) => r.status === 201 });
    if (!roomOk) {
        // 방 생성 실패 시 퀴즈셋 삭제 후 종료 (cascade로 퀴즈도 삭제됨)
        http.del(`${BASE_URL}/api/v1/quizsets/${quizSetId}`, null, { jar });
        logout(jar);
        return;
    }

    // Location 헤더에서 roomId 추출: /api/v1/rooms/{id}
    const roomId = roomRes.headers['Location'].split('/').pop();

    sleep(1);

    // ── Step 4: 방 퇴장 (방장 퇴장 → room status END + WebSocket broadcast) ──
    const leaveRes = http.del(
        `${BASE_URL}/api/v1/rooms/${roomId}/leave`,
        null,
        { jar, tags: { scenario: 'creator', api: 'room-leave' } }
    );
    check(leaveRes, { '방 퇴장 204': (r) => r.status === 204 });

    sleep(0.5);

    // ── Step 5: 퀴즈셋 삭제 (북마크·게임기록·게임세션·퀴즈 cascade 삭제) ──
    const deleteRes = http.del(
        `${BASE_URL}/api/v1/quizsets/${quizSetId}`,
        null,
        { jar, tags: { scenario: 'creator', api: 'quizset-delete' } }
    );
    check(deleteRes, { '퀴즈셋 삭제 204': (r) => r.status === 204 });

    sleep(1);
    logout(jar);
}

// ─────────────────────────────────────────────
// 진입점
// ─────────────────────────────────────────────

export default function () {
    if (__ENV.SCENARIO === 'browser') {
        browserScenario();
    } else {
        creatorScenario();
    }
}
