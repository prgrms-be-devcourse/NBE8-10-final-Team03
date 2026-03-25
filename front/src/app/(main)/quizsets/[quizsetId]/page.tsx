"use client";

import { use, useState, useEffect } from "react";
import { useRouter } from "next/navigation";
import Link from "next/link";
import api from "@/lib/api";

interface QuizSetDetail {
  id: number;
  title: string;
  description: string;
  creatorNickname: string;
  totalQuizCount: number;
}

export default function QuizSetDetailPage({ params }: { params: Promise<{ quizsetId: string }> }) {
  const { quizsetId } = use(params);
  const router = useRouter();
  const [quizSet, setQuizSet] = useState<QuizSetDetail | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchQuizSet = async () => {
      try {
        const res = await api.get(`/quizsets/${quizsetId}`);
        setQuizSet(res.data.data);
      } catch (err) {
        console.error("퀴즈셋 조회 실패", err);
      } finally {
        setLoading(false);
      }
    };
    fetchQuizSet();
  }, [quizsetId]);

  if (loading) {
    return (
      <div className="max-w-3xl mx-auto px-4 py-10 text-center">
        <p className="font-hand text-xl text-gray-400">불러오는 중...</p>
      </div>
    );
  }

  if (!quizSet) {
    return (
      <div className="max-w-3xl mx-auto px-4 py-10 text-center">
        <p className="font-hand text-xl text-gray-400">퀴즈셋을 찾을 수 없습니다.</p>
      </div>
    );
  }

  return (
    <div className="max-w-3xl mx-auto px-4 py-10">
      <Link href="/quizsets" className="inline-block mb-6 text-sm text-gray-400 hover:text-primary font-bold">
        ← 퀴즈셋 목록
      </Link>

      {/* 퀴즈셋 정보 */}
      <div className="bg-white border-[3px] border-dark rounded-2xl shadow-kitsch p-8 mb-6">
        <div className="flex items-start justify-between mb-4">
          <div>
            <h1 className="font-title text-3xl mb-2">{quizSet.title}</h1>
            <p className="text-sm text-gray-400 mb-4">{quizSet.creatorNickname}</p>
            <p className="text-sm text-gray-600">{quizSet.description}</p>
          </div>
        </div>

        <div className="flex gap-4 mt-6">
          <span className="px-4 py-2 bg-cream border-2 border-dark rounded-xl text-sm font-bold">
            📝 {quizSet.totalQuizCount}문제
          </span>
        </div>
      </div>

      {/* 추후 리뷰/댓글 영역 */}
      <div className="bg-white border-[3px] border-dark rounded-2xl shadow-kitsch p-8 mb-8 text-center">
        <p className="font-hand text-lg text-gray-300">아직 준비 중이에요!</p>
      </div>

      {/* CTA */}
      <button
  onClick={() => router.push(`/rooms?quizSetId=${quizSet.id}`)}
  className="w-full py-4 bg-primary text-white font-bold text-lg border-[3px] border-dark rounded-2xl shadow-kitsch hover:shadow-kitsch-lg hover:-translate-y-0.5 transition-all"
>
  이 퀴즈셋으로 방 만들기
</button>
    </div>
  );
}