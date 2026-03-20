"use client";

const mockUser = {
  username: "quizlover123",
  nickname: "스프링러버",
  email: "spring@example.com",
  totalGames: 42,
  totalWins: 15,
  totalRankingScore: 3200,
  createdAt: "2026-03-01T00:00:00",
};

export default function MyPage() {
  const winRate = ((mockUser.totalWins / mockUser.totalGames) * 100).toFixed(1);

  return (
    <div className="max-w-3xl mx-auto px-4 py-10">
      <h1 className="font-title text-4xl mb-8">👤 마이페이지</h1>

      {/* 프로필 카드 */}
      <div className="bg-white border-[3px] border-dark rounded-2xl shadow-kitsch p-8 mb-6">
        <div className="flex items-center gap-6 mb-8">
          <div className="w-24 h-24 bg-primary rounded-full border-[3px] border-dark flex items-center justify-center shadow-kitsch">
            <span className="font-title text-4xl text-white">{mockUser.nickname.charAt(0)}</span>
          </div>
          <div>
            <h2 className="font-title text-2xl mb-1">{mockUser.nickname}</h2>
            <p className="text-sm text-gray-400">@{mockUser.username}</p>
            <p className="text-sm text-gray-400">{mockUser.email}</p>
            <p className="text-xs text-gray-300 mt-1">
              가입일: {new Date(mockUser.createdAt).toLocaleDateString("ko-KR")}
            </p>
          </div>
        </div>

        {/* 통계 */}
        <div className="grid grid-cols-4 gap-4">
          <div className="bg-cream border-[3px] border-dark rounded-xl p-4 text-center">
            <p className="font-hand text-sm text-gray-400 mb-1">총 게임</p>
            <p className="font-title text-3xl">{mockUser.totalGames}</p>
          </div>
          <div className="bg-cream border-[3px] border-dark rounded-xl p-4 text-center">
            <p className="font-hand text-sm text-gray-400 mb-1">총 우승</p>
            <p className="font-title text-3xl text-primary">{mockUser.totalWins}</p>
          </div>
          <div className="bg-cream border-[3px] border-dark rounded-xl p-4 text-center">
            <p className="font-hand text-sm text-gray-400 mb-1">승률</p>
            <p className="font-title text-3xl text-accent">{winRate}%</p>
          </div>
          <div className="bg-cream border-[3px] border-dark rounded-xl p-4 text-center">
            <p className="font-hand text-sm text-gray-400 mb-1">랭킹 점수</p>
            <p className="font-title text-3xl text-secondary">{mockUser.totalRankingScore.toLocaleString()}</p>
          </div>
        </div>
      </div>

      {/* 바로가기 */}
      <div className="grid grid-cols-2 gap-4">
        <a href="/me/records" className="bg-white border-[3px] border-dark rounded-2xl p-6 shadow-kitsch hover:shadow-kitsch-lg hover:-translate-y-0.5 transition-all text-center">
          <div className="text-3xl mb-2">📊</div>
          <h3 className="font-title text-lg mb-1">내 전적</h3>
          <p className="text-xs text-gray-400">최근 게임 기록 확인</p>
        </a>
        <a href="/rankings" className="bg-white border-[3px] border-dark rounded-2xl p-6 shadow-kitsch hover:shadow-kitsch-lg hover:-translate-y-0.5 transition-all text-center">
          <div className="text-3xl mb-2">🏆</div>
          <h3 className="font-title text-lg mb-1">랭킹</h3>
          <p className="text-xs text-gray-400">내 순위 확인하기</p>
        </a>
      </div>
    </div>
  );
}