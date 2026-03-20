"use client";

const mockRecords = {
  totalGames: 42,
  totalWins: 15,
  totalRankingScore: 3200,
  recentRecords: [
    { sessionId: 1, quizSetTitle: "자바 기초", maxPlayers: 4, sessionRanking: 1, sessionScore: 950, earnedRankingScore: 210, playedAt: "2026-03-19T13:00:00" },
    { sessionId: 2, quizSetTitle: "스프링 심화", maxPlayers: 8, sessionRanking: 2, sessionScore: 720, earnedRankingScore: 108, playedAt: "2026-03-19T11:30:00" },
    { sessionId: 3, quizSetTitle: "알고리즘 대회", maxPlayers: 6, sessionRanking: 1, sessionScore: 880, earnedRankingScore: 195, playedAt: "2026-03-18T20:00:00" },
    { sessionId: 4, quizSetTitle: "CS 상식", maxPlayers: 4, sessionRanking: 3, sessionScore: 450, earnedRankingScore: 42, playedAt: "2026-03-18T15:00:00" },
    { sessionId: 5, quizSetTitle: "네트워크 기본", maxPlayers: 2, sessionRanking: 2, sessionScore: 600, earnedRankingScore: 72, playedAt: "2026-03-17T19:00:00" },
    { sessionId: 6, quizSetTitle: "운영체제 퀴즈", maxPlayers: 4, sessionRanking: 1, sessionScore: 900, earnedRankingScore: 200, playedAt: "2026-03-17T14:00:00" },
  ],
};

export default function RecordsPage() {
  const data = mockRecords;
  const winRate = ((data.totalWins / data.totalGames) * 100).toFixed(1);

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
      <div className="bg-white border-[3px] border-dark rounded-2xl shadow-kitsch overflow-hidden">
        {/* 테이블 헤더 */}
        <div className="flex items-center px-6 py-3 bg-cream border-b-[3px] border-dark text-sm font-bold text-gray-500">
          <span className="w-48">퀴즈셋</span>
          <span className="w-20 text-center">인원</span>
          <span className="w-20 text-center">순위</span>
          <span className="w-24 text-center">점수</span>
          <span className="w-28 text-center">랭킹 포인트</span>
          <span className="flex-1 text-right">날짜</span>
        </div>

        {/* 전적 행 */}
        {data.recentRecords.map((r) => (
          <div
            key={r.sessionId}
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
    </div>
  );
}