import axios from "axios";

const api = axios.create({
  baseURL: process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080/api/v1",
  withCredentials: true,
});

// 토큰 갱신 중인지 확인하는 플래그
let isRefreshing = false;
let failedQueue: any[] = [];

// 큐에 쌓인 요청들을 처리
const processQueue = (error: any, token: string | null = null) => {
  failedQueue.forEach((prom) => {
    if (error) {
      prom.reject(error);
    } else {
      prom.resolve(token);
    }
  });

  failedQueue = [];
};

// 응답 인터셉터: 401이면 토큰 갱신 시도
api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;

    if (error.response?.status === 401 && !originalRequest._retry) {
      const isLoginRequest = originalRequest?.url?.includes("/auth/login");
      const isReissueRequest = originalRequest?.url?.includes("/auth/reissue");

      if (isLoginRequest || isReissueRequest) {
        // 로그인이나 reissue 요청 자체가 401이면 바로 리다이렉트
        localStorage.removeItem("nickname");
        localStorage.removeItem("userId");
        window.location.href = "/login";
        return Promise.reject(error);
      }

      if (isRefreshing) {
        // 이미 갱신 중이면 큐에 추가
        return new Promise((resolve, reject) => {
          failedQueue.push({ resolve, reject });
        }).then(() => {
          return api(originalRequest);
        }).catch((err) => {
          return Promise.reject(err);
        });
      }

      originalRequest._retry = true;
      isRefreshing = true;

      try {
        // reissue 요청 (쿠키로 refreshToken 사용)
        await api.post("/auth/reissue");
        // 백엔드가 쿠키를 갱신한다고 가정

        // 큐 처리
        processQueue(null, null);

        // 원래 요청 재시도
        return api(originalRequest);
      } catch (refreshError) {
        // 갱신 실패 시 리다이렉트
        processQueue(refreshError, null);
        localStorage.removeItem("nickname");
        localStorage.removeItem("userId");
        window.location.href = "/login";
        return Promise.reject(refreshError);
      } finally {
        isRefreshing = false;
      }
    }

    return Promise.reject(error);
  }
);

export default api;