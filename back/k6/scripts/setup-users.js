import http from 'k6/http';

const BASE_URL = 'http://host.docker.internal:8080';
const TOTAL_USERS = 50;

export const options = {
    vus: 1,
    iterations: 1,
};

export default function () {
    let created = 0;
    let skipped = 0;

    for (let i = 0; i < TOTAL_USERS; i++) {
        const user = {
            username: `loadtest${String(i).padStart(4, '0')}`,
            password: `Test1234a`,
            nickname: `loadtester${i}`,
        };

        const res = http.post(
            `${BASE_URL}/api/v1/auth/signup`,
            JSON.stringify(user),
            { headers: { 'Content-Type': 'application/json' } }
        );

        if (res.status === 200 || res.status === 201) {
            created++;
        } else {
            skipped++;
        }
    }

    console.log(`생성: ${created}명 / 이미 존재: ${skipped}명`);
}
