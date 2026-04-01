import http from 'k6/http';
import { check, sleep } from 'k6';

const BASE_URL = 'http://host.docker.internal:8080';
const TOTAL_USERS = 50;

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

// 테스트 유저 사전 생성 필요: setup-users.js 먼저 실행
export default function () {
    const user = {
        username: `loadtest${String((__VU - 1) % TOTAL_USERS).padStart(4, '0')}`,
        password: `Test1234a`,
    };
    const jar = http.cookieJar();
    const jsonHeaders = { headers: { 'Content-Type': 'application/json' } };

    // 1. 로그인
    const loginRes = http.post(
        `${BASE_URL}/api/v1/auth/login`,
        JSON.stringify({ username: user.username, password: user.password }),
        { jar, ...jsonHeaders }
    );

    const loginOk = check(loginRes, {
        '로그인 200': (r) => r.status === 200,
    });

    if (!loginOk) {
        console.error(`로그인 실패: ${loginRes.status} ${loginRes.body}`);
        return;
    }

    sleep(1);

    // 2. reissue 3회 반복
    for (let i = 0; i < 3; i++) {
        const reissueRes = http.post(
            `${BASE_URL}/api/v1/auth/reissue`,
            null,
            { jar }
        );

        check(reissueRes, {
            [`reissue ${i + 1}회 200`]: (r) => r.status === 200,
        });

        sleep(1);
    }

    // 3. 로그아웃
    const logoutRes = http.post(
        `${BASE_URL}/api/v1/auth/logout`,
        null,
        { jar }
    );

    check(logoutRes, {
        '로그아웃 200': (r) => r.status === 200,
    });
}
