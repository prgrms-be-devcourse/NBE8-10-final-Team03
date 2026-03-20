"use client";

import Link from "next/link";

// TODO: 나중에 JWT에서 가져오기. 지금은 Mock
const mockUser = { nickname: "스프링러버" };
const isLoggedIn = true;

export default function Header() {
  return (
    <nav className="flex justify-between items-center px-8 py-4 bg-white border-b-[3px] border-dark">
      <Link href="/" className="font-title text-3xl tracking-tight">
        <span className="text-primary">답</span>
        <span className="text-dark">정</span>
        <span className="text-accent">너</span>
      </Link>
      <div className="flex gap-6 font-body font-bold text-sm">
        <Link href="/rooms" className="hover:text-primary">로비</Link>
        <Link href="/quizsets" className="hover:text-primary">퀴즈셋</Link>
        <Link href="/rankings" className="hover:text-primary">랭킹</Link>
      </div>
      <div className="flex gap-3 items-center">
        {isLoggedIn ? (
          <Link href="/me" className="flex items-center gap-2 px-5 py-2.5 border-[3px] border-dark rounded-xl font-bold text-sm bg-cream shadow-kitsch-sm hover:shadow-kitsch hover:-translate-y-0.5 transition-all">
            <span className="w-7 h-7 bg-primary text-white rounded-full flex items-center justify-center text-xs font-title">
              {mockUser.nickname.charAt(0)}
            </span>
            {mockUser.nickname}
          </Link>
        ) : (
          <>
            <Link href="/login" className="px-5 py-2.5 border-[3px] border-dark rounded-xl font-bold text-sm bg-white shadow-kitsch-sm hover:shadow-kitsch hover:-translate-y-0.5 transition-all">
              로그인
            </Link>
            <Link href="/signup" className="px-5 py-2.5 border-[3px] border-dark rounded-xl font-bold text-sm bg-primary text-white shadow-kitsch-sm hover:shadow-kitsch hover:-translate-y-0.5 transition-all">
              회원가입
            </Link>
          </>
        )}
      </div>
    </nav>
  );
}