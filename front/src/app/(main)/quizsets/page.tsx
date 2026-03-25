"use client";

import { useState, useEffect } from "react";
import Link from "next/link";
import api from "@/lib/api";

interface QuizSet {
  id: number;
  title: string;
  description: string;
  creatorNickname: string;
  totalQuizCount: number;
}

export default function QuizSetsPage() {
  const [quizSets, setQuizSets] = useState<QuizSet[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchQuizSets = async () => {
      try {
        const res = await api.get("/quizsets");
        // id 큰 순서(최신순)로 정렬
        const sorted = res.data.data.sort((a: QuizSet, b: QuizSet) => b.id - a.id);
        setQuizSets(sorted);
      } catch (err) {
        console.error("퀴즈셋 조회 실패", err);
      } finally {
        setLoading(false);
      }
    };
    fetchQuizSets();
  }, []);

  if (loading) {
    return (
      <div className="max-w-6xl mx-auto px-4 py-10 text-center">
        <p className="font-hand text-xl text-gray-400">퀴즈셋 불러오는 중...</p>
      </div>
    );
  }

  return (
    <div className="max-w-6xl mx-auto px-4 py-10">
      {/* 헤더 */}
      <div className="flex items-center justify-between mb-8">
        <div>
          <h1 className="font-title text-4xl mb-1">퀴즈셋</h1>
          <p className="font-hand text-lg text-gray-400">퀴즈를 만들고 공유하세요</p>
        </div>
        <Link
  href="/quizsets/create"
  className="px-6 py-3 bg-primary text-white font-bold border-[3px] border-dark rounded-xl shadow-kitsch hover:shadow-kitsch-lg hover:-translate-y-0.5 transition-all"
>
  + 퀴즈셋 만들기
</Link>
      </div>

      {/* 퀴즈셋 그리드 */}
      {quizSets.length === 0 ? (
        <div className="bg-white border-[3px] border-dark rounded-2xl shadow-kitsch p-10 text-center">
          <p className="font-hand text-lg text-gray-400">아직 퀴즈셋이 없어요!</p>
        </div>
      ) : (
        <div className="grid grid-cols-3 gap-6">
          {quizSets.map((quiz) => (
            <Link
              key={quiz.id}
              href={`/quizsets/${quiz.id}`}
              className="bg-white border-[3px] border-dark rounded-2xl shadow-kitsch hover:shadow-kitsch-lg hover:-translate-y-1 transition-all cursor-pointer overflow-hidden"
            >
              {/* 썸네일 */}
              <div className="relative bg-cream h-40 flex items-center justify-center">
                <span className="text-6xl">📝</span>
              </div>

              {/* 정보 */}
              <div className="p-5">
                <h3 className="font-bold mb-1 truncate">{quiz.title}</h3>
                <p className="text-xs text-gray-400 mb-3">{quiz.creatorNickname}</p>
                <div className="flex justify-between text-xs text-gray-500">
                  <span className="font-bold">{quiz.totalQuizCount}문제</span>
                </div>
                {quiz.description && (
                  <p className="text-xs text-gray-400 mt-2 truncate">{quiz.description}</p>
                )}
              </div>
            </Link>
          ))}
        </div>
      )}
    </div>
  );
}