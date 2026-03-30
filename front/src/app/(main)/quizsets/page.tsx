"use client";

import { useState, useEffect, useCallback } from "react";
import Link from "next/link";
import api from "@/lib/api";
import { Slice } from "../../../../types";

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
  const [fetchingMore, setFetchingMore] = useState(false);
  const [page, setPage] = useState(0);
  const [isLast, setIsLast] = useState(false);
  const [bookmarkedIds, setBookmarkedIds] = useState<Set<number>>(new Set());
  const [userId, setUserId] = useState<number | null>(null);

  useEffect(() => {
    // localStorage에서 userId 꺼내기 (저장 방식에 따라 수정 필요)
    const storedUserId = localStorage.getItem("userId");
    if (storedUserId) setUserId(Number(storedUserId));
  }, []);

  const fetchQuizSets = useCallback(async (pageNumber: number) => {
    try {
      if (pageNumber === 0) setLoading(true);
      else setFetchingMore(true);

      // 백엔드에 page와 size 파라미터 전달
      const res = await api.get(`/quizsets?page=${pageNumber}&size=12`);
      const { content, last } = res.data.data as Slice<QuizSet>;

      // 기존 데이터에 새 데이터를 이어붙임 (첫 페이지면 교체)
      setQuizSets(prev => (pageNumber === 0 ? content : [...prev, ...content]));
      setIsLast(last);
      setPage(pageNumber);
    } catch (err) {
      console.error("퀴즈셋 조회 실패", err);
    } finally {
      setLoading(false);
      setFetchingMore(false);
    }
  }, []);

  useEffect(() => {
    fetchQuizSets(0);
  }, [fetchQuizSets]);

  // '더 보기' 버튼 클릭 핸들러
  const handleLoadMore = () => {
    if (!isLast && !fetchingMore) {
      fetchQuizSets(page + 1);
    }
  };

  // 북마크 목록 조회
  useEffect(() => {
    if (!userId) return;
    const fetchBookmarks = async () => {
      try {
        const res = await api.get(`/users/${userId}/bookmarks`);
        const ids = new Set<number>(res.data.data.map((b: { quizSetId: number }) => b.quizSetId));
        setBookmarkedIds(ids);
      } catch (err) {
        console.error("북마크 조회 실패", err);
      }
    };
    fetchBookmarks();
  }, [userId]);

  // 북마크 토글
  const toggleBookmark = async (e: React.MouseEvent, quizSetId: number) => {
    e.preventDefault(); // Link 이동 방지
    if (!userId) return;

    const isBookmarked = bookmarkedIds.has(quizSetId);
    try {
      if (isBookmarked) {
        await api.delete(`/users/${userId}/bookmarks/${quizSetId}`);
        setBookmarkedIds(prev => {
          const next = new Set(prev);
          next.delete(quizSetId);
          return next;
        });
      } else {
        await api.post(`/users/${userId}/bookmarks?quizSetId=${quizSetId}`);
        setBookmarkedIds(prev => new Set(prev).add(quizSetId));
      }
    } catch (err) {
      console.error("북마크 토글 실패", err);
    }
  };

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
              className="relative bg-white border-[3px] border-dark rounded-2xl shadow-kitsch hover:shadow-kitsch-lg hover:-translate-y-1 transition-all cursor-pointer overflow-hidden"
            >
              {/* 썸네일 */}
              <div className="relative bg-cream h-40 flex items-center justify-center">
                <span className="text-6xl">📝</span>

                {/* 북마크 버튼 */}
                {userId && (
                  <button
                    onClick={(e) => toggleBookmark(e, quiz.id)}
                    className="absolute top-3 right-3 hover:scale-125 transition-transform"
                  >
                    <svg
                      width="24"
                      height="24"
                      viewBox="0 0 24 24"
                      fill={bookmarkedIds.has(quiz.id) ? "#FFFF00" : "none"}
                      stroke="#2B2D42"
                      strokeWidth="2.5"
                      strokeLinecap="round"
                      strokeLinejoin="round"
                    >
                      <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2" />
                    </svg>
                  </button>
                )}
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

      {/* 더 보기 버튼 */}
      {!isLast && (
        <div className="text-center mt-8">
          <button
            onClick={handleLoadMore}
            disabled={fetchingMore}
            className="px-6 py-3 bg-primary text-white font-bold border-[3px] border-dark rounded-xl shadow-kitsch hover:shadow-kitsch-lg hover:-translate-y-0.5 transition-all disabled:opacity-50 disabled:cursor-not-allowed"
          >
            {fetchingMore ? "불러오는 중..." : "더 보기"}
          </button>
        </div>
      )}
    </div>
  );
}