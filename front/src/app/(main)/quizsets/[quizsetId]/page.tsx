"use client";

import { use } from "react";
import Link from "next/link";

const mockQuizSet = {
  id: 1,
  title: "역사 고수만 도전해라",
  author: "퀴즈왕김철수",
  category: "역사",
  questionCount: 20,
  playCount: 342,
  description: "한국사 전 범위에서 출제됩니다. 고급 난이도 주의!",
  questions: [
    { id: 1, content: "임진왜란이 일어난 해는?", answer: "1592년" },
    { id: 2, content: "훈민정음을 창제한 왕은?", answer: "세종대왕" },
    { id: 3, content: "동학 농민 운동의 지도자는?", answer: "전봉준" },
  ],
};

export default function QuizSetDetailPage({ params }: { params: Promise<{ quizsetId: string }> }) {
  const { quizsetId } = use(params);

  return (
    <div className="max-w-3xl mx-auto px-4 py-10">
      <Link href="/quizsets" className="inline-block mb-6 text-sm text-gray-400 hover:text-primary font-bold">
        ← 퀴즈셋 목록
      </Link>

      {/* 퀴즈셋 정보 */}
      <div className="bg-white border-[3px] border-dark rounded-2xl shadow-kitsch p-8 mb-6">
        <div className="flex items-start justify-between mb-4">
          <div>
            <span className="inline-block px-3 py-1 bg-pink-50 border-2 border-dark rounded-full text-xs font-bold mb-3">
              {mockQuizSet.category}
            </span>
            <h1 className="font-title text-3xl mb-2">{mockQuizSet.title}</h1>
            <p className="text-sm text-gray-400 mb-4">{mockQuizSet.author}</p>
            <p className="text-sm text-gray-600">{mockQuizSet.description}</p>
          </div>
        </div>

        <div className="flex gap-4 mt-6">
          <span className="px-4 py-2 bg-cream border-2 border-dark rounded-xl text-sm font-bold">
            📝 {mockQuizSet.questionCount}문제
          </span>
          <span className="px-4 py-2 bg-cream border-2 border-dark rounded-xl text-sm font-bold">
            🎮 {mockQuizSet.playCount}회 플레이
          </span>
        </div>
      </div>

      {/* 추후 리뷰/댓글 영역 */}
      <div className="bg-white border-[3px] border-dark rounded-2xl shadow-kitsch p-8 mb-8 text-center">
        <p className="font-hand text-lg text-gray-300">아직 준비 중이에요!</p>
      </div>

      {/* CTA */}
      <button className="w-full py-4 bg-primary text-white font-bold text-lg border-[3px] border-dark rounded-2xl shadow-kitsch hover:shadow-kitsch-lg hover:-translate-y-0.5 transition-all">
        이 퀴즈셋으로 방 만들기
      </button>
    </div>
  );
}