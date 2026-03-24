"use client";

import { useState, useEffect } from "react";
import api from "@/lib/api";

interface RecordItem {
  quizSetTitle: string;
  maxPlayers: number;
  sessionRanking: number;
  sessionScore: number;
  earnedRankingScore: number;
  playedAt: string;
}

interface RecordsData {
  totalGames: number;
  totalWins: number;
  totalRankingScore: number;
  recentRecords: RecordItem[];
  page: number;
  size: number;
  totalElements: number;
}

export default function RecordsPage() {
  const [data, setData] = useState<RecordsData | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchRecords = async () => {
      try {
        const res = await api.get("/me/records?page=0&size=10");
        setData(res.data.data);
      } catch (err) {
        console.error("전적 조회 실패", err);
      } finally {
        setLoading(false);
      }
    };
    fetchRecords();
  }, []);

  if (loading) {
    return (
      <div className="max-w-4xl mx-auto px-4 py-10 text-center">
        <p className="font-hand text-xl text-gray-400">전적 불러오는 중...</p>
      </div>
    );
  }

  if (!data) {
    return (
      <div className="max-w-4xl mx-auto px-4 py-10 text-center">
        <h1 className="font-title text-4xl mb-4">📊 내 전적</h1>
        <p className="font-hand text-xl text-gray-400">전적을 불러올 수 없습니다.</p>
      </div>
    );
  }

  const winRate = data.totalGames > 0
    ? ((data.totalWins / data.totalGames) * 100).toFixed(1)
    : "0.0";

  return (
    <div className="max-w-4xl mx-auto px-4 py-10">
      <h1 className="font-title text-4xl mb-8">📊 내 전적</h1>

      {/* 통계 카드 */}
      <div className="grid grid-cols-4 gap-4 mb-10">
        <div className="bg-white border-[3px] border-dark rounded-2xl p-5 shadow-kitsch text-center">
          <p className="font-hand text-sm text-gray-400 mb-1">총 게임</p>
          <p className="font-title text-4xl">{data.totalGames}</p>
        </div>
        <div className="bg-white border-[3px] border-dark rounded-2xl p-5 shadow-kitsch text-center">
          <p className="font-hand text-sm text-gray-400 mb-1">총 우승</p>
          <p className="font-title text-4xl text-primary">{data.totalWins}</p>
        </div>
        <div className="bg-white border-[3px] border-dark rounded-2xl p-5 shadow-kitsch text-center">
          <p className="font-hand text-sm text-gray-400 mb-1">승률</p>
          <p className="font-title text-4xl text-accent">{winRate}%</p>
        </div>
        <div className="bg-white border-[3px] border-dark rounded-2xl p-5 shadow-kitsch text-center">
          <p className="font-hand text-sm text-gray-400 mb-1">랭킹 점수</p>
          <p className="font-title text-4xl text-secondary">{data.totalRankingScore.toLocaleString()}</p>
        </div>
      </div>

      {/* 최근 전적 */}
      <h2 className="font-title text-2xl mb-4">최근 전적</h2>
      {data.recentRecords.length === 0 ? (
        <div className="bg-white border-[3px] border-dark rounded-2xl shadow-kitsch p-10 text-center">
          <p className="font-hand text-lg text-gray-400">아직 게임 기록이 없어요!</p>
        </div>
      ) : (
        <div className="bg-white border-[3px] border-dark rounded-2xl shadow-kitsch overflow-hidden">
          <div className="flex items-center px-6 py-3 bg-cream border-b-[3px] border-dark text-sm font-bold text-gray-500">
            <span className="w-48">퀴즈셋</span>
            <span className="w-20 text-center">인원</span>
            <span className="w-20 text-center">순위</span>
            <span className="w-24 text-center">점수</span>
            <span className="w-28 text-center">랭킹 포인트</span>
            <span className="flex-1 text-right">날짜</span>
          </div>

          {data.recentRecords.map((r, i) => (
            <div
              key={i}
              className="flex items-center px-6 py-4 border-b-2 border-dashed border-gray-200 last:border-b-0 hover:bg-cream/50 transition-colors"
            >
              <span className="w-48 font-bold text-sm truncate">{r.quizSetTitle}</span>
              <span className="w-20 text-center text-sm text-gray-500">{r.maxPlayers}명</span>
              <span className="w-20 text-center">
                {r.sessionRanking === 1 ? (
                  <span className="inline-block px-3 py-1 bg-primary text-white border-2 border-dark rounded-full text-xs font-bold shadow-kitsch-sm">
                    🏆 1등
                  </span>
                ) : (
                  <span className="inline-block px-3 py-1 bg-cream border-2 border-dark rounded-full text-xs font-bold">
                    {r.sessionRanking}등
                  </span>
                )}
              </span>
              <span className="w-24 text-center font-title text-lg">{r.sessionScore}</span>
              <span className="w-28 text-center font-bold text-sm text-accent">+{r.earnedRankingScore}</span>
              <span className="flex-1 text-right text-sm text-gray-400">
                {new Date(r.playedAt).toLocaleDateString("ko-KR", { month: "short", day: "numeric" })}
              </span>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}