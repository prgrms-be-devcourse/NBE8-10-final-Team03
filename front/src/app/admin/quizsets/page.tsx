"use client";

import { useState, useEffect } from "react";
import { useRouter } from "next/navigation";
import api from "@/lib/api";

interface QuizSetItem {
  id: number;
  title: string;
  description: string;
  creatorNickname: string;
  totalQuizCount: number;
}

interface QuizItem {
  id: number;
  content: string;
  answer: string;
  choice1: string | null;
  choice2: string | null;
  choice3: string | null;
  choice4: string | null;
}

export default function AdminQuizSetsPage() {
  const router = useRouter();
  const [quizSets, setQuizSets] = useState<QuizSetItem[]>([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState("");

  // 퀴즈셋 수정 모달
  const [editTarget, setEditTarget] = useState<QuizSetItem | null>(null);
  const [editTitle, setEditTitle] = useState("");
  const [editDescription, setEditDescription] = useState("");
  const [updating, setUpdating] = useState(false);

  // 퀴즈 목록 펼치기
  const [expandedId, setExpandedId] = useState<number | null>(null);
  const [quizzesMap, setQuizzesMap] = useState<Record<number, QuizItem[]>>({});
  const [quizzesLoading, setQuizzesLoading] = useState(false);

  // 퀴즈 수정 모달
  const [editQuiz, setEditQuiz] = useState<QuizItem | null>(null);
  const [editQuizSetId, setEditQuizSetId] = useState<number | null>(null);
  const [quizForm, setQuizForm] = useState({
    content: "",
    answer: "",
    choice1: "",
    choice2: "",
    choice3: "",
    choice4: "",
  });
  const [quizUpdating, setQuizUpdating] = useState(false);

  useEffect(() => {
    fetchQuizSets();
  }, []);

  const fetchQuizSets = async () => {
    setLoading(true);
    try {
      const res = await api.get("/quizsets?size=100");
      const data = res.data.data.content ?? res.data.data;
      setQuizSets(data);
    } catch (err) {
      console.error("퀴즈셋 조회 실패", err);
    } finally {
      setLoading(false);
    }
  };

  const fetchQuizzes = async (quizSetId: number) => {
    if (quizzesMap[quizSetId]) return; // 이미 로드됨
    setQuizzesLoading(true);
    try {
      const res = await api.get(`/quizsets/${quizSetId}`);
      const quizzes = res.data.data.quizzes ?? [];
      setQuizzesMap((prev) => ({ ...prev, [quizSetId]: quizzes }));
    } catch (err) {
      console.error("퀴즈 조회 실패", err);
    } finally {
      setQuizzesLoading(false);
    }
  };

  const handleToggleExpand = async (id: number) => {
    if (expandedId === id) {
      setExpandedId(null);
    } else {
      setExpandedId(id);
      await fetchQuizzes(id);
    }
  };

  // 퀴즈셋 수정
  const handleEditOpen = (quizSet: QuizSetItem) => {
    setEditTarget(quizSet);
    setEditTitle(quizSet.title);
    setEditDescription(quizSet.description || "");
  };

  const handleUpdate = async () => {
    if (!editTarget) return;
    setUpdating(true);
    try {
      await api.patch(`/admin/quizsets/${editTarget.id}`, {
        title: editTitle,
        description: editDescription,
      });
      setQuizSets((prev) =>
        prev.map((q) =>
          q.id === editTarget.id
            ? { ...q, title: editTitle, description: editDescription }
            : q
        )
      );
      setEditTarget(null);
    } catch (err) {
      alert("수정에 실패했습니다.");
    } finally {
      setUpdating(false);
    }
  };

  const handleDelete = async (id: number, title: string) => {
    if (!confirm(`"${title}" 퀴즈셋을 삭제할까요? 연관된 모든 데이터가 삭제됩니다.`)) return;
    try {
      await api.delete(`/admin/quizsets/${id}`);
      setQuizSets((prev) => prev.filter((q) => q.id !== id));
      if (expandedId === id) setExpandedId(null);
    } catch (err) {
      alert("삭제에 실패했습니다.");
    }
  };

  // 퀴즈 수정
  const handleQuizEditOpen = (quizSetId: number, quiz: QuizItem) => {
    setEditQuizSetId(quizSetId);
    setEditQuiz(quiz);
    setQuizForm({
      content: quiz.content,
      answer: quiz.answer,
      choice1: quiz.choice1 ?? "",
      choice2: quiz.choice2 ?? "",
      choice3: quiz.choice3 ?? "",
      choice4: quiz.choice4 ?? "",
    });
  };

  const handleQuizUpdate = async () => {
    if (!editQuiz || !editQuizSetId) return;
    setQuizUpdating(true);
    try {
      await api.patch(`/admin/quizsets/${editQuizSetId}/quizzes/${editQuiz.id}`, {
        content: quizForm.content || null,
        answer: quizForm.answer || null,
        choice1: quizForm.choice1 || null,
        choice2: quizForm.choice2 || null,
        choice3: quizForm.choice3 || null,
        choice4: quizForm.choice4 || null,
      });
      setQuizzesMap((prev) => ({
        ...prev,
        [editQuizSetId]: prev[editQuizSetId].map((q) =>
          q.id === editQuiz.id
            ? {
                ...q,
                content: quizForm.content,
                answer: quizForm.answer,
                choice1: quizForm.choice1 || null,
                choice2: quizForm.choice2 || null,
                choice3: quizForm.choice3 || null,
                choice4: quizForm.choice4 || null,
              }
            : q
        ),
      }));
      setEditQuiz(null);
    } catch (err) {
      alert("퀴즈 수정에 실패했습니다.");
    } finally {
      setQuizUpdating(false);
    }
  };

  // 퀴즈 삭제
  const handleQuizDelete = async (quizSetId: number, quizId: number) => {
    if (!confirm("이 퀴즈를 삭제할까요?")) return;
    try {
      await api.delete(`/admin/quizsets/${quizSetId}/quizzes/${quizId}`);
      setQuizzesMap((prev) => ({
        ...prev,
        [quizSetId]: prev[quizSetId].filter((q) => q.id !== quizId),
      }));
      setQuizSets((prev) =>
        prev.map((q) =>
          q.id === quizSetId ? { ...q, totalQuizCount: q.totalQuizCount - 1 } : q
        )
      );
    } catch (err) {
      alert("퀴즈 삭제에 실패했습니다.");
    }
  };

  const filtered = quizSets.filter(
    (q) =>
      q.title.toLowerCase().includes(search.toLowerCase()) ||
      q.creatorNickname.toLowerCase().includes(search.toLowerCase())
  );

  if (loading)
    return (
      <div className="text-center py-20">
        <p className="font-hand text-xl text-gray-400">불러오는 중...</p>
      </div>
    );

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <h2 className="font-title text-2xl">📝 퀴즈셋 관리</h2>
        <input
          type="text"
          placeholder="제목 또는 제작자 검색..."
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          className="px-4 py-2 bg-white border-[3px] border-dark rounded-xl text-sm outline-none focus:border-primary w-64"
        />
      </div>

      {filtered.length === 0 ? (
        <div className="bg-white border-[3px] border-dark rounded-2xl p-10 text-center shadow-kitsch">
          <p className="font-hand text-lg text-gray-400">퀴즈셋이 없어요!</p>
        </div>
      ) : (
        <div className="bg-white border-[3px] border-dark rounded-2xl shadow-kitsch overflow-hidden">
          <div className="grid grid-cols-[2fr_1fr_80px_160px] gap-4 px-6 py-3 bg-cream border-b-[3px] border-dark text-sm font-bold text-gray-500">
            <span>제목</span>
            <span>제작자</span>
            <span>문제 수</span>
            <span>액션</span>
          </div>
          {filtered.map((q) => (
            <div key={q.id}>
              {/* 퀴즈셋 행 */}
              <div className="grid grid-cols-[2fr_1fr_80px_160px] gap-4 px-6 py-4 border-b-2 border-dashed border-gray-200 items-center">
                <div>
                  <button
                    onClick={() => router.push(`/quizsets/${q.id}`)}
                    className="font-bold text-sm text-primary hover:underline text-left"
                  >
                    {q.title}
                  </button>
                  {q.description && (
                    <p className="text-xs text-gray-400 truncate mt-0.5">{q.description}</p>
                  )}
                </div>
                <span className="text-sm">{q.creatorNickname}</span>
                <span className="text-sm font-bold text-center">{q.totalQuizCount}문제</span>
                <div className="flex gap-2">
                  <button
                    onClick={() => handleToggleExpand(q.id)}
                    className="px-2 py-1 bg-white border-2 border-dark rounded-lg text-xs font-bold hover:bg-cream"
                  >
                    {expandedId === q.id ? "▲" : "▼"}
                  </button>
                  <button
                    onClick={() => handleEditOpen(q)}
                    className="px-2 py-1 bg-white border-2 border-dark rounded-lg text-xs font-bold hover:bg-cream"
                  >
                    수정
                  </button>
                  <button
                    onClick={() => handleDelete(q.id, q.title)}
                    className="px-2 py-1 bg-red-500 text-white border-2 border-dark rounded-lg text-xs font-bold"
                  >
                    삭제
                  </button>
                </div>
              </div>

              {/* 퀴즈 목록 (펼쳐진 경우) */}
              {expandedId === q.id && (
                <div className="bg-cream border-b-2 border-dashed border-gray-200 px-6 py-4">
                  {quizzesLoading && !quizzesMap[q.id] ? (
                    <p className="text-sm text-gray-400">퀴즈 불러오는 중...</p>
                  ) : (quizzesMap[q.id] ?? []).length === 0 ? (
                    <p className="text-sm text-gray-400">퀴즈가 없어요.</p>
                  ) : (
                    <div className="flex flex-col gap-2">
                      {(quizzesMap[q.id] ?? []).map((quiz, idx) => (
                        <div
                          key={quiz.id}
                          className="bg-white border-2 border-dark rounded-xl px-4 py-3 flex items-start justify-between gap-4"
                        >
                          <div className="flex-1 min-w-0">
                            <p className="text-sm font-bold text-dark">
                              Q{idx + 1}. {quiz.content}
                            </p>
                            <p className="text-xs text-green-600 font-bold mt-1">
                              정답: {quiz.answer}
                            </p>
                            {[quiz.choice1, quiz.choice2, quiz.choice3, quiz.choice4].some(Boolean) && (
                              <div className="flex flex-wrap gap-2 mt-1">
                                {[quiz.choice1, quiz.choice2, quiz.choice3, quiz.choice4]
                                  .filter(Boolean)
                                  .map((c, i) => (
                                    <span
                                      key={i}
                                      className="text-xs px-2 py-0.5 bg-cream border border-gray-300 rounded-lg text-gray-600"
                                    >
                                      {c}
                                    </span>
                                  ))}
                              </div>
                            )}
                          </div>
                          <div className="flex gap-2 shrink-0">
                            <button
                              onClick={() => handleQuizEditOpen(q.id, quiz)}
                              className="px-2 py-1 bg-white border-2 border-dark rounded-lg text-xs font-bold hover:bg-cream"
                            >
                              수정
                            </button>
                            <button
                              onClick={() => handleQuizDelete(q.id, quiz.id)}
                              className="px-2 py-1 bg-red-500 text-white border-2 border-dark rounded-lg text-xs font-bold"
                            >
                              삭제
                            </button>
                          </div>
                        </div>
                      ))}
                    </div>
                  )}
                </div>
              )}
            </div>
          ))}
        </div>
      )}

      {/* 퀴즈셋 수정 모달 */}
      {editTarget && (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50">
          <div className="bg-white border-[3px] border-dark rounded-2xl shadow-kitsch-lg p-8 w-full max-w-md">
            <h3 className="font-title text-xl mb-6">퀴즈셋 수정</h3>
            <div className="mb-4">
              <label className="block text-sm font-bold mb-2">제목</label>
              <input
                type="text"
                value={editTitle}
                onChange={(e) => setEditTitle(e.target.value)}
                maxLength={30}
                className="w-full px-4 py-3 bg-cream border-[3px] border-dark rounded-xl text-sm focus:border-primary outline-none"
              />
              <p className="text-xs text-gray-400 text-right mt-1">{editTitle.length}/30</p>
            </div>
            <div className="mb-6">
              <label className="block text-sm font-bold mb-2">설명</label>
              <textarea
                value={editDescription}
                onChange={(e) => setEditDescription(e.target.value)}
                maxLength={255}
                rows={3}
                className="w-full px-4 py-3 bg-cream border-[3px] border-dark rounded-xl text-sm focus:border-primary outline-none resize-none"
              />
              <p className="text-xs text-gray-400 text-right mt-1">{editDescription.length}/255</p>
            </div>
            <div className="flex gap-3">
              <button
                onClick={handleUpdate}
                disabled={updating || !editTitle.trim()}
                className="flex-1 py-3 bg-primary text-white font-bold border-[3px] border-dark rounded-xl shadow-kitsch disabled:opacity-50"
              >
                {updating ? "저장 중..." : "저장"}
              </button>
              <button
                onClick={() => setEditTarget(null)}
                className="px-6 py-3 bg-white font-bold border-[3px] border-dark rounded-xl shadow-kitsch-sm"
              >
                취소
              </button>
            </div>
          </div>
        </div>
      )}

      {/* 퀴즈 수정 모달 */}
      {editQuiz && (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50">
          <div className="bg-white border-[3px] border-dark rounded-2xl shadow-kitsch-lg p-8 w-full max-w-lg max-h-[90vh] overflow-y-auto">
            <h3 className="font-title text-xl mb-6">퀴즈 수정</h3>
            <div className="mb-4">
              <label className="block text-sm font-bold mb-2">문제 내용</label>
              <textarea
                value={quizForm.content}
                onChange={(e) => setQuizForm((p) => ({ ...p, content: e.target.value }))}
                maxLength={2000}
                rows={3}
                className="w-full px-4 py-3 bg-cream border-[3px] border-dark rounded-xl text-sm focus:border-primary outline-none resize-none"
              />
            </div>
            <div className="mb-4">
              <label className="block text-sm font-bold mb-2">정답</label>
              <input
                type="text"
                value={quizForm.answer}
                onChange={(e) => setQuizForm((p) => ({ ...p, answer: e.target.value }))}
                className="w-full px-4 py-3 bg-cream border-[3px] border-dark rounded-xl text-sm focus:border-primary outline-none"
              />
            </div>
            <div className="mb-6">
              <label className="block text-sm font-bold mb-2">선택지 (객관식인 경우)</label>
              <div className="flex flex-col gap-2">
                {(["choice1", "choice2", "choice3", "choice4"] as const).map((key, i) => (
                  <input
                    key={key}
                    type="text"
                    placeholder={`선택지 ${i + 1}`}
                    value={quizForm[key]}
                    onChange={(e) => setQuizForm((p) => ({ ...p, [key]: e.target.value }))}
                    className="w-full px-4 py-2 bg-cream border-2 border-dark rounded-xl text-sm focus:border-primary outline-none"
                  />
                ))}
              </div>
            </div>
            <div className="flex gap-3">
              <button
                onClick={handleQuizUpdate}
                disabled={quizUpdating || !quizForm.content.trim() || !quizForm.answer.trim()}
                className="flex-1 py-3 bg-primary text-white font-bold border-[3px] border-dark rounded-xl shadow-kitsch disabled:opacity-50"
              >
                {quizUpdating ? "저장 중..." : "저장"}
              </button>
              <button
                onClick={() => setEditQuiz(null)}
                className="px-6 py-3 bg-white font-bold border-[3px] border-dark rounded-xl shadow-kitsch-sm"
              >
                취소
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}