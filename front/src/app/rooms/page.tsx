"use client";

import { useState, useEffect, useRef } from "react";
import Link from "next/link";
import api from "@/lib/api";
import Header from "@/components/common/Header";
import { Client } from "@stomp/stompjs";
import { useSearchParams } from "next/navigation";
import { jwtDecode } from "jwt-decode";

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

interface ChatMessage {
  sender: string;
  message: string;
  type: string;
}

const mockQuestion = {
  questionId: 3,
  content: "임진왜란이 일어난 해는?",
  choices: ["1392년", "1492년", "1592년", "1692년"],
  currentRound: 3,
  totalRounds: 10,
  timeLimit: 15,
};

const mockScoreboard = [
  { nickname: "퀴즈왕김철수", score: 2800, correct: 3, avatar: "퀴" },
  { nickname: "스프링러버", score: 2100, correct: 2, avatar: "스" },
  { nickname: "자바매니아", score: 1900, correct: 2, avatar: "자" },
  { nickname: "알고킹", score: 1200, correct: 1, avatar: "알" },
];

const mockResult = [
  { rank: 1, nickname: "퀴즈왕김철수", score: 9500, correct: 9, earnedRankingScore: 320 },
  { rank: 2, nickname: "스프링러버", score: 7200, correct: 7, earnedRankingScore: 180 },
  { rank: 3, nickname: "자바매니아", score: 6100, correct: 6, earnedRankingScore: 120 },
  { rank: 4, nickname: "알고킹", score: 4300, correct: 4, earnedRankingScore: 60 },
];

type GameState = "waiting" | "playing" | "result";
type ViewMode = "lobby" | "room";

