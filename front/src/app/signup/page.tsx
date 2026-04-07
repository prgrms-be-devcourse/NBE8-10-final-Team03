"use client";

import { useState, type KeyboardEvent } from "react";
import { useRouter } from "next/navigation";
import Link from "next/link";
import api from "@/lib/api";

export default function SignupPage() {
  const router = useRouter();
  const [username, setUsername] = useState("");
  const [nickname, setNickname] = useState("");
  const [password, setPassword] = useState("");
  const [passwordConfirm, setPasswordConfirm] = useState("");
  const [error, setError] = useState("");
  const [nicknameError, setNicknameError] = useState("");
  const [usernameError, setUsernameError] = useState("");
  const [isCapsLockOn, setIsCapsLockOn] = useState(false);
  const [loading, setLoading] = useState(false);

  const handleNicknameChange = (value: string) => {
    // 특수문자 포함 여부 확인
    if (/[^가-힣a-zA-Z0-9]/.test(value)) {
      setNicknameError("특수문자는 사용할 수 없습니다.");
    } else {
      setNicknameError("");
    }
    // 한글, 영문, 숫자만 입력 허용
    const filtered = value.replace(/[^가-힣a-zA-Z0-9]/g, "");
    setNickname(filtered);
  };

  const handleUsernameChange = (value: string) => {
    // 특수문자 포함 여부 확인
    if (/[^a-zA-Z0-9]/.test(value)) {
      setUsernameError("특수문자는 사용할 수 없습니다.");
    } else {
      setUsernameError("");
    }
    // 영문, 숫자만 입력 허용
    const filtered = value.replace(/[^a-zA-Z0-9]/g, "");
    setUsername(filtered);
  };

  const handleCapsLock = (e: KeyboardEvent<HTMLInputElement>) => {
    setIsCapsLockOn(e.getModifierState("CapsLock"));
  };

  const handleSignup = async () => {
    setError("");

    // 닉네임 유효성 검사
    if (!/^[가-힣a-zA-Z0-9]+$/.test(nickname)) {
      setError("닉네임은 한글, 영문, 숫자만 사용 가능합니다.");
      return;
    }

    if (password !== passwordConfirm) {
      setError("비밀번호가 일치하지 않습니다.");
      return;
    }

    setLoading(true);
    try {
      // 1. 회원가입
      await api.post("/auth/signup", { username, password, nickname });

      // 2. 바로 로그인
      const res = await api.post("/auth/login", { username, password });
      const { nickname: nick, userId } = res.data.data;

      localStorage.setItem("nickname", nick);
      localStorage.setItem("userId", String(userId));
      localStorage.removeItem("oauth");

      router.push("/rooms");
    } catch (err: any) {
      setError(err.response?.data?.message || "회원가입에 실패했습니다.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="flex min-h-screen">
      {/* 좌측 비주얼 */}
      <div className="relative w-1/2 bg-accent flex flex-col justify-center px-16 overflow-hidden">
        <span className="absolute top-8 left-8 font-title text-[100px] text-white opacity-10 -rotate-12 select-none">!</span>
        <span className="absolute top-20 right-12 font-title text-[80px] text-white opacity-10 rotate-6 select-none">?</span>
        <span className="absolute bottom-8 right-8 font-title text-[120px] text-white opacity-10 rotate-12 select-none">!?!</span>

        <Link href="/" className="absolute top-8 left-8 font-title text-2xl z-10">
          <span className="text-white">답</span>
          <span className="text-white/80">정</span>
          <span className="text-secondary">너</span>
        </Link>

        <h1 className="font-title text-5xl text-white mb-4 relative z-10">
          새로운 도전자!
          <br />환영한다
        </h1>
        <p className="font-hand text-xl text-white/70 mb-10 relative z-10">
          퀴즈 배틀의 세계에 온 걸 환영해!
        </p>
        <div className="flex gap-3 relative z-10">
          <span className="px-4 py-2 border-2 border-white/40 rounded-full text-sm font-bold text-white bg-white/15">
            실시간 대결
          </span>
          <span className="px-4 py-2 border-2 border-white/40 rounded-full text-sm font-bold text-white bg-white/15">
            AI 채점
          </span>
          <span className="px-4 py-2 border-2 border-white/40 rounded-full text-sm font-bold text-white bg-white/15">
            랭킹 경쟁
          </span>
        </div>
      </div>

      {/* 우측 폼 */}
      <div className="w-1/2 bg-white flex items-center justify-center px-16">
        <div className="w-full max-w-md">
          <div className="flex items-center gap-3 mb-8">
            <h2 className="font-title text-3xl">회원가입</h2>
            <span className="px-3 py-1 bg-secondary border-2 border-dark rounded-lg font-hand text-sm font-bold -rotate-2 shadow-kitsch-sm">
              NEW
            </span>
          </div>

          {error && (
            <div className="mb-4 px-4 py-3 bg-red-50 border-2 border-red-300 rounded-xl text-sm text-red-600 font-bold">
              {error}
            </div>
          )}

          <div className="mb-4">
            <label className="block text-sm font-bold mb-2">아이디</label>
            <input
              type="text"
              placeholder="영문, 숫자 4~20자"
              value={username}
              onChange={(e) => handleUsernameChange(e.target.value)}
              className="w-full px-4 py-3 bg-cream border-[3px] border-dark rounded-xl text-sm focus:border-primary outline-none transition-colors"
            />
            {usernameError && (
              <p className="text-xs text-orange-500 font-bold mt-1">{usernameError}</p>
            )}
          </div>

          <div className="mb-4">
            <label className="block text-sm font-bold mb-2">닉네임</label>
            <input
              type="text"
              placeholder="2~20자 (한글, 영문, 숫자만 가능)"
              value={nickname}
              onChange={(e) => handleNicknameChange(e.target.value)}
              className="w-full px-4 py-3 bg-cream border-[3px] border-dark rounded-xl text-sm focus:border-primary outline-none transition-colors"
            />
            {nicknameError && (
              <p className="text-xs text-orange-500 font-bold mt-1">{nicknameError}</p>
            )}
          </div>

          <div className="grid grid-cols-2 gap-3 mb-6">
            <div>
              <label className="block text-sm font-bold mb-2">비밀번호</label>
              <input
                type="password"
                placeholder="영문+숫자 8~20자"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                onKeyDown={handleCapsLock}
                onKeyUp={handleCapsLock}
                onBlur={() => setIsCapsLockOn(false)}
                className="w-full px-4 py-3 bg-cream border-[3px] border-dark rounded-xl text-sm focus:border-primary outline-none transition-colors"
              />
            </div>
            <div>
              <label className="block text-sm font-bold mb-2">비밀번호 확인</label>
              <input
                type="password"
                placeholder="비밀번호 재입력"
                value={passwordConfirm}
                onChange={(e) => setPasswordConfirm(e.target.value)}
                onKeyDown={(e) => {
                  handleCapsLock(e);
                  if (e.key === "Enter") handleSignup();
                }}
                onKeyUp={handleCapsLock}
                onBlur={() => setIsCapsLockOn(false)}
                className="w-full px-4 py-3 bg-cream border-[3px] border-dark rounded-xl text-sm focus:border-primary outline-none transition-colors"
              />
            </div>
          </div>
          {isCapsLockOn && (
            <p className="text-xs text-orange-500 font-bold mb-4">Caps Lock이 켜져 있습니다. 비밀번호 입력 시 주의하세요.</p>
          )}

          <button
            onClick={handleSignup}
            disabled={loading}
            className="w-full py-4 bg-primary text-white font-bold text-lg border-[3px] border-dark rounded-xl shadow-kitsch hover:shadow-kitsch-lg hover:-translate-y-0.5 transition-all disabled:opacity-50"
          >
            {loading ? "가입 중..." : "가입하기"}
          </button>

          <div className="flex items-center my-6 gap-3">
            <div className="flex-1 h-[2px] bg-gray-200"></div>
            <span className="text-sm text-gray-400 font-bold">또는</span>
            <div className="flex-1 h-[2px] bg-gray-200"></div>
          </div>

          <div className="grid grid-cols-2 gap-3">
          <button
  onClick={() => window.location.href = `${process.env.NEXT_PUBLIC_API_BASE_URL || "http://localhost:8080"}/oauth2/authorization/kakao`}
  className="flex items-center justify-center gap-2 py-3 bg-[#FEE500] border-[3px] border-dark rounded-xl font-bold text-sm shadow-kitsch-sm hover:shadow-kitsch hover:-translate-y-0.5 transition-all"
>
  <svg width="18" height="18" viewBox="0 0 18 18"><path d="M9 1C4.58 1 1 3.79 1 7.21c0 2.17 1.45 4.08 3.63 5.17l-.93 3.42c-.08.3.26.54.52.37l4.1-2.72c.22.02.44.03.68.03 4.42 0 8-2.79 8-6.27S13.42 1 9 1z" fill="#3C1E1E"/></svg>
  카카오로 시작
</button>
<button
  onClick={() => window.location.href = `${process.env.NEXT_PUBLIC_API_BASE_URL || "http://localhost:8080"}/oauth2/authorization/google`}
  className="flex items-center justify-center gap-2 py-3 bg-white border-[3px] border-dark rounded-xl font-bold text-sm shadow-kitsch-sm hover:shadow-kitsch hover:-translate-y-0.5 transition-all"
>
  <svg width="18" height="18" viewBox="0 0 18 18">
    <path d="M17.64 9.2c0-.64-.06-1.25-.16-1.84H9v3.48h4.84a4.14 4.14 0 01-1.8 2.72v2.26h2.92a8.78 8.78 0 002.68-6.62z" fill="#4285F4"/>
    <path d="M9 18c2.43 0 4.47-.8 5.96-2.18l-2.92-2.26c-.8.54-1.83.86-3.04.86-2.34 0-4.32-1.58-5.03-3.71H.96v2.33A9 9 0 009 18z" fill="#34A853"/>
    <path d="M3.97 10.71A5.41 5.41 0 013.68 9c0-.6.1-1.17.29-1.71V4.96H.96A9 9 0 000 9c0 1.45.35 2.82.96 4.04l3.01-2.33z" fill="#FBBC05"/>
    <path d="M9 3.58c1.32 0 2.5.45 3.44 1.35l2.58-2.59C13.46.89 11.43 0 9 0A9 9 0 00.96 4.96l3.01 2.33C4.68 5.16 6.66 3.58 9 3.58z" fill="#EA4335"/>
  </svg>
  Google로 시작
</button>
          </div>

          <p className="text-center mt-8 text-sm text-gray-500">
            이미 계정이 있다면?{" "}
            <Link href="/login" className="text-primary font-bold hover:underline">
              로그인
            </Link>
          </p>
        </div>
      </div>
    </div>
  );
}