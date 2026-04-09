"use client";

import { useState, useEffect, type KeyboardEvent } from "react";
import Link from "next/link";
import api from "@/lib/api";
import { useRouter } from "next/navigation";
import ProfileAvatar from "@/components/common/ProfileAvatar";

interface UserInfo {
  nickname: string;
  username: string;
  profileImage: number;
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
  const [currentPassword, setCurrentPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [updateError, setUpdateError] = useState("");
  const [updating, setUpdating] = useState(false);
  const [isOAuthUser, setIsOAuthUser] = useState(false);
  const [isCapsLockOn, setIsCapsLockOn] = useState(false);
  const [withdrawing, setWithdrawing] = useState(false);
  const [isWithdrawConfirming, setIsWithdrawConfirming] = useState(false);
  const [withdrawKeyword, setWithdrawKeyword] = useState("");
  const [profileImage, setProfileImage] = useState<number>(1);
  const [showImagePicker, setShowImagePicker] = useState(false);

  const getDisplayUsername = (username: string) => {
    if (username.startsWith("GOOGLE_")) return "google";
    if (username.startsWith("KAKAO_")) return "kakao";
    if (username.startsWith("NAVER_")) return "naver";
    return username;
  };

  useEffect(() => {
    const fetchData = async () => {
      try {
        const userId = localStorage.getItem("userId");
        if (!userId) throw new Error("userId 없음");
        setMyUserId(userId);
        setIsAdmin(localStorage.getItem("role") === "ADMIN");
        setIsOAuthUser(localStorage.getItem("oauth") === "true");
        const [userRes, recordsRes] = await Promise.all([
          api.get(`/users/${userId}`),
          api.get(`/users/${userId}/records?page=0&size=1`),
        ]);
        setUser(userRes.data.data);
        setProfileImage(userRes.data.data.profileImage || 1);
        setStats(recordsRes.data.data);
      } catch (err) {
        console.error("마이페이지 조회 실패", err);
      } finally {
        setLoading(false);
      }
    };
    fetchData();
  }, []);

  const handleCapsLock = (e: KeyboardEvent<HTMLInputElement>) => {
    setIsCapsLockOn(e.getModifierState("CapsLock"));
  };

  const handleNicknameChange = (value: string) => {
    if (/[^ㄱ-힣a-zA-Z0-9]/.test(value)) {
      setUpdateError("닉네임에 특수문자는 사용할 수 없습니다.");
    } else {
      setUpdateError("");
    }
    const filtered = value.replace(/[^ㄱ-힣a-zA-Z0-9]/g, "");
    setNewNickname(filtered);
  };

  const handleUpdate = async () => {
    setUpdateError("");
    if (!newNickname.trim() && !newPassword.trim()) {
      setUpdateError("닉네임 또는 비밀번호를 입력하세요.");
      return;
    }
    if (!isOAuthUser && (newPassword.trim() || newNickname.trim()) && !currentPassword.trim()) {
      setUpdateError("현재 비밀번호를 입력하세요.");
      return;
    }
    if (newPassword.trim() && newPassword !== confirmPassword) {
      setUpdateError("새 비밀번호가 일치하지 않습니다.");
      return;
    }
    setUpdating(true);
    try {
      const body: any = {};
      if (!isOAuthUser && (newPassword.trim() || newNickname.trim())) body.currentPassword = currentPassword;
      if (newNickname.trim()) body.nickname = newNickname;
      if (newPassword.trim()) body.password = newPassword;
      await api.patch(`/users/${myUserId}`, body);
      if (newNickname.trim()) localStorage.setItem("nickname", newNickname);
      setIsEditing(false);
      setNewNickname("");
      setNewPassword("");
      setCurrentPassword("");
      setConfirmPassword("");
      window.location.reload();
    } catch (err: any) {
      setUpdateError(err.response?.data?.message || "수정에 실패했습니다.");
    } finally {
      setUpdating(false);
    }
  };

  const handleProfileImageChange = async (num: number) => {
    try {
      await api.patch(`/users/${myUserId}`, { profileImage: num });
      localStorage.setItem("profileImage", String(num));
      setProfileImage(num);
      setShowImagePicker(false);
      window.location.reload();
    } catch (err) {
      alert("프로필 이미지 변경에 실패했습니다.");
    }
  };

  const handleWithdraw = async () => {
    if (!isWithdrawConfirming) {
      setIsWithdrawConfirming(true);
      setWithdrawKeyword("");
      return;
    }
    if (withdrawKeyword !== "탈퇴") {
      alert("'탈퇴'를 정확히 입력해주세요.");
      return;
    }
    if (!window.confirm("정말 탈퇴하시겠습니까? 모든 데이터가 삭제됩니다.")) {
      setIsWithdrawConfirming(false);
      setWithdrawKeyword("");
      return;
    }
    setWithdrawing(true);
    try {
      await api.delete("/auth/withdraw");
      localStorage.clear();
      window.location.href = "/";
    } catch (err) {
      alert("탈퇴에 실패했습니다.");
      setWithdrawing(false);
      setIsWithdrawConfirming(false);
      setWithdrawKeyword("");
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
            {/* 프로필 이미지 */}
            <div className="relative">
              <div
                className="cursor-pointer relative group"
                onClick={() => !isAdmin && setShowImagePicker(!showImagePicker)}
              >
                <ProfileAvatar profileImage={profileImage} size={96} />
                {!isAdmin && (
                  <div className="absolute inset-0 rounded-full bg-black/40 opacity-0 group-hover:opacity-100 transition-opacity flex items-center justify-center">
                    <span className="text-white text-xs font-bold">변경</span>
                  </div>
                )}
              </div>

              {showImagePicker && (
                <div className="absolute left-0 top-28 bg-white border-[3px] border-dark rounded-2xl shadow-kitsch p-3 flex gap-2 z-10">
                  {[1, 2, 3, 4].map((num) => (
                    <button
                      key={num}
                      onClick={() => handleProfileImageChange(num)}
                      className={`rounded-full border-[3px] transition-all hover:scale-110 ${
                        profileImage === num ? 'border-primary shadow-kitsch' : 'border-dark'
                      }`}
                    >
                      <ProfileAvatar profileImage={num} size={56} />
                    </button>
                  ))}
                  <button
                    onClick={() => setShowImagePicker(false)}
                    className="absolute -top-2 -right-2 w-6 h-6 bg-dark text-white rounded-full text-xs font-bold flex items-center justify-center"
                  >
                    ✕
                  </button>
                </div>
              )}
            </div>

            <div>
              <h2 className="font-title text-2xl mb-2">{user.nickname}</h2>
              <div className="flex items-center gap-2">
                {isOAuthUser ? (
                  <>
                    {getDisplayUsername(user.username) === "google" && (
                      <span className="px-2 py-1 bg-gradient-to-r from-red-50 to-blue-50 text-gray-700 text-xs font-bold rounded-full border border-gray-200 flex items-center gap-1">
                        <svg width="14" height="14" viewBox="0 0 18 18">
                          <path d="M17.64 9.2c0-.64-.06-1.25-.16-1.84H9v3.48h4.84a4.14 4.14 0 01-1.8 2.72v2.26h2.92a8.78 8.78 0 002.68-6.62z" fill="#4285F4" />
                          <path d="M9 18c2.43 0 4.47-.8 5.96-2.18l-2.92-2.26c-.8.54-1.83.86-3.04.86-2.34 0-4.32-1.58-5.03-3.71H.96v2.33A9 9 0 009 18z" fill="#34A853" />
                          <path d="M3.97 10.71A5.41 5.41 0 013.68 9c0-.6.1-1.17.29-1.71V4.96H.96A9 9 0 000 9c0 1.45.35 2.82.96 4.04l3.01-2.33z" fill="#FBBC05" />
                          <path d="M9 3.58c1.32 0 2.5.45 3.44 1.35l2.58-2.59C13.46.89 11.43 0 9 0A9 9 0 00.96 4.96l3.01 2.33C4.68 5.16 6.66 3.58 9 3.58z" fill="#EA4335" />
                        </svg>
                        Google
                      </span>
                    )}
                    {getDisplayUsername(user.username) === "kakao" && (
                      <span className="px-2 py-1 bg-yellow-50 text-gray-700 text-xs font-bold rounded-full border border-yellow-200 flex items-center gap-1">
                        <svg width="14" height="14" viewBox="0 0 18 18">
                          <path d="M9 1C4.58 1 1 3.79 1 7.21c0 2.17 1.45 4.08 3.63 5.17l-.93 3.42c-.08.3.26.54.52.37l4.1-2.72c.22.02.44.03.68.03 4.42 0 8-2.79 8-6.27S13.42 1 9 1z" fill="#3C1E1E" />
                        </svg>
                        Kakao
                      </span>
                    )}
                  </>
                ) : (
                  <span className="px-2 py-1 bg-gray-50 text-gray-700 text-xs font-bold rounded-full border border-gray-200">
                    @{user.username}
                  </span>
                )}
                {isAdmin && (
                  <span className="px-2 py-1 bg-primary text-white text-xs font-bold rounded-full border border-dark">
                    👑 ADMIN
                  </span>
                )}
              </div>
            </div>
          </div>
          {!isAdmin && (
            <button
              onClick={() => { setIsEditing(!isEditing); setUpdateError(""); }}
              className="px-4 py-2 bg-cream border-[3px] border-dark rounded-xl text-sm font-bold shadow-kitsch-sm hover:shadow-kitsch hover:-translate-y-0.5 transition-all"
            >
              {isEditing ? "✕ 취소" : "✏️ 프로필 수정"}
            </button>
          )}
        </div>

        {/* 수정 폼 */}
        {isEditing && (
          <div className="flex flex-col gap-3 mb-6 p-4 bg-cream border-[3px] border-dark rounded-xl">
            {updateError && <p className="text-sm text-red-500 font-bold">{updateError}</p>}
            {!isOAuthUser && (
              <input
                type="password"
                placeholder="현재 비밀번호 (닉네임/비밀번호 변경 시 필요)"
                value={currentPassword}
                onChange={(e) => setCurrentPassword(e.target.value)}
                onKeyDown={handleCapsLock}
                onKeyUp={handleCapsLock}
                onBlur={() => setIsCapsLockOn(false)}
                className="w-full px-4 py-3 bg-white border-[3px] border-dark rounded-xl text-sm focus:border-primary outline-none"
              />
            )}
            {!isOAuthUser && (
              <>
                <input
                  type="password"
                  placeholder="새 비밀번호 (변경 시에만 입력)"
                  value={newPassword}
                  onChange={(e) => setNewPassword(e.target.value)}
                  onKeyDown={handleCapsLock}
                  onKeyUp={handleCapsLock}
                  onBlur={() => setIsCapsLockOn(false)}
                  className="w-full px-4 py-3 bg-white border-[3px] border-dark rounded-xl text-sm focus:border-primary outline-none"
                />
                <input
                  type="password"
                  placeholder="새 비밀번호 확인"
                  value={confirmPassword}
                  onChange={(e) => setConfirmPassword(e.target.value)}
                  onKeyDown={handleCapsLock}
                  onKeyUp={handleCapsLock}
                  onBlur={() => setIsCapsLockOn(false)}
                  className="w-full px-4 py-3 bg-white border-[3px] border-dark rounded-xl text-sm focus:border-primary outline-none"
                />
              </>
            )}
            {isCapsLockOn && (
              <p className="text-xs text-orange-500 font-bold">Caps Lock이 켜져 있습니다.</p>
            )}
            <input
              type="text"
              placeholder="새 닉네임 (변경 시에만 입력)"
              value={newNickname}
              onChange={(e) => handleNicknameChange(e.target.value)}
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
        <Link href="/admin/reports" className="block bg-primary text-white border-[3px] border-dark rounded-2xl p-6 shadow-kitsch hover:shadow-kitsch-lg hover:-translate-y-0.5 transition-all text-center">
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
          <div className="flex justify-end">
            <button
              onClick={handleWithdraw}
              disabled={withdrawing}
              className="px-4 py-2 text-sm text-red-400 border-2 border-red-200 rounded-xl font-bold hover:border-red-400 hover:text-red-500 transition-all disabled:opacity-50"
            >
              {withdrawing ? "탈퇴 중..." : "회원 탈퇴"}
            </button>
          </div>

          {isWithdrawConfirming && (
            <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50">
              <div className="bg-white border-[3px] border-dark rounded-2xl shadow-kitsch p-8 max-w-sm w-full mx-4">
                <h3 className="font-title text-2xl mb-2 text-red-600">회원 탈퇴 확인</h3>
                <p className="text-sm text-gray-600 mb-4">이 작업은 되돌릴 수 없습니다.</p>
                <p className="text-sm text-gray-600 mb-6">계속하려면 "<span className="font-bold text-red-600">탈퇴</span>"를 입력하세요.</p>
                <input
                  type="text"
                  placeholder="탈퇴"
                  value={withdrawKeyword}
                  onChange={(e) => setWithdrawKeyword(e.target.value)}
                  className="w-full px-4 py-3 bg-cream border-[3px] border-dark rounded-xl text-sm focus:border-red-400 outline-none mb-6"
                />
                <div className="flex gap-3">
                  <button
                    onClick={() => { setIsWithdrawConfirming(false); setWithdrawKeyword(""); }}
                    className="flex-1 py-3 text-sm text-gray-700 bg-cream border-[3px] border-dark rounded-xl font-bold hover:shadow-kitsch transition-all"
                  >
                    취소
                  </button>
                  <button
                    onClick={handleWithdraw}
                    disabled={withdrawing || withdrawKeyword !== "탈퇴"}
                    className="flex-1 py-3 text-sm text-white bg-red-500 border-[3px] border-red-600 rounded-xl font-bold hover:shadow-kitsch transition-all disabled:opacity-50 disabled:cursor-not-allowed"
                  >
                    {withdrawing ? "탈퇴 중..." : "탈퇴"}
                  </button>
                </div>
              </div>
            </div>
          )}
        </>
      )}
    </div>
  );
}