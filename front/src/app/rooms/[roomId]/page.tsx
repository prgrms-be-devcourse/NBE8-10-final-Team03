"use client";

import { use, useState } from "react";
import Header from "@/components/common/Header";

const mockRoom = {
  id: 1,
  title: "역사 고수만 오세요",
  host: "퀴즈왕김철수",
  quizSet: "한국사 퀴즈셋",
  questionCount: 10,
  maxPlayers: 8,
  players: [
    { id: 1, nickname: "퀴즈왕김철수", isHost: true, score: 5200 },
    { id: 2, nickname: "스프링러버", isHost: false, score: 3200 },
    { id: 3, nickname: "자바매니아", isHost: false, score: 2900 },
    { id: 4, nickname: "알고킹", isHost: false, score: 2500 },
  ],
};

const mockChat = [
  { id: 1, nickname: "퀴즈왕김철수", message: "다들 준비됐어?", time: "14:32" },
  { id: 2, nickname: "스프링러버", message: "ㄱㄱ", time: "14:32" },
  { id: 3, nickname: "자바매니아", message: "한 명만 더 오면 시작하죠", time: "14:33" },
];

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

export default function RoomDetailPage({ params }: { params: Promise<{ roomId: string }> }) {
  const { roomId } = use(params);
  const [gameState, setGameState] = useState<GameState>("waiting");
  const [selectedAnswer, setSelectedAnswer] = useState<number | null>(null);
  const [timeLeft, setTimeLeft] = useState(12);

  return (
    <div>
      {/* 대기실에선 네비바 표시, 게임 중엔 숨김 */}
      {gameState === "waiting" && <Header />}

      <div className="max-w-6xl mx-auto px-4 py-6">
        {/* 상태 전환 (데모용) */}
        <div className="flex gap-2 mb-6 justify-center">
          <button
            onClick={() => { setGameState("waiting"); setSelectedAnswer(null); }}
            className={`px-5 py-2 border-[3px] border-dark rounded-full font-bold text-sm shadow-kitsch-sm transition-all ${
              gameState === "waiting" ? "bg-secondary" : "bg-white"
            }`}
          >
          대기실
        </button>
        <button
          onClick={() => { setGameState("playing"); setSelectedAnswer(null); }}
          className={`px-5 py-2 border-[3px] border-dark rounded-full font-bold text-sm shadow-kitsch-sm transition-all ${
            gameState === "playing" ? "bg-secondary" : "bg-white"
          }`}
        >
          게임 진행
        </button>
        <button
          onClick={() => setGameState("result")}
          className={`px-5 py-2 border-[3px] border-dark rounded-full font-bold text-sm shadow-kitsch-sm transition-all ${
            gameState === "result" ? "bg-secondary" : "bg-white"
          }`}
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
                  <h1 className="font-title text-3xl mb-1">{mockRoom.title}</h1>
                  <p className="text-sm text-gray-400">{mockRoom.quizSet} · {mockRoom.questionCount}문제</p>
                </div>
                <div className="text-center">
                  <p className="font-title text-4xl text-primary">{mockRoom.players.length}/{mockRoom.maxPlayers}</p>
                  <p className="font-hand text-sm text-gray-400">참여중</p>
                </div>
              </div>
              <div className="w-full h-4 bg-gray-200 rounded-full border-2 border-dark">
                <div className="h-full bg-primary rounded-full" style={{ width: `${(mockRoom.players.length / mockRoom.maxPlayers) * 100}%` }} />
              </div>
            </div>

            <div className="grid grid-cols-4 gap-3 mb-4">
              {mockRoom.players.map((p) => (
                <div key={p.id} className={`flex flex-col items-center p-5 border-[3px] rounded-2xl text-center ${
                  p.isHost ? "border-primary bg-primary/5 shadow-kitsch" : "border-dark bg-white shadow-kitsch-sm"
                }`}>
                  <div className={`w-14 h-14 rounded-full border-[3px] flex items-center justify-center font-title text-xl mb-2 ${
                    p.isHost ? "bg-primary text-white border-dark" : "bg-cream border-dark"
                  }`}>{p.nickname.charAt(0)}</div>
                  <p className="font-bold text-sm mb-1">{p.nickname}</p>
                  {p.isHost && <span className="text-xs text-primary font-bold">👑 방장</span>}
                  <p className="text-xs text-gray-400 mt-1">{p.score.toLocaleString()}P</p>
                </div>
              ))}
              {Array.from({ length: mockRoom.maxPlayers - mockRoom.players.length }).map((_, i) => (
                <div key={`empty-${i}`} className="flex flex-col items-center justify-center p-5 border-[3px] border-dashed border-gray-300 rounded-2xl">
                  <div className="w-14 h-14 rounded-full bg-gray-100 border-2 border-dashed border-gray-300 flex items-center justify-center mb-2">
                    <span className="text-gray-300 text-xl">?</span>
                  </div>
                  <p className="text-xs text-gray-300 font-bold">대기중</p>
                </div>
              ))}
            </div>

            <div className="flex gap-3">
              <button onClick={() => setGameState("playing")} className="flex-1 py-4 bg-primary text-white font-bold text-lg border-[3px] border-dark rounded-2xl shadow-kitsch hover:shadow-kitsch-lg hover:-translate-y-0.5 transition-all">
                🎮 게임 시작
              </button>
              <button className="px-8 py-4 bg-white text-dark font-bold border-[3px] border-dark rounded-2xl shadow-kitsch-sm hover:shadow-kitsch hover:-translate-y-0.5 transition-all">
                나가기
              </button>
            </div>
          </div>

          <div className="w-80 flex flex-col gap-4">
            <div className="bg-white border-[3px] border-dark rounded-2xl shadow-kitsch p-5">
              <h3 className="font-title text-lg mb-3">퀴즈 정보</h3>
              <div className="flex flex-col gap-2 text-sm">
                <div className="flex justify-between"><span className="text-gray-400">퀴즈셋</span><span className="font-bold">{mockRoom.quizSet}</span></div>
                <div className="flex justify-between"><span className="text-gray-400">문제 수</span><span className="font-bold">{mockRoom.questionCount}문제</span></div>
                <div className="flex justify-between"><span className="text-gray-400">제한시간</span><span className="font-bold">문제당 15초</span></div>
                <div className="flex justify-between"><span className="text-gray-400">최대 인원</span><span className="font-bold">{mockRoom.maxPlayers}명</span></div>
              </div>
            </div>

            <div className="bg-white border-[3px] border-dark rounded-2xl shadow-kitsch flex-1 flex flex-col overflow-hidden">
              <h3 className="font-title text-lg p-5 pb-3">💬 채팅</h3>
              <div className="flex-1 px-5 overflow-y-auto flex flex-col gap-3">
                {mockChat.map((msg) => (
                  <div key={msg.id}>
                    <div className="flex items-center gap-2 mb-1">
                      <span className="text-xs font-bold">{msg.nickname}</span>
                      <span className="text-xs text-gray-300">{msg.time}</span>
                    </div>
                    <p className="text-sm bg-cream rounded-xl px-3 py-2 inline-block border border-gray-200">{msg.message}</p>
                  </div>
                ))}
              </div>
              <div className="p-4 border-t-[3px] border-dark">
                <div className="flex gap-2">
                  <input type="text" placeholder="메시지 입력..." className="flex-1 px-3 py-2 bg-cream border-[3px] border-dark rounded-xl text-sm focus:border-primary outline-none" />
                  <button className="px-4 py-2 bg-primary text-white border-[3px] border-dark rounded-xl font-bold text-sm shadow-kitsch-sm">전송</button>
                </div>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* ========== 게임 진행 ========== */}
      {gameState === "playing" && (
        <div className="flex gap-6">
          <div className="flex-1">
            {/* 라운드 + 타이머 */}
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
                <span className={`font-title text-2xl ${timeLeft <= 5 ? "text-red-500" : "text-dark"}`}>
                  {timeLeft}초
                </span>
              </div>
            </div>

            {/* 문제 */}
            <div className="bg-white border-[3px] border-dark rounded-2xl shadow-kitsch p-10 mb-6 text-center">
              <h2 className="font-title text-3xl">{mockQuestion.content}</h2>
            </div>

            {/* 선택지 */}
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

          {/* 실시간 스코어보드 */}
          <div className="w-72">
            <div className="bg-white border-[3px] border-dark rounded-2xl shadow-kitsch p-5">
              <h3 className="font-title text-lg mb-4">실시간 순위</h3>
              <div className="flex flex-col gap-3">
                {mockScoreboard.map((p, i) => (
                  <div key={p.nickname} className={`flex items-center gap-3 p-3 rounded-xl ${i === 0 ? "bg-primary/10 border-2 border-primary" : "bg-cream"}`}>
                    <span className={`font-title text-xl w-6 ${
                      i === 0 ? "text-primary" : i === 1 ? "text-accent" : i === 2 ? "text-secondary" : "text-gray-400"
                    }`}>{i + 1}</span>
                    <div className="w-8 h-8 rounded-full bg-cream border-2 border-dark flex items-center justify-center text-xs font-bold">
                      {p.avatar}
                    </div>
                    <div className="flex-1">
                      <p className="font-bold text-sm">{p.nickname}</p>
                      <p className="text-xs text-gray-400">{p.correct}문제 정답</p>
                    </div>
                    <span className="font-title text-lg">{p.score.toLocaleString()}</span>
                  </div>
                ))}
              </div>
            </div>

            {/* 제출 현황 */}
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
            <p className="font-hand text-xl text-gray-400">{mockRoom.quizSet} · {mockRoom.questionCount}문제</p>
          </div>

          {/* 1등 강조 */}
          <div className="bg-white border-[3px] border-primary rounded-2xl shadow-kitsch p-8 mb-6 text-center">
            <div className="text-4xl mb-2">👑</div>
            <div className="w-20 h-20 mx-auto rounded-full border-[4px] border-primary bg-primary/10 flex items-center justify-center mb-3">
              <span className="font-title text-3xl text-primary">{mockResult[0].nickname.charAt(0)}</span>
            </div>
            <h2 className="font-title text-2xl mb-1">{mockResult[0].nickname}</h2>
            <p className="font-title text-4xl text-primary mb-2">{mockResult[0].score.toLocaleString()}점</p>
            <div className="flex justify-center gap-4 text-sm text-gray-500">
              <span>{mockResult[0].correct}/{mockRoom.questionCount} 정답</span>
              <span className="text-accent font-bold">+{mockResult[0].earnedRankingScore} RP</span>
            </div>
          </div>

          {/* 나머지 순위 */}
          <div className="bg-white border-[3px] border-dark rounded-2xl shadow-kitsch overflow-hidden mb-8">
            {mockResult.slice(1).map((r) => (
              <div key={r.rank} className="flex items-center px-6 py-5 border-b-2 border-dashed border-gray-200 last:border-b-0">
                <span className={`font-title text-2xl w-10 ${
                  r.rank === 2 ? "text-accent" : r.rank === 3 ? "text-secondary" : "text-gray-400"
                }`}>{r.rank}</span>
                <div className="w-12 h-12 rounded-full bg-cream border-2 border-dark flex items-center justify-center font-bold mr-4">
                  {r.nickname.charAt(0)}
                </div>
                <div className="flex-1">
                  <p className="font-bold">{r.nickname}</p>
                  <p className="text-xs text-gray-400">{r.correct}/{mockRoom.questionCount} 정답</p>
                </div>
                <div className="text-right mr-6">
                  <p className="font-title text-xl">{r.score.toLocaleString()}점</p>
                </div>
                <span className="text-sm text-accent font-bold">+{r.earnedRankingScore} RP</span>
              </div>
            ))}
          </div>

          {/* 하단 버튼 */}
          <div className="flex gap-3">
            <button onClick={() => setGameState("waiting")} className="flex-1 py-4 bg-primary text-white font-bold text-lg border-[3px] border-dark rounded-2xl shadow-kitsch hover:shadow-kitsch-lg hover:-translate-y-0.5 transition-all">
              한 판 더!
            </button>
            <a href="/rooms" className="flex-1 py-4 bg-white text-dark font-bold text-lg border-[3px] border-dark rounded-2xl shadow-kitsch-sm hover:shadow-kitsch hover:-translate-y-0.5 transition-all text-center">
              로비로 돌아가기
            </a>
          </div>
        </div>
      )}
    </div>
    </div>
  );
}