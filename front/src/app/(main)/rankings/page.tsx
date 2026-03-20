"use client";

const mockRankings = [
  { rank: 1, username: "퀴즈왕김철수", score: 5200, games: 80, wins: 45 },
  { rank: 2, username: "답정너마스터", score: 4800, games: 72, wins: 38 },
  { rank: 3, username: "코딩고수", score: 4100, games: 65, wins: 30 },
  { rank: 4, username: "스프링러버", score: 3200, games: 42, wins: 15 },
  { rank: 5, username: "자바매니아", score: 2900, games: 55, wins: 20 },
  { rank: 6, username: "알고킹", score: 2500, games: 40, wins: 18 },
  { rank: 7, username: "DB전문가", score: 2100, games: 38, wins: 14 },
  { rank: 8, username: "리눅스장인", score: 1800, games: 30, wins: 10 },
  { rank: 9, username: "네트워크신", score: 1500, games: 25, wins: 8 },
  { rank: 10, username: "초보개발자", score: 900, games: 15, wins: 3 },
];

const myRank = 4;

export default function RankingsPage() {
  const top3 = mockRankings.slice(0, 3);
  const rest = mockRankings.slice(3);

  return (
    <div className="max-w-4xl mx-auto px-4 py-10">
      {/* 헤더 */}
      <div className="flex items-center justify-between mb-10">
        <h1 className="font-title text-4xl">🏆 랭킹</h1>
        <div className="flex gap-2">
          <button className="px-5 py-2 border-[3px] border-dark rounded-full font-bold text-sm bg-secondary shadow-kitsch-sm">
            주간
          </button>
          <button className="px-5 py-2 border-[3px] border-dark rounded-full font-bold text-sm bg-white shadow-kitsch-sm hover:bg-gray-50 transition-colors">
            월간
          </button>
          <button className="px-5 py-2 border-[3px] border-dark rounded-full font-bold text-sm bg-white shadow-kitsch-sm hover:bg-gray-50 transition-colors">
            전체
          </button>
        </div>
      </div>

      {/* 포디움 */}
      <div className="flex items-end justify-center gap-6 mb-14">
        {/* 2등 */}
        <div className="text-center">
          <div className="w-24 h-24 mx-auto mb-3 rounded-full border-[4px] border-accent bg-white flex items-center justify-center">
            <span className="font-title text-3xl text-accent">2</span>
          </div>
          <p className="font-bold text-sm mb-1">{top3[1].username}</p>
          <span className="inline-block px-3 py-1 bg-accent/10 border-2 border-accent rounded-full text-xs font-bold text-accent">
            {top3[1].score.toLocaleString()}P
          </span>
        </div>

        {/* 1등 */}
        <div className="text-center -mt-6">
          <div className="text-3xl mb-1">👑</div>
          <div className="w-32 h-32 mx-auto mb-3 rounded-full border-[4px] border-primary bg-white flex items-center justify-center shadow-kitsch">
            <span className="font-title text-4xl text-primary">1</span>
          </div>
          <p className="font-bold mb-1">{top3[0].username}</p>
          <span className="inline-block px-4 py-1.5 bg-primary/10 border-2 border-primary rounded-full text-sm font-bold text-primary">
            {top3[0].score.toLocaleString()}P
          </span>
        </div>

        {/* 3등 */}
        <div className="text-center">
          <div className="w-24 h-24 mx-auto mb-3 rounded-full border-[4px] border-secondary bg-white flex items-center justify-center">
            <span className="font-title text-3xl text-secondary">3</span>
          </div>
          <p className="font-bold text-sm mb-1">{top3[2].username}</p>
          <span className="inline-block px-3 py-1 bg-secondary/10 border-2 border-secondary rounded-full text-xs font-bold text-secondary">
            {top3[2].score.toLocaleString()}P
          </span>
        </div>
      </div>

      {/* 4위부터 테이블 */}
      <div className="bg-white border-[3px] border-dark rounded-2xl shadow-kitsch overflow-hidden">
        {rest.map((r) => (
          <div
            key={r.rank}
            className={`flex items-center px-6 py-4 border-b-2 border-dashed border-gray-200 last:border-b-0 hover:bg-cream/50 transition-colors ${
              r.rank === myRank ? "bg-secondary/20" : ""
            }`}
          >
            <span className="font-title text-2xl w-12 text-center">
              {r.rank}
            </span>
            <div className="w-10 h-10 rounded-full bg-cream border-2 border-dark flex items-center justify-center text-sm font-bold mr-4">
              {r.username.charAt(0)}
            </div>
            <div className="flex-1">
              <p className="font-bold text-sm">
                {r.username}
                {r.rank === myRank && (
                  <span className="ml-2 px-2 py-0.5 bg-secondary border border-dark rounded text-xs font-bold">
                    나
                  </span>
                )}
              </p>
            </div>
            <div className="text-right mr-8">
              <p className="font-title text-lg text-primary">{r.score.toLocaleString()}P</p>
            </div>
            <div className="text-right text-sm text-gray-400 w-24">
              <p>{r.games}게임 / {r.wins}승</p>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}