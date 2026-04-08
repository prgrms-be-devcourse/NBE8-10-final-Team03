"use client";

import { useState, useEffect } from "react";
import api from "@/lib/api";
import ProfileAvatar from "@/components/common/ProfileAvatar";

interface RankingItem {
  rank: number;
  nickname: string;
  score: number;
  profileImage: number;
}

interface RankingResponse {
  myRank: number | null;
  rankings: RankingItem[];
}

export default function RankingsPage() {
  const [data, setData] = useState<RankingResponse>({ myRank: null, rankings: [] });
  const [loading, setLoading] = useState(true);
  const [period, setPeriod] = useState<"all" | "weekly" | "monthly">("all");
  const myNickname = typeof window !== "undefined" ? localStorage.getItem("nickname") : null;

  useEffect(() => {
    const fetchRankings = async () => {
      setLoading(true);
      try {
        const query = period === "all" ? "" : `?period=${period}`;
        const res = await api.get(`/rankings${query}`);
        setData(res.data.data);
      } catch (err) {
        console.error("랭킹 조회 실패", err);
      } finally {
        setLoading(false);
      }
    };
    fetchRankings();
  }, [period]);

  const top3 = data.rankings.slice(0, 3);
  const rest = data.rankings.slice(3);

  return (
    <div className="max-w-4xl mx-auto px-4 py-10">
      {/* 헤더 */}
      <div className="flex items-center justify-between mb-8">
        <h1 className="font-title text-4xl">🏆 랭킹</h1>
        <div className="flex gap-2">
          {(["all", "weekly", "monthly"] as const).map((p) => (
            <button
              key={p}
              onClick={() => setPeriod(p)}
              className={`px-5 py-2 border-[3px] border-dark rounded-full font-bold text-sm shadow-kitsch-sm transition-colors ${period === p ? "bg-secondary" : "bg-white hover:bg-gray-50"
                }`}
            >
              {p === "all" ? "전체" : p === "weekly" ? "주간" : "월간"}
            </button>
          ))}
        </div>
      </div>

      {/* 내 순위 배너 */}
      {data.myRank && (
        <div className="mb-8 px-6 py-4 bg-primary/10 border-[3px] border-primary rounded-2xl shadow-kitsch flex items-center justify-between">
          <p className="font-bold text-primary">
            {period === "all" ? "전체" : period === "weekly" ? "주간" : "월간"} 랭킹에서 내 순위
          </p>
          <p className="font-title text-3xl text-primary">{data.myRank}위</p>
        </div>
      )}

      {loading ? (
        <div className="text-center py-10">
          <p className="font-hand text-xl text-gray-400">랭킹 불러오는 중...</p>
        </div>
      ) : data.rankings.length === 0 ? (
        <div className="text-center py-10">
          <p className="font-hand text-xl text-gray-400">아직 랭킹 데이터가 없어요!</p>
        </div>
      ) : (
        <>
          {/* 포디움 - 3명 미만이면 그냥 목록으로 */}
          {top3.length >= 3 ? (
            <div className="flex items-end justify-center gap-6 mb-14">
              {/* 2등 */}
              <div className="text-center">
                <div className="mx-auto mb-3">
                  <ProfileAvatar profileImage={top3[1].profileImage || 1} size={96} />
                </div>
                <p className="font-bold text-sm mb-1">{top3[1].nickname}</p>
                <span className="inline-block px-3 py-1 bg-accent/10 border-2 border-accent rounded-full text-xs font-bold text-accent">
                  {top3[1].score.toLocaleString()}P
                </span>
              </div>

              {/* 1등 - 왕관 */}
              <div className="text-center -mt-6">
                <div className="text-3xl mb-1">👑</div>
                <div className="mx-auto mb-3">
                  <ProfileAvatar profileImage={top3[0].profileImage || 1} size={128} />
                </div>
                <p className="font-bold mb-1">{top3[0].nickname}</p>
                <span className="inline-block px-4 py-1.5 bg-primary/10 border-2 border-primary rounded-full text-sm font-bold text-primary">
                  {top3[0].score.toLocaleString()}P
                </span>
              </div>

              {/* 3등 */}
              <div className="text-center">
                <div className="mx-auto mb-3">
                  <ProfileAvatar profileImage={top3[2].profileImage || 1} size={96} />
                </div>
                <p className="font-bold text-sm mb-1">{top3[2].nickname}</p>
                <span className="inline-block px-3 py-1 bg-secondary/10 border-2 border-secondary rounded-full text-xs font-bold text-secondary">
                  {top3[2].score.toLocaleString()}P
                </span>
              </div>
            </div>
          ) : (
            <div className="bg-white border-[3px] border-dark rounded-2xl shadow-kitsch overflow-hidden mb-6">
              {data.rankings.map((r) => (
                <div
                  key={r.rank}
                  className={`flex items-center px-6 py-4 border-b-2 border-dashed border-gray-200 last:border-b-0 hover:bg-cream/50 transition-colors ${r.nickname === myNickname ? "bg-secondary/20" : ""
                    }`}
                >
                  <span className="font-title text-2xl w-12 text-center">{r.rank}</span>
                  <div className="flex-1">
                    <p className="font-bold text-sm">
                      {r.nickname}
                      {r.nickname === myNickname && (
                        <span className="ml-2 px-2 py-0.5 bg-secondary border border-dark rounded text-xs font-bold">나</span>
                      )}
                    </p>
                  </div>
                  <p className="font-title text-lg text-primary">{r.score.toLocaleString()}P</p>
                </div>
              ))}
            </div>
          )}

          {/* 4위부터 테이블 */}
          {rest.length > 0 && (
            <div className="bg-white border-[3px] border-dark rounded-2xl shadow-kitsch overflow-hidden">
              {rest.map((r) => (
                <div
                  key={r.rank}
                  className={`flex items-center px-6 py-4 border-b-2 border-dashed border-gray-200 last:border-b-0 hover:bg-cream/50 transition-colors ${r.nickname === myNickname ? "bg-secondary/20" : ""
                    }`}
                >
                  <span className="font-title text-2xl w-12 text-center">{r.rank}</span>
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
                    <p className="font-title text-lg text-primary">{r.score.toLocaleString()}P</p>
                  </div>
                </div>
              ))}
            </div>
          )}
        </>
      )}
    </div>
  );
}