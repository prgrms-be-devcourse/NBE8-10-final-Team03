"use client";

import { use, useState, useEffect } from "react";
import { useRouter } from "next/navigation";
import Link from "next/link";
import api from "@/lib/api";
import YouTube from "react-youtube";

const getYoutubeId = (url: string) => {
  if (!url) return null;
  const regExp = /(?:youtube\.com\/(?:[^\/]+\/.+\/|(?:v|e(?:mbed)?)\/|.*[?&]v=|shorts\/)|youtu\.be\/)([^"&?\/\s]{11})/i;
  const match = url.match(regExp);
  return match ? match[1] : null;
};


function QuizEditItem({
  quiz,
  index,
  quizSetId,
  onUpdated,
}: {
  quiz: QuizItem;
  index: number;
  quizSetId: number;
  onUpdated: (updated: QuizItem) => void;
}) {
  const [editing, setEditing] = useState(false);
  const [form, setForm] = useState({ ...quiz });
  const [saving, setSaving] = useState(false);

  const handleSave = async () => {
    const choices = [form.choice1, form.choice2, form.choice3, form.choice4];

    if (!form.content || !form.answer) {
      alert("문항과 정답을 입력하세요.");
      return;
    }
    if (form.content.trim().length < 5) {
      alert("문제 내용은 5자 이상 입력하세요.");
      return;
    }
    if (form.questionType === "VIDEO" || form.questionType === "AUDIO") {
      if (!form.videoUrl) {
        alert("유튜브 링크를 입력하세요.");
        return;
      }
    }
    if (form.answerType === "MULTIPLE_CHOICE") {
      const choices = [form.choice1, form.choice2, form.choice3, form.choice4];
      if (choices.some((c) => !c)) {
        alert("4개 보기를 모두 입력하세요.");
        return;
      }
      const uniqueChoices = new Set(choices.map((c) => c?.trim()));
      if (uniqueChoices.size !== 4) {
        alert("보기가 중복되었습니다.");
        return;
      }
      if (!choices.map((c) => c?.trim()).includes(form.answer.trim())) {
        alert("정답이 보기 중에 없습니다.");
        return;
      }
    }
    setSaving(true);
    try {
      await api.patch(`/quizsets/${quizSetId}/quizzes/${quiz.id}`, {
        questionType: form.questionType,
        answerType: form.answerType,
        content: form.content,
        answer: form.answer,
        choice1: form.choice1,
        choice2: form.choice2,
        choice3: form.choice3,
        choice4: form.choice4,
        videoUrl: form.videoUrl,
        startTime: form.startTime,
        endTime: form.endTime,
      });
      onUpdated(form);
      setEditing(false);
    } catch (err) {
      console.error("퀴즈 수정 실패", err);
    } finally {
      setSaving(false);
    }
  };

  if (!editing) {
    return (
      <div className="bg-cream border-[3px] border-dark rounded-xl p-4">
        <div className="flex items-center justify-between mb-3">
          <p className="font-bold flex items-center gap-1 flex-wrap">
            <span>Q{index + 1}.</span>
            <span className="px-2 py-0.5 bg-primary/20 text-primary text-xs rounded-full">
              {quiz.questionType === "VIDEO" ? "📺영상" : quiz.questionType === "AUDIO" ? "🔊음성" : quiz.questionType === "IMAGE" ? "🖼️이미지" : "📝텍스트"}
            </span>
            <span className="px-2 py-0.5 bg-accent/20 text-accent text-xs rounded-full mr-1">
              {quiz.answerType === "SHORT_ANSWER" ? "주관식" : "객관식"}
            </span>
            <span>{quiz.content}</span>
          </p>
          <button
            onClick={() => setEditing(true)}
            className="text-xs font-bold text-primary hover:underline"
          >
            수정
          </button>
        </div>
        {quiz.answerType === "MULTIPLE_CHOICE" ? (
          <div className="grid grid-cols-2 gap-2 text-sm">
            {[quiz.choice1, quiz.choice2, quiz.choice3, quiz.choice4].map((choice, j) => (
              <div
                key={j}
                className={`px-3 py-2 rounded-lg border-2 ${choice === quiz.answer
                    ? "border-accent bg-accent/10 font-bold"
                    : "border-gray-200 bg-white"
                  }`}
              >
                {j + 1}. {choice}
              </div>
            ))}
          </div>
        ) : (
          <div className="text-sm px-3 py-2 rounded-lg border-2 border-accent bg-accent/10 font-bold w-fit">
            정답: {quiz.answer}
          </div>
        )}
      </div>
    );
  }

  return (
    <div className="bg-cream border-[3px] border-primary rounded-xl p-4">
      <p className="font-bold mb-3">Q{index + 1} 수정 중</p>
      <div className="grid grid-cols-2 gap-4 mb-4">
        <div>
          <label className="block text-xs font-bold mb-1">문제 유형</label>
          <select
            value={form.questionType}
            onChange={(e) => setForm({ ...form, questionType: e.target.value })}
            className="w-full px-3 py-2 bg-white border-2 border-dark rounded-xl text-sm outline-none"
          >
            <option value="TEXT">텍스트</option>
            <option value="IMAGE">이미지</option>
            <option value="VIDEO">유튜브 영상</option>
            <option value="AUDIO">유튜브 음성(소리만)</option>
          </select>
        </div>
        <div>
          <label className="block text-xs font-bold mb-1">정답 유형</label>
          <select
            value={form.answerType}
            onChange={(e) => setForm({ ...form, answerType: e.target.value })}
            className="w-full px-3 py-2 bg-white border-2 border-dark rounded-xl text-sm outline-none"
          >
            <option value="MULTIPLE_CHOICE">객관식</option>
            <option value="SHORT_ANSWER">주관식</option>
          </select>
        </div>
      </div>
      {(form.questionType === "VIDEO" || form.questionType === "AUDIO") && (
        <div className="mb-4 bg-gray-50 border-[2px] border-dashed border-gray-300 rounded-xl p-4">
          <label className="block text-xs font-bold mb-1">유튜브 링크</label>
          <input
            type="text"
            placeholder="유튜브 영상 주소"
            value={form.videoUrl || ""}
            onChange={(e) => setForm({ ...form, videoUrl: e.target.value })}
            className="w-full px-3 py-2 bg-white border-2 border-gray-300 rounded-xl text-sm outline-none mb-3"
          />
          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="block text-xs font-bold mb-1">시작 시간 (초)</label>
              <input
                type="number"
                value={form.startTime || ""}
                onChange={(e) => setForm({ ...form, startTime: e.target.value ? Number(e.target.value) : undefined })}
                className="w-full px-3 py-2 bg-white border-2 border-gray-300 rounded-xl text-sm outline-none"
              />
            </div>
            <div>
              <label className="block text-xs font-bold mb-1">종료 시간 (초)</label>
              <input
                type="number"
                value={form.endTime || ""}
                onChange={(e) => setForm({ ...form, endTime: e.target.value ? Number(e.target.value) : undefined })}
                className="w-full px-3 py-2 bg-white border-2 border-gray-300 rounded-xl text-sm outline-none"
              />
            </div>
          </div>

          {/* 미리보기 영역 */}
          {form.videoUrl && (
            <div className="mt-4 pt-4 border-t-[2px] border-gray-300 border-dashed">
              <p className="text-sm font-bold text-gray-500 mb-2">실제 화면 미리보기 테스트 (직접 재생해보세요)</p>
              {getYoutubeId(form.videoUrl) ? (
                <div className="relative mx-auto w-full bg-black rounded-xl border-[3px] border-dark overflow-hidden aspect-video mt-3">
                  <YouTube
                    videoId={getYoutubeId(form.videoUrl)!}
                    opts={{
                      width: "100%",
                      height: "100%",
                      playerVars: {
                        autoplay: 0,
                        controls: 1, // 테스트 가능하도록 컨트롤바 켜둠
                        start: form.startTime || 0,
                        ...(form.endTime ? { end: form.endTime } : {}),
                      },
                    }}
                    className="w-full h-full"
                  />
                </div>
              ) : (
                <p className="text-sm text-red-500 font-bold mt-2">유효하지 않은 유튜브 링크입니다.</p>
              )}
              {form.questionType === "AUDIO" && getYoutubeId(form.videoUrl) && (
                <div className="text-center p-4 bg-white border-[3px] border-dark border-dashed rounded-xl mt-4 mx-auto w-full">
                  <span className="text-3xl mb-2 block animate-pulse">🎶</span>
                  <p className="font-title text-sm text-primary">이 문제는 플레이 화면에서 영상이 숨겨지고 소리만 재생됩니다.</p>
                  <p className="text-xs text-gray-400 mt-1">※ 위 플레이어를 직접 조작해 가장 완벽한 컷(초)을 찾아보세요!</p>
                </div>
              )}
            </div>
          )}
        </div>
      )}
      <div className="mb-3">
        <label className="block text-xs font-bold mb-1">문제</label>
        <input
          type="text"
          value={form.content}
          onChange={(e) => setForm({ ...form, content: e.target.value })}
          className="w-full px-3 py-2 bg-white border-2 border-dark rounded-xl text-sm outline-none"
        />
      </div>
      <div className="mb-3">
        <label className="block text-xs font-bold mb-1">정답</label>
        <input
          type="text"
          value={form.answer}
          onChange={(e) => setForm({ ...form, answer: e.target.value })}
          className="w-full px-3 py-2 bg-white border-2 border-accent rounded-xl text-sm outline-none"
        />
      </div>
      {form.answerType === "MULTIPLE_CHOICE" && (
        <div className="grid grid-cols-2 gap-2 mb-4">
          {[1, 2, 3, 4].map((n) => (
            <div key={n}>
              <label className="block text-xs font-bold mb-1">보기 {n}</label>
              <input
                type="text"
                value={form[`choice${n}` as keyof QuizItem] as string}
                onChange={(e) => setForm({ ...form, [`choice${n}`]: e.target.value })}
                className="w-full px-3 py-2 bg-white border-2 border-dark rounded-xl text-sm outline-none"
              />
            </div>
          ))}
        </div>
      )}
      <div className="flex gap-2">
        <button
          onClick={handleSave}
          disabled={saving}
          className="flex-1 py-2 bg-primary text-white font-bold border-2 border-dark rounded-xl text-sm disabled:opacity-50"
        >
          {saving ? "저장 중..." : "저장"}
        </button>
        <button
          onClick={() => { setForm({ ...quiz }); setEditing(false); }}
          className="px-4 py-2 bg-white font-bold border-2 border-dark rounded-xl text-sm"
        >
          취소
        </button>
      </div>
    </div>
  );
}

interface QuizItem {
  id: number;
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

interface QuizSetDetail {
  id: number;
  title: string;
  description: string;
  creatorNickname: string;
  totalQuizCount: number;
  quizzes: QuizItem[];
}

export default function QuizSetDetailPage({ params }: { params: Promise<{ quizsetId: string }> }) {
  const { quizsetId } = use(params);
  const router = useRouter();
  const [quizSet, setQuizSet] = useState<QuizSetDetail | null>(null);
  const [loading, setLoading] = useState(true);
  const [myNickname, setMyNickname] = useState<string | null>(null);
  const [isEditing, setIsEditing] = useState(false);
  const [editTitle, setEditTitle] = useState("");
  const [editDescription, setEditDescription] = useState("");
  const [editError, setEditError] = useState("");
  const [updating, setUpdating] = useState(false);
  const [showReportModal, setShowReportModal] = useState(false);
  const [reportReason, setReportReason] = useState("");
  const [reporting, setReporting] = useState(false);

  useEffect(() => {
    setMyNickname(localStorage.getItem("nickname"));
  }, []);

  useEffect(() => {
    const fetchQuizSet = async () => {
      try {
        const res = await api.get(`/quizsets/${quizsetId}/info`);
        console.log("퀴즈셋 조회 시도:", quizsetId);
        setQuizSet(res.data.data);
      } catch (err) {
        console.error("퀴즈셋 조회 실패", err);
      } finally {
        setLoading(false);
      }
    };
    fetchQuizSet();
  }, [quizsetId]);

  const handleReport = async () => {
    if (!reportReason.trim()) return;
    setReporting(true);
    try {
      await api.post("/reports", {
        quizSetId: quizSet?.id,  // ← currentRoom 아니고 quizSet.id
        reason: reportReason,
      });
      alert("신고가 접수되었습니다.");
      setShowReportModal(false);
      setReportReason("");
    } catch (err) {
      alert("신고 접수에 실패했습니다.");
    } finally {
      setReporting(false);
    }
  };

  const handleEditOpen = () => {
    if (!quizSet) return;
    setEditTitle(quizSet.title);
    setEditDescription(quizSet.description || "");
    setEditError("");
    setIsEditing(true);
  };

  const handleUpdate = async () => {
    setEditError("");
    if (!editTitle.trim()) { setEditError("제목을 입력하세요."); return; }
    if (editTitle.length > 30) { setEditError("제목은 30자 이하이어야 합니다."); return; }

    setUpdating(true);
    try {
      await api.patch(`/quizsets/${quizsetId}`, {
        title: editTitle,
        description: editDescription,
      });
      setQuizSet((prev) => prev ? { ...prev, title: editTitle, description: editDescription } : prev);
      setIsEditing(false);
    } catch (err) {
      console.error("수정 실패", err);
      setEditError("수정에 실패했습니다.");
    } finally {
      setUpdating(false);
    }
  };

  const handleDelete = async () => {
    if (!confirm("퀴즈셋을 삭제할까요?")) return;
    try {
      await api.delete(`/quizsets/${quizsetId}`);
      router.push("/quizsets");
    } catch (err) {
      console.error("삭제 실패", err);
    }
  };

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

  const isOwner = myNickname === quizSet.creatorNickname;

  return (
    <div className="max-w-3xl mx-auto px-4 py-10">
      <Link href="/quizsets" className="inline-block mb-6 text-sm text-gray-400 hover:text-primary font-bold">
        ← 퀴즈셋 목록
      </Link>

      {/* 퀴즈셋 정보 */}
      <div className="bg-white border-[3px] border-dark rounded-2xl shadow-kitsch p-8 mb-6">
        {isEditing ? (
          <>
            <h2 className="font-title text-2xl mb-6">퀴즈셋 수정</h2>
            {editError && (
              <div className="mb-4 px-4 py-3 bg-red-50 border-2 border-red-300 rounded-xl text-sm text-red-600 font-bold">
                {editError}
              </div>
            )}
            <div className="mb-4">
              <label className="block text-sm font-bold mb-2">제목</label>
              <input
                type="text"
                value={editTitle}
                onChange={(e) => setEditTitle(e.target.value)}
                className="w-full px-4 py-3 bg-cream border-[3px] border-dark rounded-xl text-sm focus:border-primary outline-none transition-colors"
              />
            </div>
            <div className="mb-6">
              <label className="block text-sm font-bold mb-2">설명</label>
              <textarea
                value={editDescription}
                onChange={(e) => setEditDescription(e.target.value)}
                rows={3}
                className="w-full px-4 py-3 bg-cream border-[3px] border-dark rounded-xl text-sm focus:border-primary outline-none transition-colors resize-none"
              />
            </div>

            {/* 수정 모드에서만 퀴즈 목록 표시 */}
            <div className="mb-6">
              <h3 className="font-title text-lg mb-4">문제 목록 ({quizSet.quizzes?.length ?? 0}개)</h3>
              <div className="flex flex-col gap-4">
                {quizSet.quizzes?.map((quiz, i) => (
                  <QuizEditItem
                    key={quiz.id}
                    quiz={quiz}
                    index={i}
                    quizSetId={quizSet.id}
                    onUpdated={(updated) => {
                      setQuizSet((prev) => prev ? {
                        ...prev,
                        quizzes: prev.quizzes.map((q) => q.id === updated.id ? updated : q)
                      } : prev);
                    }}
                  />
                ))}
              </div>
            </div>

            <div className="flex gap-3">
              <button
                onClick={handleUpdate}
                disabled={updating}
                className="flex-1 py-3 bg-primary text-white font-bold border-[3px] border-dark rounded-xl shadow-kitsch hover:shadow-kitsch-lg hover:-translate-y-0.5 transition-all disabled:opacity-50"
              >
                {updating ? "저장 중..." : "저장"}
              </button>
              <button
                onClick={() => setIsEditing(false)}
                className="px-6 py-3 bg-white font-bold border-[3px] border-dark rounded-xl shadow-kitsch-sm hover:shadow-kitsch hover:-translate-y-0.5 transition-all"
              >
                취소
              </button>
            </div>
          </>
        ) : (
          <>
            <div className="flex items-start justify-between mb-4">
              <div>
                <h1 className="font-title text-3xl mb-2">{quizSet.title}</h1>
                <p className="text-sm text-gray-400 mb-4">{quizSet.creatorNickname}</p>
                <p className="text-sm text-gray-600">{quizSet.description}</p>
              </div>
              <div className="flex gap-2 shrink-0">
                {isOwner ? (
                  <>
                    <button onClick={handleEditOpen} className="px-4 py-2 bg-white border-[3px] border-dark rounded-xl text-sm font-bold shadow-kitsch-sm hover:shadow-kitsch hover:-translate-y-0.5 transition-all">
                      수정
                    </button>
                    <button onClick={handleDelete} className="px-4 py-2 bg-red-500 text-white border-[3px] border-dark rounded-xl text-sm font-bold shadow-kitsch-sm hover:shadow-kitsch hover:-translate-y-0.5 transition-all">
                      삭제
                    </button>
                  </>
                ) : (
                  <button onClick={() => setShowReportModal(true)} className="px-4 py-2 bg-white text-red-500 border-[3px] border-red-300 rounded-xl text-sm font-bold shadow-kitsch-sm hover:shadow-kitsch hover:-translate-y-0.5 transition-all">
                    🚨 신고
                  </button>
                )}
              </div>
            </div>
            <div className="flex gap-4 mt-6">
              <span className="px-4 py-2 bg-cream border-2 border-dark rounded-xl text-sm font-bold">
                📝 {quizSet.totalQuizCount}문제
              </span>
            </div>
          </>
        )}
      </div>

      {!isEditing && (
        <div className="bg-white border-[3px] border-dark rounded-2xl shadow-kitsch p-8 mb-8 text-center">
          <p className="font-hand text-lg text-gray-300">아직 준비 중이에요!</p>
        </div>
      )}

      {!isEditing && (
        <button
          onClick={() => router.push(`/rooms?quizSetId=${quizSet.id}`)}
          className="w-full py-4 bg-primary text-white font-bold text-lg border-[3px] border-dark rounded-2xl shadow-kitsch hover:shadow-kitsch-lg hover:-translate-y-0.5 transition-all"
        >
          이 퀴즈셋으로 방 만들기
        </button>
      )}
      {showReportModal && (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50">
          <div className="bg-white border-[3px] border-dark rounded-2xl shadow-kitsch-lg p-8 w-full max-w-md">
            <h2 className="font-title text-2xl mb-2">퀴즈셋 신고</h2>
            <p className="text-sm text-gray-400 mb-6">{quizSet.title}</p>

            <div className="mb-6">
              <label className="block text-sm font-bold mb-2">신고 사유</label>
              <textarea
                value={reportReason}
                onChange={(e) => setReportReason(e.target.value)}
                placeholder="신고 사유를 입력해주세요. (최대 500자)"
                maxLength={500}
                rows={4}
                className="w-full px-4 py-3 bg-cream border-[3px] border-dark rounded-xl text-sm focus:border-red-400 outline-none transition-colors resize-none"
              />
              <p className="text-xs text-gray-400 text-right mt-1">{reportReason.length}/500</p>
            </div>

            <div className="flex gap-3">
              <button
                onClick={handleReport}
                disabled={reporting || !reportReason.trim()}
                className="flex-1 py-3 bg-red-500 text-white font-bold border-[3px] border-dark rounded-xl shadow-kitsch hover:shadow-kitsch-lg hover:-translate-y-0.5 transition-all disabled:opacity-50"
              >
                {reporting ? "신고 중..." : "신고하기"}
              </button>
              <button
                onClick={() => { setShowReportModal(false); setReportReason(""); }}
                className="px-6 py-3 bg-white font-bold border-[3px] border-dark rounded-xl shadow-kitsch-sm hover:shadow-kitsch hover:-translate-y-0.5 transition-all"
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