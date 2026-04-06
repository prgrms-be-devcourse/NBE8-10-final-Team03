"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import api from "@/lib/api";
import YouTube from "react-youtube";

const getYoutubeId = (url: string) => {
  if (!url) return null;
  const regExp = /(?:youtube\.com\/(?:[^\/]+\/.+\/|(?:v|e(?:mbed)?)\/|.*[?&]v=|shorts\/)|youtu\.be\/)([^"&?\/\s]{11})/i;
  const match = url.match(regExp);
  return match ? match[1] : null;
};

interface QuizInput {
  questionType: string;
  answerType: string;
  content: string;
  answer: string;
  choice1: string;
  choice2: string;
  choice3: string;
  choice4: string;
  videoUrl?: string;
  startTime?: number;
  endTime?: number;
}

const emptyQuiz: QuizInput = {
  questionType: "TEXT",
  answerType: "MULTIPLE_CHOICE",
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

  const setErrorAndScroll = (msg: string) => {
    setError(msg);
    window.scrollTo({ top: 0, behavior: "smooth" });
  };

  const addQuiz = () => setQuizzes([...quizzes, { ...emptyQuiz }]);

  const removeQuiz = (index: number) => {
    if (quizzes.length <= 1) return;
    setQuizzes(quizzes.filter((_, i) => i !== index));
  };

  const updateQuiz = (index: number, field: keyof QuizInput, value: any) => {
    const updated = [...quizzes];
    updated[index] = { ...updated[index], [field]: value };
    setQuizzes(updated);
  };

  const handleSubmit = async () => {
    setError("");

    if (!title.trim()) { setErrorAndScroll("퀴즈셋 제목을 입력하세요."); return; }
    if (title.length < 5) { setErrorAndScroll("퀴즈셋 제목은 5자 이상이어야 합니다."); return; }
    if (quizzes.length < 5) { setErrorAndScroll("최소 5문제 이상 등록해야 합니다."); return; }
    if (title.startsWith("[AI]")) {
      setErrorAndScroll("제목은 [AI]로 시작할 수 없습니다.");
      return;
    }

    for (let i = 0; i < quizzes.length; i++) {
      const q = quizzes[i];
      const num = i + 1;

      if (!q.content || !q.answer) {
        setErrorAndScroll(`${num}번 문제의 필수 항목을 입력하세요.`);
        return;
      }
      if (q.content.trim().length < 5) {
        setErrorAndScroll(`${num}번 문제 내용은 5자 이상 입력하세요.`);
        return;
      }
      if (q.questionType === "VIDEO" || q.questionType === "AUDIO") {
        if (!q.videoUrl) {
          setErrorAndScroll(`${num}번 문제의 유튜브 링크를 입력하세요.`);
          return;
        }
      }
      if (q.answerType === "MULTIPLE_CHOICE") {
        if (!q.choice1 || !q.choice2 || !q.choice3 || !q.choice4) {
          setErrorAndScroll(`${num}번 문제의 4개 보기를 모두 입력하세요.`);
          return;
        }
        const choices = [q.choice1, q.choice2, q.choice3, q.choice4];
        const uniqueChoices = new Set(choices.map((c) => c.trim()));
        if (uniqueChoices.size !== 4) {
          setErrorAndScroll(`${num}번 문제의 보기가 중복되었습니다.`);
          return;
        }
        if (!choices.map((c) => c.trim()).includes(q.answer.trim())) {
          setErrorAndScroll(`${num}번 문제의 정답이 보기 중에 없습니다.`);
          return;
        }
      }
    }

    setLoading(true);
    try {
      const quizSetRes = await api.post("/quizsets", { title, description });
      const location = quizSetRes.headers["location"];
      const quizSetId = location?.split("/").pop();

      if (!quizSetId) throw new Error("퀴즈셋 ID를 가져오지 못했습니다.");

      for (const quiz of quizzes) {
        await api.post(`/quizsets/${quizSetId}/quizzes`, {
          questionType: quiz.questionType,
          answerType: quiz.answerType,
          content: quiz.content,
          answer: quiz.answer,
          choice1: quiz.choice1,
          choice2: quiz.choice2,
          choice3: quiz.choice3,
          choice4: quiz.choice4,
          videoUrl: quiz.videoUrl,
          startTime: quiz.startTime,
          endTime: quiz.endTime,
        });
      }

      router.push("/quizsets");
    } catch (err: any) {
      setErrorAndScroll(err.response?.data?.message || err.message || "퀴즈셋 생성에 실패했습니다.");
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
        <h2 className="font-title text-xl">
          문제 ({quizzes.length}개)
          {quizzes.length < 5 && (
            <span className="ml-2 font-hand text-sm text-red-400">
              최소 5문제 필요 ({5 - quizzes.length}개 더 추가하세요)
            </span>
          )}
        </h2>
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
            <div className="grid grid-cols-2 gap-4 mb-4">
              <div>
                <label className="block text-sm font-bold mb-2">문제 유형</label>
                <select
                  value={quiz.questionType}
                  onChange={(e) => updateQuiz(index, "questionType", e.target.value)}
                  className="w-full px-4 py-3 bg-cream border-[3px] border-dark rounded-xl text-sm focus:border-primary outline-none transition-colors"
                >
                  <option value="TEXT">텍스트</option>
                  <option value="IMAGE">이미지</option>
                  <option value="VIDEO">유튜브 영상</option>
                  <option value="AUDIO">유튜브 음성(소리만)</option>
                </select>
              </div>
              <div>
                <label className="block text-sm font-bold mb-2">정답 유형</label>
                <select
                  value={quiz.answerType}
                  onChange={(e) => updateQuiz(index, "answerType", e.target.value)}
                  className="w-full px-4 py-3 bg-cream border-[3px] border-dark rounded-xl text-sm focus:border-primary outline-none transition-colors"
                >
                  <option value="MULTIPLE_CHOICE">객관식</option>
                  <option value="SHORT_ANSWER">주관식</option>
                </select>
              </div>
            </div>
            
            {(quiz.questionType === "VIDEO" || quiz.questionType === "AUDIO") && (
              <div className="mb-4 bg-gray-50 border-[2px] border-dashed border-gray-300 rounded-xl p-4">
                <label className="block text-sm font-bold mb-2">유튜브 링크</label>
                <input
                  type="text"
                  placeholder="유튜브 영상 주소 (예: https://youtu.be/...)"
                  value={quiz.videoUrl || ""}
                  onChange={(e) => updateQuiz(index, "videoUrl", e.target.value)}
                  className="w-full px-4 py-2 mb-3 bg-white border-[2px] border-gray-300 rounded-lg text-sm outline-none"
                />
                <div className="grid grid-cols-2 gap-3">
                  <div>
                    <label className="block text-xs font-bold mb-1">시작 시간 (초 - 선택)</label>
                    <input
                      type="number"
                      placeholder="예: 30"
                      value={quiz.startTime || ""}
                      onChange={(e) => updateQuiz(index, "startTime", e.target.value ? Number(e.target.value) : undefined)}
                      className="w-full px-3 py-2 bg-white border-[2px] border-gray-300 rounded-lg text-sm outline-none"
                    />
                  </div>
                  <div>
                    <label className="block text-xs font-bold mb-1">종료 시간 (초 - 선택)</label>
                    <input
                      type="number"
                      placeholder="예: 60"
                      value={quiz.endTime || ""}
                      onChange={(e) => updateQuiz(index, "endTime", e.target.value ? Number(e.target.value) : undefined)}
                      className="w-full px-3 py-2 bg-white border-[2px] border-gray-300 rounded-lg text-sm outline-none"
                    />
                  </div>
                </div>

                {/* 미리보기 영역 */}
                {quiz.videoUrl && (
                  <div className="mt-4 pt-4 border-t-[2px] border-gray-300 border-dashed">
                    <p className="text-sm font-bold text-gray-500 mb-2">실제 화면 미리보기 테스트 (직접 재생해보세요)</p>
                    {getYoutubeId(quiz.videoUrl) ? (
                      <div className={`relative mx-auto ${quiz.questionType === "AUDIO" ? "w-0 h-0 overflow-hidden opacity-0" : "w-full max-w-sm bg-black rounded-xl border-[3px] border-dark overflow-hidden aspect-video"}`}>
                        <YouTube
                          videoId={getYoutubeId(quiz.videoUrl)!}
                          opts={{
                            width: "100%",
                            height: "100%",
                            playerVars: {
                              autoplay: 0,
                              controls: 1, // 미리보기에서는 테스트하기 편하게 컨트롤바 허용
                              start: quiz.startTime || 0,
                              ...(quiz.endTime ? { end: quiz.endTime } : {}),
                            },
                          }}
                          className="w-full h-full"
                        />
                      </div>
                    ) : (
                      <p className="text-sm text-red-500 font-bold mt-2">유효하지 않은 유튜브 링크입니다.</p>
                    )}
                    {quiz.questionType === "AUDIO" && getYoutubeId(quiz.videoUrl) && (
                      <div className="text-center p-4 bg-white border-[3px] border-dark border-dashed rounded-xl mt-2 mx-auto max-w-sm">
                        <span className="text-3xl mb-2 block">🎶</span>
                        <p className="font-title text-sm text-primary">미리보기 (오디오 모드)</p>
                        <p className="text-xs text-gray-400 mt-1">※ 위 검은 화면(플레이어)을 클릭하여 시간이 짤렸는지 소리를 확인해보세요.</p>
                      </div>
                    )}
                  </div>
                )}
              </div>
            )}

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
            {quiz.answerType === "MULTIPLE_CHOICE" && (
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
            )}
          </div>
        ))}
      </div>

      {/* 제출 버튼 */}
      <button
        onClick={handleSubmit}
        disabled={loading || quizzes.length < 5}
        className={`w-full py-4 text-white font-bold text-lg border-[3px] border-dark rounded-2xl shadow-kitsch transition-all ${
          quizzes.length < 5
            ? "bg-gray-300 cursor-not-allowed opacity-60"
            : "bg-primary hover:shadow-kitsch-lg hover:-translate-y-0.5"
        }`}
      >
        {loading ? "생성 중..." : quizzes.length < 5
          ? `퀴즈셋 만들기 (${quizzes.length}/5문제)`
          : `퀴즈셋 만들기 (${quizzes.length}문제)`
        }
      </button>
    </div>
  );
}