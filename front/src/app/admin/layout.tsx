"use client";

import { useEffect, useState } from "react";
import { useRouter, usePathname } from "next/navigation";
import Link from "next/link";

export default function AdminLayout({ children }: { children: React.ReactNode }) {
  const router = useRouter();
  const pathname = usePathname();
  const [checking, setChecking] = useState(true);

  useEffect(() => {
    // 임시로 localStorage role로 체크 (백엔드에서 403 내려줘도 각 페이지에서 처리)
    const nickname = localStorage.getItem("nickname");
    if (!nickname) {
      router.push("/login");
      return;
    }
    setChecking(false);
  }, []);

  if (checking) return (
    <div className="flex min-h-screen items-center justify-center">
      <p className="font-hand text-xl text-gray-400">확인 중...</p>
    </div>
  );

  const navItems = [
    { href: "/admin/reports", label: "🚨 신고 관리" },
    { href: "/admin/quizsets", label: "📝 퀴즈셋 관리" },
    { href: "/admin/users", label: "👤 사용자 관리" },
  ];

  return (
    <div className="min-h-screen bg-cream">
      {/* 헤더 */}
      <div className="border-b-[3px] border-dark bg-white px-6 py-4 flex items-center justify-between">
        <div className="flex items-center gap-4">
          <Link href="/" className="font-title text-xl">답정너</Link>
          <span className="px-3 py-1 bg-primary text-white text-xs font-bold rounded-full border-2 border-dark">ADMIN</span>
        </div>
        <Link href="/rooms" className="text-sm font-bold text-gray-400 hover:text-primary">
          ← 서비스로 돌아가기
        </Link>
      </div>

      <div className="max-w-7xl mx-auto px-4 py-8 flex gap-6">
        {/* 사이드바 */}
        <div className="w-56 shrink-0">
          <div className="bg-white border-[3px] border-dark rounded-2xl shadow-kitsch p-4">
            <h2 className="font-title text-lg mb-4 px-2">관리자 메뉴</h2>
            <nav className="flex flex-col gap-1">
              {navItems.map((item) => (
                <Link
                  key={item.href}
                  href={item.href}
                  className={`px-4 py-3 rounded-xl font-bold text-sm transition-colors ${
                    pathname === item.href
                      ? "bg-primary text-white"
                      : "hover:bg-cream text-dark"
                  }`}
                >
                  {item.label}
                </Link>
              ))}
            </nav>
          </div>
        </div>

        {/* 메인 콘텐츠 */}
        <div className="flex-1">
          {children}
        </div>
      </div>
    </div>
  );
}