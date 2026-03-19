"use client";

const mockRooms = [
  { id: 1, title: "역사 고수만 오세요", quizSet: "한국사 퀴즈셋", questionCount: 10, host: "퀴즈왕김철수", category: "역사", categoryColor: "bg-red-100 text-red-600 border-red-300", players: 3, maxPlayers: 4, status: "waiting" },
  { id: 2, title: "아무나 들어와~", quizSet: "상식 퀴즈셋", questionCount: 15, host: "답정너마스터", category: "일반상식", categoryColor: "bg-blue-100 text-blue-600 border-blue-300", players: 2, maxPlayers: 6, status: "waiting" },
  { id: 3, title: "과학 배틀 (고인물 환영)", quizSet: "과학 퀴즈셋", questionCount: 20, host: "코딩고수", category: "과학", categoryColor: "bg-green-100 text-green-600 border-green-300", players: 5, maxPlayers: 5, status: "playing" },
  { id: 4, title: "영화 덕후 모여라", quizSet: "영화 퀴즈셋", questionCount: 10, host: "자바매니아", category: "영화", categoryColor: "bg-purple-100 text-purple-600 border-purple-300", players: 1, maxPlayers: 4, status: "waiting" },
  { id: 5, title: "초보만 오세요 ㅎㅎ", quizSet: "기초 상식", questionCount: 8, host: "초보개발자", category: "일반상식", categoryColor: "bg-blue-100 text-blue-600 border-blue-300", players: 4, maxPlayers: 8, status: "playing" },
];

const mockWeeklyRanking = [
  { rank: 1, username: "퀴즈왕김철수", score: 1200 },
  { rank: 2, username: "답정너마스터", score: 980 },
  { rank: 3, username: "코딩고수", score: 870 },
  { rank: 4, username: "스프링러버", score: 650 },
  { rank: 5, username: "자바매니아", score: 520 },
];

const rankColors = ["text-primary", "text-accent", "text-secondary", "text-gray-400", "text-gray-400"];

export default function RoomsPage() {
  return (
    <div className="max-w-7xl mx-auto px-4 py-10 flex gap-6">
      {/* 좌측 메인 */}
      <div className="flex-1">
        <div className="mb-6">
          <h1 className="font-title text-4xl mb-1">게임 대기실</h1>
          <p className="font-hand text-lg text-gray-400">참여할 퀴즈방을 선택하세요</p>
        </div>

        {/* 필터 */}
        <div className="flex gap-2 mb-6">
          <button className="px-5 py-2 border-[3px] border-dark rounded-full font-bold text-sm bg-secondary shadow-kitsch-sm">
            전체
          </button>
          <button className="px-5 py-2 border-[3px] border-dark rounded-full font-bold text-sm bg-white shadow-kitsch-sm hover:bg-gray-50 transition-colors">
            대기중
          </button>
          <button className="px-5 py-2 border-[3px] border-dark rounded-full font-bold text-sm bg-white shadow-kitsch-sm hover:bg-gray-50 transition-colors">
            게임중
          </button>
        </div>

        {/* 방 목록 */}
        <div className="flex flex-col gap-3 mb-6">
          {mockRooms.map((room) => (
            <div
              key={room.id}
              className="flex items-center bg-white border-[3px] border-dark rounded-2xl px-6 py-4 shadow-kitsch hover:shadow-kitsch-lg hover:-translate-y-0.5 transition-all cursor-pointer"
            >
              <div className="flex-1">
                <h3 className="font-bold mb-1">{room.title}</h3>
                <p className="text-xs text-gray-400">
                  {room.quizSet} · {room.questionCount}문제 · {room.host}
                </p>
              </div>

              <span className={`px-3 py-1 border-2 rounded-full text-xs font-bold mr-6 ${room.categoryColor}`}>
                {room.category}
              </span>

              <div className="flex items-center gap-4">
                <div className="text-right">
                  <p className="font-title text-xl text-primary">{room.players}/{room.maxPlayers}</p>
                  <div className="w-16 h-2 bg-gray-200 rounded-full mt-1">
                    <div
                      className="h-full bg-primary rounded-full"
                      style={{ width: `${(room.players / room.maxPlayers) * 100}%` }}
                    />
                  </div>
                </div>

                {room.status === "waiting" ? (
                  <button className="px-4 py-2 bg-primary text-white border-[3px] border-dark rounded-xl text-sm font-bold shadow-kitsch-sm hover:shadow-kitsch hover:-translate-y-0.5 transition-all">
                    참여하기
                  </button>
                ) : (
                  <span className="px-4 py-2 bg-gray-200 text-gray-500 border-[3px] border-gray-300 rounded-xl text-sm font-bold">
                    게임중
                  </span>
                )}
              </div>
            </div>
          ))}
        </div>

        {/* 방 만들기 */}
        <button className="w-full py-4 bg-accent text-white font-bold text-lg border-[3px] border-dark rounded-2xl shadow-kitsch hover:shadow-kitsch-lg hover:-translate-y-0.5 transition-all">
          + 새 방 만들기
        </button>
      </div>

      {/* 우측 사이드바 */}
      <div className="w-80 flex flex-col gap-4">
        {/* 접속자 수 */}
        <div className="bg-white border-[3px] border-dark rounded-2xl p-6 shadow-kitsch text-center">
          <p className="font-title text-5xl text-accent mb-1">127</p>
          <p className="font-hand text-lg text-gray-400">명 접속중</p>
        </div>

        {/* 이번 주 랭킹 */}
        <div className="bg-white border-[3px] border-dark rounded-2xl p-6 shadow-kitsch">
          <h3 className="font-title text-lg mb-4">이번 주 랭킹</h3>
          <div className="flex flex-col gap-3">
            {mockWeeklyRanking.map((r) => (
              <div key={r.rank} className="flex items-center gap-3">
                <span className={`font-title text-xl w-8 ${rankColors[r.rank - 1]}`}>
                  {r.rank}
                </span>
                <span className="flex-1 text-sm font-bold truncate">{r.username}</span>
                <span className="text-sm text-gray-400">{r.score.toLocaleString()}P</span>
              </div>
            ))}
          </div>
          <button className="w-full mt-4 py-2 bg-cream border-[3px] border-dark rounded-xl text-sm font-bold shadow-kitsch-sm hover:shadow-kitsch hover:-translate-y-0.5 transition-all">
            전체 랭킹 보기
          </button>
        </div>

        {/* 바로가기 */}
        <div className="bg-white border-[3px] border-dark rounded-2xl p-6 shadow-kitsch">
          <h3 className="font-title text-lg mb-4">바로가기</h3>
          <div className="flex flex-col gap-2">
            <button className="w-full py-3 bg-cream border-[3px] border-dark rounded-xl text-sm font-bold shadow-kitsch-sm hover:shadow-kitsch hover:-translate-y-0.5 transition-all">
              📝 퀴즈셋 만들기
            </button>
            <button className="w-full py-3 bg-cream border-[3px] border-dark rounded-xl text-sm font-bold shadow-kitsch-sm hover:shadow-kitsch hover:-translate-y-0.5 transition-all">
              ⭐ 즐겨찾기
            </button>
            <button className="w-full py-3 bg-cream border-[3px] border-dark rounded-xl text-sm font-bold shadow-kitsch-sm hover:shadow-kitsch hover:-translate-y-0.5 transition-all">
              👤 마이페이지
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}