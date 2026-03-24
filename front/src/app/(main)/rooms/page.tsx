"use client";

import { useState, useEffect } from "react";
import { useRouter } from "next/navigation";
import Link from "next/link";
import api from "@/lib/api";

interface Room {
  gameSessionId: number;
  roomName: string;
  hostNickname: string;
  quizSetId: number;
  quizSetTitle: string;
  currentPlayerCount: number;
  maxPlayer: number;
  status: string;
}

interface RankingItem {
  rank: number;
  nickname: string;
  totalRankingScore: number;
}

interface QuizSet {
  id: number;
  title: string;
  totalQuizCount: number;
}

export default function RoomsPage() {
  const router = useRouter();
  const [rooms, setRooms] = useState<Room[]>([]);
  const [rankings, setRankings] = useState<RankingItem[]>([]);
  const [filter, setFilter] = useState<string>("all");
  const [loading, setLoading] = useState(true);

  // 방 만들기 모달
  const [showModal, setShowModal] = useState(false);
  const [quizSets, setQuizSets] = useState<QuizSet[]>([]);
  const [roomName, setRoomName] = useState("");
  const [selectedQuizSetId, setSelectedQuizSetId] = useState<number | null>(null);
  const [maxPlayers, setMaxPlayers] = useState(4);
  const [maxQuizzes, setMaxQuizzes] = useState(10);
  const [createError, setCreateError] = useState("");
  const [creating, setCreating] = useState(false);

  useEffect(() => {
    const fetchData = async () => {
      try {
        const [roomsRes, rankingsRes] = await Promise.all([
          api.get("/rooms"),
          api.get("/rankings"),
        ]);
        setRooms(roomsRes.data.data);
        setRankings(rankingsRes.data.data.rankings.slice(0, 5));
      } catch (err) {
        console.error("로비 데이터 조회 실패", err);
      } finally {
        setLoading(false);
      }
    };
    fetchData();
  }, []);

  const handleOpenModal = async () => {
    try {
      const res = await api.get("/quizsets");
      setQuizSets(res.data.data);
      if (res.data.data.length > 0) {
        setSelectedQuizSetId(res.data.data[0].id);
        setMaxQuizzes(res.data.data[0].totalQuizCount);
      }
    } catch (err) {
      console.error("퀴즈셋 조회 실패", err);
    }
    setShowModal(true);
  };

  const handleQuizSetChange = (quizSetId: number) => {
    setSelectedQuizSetId(quizSetId);
    const selected = quizSets.find((q) => q.id === quizSetId);
    if (selected) {
      setMaxQuizzes(selected.totalQuizCount);
    }
  };

  const handleCreateRoom = async () => {
    setCreateError("");
    if (!roomName.trim()) {
      setCreateError("방 제목을 입력하세요.");
      return;
    }
    if (!selectedQuizSetId) {
      setCreateError("퀴즈셋을 선택하세요.");
      return;
    }

    setCreating(true);
    try {
      const res = await api.post("/rooms", {
        roomName,
        quizSetId: selectedQuizSetId,
        maxPlayers,
        maxQuizzes,
      });
      const gameSessionId = res.data.data.gameSessionId;
      setShowModal(false);
      router.push(`/rooms/${gameSessionId}`);
    } catch (err: any) {
      setCreateError(err.response?.data?.message || "방 생성에 실패했습니다.");
    } finally {
      setCreating(false);
    }
  };

  const filteredRooms = rooms.filter((room) => {
    if (filter === "waiting") return room.status === "WAIT";
    if (filter === "playing") return room.status === "START";
    return true;
  });

  const rankColors = ["text-primary", "text-accent", "text-secondary", "text-gray-400", "text-gray-400"];

  if (loading) {
    return (
      <div className="max-w-7xl mx-auto px-4 py-10 text-center">
        <p className="font-hand text-xl text-gray-400">로비 불러오는 중...</p>
      </div>
    );
  }

  return (
    <div className="max-w-7xl mx-auto px-4 py-10 flex gap-6">
      {/* 좌측 메인 */}
      <div className="flex-1">
        <div className="mb-6">
          <h1 className="font-title text-4xl mb-1">게임 대기실</h1>
          <p className="font-hand text-lg text-gray-400">참여할 퀴즈방을 선택하세요</p>
        </div>

        {/* 필터 */}
        <div className="flex gap-2 mb-6">
          {[
            { key: "all", label: "전체" },
            { key: "waiting", label: "대기중" },
            { key: "playing", label: "게임중" },
          ].map((f) => (
            <button
              key={f.key}
              onClick={() => setFilter(f.key)}
              className={`px-5 py-2 border-[3px] border-dark rounded-full font-bold text-sm shadow-kitsch-sm transition-all ${
                filter === f.key ? "bg-secondary" : "bg-white hover:bg-gray-50"
              }`}
            >
              {f.label}
            </button>
          ))}
        </div>

        {/* 방 목록 */}
        <div className="flex flex-col gap-3 mb-6">
          {filteredRooms.length === 0 ? (
            <div className="bg-white border-[3px] border-dark rounded-2xl p-10 shadow-kitsch text-center">
              <p className="font-hand text-lg text-gray-400">방이 없어요. 새로 만들어보세요!</p>
            </div>
          ) : (
            filteredRooms.map((room) => (
              <div
                key={room.gameSessionId}
                className="flex items-center bg-white border-[3px] border-dark rounded-2xl px-6 py-4 shadow-kitsch hover:shadow-kitsch-lg hover:-translate-y-0.5 transition-all cursor-pointer"
              >
                <div className="flex-1">
                  <h3 className="font-bold mb-1">{room.roomName}</h3>
                  <p className="text-xs text-gray-400">
                    {room.quizSetTitle} · {room.hostNickname}
                  </p>
                </div>

                <div className="flex items-center gap-4">
                  <div className="text-right">
                    <p className="font-title text-xl text-primary">
                      {room.currentPlayerCount}/{room.maxPlayer}
                    </p>
                    <div className="w-16 h-2 bg-gray-200 rounded-full mt-1">
                      <div
                        className="h-full bg-primary rounded-full"
                        style={{ width: `${(room.currentPlayerCount / room.maxPlayer) * 100}%` }}
                      />
                    </div>
                  </div>

                  {room.status === "WAIT" ? (
                    <button
                      onClick={() => router.push(`/rooms/${room.gameSessionId}`)}
                      className="px-4 py-2 bg-primary text-white border-[3px] border-dark rounded-xl text-sm font-bold shadow-kitsch-sm hover:shadow-kitsch hover:-translate-y-0.5 transition-all"
                    >
                      참여하기
                    </button>
                  ) : (
                    <span className="px-4 py-2 bg-gray-200 text-gray-500 border-[3px] border-gray-300 rounded-xl text-sm font-bold">
                      {room.status === "START" ? "게임중" : "종료"}
                    </span>
                  )}
                </div>
              </div>
            ))
          )}
        </div>

        {/* 방 만들기 */}
        <button
          onClick={handleOpenModal}
          className="w-full py-4 bg-accent text-white font-bold text-lg border-[3px] border-dark rounded-2xl shadow-kitsch hover:shadow-kitsch-lg hover:-translate-y-0.5 transition-all"
        >
          + 새 방 만들기
        </button>
      </div>

      {/* 우측 사이드바 */}
      <div className="w-80 flex flex-col gap-4">
        <div className="bg-white border-[3px] border-dark rounded-2xl p-6 shadow-kitsch">
          <h3 className="font-title text-lg mb-4">🏆 TOP 5</h3>
          <div className="flex flex-col gap-3">
            {rankings.map((r) => (
              <div key={r.rank} className="flex items-center gap-3">
                <span className={`font-title text-xl w-8 ${rankColors[r.rank - 1]}`}>
                  {r.rank}
                </span>
                <span className="flex-1 text-sm font-bold truncate">{r.nickname}</span>
                <span className="text-sm text-gray-400">{r.totalRankingScore.toLocaleString()}P</span>
              </div>
            ))}
          </div>
          <Link href="/rankings" className="block w-full mt-4 py-2 bg-cream border-[3px] border-dark rounded-xl text-sm font-bold shadow-kitsch-sm hover:shadow-kitsch hover:-translate-y-0.5 transition-all text-center">
            전체 랭킹 보기
          </Link>
        </div>

        <div className="bg-white border-[3px] border-dark rounded-2xl p-6 shadow-kitsch">
          <h3 className="font-title text-lg mb-4">바로가기</h3>
          <div className="flex flex-col gap-2">
            <Link href="/quizsets" className="w-full py-3 bg-cream border-[3px] border-dark rounded-xl text-sm font-bold shadow-kitsch-sm hover:shadow-kitsch hover:-translate-y-0.5 transition-all text-center">
              📝 퀴즈셋
            </Link>
            <Link href="/me" className="w-full py-3 bg-cream border-[3px] border-dark rounded-xl text-sm font-bold shadow-kitsch-sm hover:shadow-kitsch hover:-translate-y-0.5 transition-all text-center">
              👤 마이페이지
            </Link>
          </div>
        </div>
      </div>

      {/* 방 만들기 모달 */}
      {showModal && (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50">
          <div className="bg-white border-[3px] border-dark rounded-2xl shadow-kitsch-lg p-8 w-full max-w-md">
            <h2 className="font-title text-2xl mb-6">새 방 만들기</h2>

            {createError && (
              <div className="mb-4 px-4 py-3 bg-red-50 border-2 border-red-300 rounded-xl text-sm text-red-600 font-bold">
                {createError}
              </div>
            )}

            <div className="mb-4">
              <label className="block text-sm font-bold mb-2">방 제목</label>
              <input
                type="text"
                placeholder="방 제목을 입력하세요"
                value={roomName}
                onChange={(e) => setRoomName(e.target.value)}
                className="w-full px-4 py-3 bg-cream border-[3px] border-dark rounded-xl text-sm focus:border-primary outline-none transition-colors"
              />
            </div>

            <div className="mb-4">
              <label className="block text-sm font-bold mb-2">퀴즈셋</label>
              <select
                value={selectedQuizSetId || ""}
                onChange={(e) => handleQuizSetChange(Number(e.target.value))}
                className="w-full px-4 py-3 bg-cream border-[3px] border-dark rounded-xl text-sm focus:border-primary outline-none transition-colors"
              >
                {quizSets.map((q) => (
                  <option key={q.id} value={q.id}>
                    {q.title} ({q.totalQuizCount}문제)
                  </option>
                ))}
              </select>
            </div>

            <div className="grid grid-cols-2 gap-3 mb-6">
              <div>
                <label className="block text-sm font-bold mb-2">최대 인원</label>
                <select
                  value={maxPlayers}
                  onChange={(e) => setMaxPlayers(Number(e.target.value))}
                  className="w-full px-4 py-3 bg-cream border-[3px] border-dark rounded-xl text-sm focus:border-primary outline-none transition-colors"
                >
                  {[2, 3, 4, 5, 6, 7, 8].map((n) => (
                    <option key={n} value={n}>{n}명</option>
                  ))}
                </select>
              </div>
              <div>
                <label className="block text-sm font-bold mb-2">문제 수</label>
                <input
                  type="number"
                  min={1}
                  max={quizSets.find((q) => q.id === selectedQuizSetId)?.totalQuizCount || 50}
                  value={maxQuizzes}
                  onChange={(e) => setMaxQuizzes(Number(e.target.value))}
                  className="w-full px-4 py-3 bg-cream border-[3px] border-dark rounded-xl text-sm focus:border-primary outline-none transition-colors"
                />
              </div>
            </div>

            <div className="flex gap-3">
              <button
                onClick={handleCreateRoom}
                disabled={creating}
                className="flex-1 py-4 bg-primary text-white font-bold text-lg border-[3px] border-dark rounded-xl shadow-kitsch hover:shadow-kitsch-lg hover:-translate-y-0.5 transition-all disabled:opacity-50"
              >
                {creating ? "생성 중..." : "방 만들기"}
              </button>
              <button
                onClick={() => setShowModal(false)}
                className="px-6 py-4 bg-white text-dark font-bold border-[3px] border-dark rounded-xl shadow-kitsch-sm hover:shadow-kitsch hover:-translate-y-0.5 transition-all"
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