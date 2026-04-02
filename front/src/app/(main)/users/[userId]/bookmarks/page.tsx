"use client";

import { useState, useEffect } from "react";
import Link from "next/link";
import { useParams } from "next/navigation";
import api from "@/lib/api";

interface QuizSet {
  id: number;
  title: string;
  description: string;
  creatorNickname: string;
  totalQuizCount: number;
}

export default function BookmarksPage() {
  const { userId } = useParams();
  const [quizSets, setQuizSets] = useState<QuizSet[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchBookmarks = async () => {
      try {
        const bookmarkRes = await api.get(`/users/${userId}/bookmarks`);
        const bookmarkIds: number[] = bookmarkRes.data.data.map(
          (b: { quizSetId: number }) => b.quizSetId
        );
        const quizSetRes = await Promise.all(
          bookmarkIds.map((id) => api.get(`/quizsets/${id}/info`))
        );
        setQuizSets(quizSetRes.map((res) => res.data.data));
      } catch (err) {
        console.error("북마크 조회 실패", err);
      } finally {
        setLoading(false);
      }
    };
    fetchBookmarks();
  }, [userId]);

  const removeBookmark = async (e: React.MouseEvent, quizSetId: number) => {
    e.preventDefault();
    try {
      await api.delete(`/users/${userId}/bookmarks/${quizSetId}`);
      // 목록에서 바로 제거
      setQuizSets(prev => prev.filter((q) => q.id !== quizSetId));
    } catch (err) {
      console.error("북마크 제거 실패", err);
    }
  };

  if (loading) {
    return (
      <div className="max-w-6xl mx-auto px-4 py-10 text-center">
        <p className="font-hand text-xl text-gray-400">북마크 불러오는 중...</p>
      </div>
    );
  }

  return (
    <div className="max-w-6xl mx-auto px-4 py-10">
      {/* 헤더 */}
      <div className="flex items-center justify-between mb-8">
        <div>
          <h1 className="font-title text-4xl mb-1">⭐ 북마크</h1>
          <p className="font-hand text-lg text-gray-400">저장한 퀴즈셋 모음</p>
        </div>
        <Link
          href="/quizsets"
          className="px-6 py-3 bg-white font-bold border-[3px] border-dark rounded-xl shadow-kitsch hover:shadow-kitsch-lg hover:-translate-y-0.5 transition-all"
        >
          퀴즈셋 보러가기
        </Link>
      </div>

      {/* 퀴즈셋 그리드 */}
      {quizSets.length === 0 ? (
        <div className="bg-white border-[3px] border-dark rounded-2xl shadow-kitsch p-10 text-center">
          <p className="font-hand text-lg text-gray-400">북마크한 퀴즈셋이 없어요!</p>
          <Link
            href="/quizsets"
            className="inline-block mt-4 px-6 py-3 bg-primary text-white font-bold border-[3px] border-dark rounded-xl shadow-kitsch hover:shadow-kitsch-lg hover:-translate-y-0.5 transition-all"
          >
            퀴즈셋 둘러보기
          </Link>
        </div>
      ) : (
        <div className="grid grid-cols-3 gap-6">
          {quizSets.map((quiz) => (
            <Link
              key={quiz.id}
              href={`/quizsets/${quiz.id}`}
              className="relative bg-white border-[3px] border-dark rounded-2xl shadow-kitsch hover:shadow-kitsch-lg hover:-translate-y-1 transition-all cursor-pointer overflow-hidden"
            >
              <div className="relative bg-cream h-40 flex items-center justify-center">
                <span className="text-6xl">📝</span>
                <button
                  onClick={(e) => removeBookmark(e, quiz.id)}
                  className="absolute top-3 right-3 hover:scale-125 transition-transform"
                >
                  <svg
                    width="24"
                    height="24"
                    viewBox="0 0 24 24"
                    fill="#FFFF00"
                    stroke="#2B2D42"
                    strokeWidth="2.5"
                    strokeLinecap="round"
                    strokeLinejoin="round"
                  >
                    <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2" />
                  </svg>
                </button>
              </div>

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