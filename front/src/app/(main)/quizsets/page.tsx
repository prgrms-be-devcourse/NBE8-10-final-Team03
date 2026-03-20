"use client";

const categories = [
  { name: "전체", active: true },
  { name: "상식", active: false },
  { name: "역사", active: false },
  { name: "과학", active: false },
  { name: "영어", active: false },
  { name: "음악", active: false },
  { name: "영화", active: false },
  { name: "IT", active: false },
];

const categoryStyles: Record<string, { bg: string; icon: string }> = {
  상식: { bg: "bg-amber-50", icon: "💡" },
  역사: { bg: "bg-pink-50", icon: "🏛️" },
  과학: { bg: "bg-emerald-50", icon: "🔬" },
  영어: { bg: "bg-blue-50", icon: "🔤" },
  음악: { bg: "bg-purple-50", icon: "🎵" },
  영화: { bg: "bg-rose-50", icon: "🎬" },
  IT: { bg: "bg-cyan-50", icon: "💻" },
};

const mockQuizSets = [
  { id: 1, title: "역사 고수만 도전해라", author: "퀴즈왕김철수", category: "역사", questionCount: 20, playCount: 342, bookmarked: true },
  { id: 2, title: "AI 시대 IT 상식", author: "코딩고수", category: "IT", questionCount: 15, playCount: 189, bookmarked: false },
  { id: 3, title: "팝송 가사 맞추기", author: "음악러버", category: "음악", questionCount: 10, playCount: 567, bookmarked: true },
  { id: 4, title: "영화 명대사 퀴즈", author: "자바매니아", category: "영화", questionCount: 12, playCount: 423, bookmarked: false },
  { id: 5, title: "중학교 과학 총정리", author: "답정너마스터", category: "과학", questionCount: 25, playCount: 891, bookmarked: false },
  { id: 6, title: "일반 상식 왕중왕", author: "스프링러버", category: "상식", questionCount: 30, playCount: 1204, bookmarked: true },
  { id: 7, title: "영어 숙어 마스터", author: "알고킹", category: "영어", questionCount: 20, playCount: 256, bookmarked: false },
  { id: 8, title: "한국 근현대사", author: "DB전문가", category: "역사", questionCount: 18, playCount: 378, bookmarked: false },
  { id: 9, title: "클래식 음악 퀴즈", author: "리눅스장인", category: "음악", questionCount: 10, playCount: 145, bookmarked: false },
];

export default function QuizSetsPage() {
  return (
    <div className="max-w-6xl mx-auto px-4 py-10">
      {/* 헤더 */}
      <div className="flex items-center justify-between mb-6">
        <div>
          <h1 className="font-title text-4xl mb-1">퀴즈셋</h1>
          <p className="font-hand text-lg text-gray-400">퀴즈를 만들고 공유하세요</p>
        </div>
        <button className="px-6 py-3 bg-primary text-white font-bold border-[3px] border-dark rounded-xl shadow-kitsch hover:shadow-kitsch-lg hover:-translate-y-0.5 transition-all">
          + 퀴즈셋 만들기
        </button>
      </div>

      {/* 카테고리 필터 */}
      <div className="flex gap-2 mb-8 overflow-x-auto pb-2">
        {categories.map((cat) => (
          <button
            key={cat.name}
            className={`px-5 py-2 border-[3px] border-dark rounded-full font-bold text-sm whitespace-nowrap shadow-kitsch-sm hover:-translate-y-0.5 transition-all ${
              cat.active ? "bg-secondary" : "bg-white hover:bg-gray-50"
            }`}
          >
            {cat.name}
          </button>
        ))}
      </div>

      {/* 퀴즈셋 그리드 */}
      <div className="grid grid-cols-3 gap-6">
        {mockQuizSets.map((quiz) => {
          const style = categoryStyles[quiz.category] || { bg: "bg-gray-50", icon: "📋" };
          return (
            <div
              key={quiz.id}
              className="bg-white border-[3px] border-dark rounded-2xl shadow-kitsch hover:shadow-kitsch-lg hover:-translate-y-1 transition-all cursor-pointer overflow-hidden"
            >
              {/* 썸네일 */}
              <div className={`relative ${style.bg} h-40 flex items-center justify-center`}>
                <span className="text-6xl">{style.icon}</span>

                {/* 카테고리 태그 */}
                <span className="absolute top-3 left-3 px-3 py-1 bg-white border-2 border-dark rounded-full text-xs font-bold shadow-kitsch-sm">
                  {quiz.category}
                </span>

                {/* 즐겨찾기 */}
                <button className="absolute top-3 right-3 w-8 h-8 bg-white border-2 border-dark rounded-full flex items-center justify-center shadow-kitsch-sm hover:scale-110 transition-transform">
                  {quiz.bookmarked ? (
                    <span className="text-secondary text-sm">★</span>
                  ) : (
                    <span className="text-gray-300 text-sm">☆</span>
                  )}
                </button>
              </div>

              {/* 정보 */}
              <div className="p-5">
                <h3 className="font-bold mb-1 truncate">{quiz.title}</h3>
                <p className="text-xs text-gray-400 mb-3">{quiz.author}</p>
                <div className="flex justify-between text-xs text-gray-500">
                  <span className="font-bold">{quiz.questionCount}문제</span>
                  <span>{quiz.playCount.toLocaleString()}회 플레이</span>
                </div>
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
}