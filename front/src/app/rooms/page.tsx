"use client";

import { useState, useEffect, useRef } from "react";
import Link from "next/link";
import api from "@/lib/api";
import Header from "@/components/common/Header";
import { Client } from "@stomp/stompjs";
import { useSearchParams } from "next/navigation";
import { Suspense } from "react";
import YouTube, { YouTubePlayer } from "react-youtube";
import { useRouter } from "next/navigation";

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
  score: number;
}

interface QuizSet {
  id: number;
  title: string;
  totalQuizCount: number;
  creatorNickname: string;
}

interface ChatMessage {
  sender: string;
  message: string;
  type: string;
}

interface QuizBroadcastResponse {
  questionId: number;
  questionType: string;
  answerType: string;
  content: string;
  choice1: string;
  choice2: string;
  choice3: string;
  choice4: string;
  videoUrl?: string;
  startTime?: number;
  endTime?: number;
  timeLimit: number;
}

const getYoutubeId = (url: string) => {
  if (!url) return null;
  const regExp = /(?:youtube\.com\/(?:[^\/]+\/.+\/|(?:v|e(?:mbed)?)\/|.*[?&]v=|shorts\/)|youtu\.be\/)([^"&?\/\s]{11})/i;
  const match = url.match(regExp);
  return match ? match[1] : null;
};

interface ScoreboardItem {
  username: string;
  score: number;
}

interface QuizResultResponse {
  correctAnswer: string;
  correctUsernames: string[];
  scoreboard: ScoreboardItem[];
}

type GameState = "waiting" | "playing" | "roundResult" | "result";
type ViewMode = "lobby" | "room";

function RoomsContent() {
  const router = useRouter();
  const [viewMode, setViewMode] = useState<ViewMode>("lobby");
  const [rooms, setRooms] = useState<Room[]>([]);
  const [rankings, setRankings] = useState<RankingItem[]>([]);
  const [filter, setFilter] = useState<string>("all");
  const [loading, setLoading] = useState(true);
  const [currentRoom, setCurrentRoom] = useState<any>(null);
  const [stompConnected, setStompConnected] = useState(false);
  const searchParams = useSearchParams();
  const [myNickname, setMyNickname] = useState<string | null>(null);
  const [myUserId, setMyUserId] = useState<string | null>(null);
  const [showQuizSetModal, setShowQuizSetModal] = useState(false);
  const [quizSetSearch, setQuizSetSearch] = useState("");
  const [quizSetTab, setQuizSetTab] = useState<"all" | "mine" | "bookmark" | "ai">("all");
  const [bookmarkedQuizSetIds, setBookmarkedQuizSetIds] = useState<Set<number>>(new Set());
  const [selectedQuizSet, setSelectedQuizSet] = useState<QuizSet | null>(null);
  const [myQuizSets, setMyQuizSets] = useState<QuizSet[]>([]);
  const [bookmarkedQuizSets, setBookmarkedQuizSets] = useState<QuizSet[]>([]);
  const [showReportModal, setShowReportModal] = useState(false);
  const [reportReason, setReportReason] = useState("");
  const [reporting, setReporting] = useState(false);
  const [useAiQuiz, setUseAiQuiz] = useState(false);
  const [aiTopic, setAiTopic] = useState("");
  const [aiGenerating, setAiGenerating] = useState(false);
  const [isStarting, setIsStarting] = useState(false);

  // 방 만들기 모달
  const [showModal, setShowModal] = useState(false);
  const [quizSets, setQuizSets] = useState<QuizSet[]>([]);
  const [roomName, setRoomName] = useState("");
  const [selectedQuizSetId, setSelectedQuizSetId] = useState<number | null>(null);
  const [maxPlayers, setMaxPlayers] = useState(4);
  const [maxQuizzes, setMaxQuizzes] = useState(10);
  const [createError, setCreateError] = useState("");
  const [creating, setCreating] = useState(false);

  // 게임 state
  const [gameState, setGameState] = useState<GameState>("waiting");
  const [currentQuestion, setCurrentQuestion] = useState<QuizBroadcastResponse | null>(null);
  const [roundResult, setRoundResult] = useState<QuizResultResponse | null>(null);
  const [scoreboard, setScoreboard] = useState<ScoreboardItem[]>([]);
  const [selectedAnswer, setSelectedAnswer] = useState<string | null>(null);
  const [shortAnswerInput, setShortAnswerInput] = useState("");
  const [answerSubmitted, setAnswerSubmitted] = useState(false);
  const [timeLeft, setTimeLeft] = useState(0);
  const [currentRound, setCurrentRound] = useState(0);
  const timerRef = useRef<NodeJS.Timeout | null>(null);

  // STOMP 채팅 state
  const stompClientRef = useRef<Client | null>(null);
  const [chatMessages, setChatMessages] = useState<ChatMessage[]>([]);
  const [chatInput, setChatInput] = useState("");
  const chatBottomRef = useRef<HTMLDivElement | null>(null);

  // 미디어 재생 state
  const playerRef = useRef<YouTubePlayer>(null);
  const [isPlayingMedia, setIsPlayingMedia] = useState(false);

  useEffect(() => {
    setMyNickname(localStorage.getItem("nickname"));
    setMyUserId(localStorage.getItem("userId"));
    const quizSetIdParam = searchParams.get("quizSetId");
    if (quizSetIdParam && quizSets.length === 0) {
      const openModalWithQuizSet = async () => {
        try {
          const res = await api.get("/quizsets?size=100");
          const content = res.data.data.content as QuizSet[];
          setQuizSets(content);
          const targetId = Number(quizSetIdParam);
          const target = content.find((q: QuizSet) => q.id === targetId);
          if (target) {
            setSelectedQuizSetId(target.id);
            setMaxQuizzes(target.totalQuizCount);
          }
          setShowModal(true);
        } catch (err) {
          console.error("퀴즈셋 조회 실패", err);
        }
      };
      openModalWithQuizSet();
    }
  }, [searchParams]);

  useEffect(() => {
    if (viewMode === "lobby") fetchLobbyData();
  }, [viewMode]);

  useEffect(() => {
    chatBottomRef.current?.scrollIntoView({ behavior: "smooth" });
  }, [chatMessages]);

  // 타이머
  useEffect(() => {
    if (gameState === "playing" && timeLeft > 0) {
      timerRef.current = setTimeout(() => setTimeLeft((t) => t - 1), 1000);
    }
    return () => { if (timerRef.current) clearTimeout(timerRef.current); };
  }, [gameState, timeLeft]);

  const fetchLobbyData = async () => {
    setLoading(true);
    try {
      const [roomsRes, rankingsRes] = await Promise.all([
        api.get("/rooms"),
        api.get("/rankings"),
      ]);
      setRooms(roomsRes.data.data);
      setRankings(rankingsRes.data.data.rankings?.slice(0, 5) || []);
    } catch (err) {
      console.error("로비 데이터 조회 실패", err);
    } finally {
      setLoading(false);
    }
  };

  const handleJoinRoom = async (gameSessionId: number) => {
    try {
      const res = await api.post(`/rooms/${gameSessionId}/join`);
      console.log("✅ join 응답:", res.data.data);

      // 퀴즈셋 정보 + 방 정보 병렬 조회
      const [quizSetRes, roomsRes] = await Promise.all([
        api.get(`/quizsets/${res.data.data.quizSetId}/info`),
        api.get("/rooms"),
      ]);

      const roomDetail = roomsRes.data.data.find((r: any) => r.gameSessionId === gameSessionId);

      const enrichedRoom = {
        ...res.data.data,
        quizSetTitle: quizSetRes.data.data.title,
        // maxPlayer, maxQuizzes는 이제 join 응답에서 바로 옴
      };

      setCurrentRoom(enrichedRoom);
      setGameState("waiting");
      setSelectedAnswer(null);
      setShortAnswerInput("");
      setAnswerSubmitted(false);
      setChatMessages([]);

      const client = new Client({
        brokerURL: process.env.NEXT_PUBLIC_WS_URL || "ws://localhost:8080/ws/game",
        reconnectDelay: 0,
        onConnect: () => {
          setStompConnected(true);
          client.subscribe(`/topic/rooms/${gameSessionId}/chat`, (msg) => {
            const data = JSON.parse(msg.body);
            console.log("📨 STOMP 메시지:", data);

            switch (data.type) {
              case "CHAT":
                setChatMessages((prev) => [...prev, {
                  sender: data.sender,
                  message: data.message,
                  type: "CHAT",
                }]);
                break;

              case "ENTER":
                setChatMessages((prev) => [...prev, {
                  sender: "SYSTEM",
                  message: data.message || `${data.sender}님이 입장했습니다.`,
                  type: "SYSTEM",
                }]);
                if (data.data) setCurrentRoom((prev: any) => ({ ...prev, ...data.data }));
                break;

              case "LEAVE":
                setChatMessages((prev) => [...prev, {
                  sender: "SYSTEM",
                  message: data.message,
                  type: "SYSTEM",
                }]);
                if (data.data?.players) {
                  setCurrentRoom((prev: any) => ({ ...prev, players: data.data.players }));
                }
                break;

              case "ROOM_ENDED":
                alert("방장이 나가 방이 삭제되었습니다.");
                stompClientRef.current?.deactivate();
                stompClientRef.current = null;
                setStompConnected(false);
                setChatMessages([]);
                setCurrentRoom(null);
                setViewMode("lobby");
                break;

              case "QUIZ":
                setCurrentQuestion(data.data);
                setCurrentRound((prev) => prev + 1);
                setSelectedAnswer(null);
                setShortAnswerInput("");
                setAnswerSubmitted(false);
                setIsPlayingMedia(false);
                playerRef.current = null;
                setTimeLeft(data.data.timeLimit);
                setGameState("playing");
                setChatMessages((prev) => [...prev, {
                  sender: "SYSTEM",
                  message: data.message,
                  type: "SYSTEM",
                }]);
                break;

              case "RESULT":
                setRoundResult(data.data);
                setScoreboard(data.data.scoreboard);
                setGameState("roundResult");
                setChatMessages((prev) => [...prev, {
                  sender: "SYSTEM",
                  message: `정답: ${data.data.correctAnswer}`,
                  type: "SYSTEM",
                }]);
                break;

              case "QUIZ_END":
                setGameState("result");
                setChatMessages((prev) => [...prev, {
                  sender: "SYSTEM",
                  message: data.message,
                  type: "SYSTEM",
                }]);
                break;

              case "ERROR":
                setChatMessages((prev) => [...prev, {
                  sender: "SYSTEM",
                  message: `⚠️ ${data.message}`,
                  type: "SYSTEM",
                }]);
                break;
            }
          });
        },
        onStompError: (frame) => console.error("❌ STOMP 에러:", frame),
        onWebSocketError: (event) => console.error("❌ WebSocket 에러:", event),
        onDisconnect: () => setStompConnected(false),
      });
      stompClientRef.current = client;
      client.activate();
      setViewMode("room");
    } catch (err: any) {
      console.error("방 입장 실패", err.response?.data);
    }
  };

  const handleLeaveRoom = async () => {
    if (!currentRoom) return;
    try {
      await api.delete(`/rooms/${currentRoom.gameSessionId}/leave`);
    } catch (err) {
      console.error("퇴장 실패", err);
    }
    stompClientRef.current?.deactivate();
    stompClientRef.current = null;
    setStompConnected(false);
    setChatMessages([]);
    setCurrentRoom(null);
    setCurrentQuestion(null);
    setRoundResult(null);
    setScoreboard([]);
    setCurrentRound(0);
    setViewMode("lobby");
  };

  const handleSelectQuizSet = (quiz: QuizSet) => {
    setSelectedQuizSet(quiz);
    setSelectedQuizSetId(quiz.id);
    setMaxQuizzes(quiz.totalQuizCount);
    setShowQuizSetModal(false);
    setQuizSetSearch("");
  };

  const handleStartGame = () => {
    if (isStarting || !stompClientRef.current?.connected || !currentRoom) return;
    setIsStarting(true);
    stompClientRef.current.publish({
      destination: `/app/rooms/${currentRoom.gameSessionId}/start`,
      body: "",
    });
    setTimeout(() => setIsStarting(false), 3000);
  };

  const handleSubmitAnswer = (answer: string) => {
    if (answerSubmitted || !stompClientRef.current?.connected || !currentRoom) return;
    setSelectedAnswer(answer);
    setAnswerSubmitted(true);
    stompClientRef.current.publish({
      destination: `/app/rooms/${currentRoom.gameSessionId}/answer`,
      body: JSON.stringify({ answer }),
    });
  };

  const sendChat = () => {
    if (!chatInput.trim() || !stompClientRef.current?.connected || !currentRoom) return;
    stompClientRef.current.publish({
      destination: `/app/rooms/${currentRoom.gameSessionId}/chat`,
      body: JSON.stringify({ message: chatInput }),
    });
    setChatInput("");
  };

  const handleOpenModal = async () => {
    const userId = localStorage.getItem("userId");
    const nickname = localStorage.getItem("nickname");
    try {
      const promises: Promise<any>[] = [api.get("/quizsets")];
      if (userId) {
        promises.push(api.get(`/users/${userId}/bookmarks`));
      }
      const [quizSetsRes, bookmarksRes] = await Promise.all(promises);
      const allQuizSets: QuizSet[] = quizSetsRes.data.data.content ?? quizSetsRes.data.data;
      setQuizSets(allQuizSets);
      setMyQuizSets(allQuizSets.filter((q: any) => q.creatorNickname === nickname));

      if (bookmarksRes) {
        const bookmarkIds = bookmarksRes.data.data.map((b: any) => b.quizSetId);
        setBookmarkedQuizSetIds(new Set(bookmarkIds));
        const bookmarked = allQuizSets.filter((q: QuizSet) => bookmarkIds.includes(q.id));
        setBookmarkedQuizSets(bookmarked);
      }

      if (allQuizSets.length > 0 && !selectedQuizSet) {
        setSelectedQuizSet(allQuizSets[0]);
        setSelectedQuizSetId(allQuizSets[0].id);
        setMaxQuizzes(allQuizSets[0].totalQuizCount);
      }
    } catch (err) {
      console.error("퀴즈셋 조회 실패", err);
    }
    setShowModal(true);
  };

  const handleQuizSetChange = (quizSetId: number) => {
    setSelectedQuizSetId(quizSetId);
    const selected = quizSets.find((q) => q.id === quizSetId);
    if (selected) setMaxQuizzes(selected.totalQuizCount);
  };

  const handleCreateRoom = async () => {
    setCreateError("");
    if (!roomName.trim()) { setCreateError("방 제목을 입력하세요."); return; }
    if (!selectedQuizSetId) { setCreateError("퀴즈셋을 선택하거나 AI로 생성하세요."); return; }
    if (maxQuizzes < 5) { setCreateError("문제 수는 최소 5개 이상이어야 합니다."); return; }
    setCreating(true);
    try {
      const res = await api.post("/rooms", { roomName, quizSetId: selectedQuizSetId, maxPlayers, maxQuizzes });
      const gameSessionId = res.data.data.gameSessionId;
      setShowModal(false);
      setRoomName("");
      await handleJoinRoom(gameSessionId);
    } catch (err: any) {
      setCreateError(err.response?.data?.message || "방 생성에 실패했습니다.");
    } finally {
      setCreating(false);
    }
  };

  const handleReport = async () => {
    if (!reportReason.trim()) return;
    setReporting(true);
    try {
      await api.post("/reports", {
        quizSetId: currentRoom?.quizSetId,
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

  const handleAiGenerate = async () => {
    if (!roomName.trim()) { setCreateError("방 제목을 입력하세요."); return; }
    if (!aiTopic.trim()) { setCreateError("주제를 입력하세요."); return; }
    setCreateError("");
    setAiGenerating(true);
    try {
      const res = await api.post(`/ai/quizzes?topic=${encodeURIComponent(aiTopic)}`);
      const quizSetId = res.data.data.quizSetId;
      setSelectedQuizSetId(quizSetId);
      setSelectedQuizSet({ id: quizSetId, title: `[AI] ${aiTopic}`, totalQuizCount: 5 } as any);
      // 바로 방 생성
      const roomRes = await api.post("/rooms", { roomName, quizSetId, maxPlayers, maxQuizzes: 5 });
      const gameSessionId = roomRes.data.data.gameSessionId;
      setShowModal(false);
      setRoomName("");
      await handleJoinRoom(gameSessionId);
    } catch (err: any) {
      setCreateError(err.response?.data?.message || "입력한 주제로는 퀴즈를 생성할 수 없습니다.");
    }
  };

  const filteredRooms = rooms.filter((room) => {
    if (room.status === "END") return false;
    if (filter === "waiting") return room.status === "WAIT";
    if (filter === "playing") return room.status === "START";
    return true;
  });

  const rankColors = ["text-primary", "text-accent", "text-secondary", "text-gray-400", "text-gray-400"];
  const choiceColors = [
    { base: "border-dark bg-white", hover: "hover:bg-primary hover:text-white hover:border-primary", selected: "bg-primary text-white border-primary" },
    { base: "border-dark bg-white", hover: "hover:bg-accent hover:text-white hover:border-accent", selected: "bg-accent text-white border-accent" },
    { base: "border-dark bg-white", hover: "hover:bg-secondary hover:border-secondary", selected: "bg-secondary border-secondary" },
    { base: "border-dark bg-white", hover: "hover:bg-purple-500 hover:text-white hover:border-purple-500", selected: "bg-purple-500 text-white border-purple-500" },
  ];

  // ========== 방 화면 ==========
  if (viewMode === "room") {
    const roomInfo = currentRoom;
    const hostNickname = currentRoom?.players?.find((p: any) => p.isHost)?.nickname;
    const choices = currentQuestion
      ? [currentQuestion.choice1, currentQuestion.choice2, currentQuestion.choice3, currentQuestion.choice4]
      : [];

    return (
      <div>
        {gameState === "waiting" && (
          <div className="border-b-[3px] border-dark bg-white px-6 py-3 flex items-center justify-between">
            <span className="font-title text-xl">답정너</span>
            <button onClick={handleLeaveRoom} className="px-4 py-2 bg-white text-dark font-bold border-[3px] border-dark rounded-xl shadow-kitsch-sm hover:shadow-kitsch hover:-translate-y-0.5 transition-all text-sm">
              ← 나가기
            </button>
          </div>
        )}

        <div className="max-w-6xl mx-auto px-4 py-6">

          {/* ========== 대기실 ========== */}
          {gameState === "waiting" && (
            <div className="flex gap-6">
              <div className="flex-1">
                <div className="bg-white border-[3px] border-dark rounded-2xl shadow-kitsch p-6 mb-4">
                  <div className="flex items-center justify-between mb-4">
                    <div>
                      <h1 className="font-title text-3xl mb-1">{roomInfo?.roomName || "대기실"}</h1>
                      <p className="text-sm text-gray-400">{roomInfo?.quizSetTitle}</p>
                    </div>
                    <p className="font-title text-4xl text-primary">{roomInfo?.players?.length ?? 0}명 참여중</p>
                  </div>
                  <div className="w-full h-4 bg-gray-200 rounded-full border-2 border-dark">
                    <div
                      className="h-full bg-primary rounded-full"
                      style={{ width: `${Math.min((roomInfo?.players?.length ?? 0) / (roomInfo?.maxPlayers || 4) * 100, 100)}%` }}
                    />
                  </div>
                </div>

                <div className="grid grid-cols-4 gap-3 mb-4">
                  {(roomInfo?.players || []).map((p: any, i: number) => (
                    <div
                      key={`player-${p.nickname || i}`}
                      className={`flex flex-col items-center p-5 border-[3px] rounded-2xl text-center ${p.isHost ? "border-primary bg-primary/5 shadow-kitsch" : "border-dark bg-white shadow-kitsch-sm"}`}
                    >
                      <div className={`w-14 h-14 rounded-full border-[3px] flex items-center justify-center font-title text-xl mb-2 ${p.isHost ? "bg-primary text-white border-dark" : "bg-cream border-dark"}`}>
                        {p.nickname.charAt(0)}
                      </div>
                      <p className="font-bold text-sm mb-1">{p.nickname}</p>
                      {p.isHost && <span className="text-sm text-primary font-bold">방장</span>}
                      {p.nickname === myNickname && <span className="text-sm text-accent font-bold">나</span>}
                    </div>
                  ))}
                  {Array.from({ length: (roomInfo?.maxPlayers || 4) - (roomInfo?.players?.length || 0) }).map((_, i) => (
                    <div key={`empty-${i}`} className="flex flex-col items-center justify-center p-5 border-[3px] border-dashed border-gray-300 rounded-2xl">
                      <div className="w-14 h-14 rounded-full bg-gray-100 border-2 border-dashed border-gray-300 flex items-center justify-center mb-2">
                        <span className="text-gray-300 text-xl">?</span>
                      </div>
                      <p className="text-xs text-gray-300 font-bold">대기중</p>
                    </div>
                  ))}
                </div>

                <div className="flex gap-3">
                  {hostNickname === myNickname ? (
                    <button
                      onClick={handleStartGame}
                      disabled={!stompConnected || (roomInfo?.players?.length ?? 0) < 2 || isStarting}
                      className="flex-1 py-4 bg-primary text-white font-bold text-lg border-[3px] border-dark rounded-2xl shadow-kitsch hover:shadow-kitsch-lg hover:-translate-y-0.5 transition-all disabled:opacity-50"
                    >
                      {isStarting ? "시작 중..." : (roomInfo?.players?.length ?? 0) < 2 ? "2명 이상이어야 시작할 수 있어요" : "🎮 게임 시작"}
                    </button>
                  ) : (
                    <div className="flex-1 py-4 bg-cream border-[3px] border-dark rounded-2xl text-center font-bold text-gray-400">
                      방장이 게임을 시작할 때까지 기다려주세요...
                    </div>
                  )}
                  <button onClick={handleLeaveRoom} className="px-8 py-4 bg-white text-dark font-bold border-[3px] border-dark rounded-2xl shadow-kitsch-sm hover:shadow-kitsch hover:-translate-y-0.5 transition-all">
                    나가기
                  </button>
                </div>
              </div>

              {/* 채팅 */}
              <div className="w-80 flex flex-col gap-4">
                <div className="bg-white border-[3px] border-dark rounded-2xl shadow-kitsch p-5">
                  <h3 className="font-title text-lg mb-3">퀴즈 정보</h3>
                  <div className="flex flex-col gap-2 text-sm">
                    <div className="flex justify-between"><span className="text-gray-400">퀴즈셋</span><span className="font-bold">{roomInfo?.quizSetTitle}</span></div>
                    <div className="flex justify-between"><span className="text-gray-400">문제 수</span><span className="font-bold">{roomInfo?.maxQuizzes}문제</span></div>
                    <div className="flex justify-between"><span className="text-gray-400">최대 인원</span><span className="font-bold">{roomInfo?.maxPlayers}명</span></div>
                  </div>
                </div>
                <div className="bg-white border-[3px] border-dark rounded-2xl shadow-kitsch flex-1 flex flex-col overflow-hidden" style={{ maxHeight: "400px" }}>
                  <h3 className="font-title text-lg p-5 pb-3">💬 채팅</h3>
                  <div className="flex-1 px-5 overflow-y-auto flex flex-col gap-3">
                    {chatMessages.map((msg, i) => (
                      <div key={`chat-${i}`}>
                        {msg.type === "SYSTEM" ? (
                          <p className="text-xs text-gray-400 text-center py-1">{msg.message}</p>
                        ) : (
                          <>
                            <div className="flex items-center gap-2 mb-1">
                              <span className="text-xs font-bold">{msg.sender}</span>
                              <span className="text-xs text-gray-300">{new Date().toLocaleTimeString("ko-KR", { hour: "2-digit", minute: "2-digit" })}</span>
                            </div>
                            <p className="text-sm bg-cream rounded-xl px-3 py-2 inline-block border border-gray-200">{msg.message}</p>
                          </>
                        )}
                      </div>
                    ))}
                    <div ref={chatBottomRef} />
                  </div>
                  <div className="p-4 border-t-[3px] border-dark">
                    <div className="flex flex-row items-center gap-2">
                      <input
                        type="text"
                        value={chatInput}
                        onChange={(e) => setChatInput(e.target.value)}
                        onKeyDown={(e) => e.key === "Enter" && sendChat()}
                        placeholder="메시지 입력..."
                        className="flex-1 px-3 py-2 bg-cream border-[3px] border-dark rounded-xl text-sm focus:border-primary outline-none"
                      />
                      <button onClick={sendChat} disabled={!stompConnected} className="shrink-0 px-4 py-2 bg-primary text-white border-[3px] border-dark rounded-xl font-bold text-sm shadow-kitsch-sm disabled:opacity-40">
                        전송
                      </button>
                    </div>
                    {!stompConnected && <p className="text-xs text-gray-400 mt-1">채팅 연결 중...</p>}
                  </div>
                </div>
              </div>
            </div>
          )}

          {/* ========== 게임 진행 ========== */}
          {gameState === "playing" && currentQuestion && (
            <div className="flex gap-6">
              <div className="flex-1">
                <div className="flex items-center justify-between mb-6">
                  <span className="px-4 py-2 bg-white border-[3px] border-dark rounded-xl font-title text-lg shadow-kitsch-sm">
                    Q{currentRound}
                  </span>
                  <div className="flex items-center gap-3">
                    <div className="w-48 h-4 bg-gray-200 rounded-full border-2 border-dark overflow-hidden">
                      <div
                        className={`h-full rounded-full transition-all duration-1000 ${timeLeft <= 5 ? "bg-red-500" : "bg-accent"}`}
                        style={{ width: `${(timeLeft / currentQuestion.timeLimit) * 100}%` }}
                      />
                    </div>
                    <span className={`font-title text-2xl ${timeLeft <= 5 ? "text-red-500" : "text-dark"}`}>{timeLeft}초</span>
                  </div>
                </div>

                <div className="bg-white border-[3px] border-dark rounded-2xl shadow-kitsch p-10 mb-6 text-center">
                  <h2 className="font-title text-3xl">{currentQuestion.content}</h2>
                  {(currentQuestion.questionType === "VIDEO" || currentQuestion.questionType === "AUDIO") && currentQuestion.videoUrl && (
                    <div className="mt-6 flex flex-col justify-center items-center">
                      {getYoutubeId(currentQuestion.videoUrl) ? (
                        <>
                          <div className={`relative ${currentQuestion.questionType === "AUDIO" ? "w-0 h-0 overflow-hidden opacity-0" : "w-full max-w-2xl bg-black rounded-xl border-[3px] border-dark overflow-hidden aspect-video pointer-events-none select-none"}`}>
                            <YouTube
                              videoId={getYoutubeId(currentQuestion.videoUrl)!}
                              opts={{
                                width: "100%",
                                height: "100%",
                                playerVars: {
                                  autoplay: 1,
                                  controls: 0,
                                  disablekb: 1,
                                  fs: 0,
                                  iv_load_policy: 3,
                                  start: currentQuestion.startTime || 0,
                                  ...(currentQuestion.endTime ? { end: currentQuestion.endTime } : {}),
                                  rel: 0,
                                  modestbranding: 1,
                                  origin: typeof window !== "undefined" ? window.location.origin : undefined,
                                },
                              }}
                              onReady={(e) => {
                                playerRef.current = e.target;
                                e.target.playVideo();
                              }}
                              onStateChange={(e) => {
                                // 1: PLAYING, 기타: buffering, paused 등
                                if (e.data === 1) setIsPlayingMedia(true);
                                else setIsPlayingMedia(false);
                              }}
                              className="w-full h-full pointer-events-none"
                              iframeClassName="w-full h-full pointer-events-none"
                            />
                          </div>
                      
                      {/* 자동재생이 차단되었을 때 띄워주는 직접 재생 UI */}
                      {!isPlayingMedia && (
                        <div className="mt-4 p-6 bg-cream border-[3px] border-dark border-dashed rounded-2xl shadow-kitsch-sm flex flex-col items-center">
                          <p className="font-title text-lg text-primary mb-3">
                            브라우저 정책으로 미디어 자동 재생이 정지되었습니다.
                          </p>
                          <button
                            onClick={() => {
                              if (playerRef.current) {
                                playerRef.current.playVideo();
                                setIsPlayingMedia(true);
                              }
                            }}
                            className="px-6 py-3 bg-accent text-white font-bold text-lg border-[3px] border-dark rounded-xl shadow-kitsch-sm hover:-translate-y-0.5 hover:shadow-kitsch transition-all"
                          >
                            ▶ 수동 재생하기
                          </button>
                        </div>
                      )}

                      {currentQuestion.questionType === "AUDIO" && isPlayingMedia && (
                        <div className="text-center p-8 bg-cream border-[3px] border-dark border-dashed rounded-2xl shadow-kitsch-sm mt-4">
                          <span className="text-6xl mb-4 block animate-bounce">🎶</span>
                          <p className="font-title text-xl text-primary">소리를 듣고 정답을 맞춰주세요!</p>
                        </div>
                      )}
                    </>
                  ) : (
                    <div className="mt-6 p-6 bg-cream border-[3px] border-red-400 border-dashed rounded-2xl flex flex-col items-center">
                      <p className="font-title text-lg text-red-500">유효하지 않은 유튜브 링크입니다.</p>
                      <p className="text-sm text-gray-500 mt-2">({currentQuestion.videoUrl})</p>
                    </div>
                  )}
                    </div>
                  )}
                </div>

                {currentQuestion.answerType === "SHORT_ANSWER" ? (
                  <div className="flex gap-3 max-w-xl mx-auto">
                    <input
                      type="text"
                      placeholder="정답을 입력하세요"
                      value={shortAnswerInput}
                      onChange={(e) => setShortAnswerInput(e.target.value)}
                      onKeyDown={(e) => e.key === "Enter" && handleSubmitAnswer(shortAnswerInput)}
                      disabled={answerSubmitted}
                      className="flex-1 px-6 py-4 bg-white border-[3px] border-dark rounded-2xl text-xl font-bold focus:border-primary outline-none transition-colors"
                    />
                    <button
                      onClick={() => handleSubmitAnswer(shortAnswerInput)}
                      disabled={answerSubmitted || !shortAnswerInput.trim()}
                      className="px-8 py-4 bg-primary text-white font-bold text-xl border-[3px] border-dark rounded-2xl shadow-kitsch hover:shadow-kitsch-lg hover:-translate-y-0.5 transition-all disabled:opacity-50"
                    >
                      제출
                    </button>
                  </div>
                ) : (
                  <div className="grid grid-cols-2 gap-4">
                    {choices.map((choice, i) => {
                      const isSelected = selectedAnswer === choice;
                      return (
                        <button
                          key={i}
                          onClick={() => handleSubmitAnswer(choice)}
                          disabled={answerSubmitted}
                          className={`p-6 border-[3px] rounded-2xl font-bold text-lg shadow-kitsch-sm transition-all hover:-translate-y-0.5 hover:shadow-kitsch disabled:cursor-not-allowed ${isSelected ? choiceColors[i].selected : `${choiceColors[i].base} ${choiceColors[i].hover}`
                            }`}
                        >
                          <span className="font-title mr-2">{i + 1}.</span>
                          {choice}
                        </button>
                      );
                    })}
                  </div>
                )}
                {answerSubmitted && (
                  <p className="text-center font-hand text-lg text-accent mt-4">정답을 제출했습니다! 결과를 기다려주세요...</p>
                )}
              </div>

              <div className="w-72">
                <div className="bg-white border-[3px] border-dark rounded-2xl shadow-kitsch p-5">
                  <h3 className="font-title text-lg mb-4">실시간 순위</h3>
                  <div className="flex flex-col gap-3">
                    {scoreboard.map((p, i) => (
                      <div key={p.username} className={`flex items-center gap-3 p-3 rounded-xl ${i === 0 ? "bg-primary/10 border-2 border-primary" : "bg-cream"}`}>
                        <span className={`font-title text-xl w-6 ${i === 0 ? "text-primary" : i === 1 ? "text-accent" : i === 2 ? "text-secondary" : "text-gray-400"}`}>{i + 1}</span>
                        <div className="w-8 h-8 rounded-full bg-cream border-2 border-dark flex items-center justify-center text-xs font-bold">{p.username.charAt(0)}</div>
                        <div className="flex-1">
                          <p className="font-bold text-sm">{p.username}</p>
                        </div>
                        <span className="font-title text-lg">{p.score.toLocaleString()}</span>
                      </div>
                    ))}
                  </div>
                </div>
              </div>
            </div>
          )}

          {/* ========== 라운드 결과 ========== */}
          {gameState === "roundResult" && roundResult && (
            <div className="max-w-2xl mx-auto">
              <div className="bg-white border-[3px] border-dark rounded-2xl shadow-kitsch p-8 mb-6 text-center">
                <h2 className="font-title text-2xl mb-2">라운드 결과</h2>
                <p className="font-hand text-lg text-gray-400 mb-4">정답: <span className="text-accent font-bold">{roundResult.correctAnswer}</span></p>
                {roundResult.correctUsernames.length > 0 ? (
                  <p className="text-sm font-bold text-primary">{roundResult.correctUsernames.join(", ")}님이 맞추셨습니다! 🎉</p>
                ) : (
                  <p className="text-sm font-bold text-gray-400">아무도 맞추지 못했습니다.</p>
                )}
              </div>

              <div className="bg-white border-[3px] border-dark rounded-2xl shadow-kitsch p-6">
                <h3 className="font-title text-lg mb-4">현재 순위</h3>
                <div className="flex flex-col gap-3">
                  {roundResult.scoreboard.map((p, i) => (
                    <div key={p.username} className={`flex items-center gap-3 p-3 rounded-xl ${p.username === myNickname ? "bg-secondary/20 border-2 border-secondary" : "bg-cream"}`}>
                      <span className={`font-title text-xl w-8 ${i === 0 ? "text-primary" : i === 1 ? "text-accent" : i === 2 ? "text-secondary" : "text-gray-400"}`}>{i + 1}</span>
                      <div className="w-8 h-8 rounded-full bg-white border-2 border-dark flex items-center justify-center text-xs font-bold">{p.username.charAt(0)}</div>
                      <p className="flex-1 font-bold text-sm">{p.username}</p>
                      <span className="font-title text-lg">{p.score.toLocaleString()}</span>
                    </div>
                  ))}
                </div>
              </div>
              <p className="text-center font-hand text-gray-400 mt-4">다음 문제를 기다리는 중...</p>
            </div>
          )}

          {/* ========== 최종 결과 ========== */}
          {gameState === "result" && (
            <div className="max-w-3xl mx-auto">
              {/* 우측 상단 신고 버튼 */}
              <div className="flex justify-end mb-4">
                <button
                  onClick={() => setShowReportModal(true)}
                  className="px-4 py-2 bg-white text-red-400 border-2 border-red-200 rounded-xl text-sm font-bold hover:border-red-400 hover:text-red-500 transition-all"
                >
                  🚨 신고
                </button>
              </div>
              

              <div className="text-center mb-10">
                <h1 className="font-title text-4xl mb-2">🎉 게임 종료!</h1>
              </div>

              {scoreboard.length > 0 && (
                <>
                  <div className="bg-white border-[3px] border-primary rounded-2xl shadow-kitsch p-8 mb-6 text-center">
                    <div className="text-4xl mb-2">👑</div>
                    <div className="w-20 h-20 mx-auto rounded-full border-[4px] border-primary bg-primary/10 flex items-center justify-center mb-3">
                      <span className="font-title text-3xl text-primary">{scoreboard[0].username.charAt(0)}</span>
                    </div>
                    <h2 className="font-title text-2xl mb-1">{scoreboard[0].username}</h2>
                    <p className="font-title text-4xl text-primary">{scoreboard[0].score.toLocaleString()}점</p>
                  </div>

                  <div className="bg-white border-[3px] border-dark rounded-2xl shadow-kitsch overflow-hidden mb-8">
                    {scoreboard.slice(1).map((p, i) => (
                      <div key={p.username} className={`flex items-center px-6 py-5 border-b-2 border-dashed border-gray-200 last:border-b-0 ${p.username === myNickname ? "bg-secondary/20" : ""}`}>
                        <span className={`font-title text-2xl w-10 ${i + 2 === 2 ? "text-accent" : i + 2 === 3 ? "text-secondary" : "text-gray-400"}`}>{i + 2}</span>
                        <div className="w-12 h-12 rounded-full bg-cream border-2 border-dark flex items-center justify-center font-bold mr-4">{p.username.charAt(0)}</div>
                        <p className="flex-1 font-bold">{p.username}</p>
                        <p className="font-title text-xl">{p.score.toLocaleString()}점</p>
                      </div>
                    ))}
                  </div>
                </>
              )}

              <div className="flex gap-3">
                <button
                  onClick={() => {
                    setGameState("waiting");
                    setCurrentQuestion(null);
                    setRoundResult(null);
                    setScoreboard([]);
                    setCurrentRound(0);
                    setIsStarting(false);
                  }}
                  className="flex-1 py-4 bg-primary text-white font-bold text-lg border-[3px] border-dark rounded-2xl shadow-kitsch hover:shadow-kitsch-lg hover:-translate-y-0.5 transition-all"
                >
                  한 판 더!
                </button>
                <button onClick={handleLeaveRoom} className="flex-1 py-4 bg-white text-dark font-bold text-lg border-[3px] border-dark rounded-2xl shadow-kitsch-sm hover:shadow-kitsch hover:-translate-y-0.5 transition-all text-center">
                  로비로 돌아가기
                </button>
              </div>
            </div>
          )}
        </div>
        {/* 신고 모달 */}
        {showReportModal && (
          <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50">
            <div className="bg-white border-[3px] border-dark rounded-2xl shadow-kitsch-lg p-8 w-full max-w-md">
              <h2 className="font-title text-2xl mb-2">퀴즈셋 신고</h2>
              <p className="text-sm text-gray-400 mb-6">{currentRoom?.quizSetTitle}</p>
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
  // ========== 로비 화면 ==========
  if (loading) {
    return (
      <div className="max-w-7xl mx-auto px-4 py-10 text-center">
        <p className="font-hand text-xl text-gray-400">로비 불러오는 중...</p>
      </div>
    );
  }

  return (
    <>
      <Header />
      <div className="max-w-7xl mx-auto px-4 py-10 flex gap-6">
        <div className="flex-1">
          <div className="mb-6">
            <h1 className="font-title text-4xl mb-1">게임 대기실</h1>
            <p className="font-hand text-lg text-gray-400">참여할 퀴즈방을 선택하세요</p>
            <button onClick={fetchLobbyData} className="font-title text-gray-400">(새로고침)</button>
          </div>

          <div className="flex gap-2 mb-6">
            {[{ key: "all", label: "전체" }, { key: "waiting", label: "대기중" }, { key: "playing", label: "게임중" }].map((f) => (
              <button
                key={f.key}
                onClick={() => setFilter(f.key)}
                className={`px-5 py-2 border-[3px] border-dark rounded-full font-bold text-sm shadow-kitsch-sm transition-all ${filter === f.key ? "bg-secondary" : "bg-white hover:bg-gray-50"}`}
              >
                {f.label}
              </button>
            ))}
          </div>

          <div className="flex flex-col gap-3 mb-6">
            {filteredRooms.length === 0 ? (
              <div className="bg-white border-[3px] border-dark rounded-2xl p-10 shadow-kitsch text-center">
                <p className="font-hand text-lg text-gray-400">방이 없어요. 새로 만들어보세요!</p>
              </div>
            ) : (
              filteredRooms.map((room) => (
                <div key={room.gameSessionId} className="flex items-center bg-white border-[3px] border-dark rounded-2xl px-6 py-4 shadow-kitsch hover:shadow-kitsch-lg hover:-translate-y-0.5 transition-all cursor-pointer">
                  <div className="flex-1">
                    <h3 className="font-bold mb-1">{room.roomName}</h3>
                    <p className="text-xs text-gray-400">{room.quizSetTitle} · {room.hostNickname}</p>
                  </div>
                  <div className="flex items-center gap-4">
                    <div className="text-right">
                      <p className="font-title text-xl text-primary">{room.currentPlayerCount}/{room.maxPlayer}</p>
                      <div className="w-16 h-2 bg-gray-200 rounded-full mt-1">
                        <div className="h-full bg-primary rounded-full" style={{ width: `${(room.currentPlayerCount / room.maxPlayer) * 100}%` }} />
                      </div>
                    </div>
                    {room.status === "WAIT" ? (
                      <button onClick={() => handleJoinRoom(room.gameSessionId)} className="px-4 py-2 bg-primary text-white border-[3px] border-dark rounded-xl text-sm font-bold shadow-kitsch-sm hover:shadow-kitsch hover:-translate-y-0.5 transition-all">
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

          <button onClick={handleOpenModal} className="w-full py-4 bg-accent text-white font-bold text-lg border-[3px] border-dark rounded-2xl shadow-kitsch hover:shadow-kitsch-lg hover:-translate-y-0.5 transition-all">
            + 새 방 만들기
          </button>
        </div>

        <div className="w-80 flex flex-col gap-4">
          <div className="bg-white border-[3px] border-dark rounded-2xl p-6 shadow-kitsch">
            <h3 className="font-title text-lg mb-4">🏆 TOP 5</h3>
            <div className="flex flex-col gap-3">
              {rankings.map((r, i) => (
                <div key={`rank-${r.rank}-${i}`} className="flex items-center gap-3">
                  <span className={`font-title text-xl w-8 ${rankColors[i]}`}>{r.rank}</span>
                  <span className="flex-1 text-sm font-bold truncate">{r.nickname}</span>
                  <span className="text-sm text-gray-400">{r.score.toLocaleString()}P</span>
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
              <Link href={`/users/${myUserId}`} className="w-full py-3 bg-cream border-[3px] border-dark rounded-xl text-sm font-bold shadow-kitsch-sm hover:shadow-kitsch hover:-translate-y-0.5 transition-all text-center">
                👤 마이페이지
              </Link>
            </div>
          </div>
        </div>

        {showModal && (
          <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50">
            <div className="bg-white border-[3px] border-dark rounded-2xl shadow-kitsch-lg p-8 w-full max-w-md">
              <h2 className="font-title text-2xl mb-6">새 방 만들기</h2>
              {createError && (
                <div className="mb-4 px-4 py-3 bg-red-50 border-2 border-red-300 rounded-xl text-sm text-red-600 font-bold">{createError}</div>
              )}
              <div className="mb-4">
                <label className="block text-sm font-bold mb-2">방 제목</label>
                <input type="text" placeholder="방 제목을 입력하세요" value={roomName} onChange={(e) => { setRoomName(e.target.value); setCreateError(""); }} className="w-full px-4 py-3 bg-cream border-[3px] border-dark rounded-xl text-sm focus:border-primary outline-none transition-colors" />
              </div>
              <div className="mb-4">
                <label className="block text-sm font-bold mb-2">퀴즈셋</label>
                <button
                  onClick={() => setShowQuizSetModal(true)}
                  className="w-full px-4 py-3 bg-cream border-[3px] border-dark rounded-xl text-sm text-left hover:border-primary transition-colors"
                >
                  {selectedQuizSet
                    ? `${selectedQuizSet.title} (${selectedQuizSet.totalQuizCount}문제)`
                    : "퀴즈셋을 선택하세요"}
                </button>
              </div>
              {showQuizSetModal && (
                <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-[60]">
                  <div className="bg-white border-[3px] border-dark rounded-2xl shadow-kitsch-lg p-6 w-full max-w-lg max-h-[80vh] flex flex-col">
                    <div className="flex items-center justify-between mb-4">
                      <h3 className="font-title text-xl">퀴즈셋 선택</h3>
                      {createError && (
                        <div className="mb-4 px-4 py-3 bg-red-50 border-2 border-red-300 rounded-xl text-sm text-red-600 font-bold">
                          {createError}
                        </div>
                      )}
                      <button
                        onClick={() => { setShowQuizSetModal(false); setQuizSetSearch(""); }}
                        className="text-gray-400 hover:text-dark font-bold text-lg"
                      >
                        ✕
                      </button>
                    </div>
                    {/* AI 퀴즈 생성 */}
                    <div className="mb-4">
                      <label className="block text-sm font-bold mb-2">
                        <input
                          type="checkbox"
                          checked={useAiQuiz}
                          onChange={(e) => {
                            setUseAiQuiz(e.target.checked);
                            if (e.target.checked) {
                              setSelectedQuizSetId(null);
                              setSelectedQuizSet(null);
                            }
                          }}
                          className="mr-2"
                        />
                        🤖 AI로 퀴즈 생성하기
                      </label>
                      {useAiQuiz && (
                        <div className="flex gap-2">
                          <input
                            type="text"
                            placeholder="주제를 입력하세요 (예: 한국사, 축구, 과학)"
                            value={aiTopic}
                            onChange={(e) => { setAiTopic(e.target.value); setCreateError(""); }}
                            className="flex-1 px-4 py-3 bg-cream border-[3px] border-dark rounded-xl text-sm focus:border-primary outline-none"
                          />
                          <button
                            onClick={handleAiGenerate}
                            disabled={aiGenerating || !aiTopic.trim()}
                            className="px-4 py-3 bg-secondary font-bold border-[3px] border-dark rounded-xl text-sm shadow-kitsch disabled:opacity-50"
                          >
                            {aiGenerating ? "생성 중..." : "생성"}
                          </button>
                        </div>
                      )}
                    </div>

                    {/* 검색 */}
                    <input
                      type="text"
                      placeholder="퀴즈셋 검색..."
                      value={quizSetSearch}
                      onChange={(e) => setQuizSetSearch(e.target.value)}
                      className="w-full px-4 py-2 bg-cream border-[3px] border-dark rounded-xl text-sm outline-none focus:border-primary mb-4"
                    />

                    {/* 탭 */}
                    <div className="flex gap-2 mb-4">
                      {([
                        { key: "all", label: "전체" },
                        { key: "mine", label: "내 퀴즈셋" },
                        { key: "bookmark", label: "북마크" },
                        { key: "ai", label: "🤖 AI 생성" },
                      ] as const).map((tab) => (
                        <button
                          key={tab.key}
                          onClick={() => setQuizSetTab(tab.key)}
                          className={`px-4 py-2 border-[3px] border-dark rounded-full font-bold text-sm transition-colors ${quizSetTab === tab.key ? "bg-secondary" : "bg-white hover:bg-gray-50"
                            }`}
                        >
                          {tab.label}
                        </button>
                      ))}
                    </div>

                    {/* 목록 */}
                    <div className="flex-1 overflow-y-auto flex flex-col gap-2">
                      {(quizSetTab === "all" ? quizSets.filter((q) => !q.title.startsWith("[AI]"))
                        : quizSetTab === "mine" ? myQuizSets.filter((q) => !q.title.startsWith("[AI]"))
                          : quizSetTab === "bookmark" ? bookmarkedQuizSets.filter((q) => !q.title.startsWith("[AI]"))
                            : quizSets.filter((q) => q.title.startsWith("[AI]"))
                      )
                        .filter((q) => q.title.toLowerCase().includes(quizSetSearch.toLowerCase()))
                        .map((q) => (
                          <button
                            key={q.id}
                            onClick={() => handleSelectQuizSet(q)}
                            className={`w-full flex items-center justify-between px-4 py-3 border-[3px] rounded-xl text-left transition-all hover:-translate-y-0.5 ${selectedQuizSetId === q.id
                              ? "border-primary bg-primary/5 shadow-kitsch-sm"
                              : "border-dark bg-white hover:shadow-kitsch-sm"
                              }`}
                          >
                            <div>
                              <p className="font-bold text-sm">{q.title}</p>
                              <p className="text-xs text-gray-400">{(q as any).creatorNickname}</p>
                            </div>
                            <div className="flex items-center gap-2">
                              {bookmarkedQuizSetIds.has(q.id) && (
                                <svg width="16" height="16" viewBox="0 0 24 24" fill="#FFFF00" stroke="#2B2D42" strokeWidth="2.5">
                                  <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2" />
                                </svg>
                              )}
                              <span className="text-xs font-bold text-gray-400">{q.totalQuizCount}문제</span>
                            </div>
                          </button>
                        ))}
                      {(quizSetTab === "all" ? quizSets.filter((q) => !q.title.startsWith("[AI]"))
                        : quizSetTab === "mine" ? myQuizSets.filter((q) => !q.title.startsWith("[AI]"))
                          : quizSetTab === "bookmark" ? bookmarkedQuizSets.filter((q) => !q.title.startsWith("[AI]"))
                            : quizSets.filter((q) => q.title.startsWith("[AI]"))
                      )
                        .filter((q) => q.title.toLowerCase().includes(quizSetSearch.toLowerCase())).length === 0 && (
                          <div className="text-center py-10">
                            <p className="font-hand text-gray-400">퀴즈셋이 없어요!</p>
                          </div>
                        )}
                    </div>
                  </div>
                </div>
              )}
              <div className="grid grid-cols-2 gap-3 mb-6">
                <div>
                  <label className="block text-sm font-bold mb-2">최대 인원</label>
                  <select value={maxPlayers} onChange={(e) => setMaxPlayers(Number(e.target.value))} className="w-full px-4 py-3 bg-cream border-[3px] border-dark rounded-xl text-sm focus:border-primary outline-none transition-colors">
                    {[2, 3, 4, 5, 6, 7, 8].map((n) => <option key={n} value={n}>{n}명</option>)}
                  </select>
                </div>
                <div>
                  <label className="block text-sm font-bold mb-2">문제 수</label>
                  <input type="number" min={5} max={quizSets.find((q) => q.id === selectedQuizSetId)?.totalQuizCount || 50} value={maxQuizzes} onChange={(e) => setMaxQuizzes(Number(e.target.value))} className="w-full px-4 py-3 bg-cream border-[3px] border-dark rounded-xl text-sm focus:border-primary outline-none transition-colors" />
                </div>
              </div>
              <div className="flex gap-3">
                <button onClick={handleCreateRoom} disabled={creating} className="flex-1 py-4 bg-primary text-white font-bold text-lg border-[3px] border-dark rounded-xl shadow-kitsch hover:shadow-kitsch-lg hover:-translate-y-0.5 transition-all disabled:opacity-50">
                  {creating ? "생성 중..." : "방 만들기"}
                </button>
                <button onClick={() => setShowModal(false)} className="px-6 py-4 bg-white text-dark font-bold border-[3px] border-dark rounded-xl shadow-kitsch-sm hover:shadow-kitsch hover:-translate-y-0.5 transition-all">
                  취소
                </button>
              </div>
            </div>
          </div>
        )}
      </div>
    </>
  );
}

export default function RoomsPage() {
  return (
    <Suspense fallback={
      <div className="max-w-7xl mx-auto px-4 py-10 text-center">
        <p className="font-hand text-xl text-gray-400">로딩 중...</p>
      </div>
    }>
      <RoomsContent />
    </Suspense>
  );
}