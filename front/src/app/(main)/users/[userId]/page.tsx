"use client";

import { useState, useEffect } from "react";
import Link from "next/link";
import api from "@/lib/api";
import { useRouter } from "next/navigation";

interface UserInfo {
  nickname: string;
  username: string;
}

interface RecordsStats {
  totalGames: number;
  totalWins: number;
  totalRankingScore: number;
}

export default function MyPage() {
  const router = useRouter();
  const [user, setUser] = useState<UserInfo | null>(null);
  const [stats, setStats] = useState<RecordsStats | null>(null);
  const [loading, setLoading] = useState(true);
  const [myUserId, setMyUserId] = useState<string | null>(null);
  const [isAdmin, setIsAdmin] = useState(false);
  const [isEditing, setIsEditing] = useState(false);
  const [newNickname, setNewNickname] = useState("");
  const [newPassword, setNewPassword] = useState("");
  const [updateError, setUpdateError] = useState("");
  const [updating, setUpdating] = useState(false);
  const [withdrawing, setWithdrawing] = useState(false);

  useEffect(() => {
    const fetchData = async () => {
      try {
        const userId = localStorage.getItem("userId");
        if (!userId) throw new Error("userId 없음");
        setMyUserId(userId);
        setIsAdmin(localStorage.getItem("role") === "ADMIN");
        const [userRes, recordsRes] = await Promise.all([
          api.get(`/users/${userId}`),
          api.get(`/users/${userId}/records?page=0&size=1`),
        ]);
        setUser(userRes.data.data);
        setStats(recordsRes.data.data);
      } catch (err) {
        console.error("마이페이지 조회 실패", err);
      } finally {
        setLoading(false);
      }
    };
    fetchData();
  }, []);

  const handleUpdate = async () => {
    setUpdateError("");
    if (!newNickname.trim() && !newPassword.trim()) {
      setUpdateError("닉네임 또는 비밀번호를 입력하세요.");
      return;
    }
    setUpdating(true);
    try {
      const body: any = {};
      if (newNickname.trim()) body.nickname = newNickname;
      if (newPassword.trim()) body.password = newPassword;
      await api.patch(`/users/${myUserId}`, body);
      if (newNickname.trim()) {
        localStorage.setItem("nickname", newNickname);
      }
      window.location.reload();
    } catch (err: any) {
      setUpdateError(err.response?.data?.message || "수정에 실패했습니다.");
    } finally {
      setUpdating(false);
    }
  };

  const handleWithdraw = async () => {
    if (!window.confirm("정말 탈퇴하시겠습니까? 모든 데이터가 삭제됩니다.")) return;
    setWithdrawing(true);
    try {
      await api.delete("/auth/withdraw");
      localStorage.clear();
      window.location.href = "/";  // router.push 대신 이걸로
    } catch (err) {
      alert("탈퇴에 실패했습니다.");
    } finally {
      setWithdrawing(false);
    }
  };

  if (loading) {
    return (
      <div className="max-w-3xl mx-auto px-4 py-10 text-center">
        <p className="font-hand text-xl text-gray-400">불러오는 중...</p>
      </div>
    );
  }

  if (!user || !stats) {
    return (
      <div className="max-w-3xl mx-auto px-4 py-10 text-center">
        <p className="font-hand text-xl text-gray-400">정보를 불러올 수 없습니다.</p>
      </div>
    );
  }

  const winRate = stats.totalGames > 0
    ? ((stats.totalWins / stats.totalGames) * 100).toFixed(1)
    : "0.0";

  return (
    <div className="max-w-3xl mx-auto px-4 py-10">
      <h1 className="font-title text-4xl mb-8">👤 마이페이지</h1>

      {/* 프로필 카드 */}
      <div className="bg-white border-[3px] border-dark rounded-2xl shadow-kitsch p-8 mb-6">
        <div className="flex items-center justify-between mb-6">
          <div className="flex items-center gap-6">
            <div className="w-24 h-24 bg-primary rounded-full border-[3px] border-dark flex items-center justify-center shadow-kitsch">
              <span className="font-title text-4xl text-white">{user.nickname.charAt(0)}</span>
            </div>
            <div>
              <h2 className="font-title text-2xl mb-1">{user.nickname}</h2>
              <p className="text-sm text-gray-400">@{user.username}</p>
              {isAdmin && (
                <span className="inline-block mt-1 px-2 py-0.5 bg-primary text-white text-xs font-bold rounded-full border border-dark">
                  ADMIN
                </span>
              )}
            </div>
          </div>
          {/* 수정하기 버튼 - 우측 상단 */}
          {!isAdmin && (
            <button
              onClick={() => { setIsEditing(!isEditing); setUpdateError(""); }}
              className="px-4 py-2 bg-cream border-[3px] border-dark rounded-xl text-sm font-bold shadow-kitsch-sm hover:shadow-kitsch hover:-translate-y-0.5 transition-all"
            >
              {isEditing ? "✕ 취소" : "✏️프로필 수정"}
            </button>
          )}
        </div>

        {/* 수정 폼 */}
        {isEditing && (
          <div className="flex flex-col gap-3 mb-6 p-4 bg-cream border-[3px] border-dark rounded-xl">
            {updateError && <p className="text-sm text-red-500 font-bold">{updateError}</p>}
            <input
              type="text"
              placeholder="새 닉네임 (변경 시에만 입력)"
              value={newNickname}
              onChange={(e) => setNewNickname(e.target.value)}
              className="w-full px-4 py-3 bg-white border-[3px] border-dark rounded-xl text-sm focus:border-primary outline-none"
            />
            <input
              type="password"
              placeholder="새 비밀번호 (변경 시에만 입력)"
              value={newPassword}
              onChange={(e) => setNewPassword(e.target.value)}
              className="w-full px-4 py-3 bg-white border-[3px] border-dark rounded-xl text-sm focus:border-primary outline-none"
            />
            <button
              onClick={handleUpdate}
              disabled={updating}
              className="w-full py-3 bg-primary text-white font-bold border-[3px] border-dark rounded-xl shadow-kitsch hover:shadow-kitsch-lg hover:-translate-y-0.5 transition-all disabled:opacity-50"
            >
              {updating ? "수정 중..." : "저장"}
            </button>
          </div>
        )}

        {/* 통계 */}
        <div className="grid grid-cols-4 gap-4">
          <div className="bg-cream border-[3px] border-dark rounded-xl p-4 text-center">
            <p className="font-hand text-sm text-gray-400 mb-1">총 게임</p>
            <p className="font-title text-3xl">{stats.totalGames}</p>
          </div>
          <div className="bg-cream border-[3px] border-dark rounded-xl p-4 text-center">
            <p className="font-hand text-sm text-gray-400 mb-1">총 우승</p>
            <p className="font-title text-3xl text-primary">{stats.totalWins}</p>
          </div>
          <div className="bg-cream border-[3px] border-dark rounded-xl p-4 text-center">
            <p className="font-hand text-sm text-gray-400 mb-1">승률</p>
            <p className="font-title text-3xl text-accent">{winRate}%</p>
          </div>
          <div className="bg-cream border-[3px] border-dark rounded-xl p-4 text-center">
            <p className="font-hand text-sm text-gray-400 mb-1">랭킹 점수</p>
            <p className="font-title text-3xl text-secondary">{stats.totalRankingScore.toLocaleString()}</p>
          </div>
        </div>
      </div>

      {/* 바로가기 */}
      {isAdmin ? (
        <Link
          href="/admin/reports"
          className="block bg-primary text-white border-[3px] border-dark rounded-2xl p-6 shadow-kitsch hover:shadow-kitsch-lg hover:-translate-y-0.5 transition-all text-center"
        >
          <div className="text-3xl mb-2">⚙️</div>
          <h3 className="font-title text-lg mb-1">관리자 페이지</h3>
          <p className="text-xs text-white/70">신고 관리, 퀴즈셋 관리</p>
        </Link>
      ) : (
        <>
          <div className="grid grid-cols-2 gap-4 mb-4">
            <Link href={`/users/${myUserId}/records`} className="bg-white border-[3px] border-dark rounded-2xl p-6 shadow-kitsch hover:shadow-kitsch-lg hover:-translate-y-0.5 transition-all text-center">
              <div className="text-3xl mb-2">📊</div>
              <h3 className="font-title text-lg mb-1">내 전적</h3>
              <p className="text-xs text-gray-400">최근 게임 기록 확인</p>
            </Link>
            <Link href={`/users/${myUserId}/bookmarks`} className="bg-white border-[3px] border-dark rounded-2xl p-6 shadow-kitsch hover:shadow-kitsch-lg hover:-translate-y-0.5 transition-all text-center">
              <div className="text-3xl mb-2">⭐</div>
              <h3 className="font-title text-lg mb-1">즐겨찾기</h3>
              <p className="text-xs text-gray-400">저장한 퀴즈셋 보기</p>
            </Link>
          </div>
          {/* 회원 탈퇴 - 우측 하단 */}
          <div className="flex justify-end">
            <button
              onClick={handleWithdraw}
              disabled={withdrawing}
              className="px-4 py-2 text-sm text-red-400 border-2 border-red-200 rounded-xl font-bold hover:border-red-400 hover:text-red-500 transition-all disabled:opacity-50"
            >
              {withdrawing ? "탈퇴 중..." : "회원 탈퇴"}
            </button>
          </div>
        </>
      )}
    </div>
  );
}