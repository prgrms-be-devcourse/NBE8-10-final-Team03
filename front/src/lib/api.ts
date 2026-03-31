import axios from "axios";

const api = axios.create({
  baseURL: "http://localhost:8080/api/v1",
  withCredentials: true,
});

// 응답 인터셉터: 401이면 로그인 페이지로
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      const isLoginRequest = error.config?.url?.includes("/auth/login");
      if (!isLoginRequest) {
        // accessToken localStorage에서 제거 → 이제 쿠키라 불필요하지만
        // nickname, userId는 지워줘야 해
        localStorage.removeItem("nickname");
        localStorage.removeItem("userId");
        window.location.href = "/login";
      }
    }
    return Promise.reject(error);
  }
);

export default api;