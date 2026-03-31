"use client";

import { useEffect, Suspense } from "react";
import { useRouter, useSearchParams } from "next/navigation";

function OAuthCallbackContent() {
  const router = useRouter();
  const searchParams = useSearchParams();

  useEffect(() => {
    const userId = searchParams.get("userId");
    const nickname = searchParams.get("nickname");

    if (!userId || !nickname) {
      router.push("/login");
      return;
    }

    localStorage.setItem("userId", userId);
    localStorage.setItem("nickname", nickname);
    router.push("/rooms");
  }, []);

  return (
    <div className="flex min-h-screen items-center justify-center">
      <p className="font-hand text-xl text-gray-400">로그인 처리 중...</p>
    </div>
  );
}

export default function OAuthCallbackPage() {
  return (
    <Suspense fallback={
      <div className="flex min-h-screen items-center justify-center">
        <p className="font-hand text-xl text-gray-400">로딩 중...</p>
      </div>
    }>
      <OAuthCallbackContent />
    </Suspense>
  );
}