export default function RoomsPage() {
  // 로비 state
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

  // 방 만들기 모달
  const [showModal, setShowModal] = useState(false);
  const [quizSets, setQuizSets] = useState<QuizSet[]>([]);
  const [roomName, setRoomName] = useState("");
  const [selectedQuizSetId, setSelectedQuizSetId] = useState<number | null>(null);
  const [maxPlayers, setMaxPlayers] = useState(4);
  const [maxQuizzes, setMaxQuizzes] = useState(10);
  const [createError, setCreateError] = useState("");
  const [creating, setCreating] = useState(false);

  // 방 상세 state
  const [gameState, setGameState] = useState<GameState>("waiting");
  const [selectedAnswer, setSelectedAnswer] = useState<number | null>(null);
  const [timeLeft] = useState(12);

  // STOMP 채팅 state
  const stompClientRef = useRef<Client | null>(null);
  const [chatMessages, setChatMessages] = useState<ChatMessage[]>([]);
  const [chatInput, setChatInput] = useState("");
  const chatBottomRef = useRef<HTMLDivElement | null>(null);

  useEffect(() => {
    setMyNickname(localStorage.getItem("nickname"));
    const token = localStorage.getItem("accessToken");
    if (token) {
      const decoded: any = jwtDecode(token);
      setMyUserId(decoded.sub);
    }
    const quizSetIdParam = searchParams.get("quizSetId");
    
    if (quizSetIdParam && quizSets.length === 0) {
      // 퀴즈셋 목록 불러오고 모달 열기
      const openModalWithQuizSet = async () => {
        try {
          const res = await api.get("/quizsets");
          setQuizSets(res.data.data);
          const targetId = Number(quizSetIdParam);
          const target = res.data.data.find((q: QuizSet) => q.id === targetId);
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
  }, [searchParams]);  // searchParams 바뀔 때마다 실행

  
useEffect(() => {
  if (viewMode === "lobby") {
    fetchLobbyData();
    // interval 완전 제거!
  }
}, [viewMode]);

  // 채팅 자동 스크롤
  useEffect(() => {
    chatBottomRef.current?.scrollIntoView({ behavior: "smooth" });
  }, [chatMessages]);

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
      setCurrentRoom(res.data.data);
      setGameState("waiting");
      setSelectedAnswer(null);
      setChatMessages([]);
  
      const token = localStorage.getItem("accessToken");
      const client = new Client({
        brokerURL: "ws://localhost:8080/ws/game",
        connectHeaders: { Authorization: `Bearer ${token}` },
        reconnectDelay: 0,
        onConnect: () => {
          console.log("✅ STOMP 연결 성공!");
          setStompConnected(true);
  
          // ✅ 여기만 교체!
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
                    message: data.message || `${data.sender}님이 입장했습니다.`,  // 백엔드 message 우선
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
                    // data.data에 players 있으면 업데이트, 없으면 유지
                    if (data.data?.players) {
                      setCurrentRoom((prev: any) => ({
                        ...prev,
                        players: data.data.players,
                      }));
                    }
                    break;
  
              case "ROOM_DELETED":
                alert("방장이 나가 방이 삭제되었습니다.");
                stompClientRef.current?.deactivate();
                stompClientRef.current = null;
                setStompConnected(false);
                setChatMessages([]);
                setCurrentRoom(null);
                setViewMode("lobby");
                break;
            }
          });
        },
        onStompError: (frame) => {
          console.error("❌ STOMP 에러:", frame);
          console.error("headers:", frame.headers);
          console.error("body:", frame.body);
        },
        onWebSocketError: (event) => {
          console.error("❌ WebSocket 에러:", event);
        },
        onDisconnect: () => {
          console.log("🔌 STOMP 연결 해제");
          setStompConnected(false);
        },
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
    setViewMode("lobby");
  };

  const sendChat = () => {
    if (!chatInput.trim()) return;
    if (!stompClientRef.current) return;
    if (!stompClientRef.current.connected) return;  // ✅ 이거 추가!
    if (!currentRoom) return;
  
    stompClientRef.current.publish({
      destination: `/app/rooms/${currentRoom.gameSessionId}/chat`,
      body: JSON.stringify({ message: chatInput }),
    });
    setChatInput("");
  };

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
    if (selected) setMaxQuizzes(selected.totalQuizCount);
  };

  const handleCreateRoom = async () => {
    setCreateError("");
    if (!roomName.trim()) { setCreateError("방 제목을 입력하세요."); return; }
    if (!selectedQuizSetId) { setCreateError("퀴즈셋을 선택하세요."); return; }

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
      setRoomName("");
      await handleJoinRoom(gameSessionId);
    } catch (err: any) {
      setCreateError(err.response?.data?.message || "방 생성에 실패했습니다.");
    } finally {
      setCreating(false);
    }
  };

  const filteredRooms = rooms.filter((room) => {
    if (room.status === "END") return false;
    if (filter === "waiting") return room.status === "WAIT";
    if (filter === "playing") return room.status === "START";
    return true;
  });

  const rankColors = ["text-primary", "text-accent", "text-secondary", "text-gray-400", "text-gray-400"];

  // ========== 방 상세 화면 ==========
  if (viewMode === "room") {
    const roomInfo = currentRoom;
    return (
      <div>
        {gameState === "waiting" && (
          <div className="border-b-[3px] border-dark bg-white px-6 py-3 flex items-center justify-between">
            <span className="font-title text-xl">답정너</span>
            <button
              onClick={handleLeaveRoom}
              className="px-4 py-2 bg-white text-dark font-bold border-[3px] border-dark rounded-xl shadow-kitsch-sm hover:shadow-kitsch hover:-translate-y-0.5 transition-all text-sm"
            >
              ← 나가기
            </button>
          </div>
        )}

        <div className="max-w-6xl mx-auto px-4 py-6">
          {/* 상태 전환 (데모용) */}
          <div className="flex gap-2 mb-6 justify-center">
            <button
              onClick={() => { setGameState("waiting"); }}
              className={`px-5 py-2 border-[3px] border-dark rounded-full font-bold text-sm shadow-kitsch-sm transition-all ${gameState === "waiting" ? "bg-secondary" : "bg-white"}`}
            >
              대기실
            </button>
            <button
              onClick={() => { setGameState("playing"); setSelectedAnswer(null); }}
              className={`px-5 py-2 border-[3px] border-dark rounded-full font-bold text-sm shadow-kitsch-sm transition-all ${gameState === "playing" ? "bg-secondary" : "bg-white"}`}
            >
              게임 진행
            </button>
            <button
              onClick={() => setGameState("result")}
              className={`px-5 py-2 border-[3px] border-dark rounded-full font-bold text-sm shadow-kitsch-sm transition-all ${gameState === "result" ? "bg-secondary" : "bg-white"}`}
            >
              결과 화면
            </button>
          </div>

{/* ========== 대기실 ========== */}
{gameState === "waiting" && (
  <div className="flex gap-6">
    <div className="flex-1">
      <div className="bg-white border-[3px] border-dark rounded-2xl shadow-kitsch p-6 mb-4">
        <div className="flex items-center justify-between mb-4">
          <div>
            <h1 className="font-title text-3xl mb-1">{roomInfo?.title || "대기실"}</h1>
            <p className="text-sm text-gray-400">{roomInfo?.title}</p>
          </div>
          <div className="text-center">
            <p className="font-title text-4xl text-primary">
              {roomInfo?.players?.length ?? 0}명 참여중
            </p>
          </div>
        </div>
        <div className="w-full h-4 bg-gray-200 rounded-full border-2 border-dark">
          <div
            className="h-full bg-primary rounded-full"
            style={{ width: `${Math.min((roomInfo?.players?.length ?? 0) / (roomInfo?.maxPlayer || 4) * 100, 100)}%` }}
          />
        </div>
      </div>

      <div className="grid grid-cols-4 gap-3 mb-4">
        {(roomInfo?.players || []).map((p: any, i: number) => {
          const isMe = p.nickname === myNickname;  // ✅

          return (
            <div
              key={`player-${p.nickname || i}`}
              className={`flex flex-col items-center p-5 border-[3px] rounded-2xl text-center ${
                p.isHost ? "border-primary bg-primary/5 shadow-kitsch" : "border-dark bg-white shadow-kitsch-sm"
              }`}
            >
              <div className={`w-14 h-14 rounded-full border-[3px] flex items-center justify-center font-title text-xl mb-2 ${
                p.isHost ? "bg-primary text-white border-dark" : "bg-cream border-dark"
              }`}>
                {p.nickname.charAt(0)}
              </div>
              <p className="font-bold text-sm mb-1">{p.nickname}</p>
              {p.isHost && <span className="text-hand text-sm text-primary font-bold"> 방장</span>}
              {isMe && <span className="text-hand text-sm text-accent font-bold"> 나</span>}  {/* ✅ */}
            </div>
          );
        })}

        {Array.from({
          length: (roomInfo?.maxPlayer || 4) - (roomInfo?.players?.length || roomInfo?.currentPlayerCount || 0)
        }).map((_, i) => (
          <div key={`empty-${i}`} className="flex flex-col items-center justify-center p-5 border-[3px] border-dashed border-gray-300 rounded-2xl">
            <div className="w-14 h-14 rounded-full bg-gray-100 border-2 border-dashed border-gray-300 flex items-center justify-center mb-2">
              <span className="text-gray-300 text-xl">?</span>
            </div>
            <p className="text-xs text-gray-300 font-bold">대기중</p>
          </div>
        ))}
      </div>

                <div className="flex gap-3">
                  <button
                    onClick={() => setGameState("playing")}
                    className="flex-1 py-4 bg-primary text-white font-bold text-lg border-[3px] border-dark rounded-2xl shadow-kitsch hover:shadow-kitsch-lg hover:-translate-y-0.5 transition-all"
                  >
                    🎮 게임 시작
                  </button>
                  <button
                    onClick={handleLeaveRoom}
                    className="px-8 py-4 bg-white text-dark font-bold border-[3px] border-dark rounded-2xl shadow-kitsch-sm hover:shadow-kitsch hover:-translate-y-0.5 transition-all"
                  >
                    나가기
                  </button>
                </div>
              </div>

              <div className="w-80 flex flex-col gap-4">
                <div className="bg-white border-[3px] border-dark rounded-2xl shadow-kitsch p-5">
                  <h3 className="font-title text-lg mb-3">퀴즈 정보</h3>
                  <div className="flex flex-col gap-2 text-sm">
                    <div className="flex justify-between"><span className="text-gray-400">퀴즈셋</span><span className="font-bold">{roomInfo?.quizSetId}번 퀴즈셋</span></div>
                    <div className="flex justify-between"><span className="text-gray-400">문제 수</span><span className="font-bold">-</span></div>
                    <div className="flex justify-between"><span className="text-gray-400">제한시간</span><span className="font-bold">문제당 15초</span></div>
                    <div className="flex justify-between"><span className="text-gray-400">최대 인원</span><span className="font-bold">-</span></div>
                  </div>
                </div>

                {/* 채팅 */}
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
          <span className="text-xs text-gray-300">
            {new Date().toLocaleTimeString("ko-KR", { hour: "2-digit", minute: "2-digit" })}
          </span>
        </div>
        <p className="text-sm bg-cream rounded-xl px-3 py-2 inline-block border border-gray-200">
          {msg.message}
        </p>
      </>
    )}
  </div>
))}
                    <div ref={chatBottomRef} />
                  </div>
                  <div className="p-4 border-t-[3px] border-dark">
  <div className="flex flex-row items-center gap-2">  {/* ✅ flex-row items-center 추가 */}
    <input
      type="text"
      value={chatInput}
      onChange={(e) => setChatInput(e.target.value)}
      onKeyDown={(e) => e.key === "Enter" && sendChat()}
      placeholder="메시지 입력..."
      className="flex-1 px-3 py-2 bg-cream border-[3px] border-dark rounded-xl text-sm focus:border-primary outline-none"
    />
    <button
      onClick={sendChat}
      disabled={!stompConnected}  // ✅ 연결 전엔 비활성화
      className="shrink-0 px-4 py-2 bg-primary text-white border-[3px] border-dark rounded-xl font-bold text-sm shadow-kitsch-sm disabled:opacity-40 whitespace-nowrap"
    >
      전송
    </button>
  </div>
  {!stompConnected && (
    <p className="text-xs text-gray-400 mt-1">채팅 연결 중...</p>  // ✅ 연결 상태 표시
  )}
                  </div>
                </div>
              </div>
            </div>
          )}

          {/* ========== 게임 진행 ========== */}
          {gameState === "playing" && (
            <div className="flex gap-6">
              <div className="flex-1">
                <div className="flex items-center justify-between mb-6">
                  <span className="px-4 py-2 bg-white border-[3px] border-dark rounded-xl font-title text-lg shadow-kitsch-sm">
                    Q{mockQuestion.currentRound}/{mockQuestion.totalRounds}
                  </span>
                  <div className="flex items-center gap-3">
                    <div className="w-48 h-4 bg-gray-200 rounded-full border-2 border-dark overflow-hidden">
                      <div
                        className={`h-full rounded-full transition-all ${timeLeft <= 5 ? "bg-red-500" : "bg-accent"}`}
                        style={{ width: `${(timeLeft / mockQuestion.timeLimit) * 100}%` }}
                      />
                    </div>
                    <span className={`font-title text-2xl ${timeLeft <= 5 ? "text-red-500" : "text-dark"}`}>{timeLeft}초</span>
                  </div>
                </div>

                <div className="bg-white border-[3px] border-dark rounded-2xl shadow-kitsch p-10 mb-6 text-center">
                  <h2 className="font-title text-3xl">{mockQuestion.content}</h2>
                </div>

                <div className="grid grid-cols-2 gap-4">
                  {mockQuestion.choices.map((choice, i) => {
                    const colors = [
                      "hover:bg-primary hover:text-white hover:border-primary",
                      "hover:bg-accent hover:text-white hover:border-accent",
                      "hover:bg-secondary hover:border-secondary",
                      "hover:bg-purple-500 hover:text-white hover:border-purple-500",
                    ];
                    const selectedColors = [
                      "bg-primary text-white border-primary",
                      "bg-accent text-white border-accent",
                      "bg-secondary border-secondary",
                      "bg-purple-500 text-white border-purple-500",
                    ];
                    const isSelected = selectedAnswer === i;
                    return (
                      <button
                        key={i}
                        onClick={() => setSelectedAnswer(i)}
                        className={`p-6 border-[3px] rounded-2xl font-bold text-lg shadow-kitsch-sm transition-all hover:-translate-y-0.5 hover:shadow-kitsch ${
                          isSelected ? selectedColors[i] : `bg-white border-dark ${colors[i]}`
                        }`}
                      >
                        <span className="font-title mr-2">{i + 1}.</span>
                        {choice}
                      </button>
                    );
                  })}
                </div>
              </div>

              <div className="w-72">
                <div className="bg-white border-[3px] border-dark rounded-2xl shadow-kitsch p-5">
                  <h3 className="font-title text-lg mb-4">실시간 순위</h3>
                  <div className="flex flex-col gap-3">
                    {mockScoreboard.map((p, i) => (
                      <div key={p.nickname} className={`flex items-center gap-3 p-3 rounded-xl ${i === 0 ? "bg-primary/10 border-2 border-primary" : "bg-cream"}`}>
                        <span className={`font-title text-xl w-6 ${i === 0 ? "text-primary" : i === 1 ? "text-accent" : i === 2 ? "text-secondary" : "text-gray-400"}`}>{i + 1}</span>
                        <div className="w-8 h-8 rounded-full bg-cream border-2 border-dark flex items-center justify-center text-xs font-bold">{p.avatar}</div>
                        <div className="flex-1">
                          <p className="font-bold text-sm">{p.nickname}</p>
                          <p className="text-xs text-gray-400">{p.correct}문제 정답</p>
                        </div>
                        <span className="font-title text-lg">{p.score.toLocaleString()}</span>
                      </div>
                    ))}
                  </div>
                </div>

                <div className="bg-white border-[3px] border-dark rounded-2xl shadow-kitsch p-5 mt-4">
                  <h3 className="font-title text-sm mb-3">제출 현황</h3>
                  <div className="flex gap-2">
                    <span className="w-8 h-8 rounded-full bg-accent border-2 border-dark flex items-center justify-center text-white text-xs font-bold">✓</span>
                    <span className="w-8 h-8 rounded-full bg-accent border-2 border-dark flex items-center justify-center text-white text-xs font-bold">✓</span>
                    <span className="w-8 h-8 rounded-full bg-gray-200 border-2 border-dark flex items-center justify-center text-gray-400 text-xs font-bold">?</span>
                    <span className="w-8 h-8 rounded-full bg-gray-200 border-2 border-dark flex items-center justify-center text-gray-400 text-xs font-bold">?</span>
                  </div>
                  <p className="text-xs text-gray-400 mt-2">2/4명 제출 완료</p>
                </div>
              </div>
            </div>
          )}

          {/* ========== 결과 화면 ========== */}
          {gameState === "result" && (
            <div className="max-w-3xl mx-auto">
              <div className="text-center mb-10">
                <h1 className="font-title text-4xl mb-2">🎉 게임 종료!</h1>
                <p className="font-hand text-xl text-gray-400">{roomInfo?.quizSetTitle} · {roomInfo?.maxQuizzes}문제</p>
              </div>

              <div className="bg-white border-[3px] border-primary rounded-2xl shadow-kitsch p-8 mb-6 text-center">
                <div className="text-4xl mb-2">👑</div>
                <div className="w-20 h-20 mx-auto rounded-full border-[4px] border-primary bg-primary/10 flex items-center justify-center mb-3">
                  <span className="font-title text-3xl text-primary">{mockResult[0].nickname.charAt(0)}</span>
                </div>
                <h2 className="font-title text-2xl mb-1">{mockResult[0].nickname}</h2>
                <p className="font-title text-4xl text-primary mb-2">{mockResult[0].score.toLocaleString()}점</p>
                <div className="flex justify-center gap-4 text-sm text-gray-500">
                  <span>{mockResult[0].correct}/{roomInfo?.maxQuizzes} 정답</span>
                  <span className="text-accent font-bold">+{mockResult[0].earnedRankingScore} RP</span>
                </div>
              </div>

              <div className="bg-white border-[3px] border-dark rounded-2xl shadow-kitsch overflow-hidden mb-8">
                {mockResult.slice(1).map((r) => (
                  <div key={r.rank} className="flex items-center px-6 py-5 border-b-2 border-dashed border-gray-200 last:border-b-0">
                    <span className={`font-title text-2xl w-10 ${r.rank === 2 ? "text-accent" : r.rank === 3 ? "text-secondary" : "text-gray-400"}`}>{r.rank}</span>
                    <div className="w-12 h-12 rounded-full bg-cream border-2 border-dark flex items-center justify-center font-bold mr-4">{r.nickname.charAt(0)}</div>
                    <div className="flex-1">
                      <p className="font-bold">{r.nickname}</p>
                      <p className="text-xs text-gray-400">{r.correct}/{roomInfo?.maxQuizzes} 정답</p>
                    </div>
                    <div className="text-right mr-6">
                      <p className="font-title text-xl">{r.score.toLocaleString()}점</p>
                    </div>
                    <span className="text-sm text-accent font-bold">+{r.earnedRankingScore} RP</span>
                  </div>
                ))}
              </div>

              <div className="flex gap-3">
                <button
                  onClick={() => setGameState("waiting")}
                  className="flex-1 py-4 bg-primary text-white font-bold text-lg border-[3px] border-dark rounded-2xl shadow-kitsch hover:shadow-kitsch-lg hover:-translate-y-0.5 transition-all"
                >
                  한 판 더!
                </button>
                <button
                  onClick={handleLeaveRoom}
                  className="flex-1 py-4 bg-white text-dark font-bold text-lg border-[3px] border-dark rounded-2xl shadow-kitsch-sm hover:shadow-kitsch hover:-translate-y-0.5 transition-all text-center"
                >
                  로비로 돌아가기
                </button>
              </div>
            </div>
          )}
        </div>
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
        {/* 좌측 메인 */}
        <div className="flex-1">
          <div className="mb-6">
            <h1 className="font-title text-4xl mb-1">게임 대기실</h1>
            <p className="font-hand text-lg text-gray-400">참여할 퀴즈방을 선택하세요</p>
            <button onClick={fetchLobbyData} className="font-title text-gray-400 ">
   (새로고침)
