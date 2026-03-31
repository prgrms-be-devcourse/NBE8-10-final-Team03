"use client";

import { useState, useEffect } from "react";
import Link from "next/link";
import api from "@/lib/api";

interface UserInfo {
  nickname: string;
  username: string;
}

interface RecordsStats {
  totalGames: number;
  totalWins: number;
  totalRankingScore: number;
}

export default function MyPage() {
  const [user, setUser] = useState<UserInfo | null>(null);
  const [stats, setStats] = useState<RecordsStats | null>(null);
  const [loading, setLoading] = useState(true);
  const [myUserId, setMyUserId] = useState<string | null>(null);

  useEffect(() => {
    const fetchData = async () => {
      try {
        const userId = localStorage.getItem("userId");
        if (!userId) throw new Error("userId 없음");
  
        setMyUserId(userId);
        
        const [userRes, recordsRes] = await Promise.all([
          api.get(`/users/${userId}`),
          api.get(`/users/${userId}/records?page=0&size=1`),
        ]);
        setUser(userRes.data.data);
        setStats(recordsRes.data.data);
      } catch (err) {
        console.error("마이페이지 조회 실패", err);
      } finally {
        setLoading(false);
      }
    };
    fetchData();
  }, []);

  if (loading) {
    return (
      <div className="max-w-3xl mx-auto px-4 py-10 text-center">
        <p className="font-hand text-xl text-gray-400">불러오는 중...</p>
      </div>
    );
  }

  if (!user || !stats) {
    return (
      <div className="max-w-3xl mx-auto px-4 py-10 text-center">
        <p className="font-hand text-xl text-gray-400">정보를 불러올 수 없습니다.</p>
      </div>
    );
  }

  const winRate = stats.totalGames > 0
    ? ((stats.totalWins / stats.totalGames) * 100).toFixed(1)
    : "0.0";

  return (
    <div className="max-w-3xl mx-auto px-4 py-10">
      <h1 className="font-title text-4xl mb-8">👤 마이페이지</h1>

      {/* 프로필 카드 */}
      <div className="bg-white border-[3px] border-dark rounded-2xl shadow-kitsch p-8 mb-6">
        <div className="flex items-center gap-6 mb-8">
          <div className="w-24 h-24 bg-primary rounded-full border-[3px] border-dark flex items-center justify-center shadow-kitsch">
            <span className="font-title text-4xl text-white">{user.nickname.charAt(0)}</span>
          </div>
          <div>
            <h2 className="font-title text-2xl mb-1">{user.nickname}</h2>
            <p className="text-sm text-gray-400">@{user.username}</p>
          </div>
        </div>

        {/* 통계 */}
        <div className="grid grid-cols-4 gap-4">
          <div className="bg-cream border-[3px] border-dark rounded-xl p-4 text-center">
            <p className="font-hand text-sm text-gray-400 mb-1">총 게임</p>
            <p className="font-title text-3xl">{stats.totalGames}</p>
          </div>
          <div className="bg-cream border-[3px] border-dark rounded-xl p-4 text-center">
            <p className="font-hand text-sm text-gray-400 mb-1">총 우승</p>
            <p className="font-title text-3xl text-primary">{stats.totalWins}</p>
          </div>
          <div className="bg-cream border-[3px] border-dark rounded-xl p-4 text-center">
            <p className="font-hand text-sm text-gray-400 mb-1">승률</p>
            <p className="font-title text-3xl text-accent">{winRate}%</p>
          </div>
          <div className="bg-cream border-[3px] border-dark rounded-xl p-4 text-center">
            <p className="font-hand text-sm text-gray-400 mb-1">랭킹 점수</p>
            <p className="font-title text-3xl text-secondary">{stats.totalRankingScore.toLocaleString()}</p>
          </div>
        </div>
      </div>

      {/* 바로가기 */}
      <div className="grid grid-cols-2 gap-4">
        <Link href={`/users/${myUserId}/records`} className="bg-white border-[3px] border-dark rounded-2xl p-6 shadow-kitsch hover:shadow-kitsch-lg hover:-translate-y-0.5 transition-all text-center">
          <div className="text-3xl mb-2">📊</div>
          <h3 className="font-title text-lg mb-1">내 전적</h3>
          <p className="text-xs text-gray-400">최근 게임 기록 확인</p>
        </Link>
        <Link
    href={`/users/${myUserId}/bookmarks`}
    className="bg-white border-[3px] border-dark rounded-2xl p-6 shadow-kitsch hover:shadow-kitsch-lg hover:-translate-y-0.5 transition-all text-center"
  >
    <div className="text-3xl mb-2">⭐</div>
    <h3 className="font-title text-lg mb-1">즐겨찾기</h3>
    <p className="text-xs text-gray-400">저장한 퀴즈셋 보기</p>
  </Link>
</div>
    </div>
  );
}