"use client";

import { useState, useEffect } from "react";
import api from "@/lib/api";

interface RankingItem {
  rank: number;
  nickname: string;
  totalRankingScore: number;
}

export default function RankingsPage() {
  const [rankings, setRankings] = useState<RankingItem[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchRankings = async () => {
      try {
        const res = await api.get("/rankings");
        setRankings(res.data.data.rankings);
      } catch (err) {
        console.error("랭킹 조회 실패", err);
      } finally {
        setLoading(false);
      }
    };
    fetchRankings();
  }, []);

  if (loading) {
    return (
      <div className="max-w-4xl mx-auto px-4 py-10 text-center">
        <p className="font-hand text-xl text-gray-400">랭킹 불러오는 중...</p>
      </div>
    );
  }

  if (rankings.length === 0) {
    return (
      <div className="max-w-4xl mx-auto px-4 py-10 text-center">
        <h1 className="font-title text-4xl mb-4">🏆 랭킹</h1>
        <p className="font-hand text-xl text-gray-400">아직 랭킹 데이터가 없어요!</p>
      </div>
    );
  }

  const top3 = rankings.slice(0, 3);
  const rest = rankings.slice(3);
  const myNickname = localStorage.getItem("nickname");

  return (
    <div className="max-w-4xl mx-auto px-4 py-10">
      {/* 헤더 */}
      <div className="flex items-center justify-between mb-10">
        <h1 className="font-title text-4xl">🏆 랭킹</h1>
        <div className="flex gap-2">
          <button className="px-5 py-2 border-[3px] border-dark rounded-full font-bold text-sm bg-secondary shadow-kitsch-sm">
            전체
          </button>
          <button className="px-5 py-2 border-[3px] border-dark rounded-full font-bold text-sm bg-white shadow-kitsch-sm hover:bg-gray-50 transition-colors" disabled>
            주간
          </button>
          <button className="px-5 py-2 border-[3px] border-dark rounded-full font-bold text-sm bg-white shadow-kitsch-sm hover:bg-gray-50 transition-colors" disabled>
            월간
          </button>
        </div>
      </div>

      {/* 포디움 */}
      {top3.length >= 3 && (
        <div className="flex items-end justify-center gap-6 mb-14">
          {/* 2등 */}
          <div className="text-center">
            <div className="w-24 h-24 mx-auto mb-3 rounded-full border-[4px] border-accent bg-white flex items-center justify-center">
              <span className="font-title text-3xl text-accent">2</span>
            </div>
            <p className="font-bold text-sm mb-1">{top3[1].nickname}</p>
            <span className="inline-block px-3 py-1 bg-accent/10 border-2 border-accent rounded-full text-xs font-bold text-accent">
              {top3[1].totalRankingScore.toLocaleString()}P
            </span>
          </div>

          {/* 1등 */}
          <div className="text-center -mt-6">
            <div className="text-3xl mb-1">👑</div>
            <div className="w-32 h-32 mx-auto mb-3 rounded-full border-[4px] border-primary bg-white flex items-center justify-center shadow-kitsch">
              <span className="font-title text-4xl text-primary">1</span>
            </div>
            <p className="font-bold mb-1">{top3[0].nickname}</p>
            <span className="inline-block px-4 py-1.5 bg-primary/10 border-2 border-primary rounded-full text-sm font-bold text-primary">
              {top3[0].totalRankingScore.toLocaleString()}P
            </span>
          </div>

          {/* 3등 */}
          <div className="text-center">
            <div className="w-24 h-24 mx-auto mb-3 rounded-full border-[4px] border-secondary bg-white flex items-center justify-center">
              <span className="font-title text-3xl text-secondary">3</span>
            </div>
            <p className="font-bold text-sm mb-1">{top3[2].nickname}</p>
            <span className="inline-block px-3 py-1 bg-secondary/10 border-2 border-secondary rounded-full text-xs font-bold text-secondary">
              {top3[2].totalRankingScore.toLocaleString()}P
            </span>
          </div>
        </div>
      )}

      {/* 4위부터 테이블 */}
      {rest.length > 0 && (
        <div className="bg-white border-[3px] border-dark rounded-2xl shadow-kitsch overflow-hidden">
          {rest.map((r) => (
            <div
              key={r.rank}
              className={`flex items-center px-6 py-4 border-b-2 border-dashed border-gray-200 last:border-b-0 hover:bg-cream/50 transition-colors ${
                r.nickname === myNickname ? "bg-secondary/20" : ""
              }`}
            >
              <span className="font-title text-2xl w-12 text-center">
                {r.rank}
              </span>
              <div className="w-10 h-10 rounded-full bg-cream border-2 border-dark flex items-center justify-center text-sm font-bold mr-4">
                {r.nickname.charAt(0)}
              </div>
              <div className="flex-1">
                <p className="font-bold text-sm">
                  {r.nickname}
                  {r.nickname === myNickname && (
                    <span className="ml-2 px-2 py-0.5 bg-secondary border border-dark rounded text-xs font-bold">
                      나
                    </span>
                  )}
                </p>
              </div>
              <div className="text-right">
                <p className="font-title text-lg text-primary">{r.totalRankingScore.toLocaleString()}P</p>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}