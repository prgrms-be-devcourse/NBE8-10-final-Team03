import http from 'k6/http';
import { check, sleep } from 'k6';

const BASE_URL = 'http://host.docker.internal:8080';

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

const PERIODS = ['all', 'weekly', 'monthly'];

export default function () {
    const period = PERIODS[__VU % PERIODS.length];

    const res = http.get(`${BASE_URL}/api/v1/rankings?period=${period}`);

    check(res, {
        [`랭킹 조회(${period}) 200`]: (r) => r.status === 200,
    });

    sleep(1);
}
