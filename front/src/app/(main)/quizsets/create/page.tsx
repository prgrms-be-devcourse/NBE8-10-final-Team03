"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import api from "@/lib/api";

interface QuizInput {
  content: string;
  answer: string;
  choice1: string;
  choice2: string;
  choice3: string;
  choice4: string;
}

const emptyQuiz: QuizInput = {
  content: "",
  answer: "",
  choice1: "",
  choice2: "",
  choice3: "",
  choice4: "",
};

export default function QuizSetCreatePage() {
  const router = useRouter();
  const [title, setTitle] = useState("");
  const [description, setDescription] = useState("");
  const [quizzes, setQuizzes] = useState<QuizInput[]>([{ ...emptyQuiz }]);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  const addQuiz = () => {
    setQuizzes([...quizzes, { ...emptyQuiz }]);
  };

  const removeQuiz = (index: number) => {
    if (quizzes.length <= 1) return;
    setQuizzes(quizzes.filter((_, i) => i !== index));
  };

  const updateQuiz = (index: number, field: keyof QuizInput, value: string) => {
    const updated = [...quizzes];
    updated[index] = { ...updated[index], [field]: value };
    setQuizzes(updated);
  };

  const handleSubmit = async () => {
    setError("");

    if (!title.trim()) {
      setError("퀴즈셋 제목을 입력하세요.");
      return;
    }

    if (title.length < 5) {
      setError("퀴즈셋 제목은 5자 이상이어야 합니다.");
      return;
    }

    if (quizzes.length < 5) {
      setError("최소 5문제 이상 등록해야 합니다.");
      return;
    }

    for (let i = 0; i < quizzes.length; i++) {
      const q = quizzes[i];
      if (!q.content || !q.answer || !q.choice1 || !q.choice2 || !q.choice3 || !q.choice4) {
        setError(`${i + 1}번 문제의 모든 항목을 입력하세요.`);
        return;
      }
    }

    setLoading(true);
    try {
      await api.post("/quizsets", {
        title,
        description,
        totalQuizCount: quizzes.length,
        quizzes: quizzes.map((q, i) => ({
          ...q,
          sequence: i + 1,
        })),
      });
      router.push("/quizsets");
    } catch (err: any) {
      setError(err.response?.data?.message || "퀴즈셋 생성에 실패했습니다.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="max-w-3xl mx-auto px-4 py-10">
      <h1 className="font-title text-4xl mb-8">📝 퀴즈셋 만들기</h1>

      {error && (
        <div className="mb-6 px-4 py-3 bg-red-50 border-2 border-red-300 rounded-xl text-sm text-red-600 font-bold">
          {error}
        </div>
      )}

      {/* 기본 정보 */}
      <div className="bg-white border-[3px] border-dark rounded-2xl shadow-kitsch p-6 mb-6">
        <h2 className="font-title text-xl mb-4">기본 정보</h2>
        <div className="mb-4">
          <label className="block text-sm font-bold mb-2">제목</label>
          <input
            type="text"
            placeholder="퀴즈셋 제목을 입력하세요"
            value={title}
            onChange={(e) => setTitle(e.target.value)}
            className="w-full px-4 py-3 bg-cream border-[3px] border-dark rounded-xl text-sm focus:border-primary outline-none transition-colors"
          />
        </div>
        <div>
          <label className="block text-sm font-bold mb-2">설명</label>
          <textarea
            placeholder="퀴즈셋에 대한 설명을 입력하세요 (선택)"
            value={description}
            onChange={(e) => setDescription(e.target.value)}
            rows={3}
            className="w-full px-4 py-3 bg-cream border-[3px] border-dark rounded-xl text-sm focus:border-primary outline-none transition-colors resize-none"
          />
        </div>
      </div>

      {/* 문제 목록 */}
      <div className="flex items-center justify-between mb-4">
        <h2 className="font-title text-xl">문제 ({quizzes.length}개)</h2>
        <button
          onClick={addQuiz}
          className="px-4 py-2 bg-accent text-white border-[3px] border-dark rounded-xl font-bold text-sm shadow-kitsch-sm hover:shadow-kitsch hover:-translate-y-0.5 transition-all"
        >
          + 문제 추가
        </button>
      </div>

      <div className="flex flex-col gap-4 mb-8">
        {quizzes.map((quiz, index) => (
          <div key={index} className="bg-white border-[3px] border-dark rounded-2xl shadow-kitsch p-6">
            <div className="flex items-center justify-between mb-4">
              <span className="font-title text-lg text-primary">Q{index + 1}</span>
              {quizzes.length > 1 && (
                <button
                  onClick={() => removeQuiz(index)}
                  className="text-sm text-red-500 font-bold hover:text-red-700"
                >
                  삭제
                </button>
              )}
            </div>

            <div className="mb-4">
              <label className="block text-sm font-bold mb-2">문제</label>
              <input
                type="text"
                placeholder="문제를 입력하세요"
                value={quiz.content}
                onChange={(e) => updateQuiz(index, "content", e.target.value)}
                className="w-full px-4 py-3 bg-cream border-[3px] border-dark rounded-xl text-sm focus:border-primary outline-none transition-colors"
              />
            </div>

            <div className="mb-4">
              <label className="block text-sm font-bold mb-2">정답</label>
              <input
                type="text"
                placeholder="정답을 입력하세요"
                value={quiz.answer}
                onChange={(e) => updateQuiz(index, "answer", e.target.value)}
                className="w-full px-4 py-3 bg-cream border-[3px] border-dark rounded-xl text-sm focus:border-accent outline-none transition-colors"
              />
            </div>

            <div className="grid grid-cols-2 gap-3">
              {[1, 2, 3, 4].map((n) => (
                <div key={n}>
                  <label className="block text-xs font-bold mb-1">보기 {n}</label>
                  <input
                    type="text"
                    placeholder={`보기 ${n}`}
                    value={quiz[`choice${n}` as keyof QuizInput]}
                    onChange={(e) => updateQuiz(index, `choice${n}` as keyof QuizInput, e.target.value)}
                    className="w-full px-3 py-2 bg-cream border-[3px] border-dark rounded-xl text-sm focus:border-primary outline-none transition-colors"
                  />
                </div>
              ))}
            </div>
          </div>
        ))}
      </div>

      {/* 제출 */}
      <button
        onClick={handleSubmit}
        disabled={loading}
        className="w-full py-4 bg-primary text-white font-bold text-lg border-[3px] border-dark rounded-2xl shadow-kitsch hover:shadow-kitsch-lg hover:-translate-y-0.5 transition-all disabled:opacity-50"
      >
        {loading ? "생성 중..." : `퀴즈셋 만들기 (${quizzes.length}문제)`}
      </button>
    </div>
  );
}