</button>
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
                    <p className="text-xs text-gray-400">{room.quizSetTitle} · {room.hostNickname}</p>
                  </div>

                  <div className="flex items-center gap-4">
                    <div className="text-right">
                      <p className="font-title text-xl text-primary">{room.currentPlayerCount}/{room.maxPlayer}</p>
                      <div className="w-16 h-2 bg-gray-200 rounded-full mt-1">
                        <div
                          className="h-full bg-primary rounded-full"
                          style={{ width: `${(room.currentPlayerCount / room.maxPlayer) * 100}%` }}
                        />
                      </div>
                    </div>

                    {room.status === "WAIT" ? (
                      <button
                        onClick={() => handleJoinRoom(room.gameSessionId)}
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
              {rankings.map((r, i) => (
                <div key={`rank-${r.rank}-${i}`} className="flex items-center gap-3">
                  <span className={`font-title text-xl w-8 ${rankColors[i]}`}>{r.rank}</span>
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
              <Link href={`/users/${myUserId}`} className="w-full py-3 bg-cream border-[3px] border-dark rounded-xl text-sm font-bold shadow-kitsch-sm hover:shadow-kitsch hover:-translate-y-0.5 transition-all text-center">
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
                    <option key={q.id} value={q.id}>{q.title} ({q.totalQuizCount}문제)</option>
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
    </>
  );
}
