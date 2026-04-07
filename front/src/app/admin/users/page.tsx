"use client";

import { useState, useEffect } from "react";
import api from "@/lib/api";

interface AdminUser {
  id: number;
  username: string;
  nickname: string;
  email: string;
  role: string;
  status: string;
  totalRankingScore: number;
  createdAt: string;
}

export default function AdminUsersPage() {
  const [users, setUsers] = useState<AdminUser[]>([]);
  const [loading, setLoading] = useState(true);
  const [keyword, setKeyword] = useState("");
  const [search, setSearch] = useState("");
  const [page, setPage] = useState(0);
  const [hasNext, setHasNext] = useState(false);
  const [suspendTarget, setSuspendTarget] = useState<AdminUser | null>(null);
  const [suspendReason, setSuspendReason] = useState("");
  const [suspendDays, setSuspendDays] = useState(1);
  const [suspending, setSuspending] = useState(false);

  const fetchUsers = async (kw: string, p: number) => {
    setLoading(true);
    try {
      const res = await api.get("/admin/users", {
        params: { keyword: kw || undefined, page: p, size: 10 },
      });
      setUsers(res.data.data.content);
      setHasNext(res.data.data.hasNext);
    } catch (err) {
      console.error("유저 목록 조회 실패", err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchUsers(search, page);
  }, [search, page]);

  const handleSearch = () => {
    setPage(0);
    setSearch(keyword);
  };

  const handleSuspend = async () => {
    if (!suspendTarget) return;
    setSuspending(true);
    try {
      await api.patch(`/admin/users/${suspendTarget.id}/suspend`, {
        reason: suspendReason,
        suspensionDays: suspendDays,
      });
      alert("정지 처리되었습니다.");
      setSuspendTarget(null);
      setSuspendReason("");
      setSuspendDays(1);
      fetchUsers(search, page);
    } catch (err) {
      alert("정지 처리에 실패했습니다.");
    } finally {
      setSuspending(false);
    }
  };

  const handleDelete = async (user: AdminUser) => {
    if (!window.confirm(`${user.nickname}님을 강제 탈퇴 처리하시겠습니까?`)) return;
    try {
      await api.patch(`/admin/users/${user.id}/delete`);
      alert("탈퇴 처리되었습니다.");
      fetchUsers(search, page);
    } catch (err) {
      alert("탈퇴 처리에 실패했습니다.");
    }
  };

  return (
    <div>
      <h2 className="font-title text-2xl mb-6">👤 사용자 관리</h2>

      {/* 검색 */}
      <div className="flex gap-2 mb-6">
        <input
          type="text"
          placeholder="닉네임 검색..."
          value={keyword}
          onChange={(e) => setKeyword(e.target.value)}
          onKeyDown={(e) => e.key === "Enter" && handleSearch()}
          className="flex-1 px-4 py-3 bg-white border-[3px] border-dark rounded-xl text-sm focus:border-primary outline-none"
        />
        <button
          onClick={handleSearch}
          className="px-6 py-3 bg-primary text-white font-bold border-[3px] border-dark rounded-xl shadow-kitsch-sm hover:shadow-kitsch hover:-translate-y-0.5 transition-all"
        >
          검색
        </button>
      </div>

      {/* 테이블 */}
      <div className="bg-white border-[3px] border-dark rounded-2xl shadow-kitsch overflow-hidden">
        {loading ? (
          <div className="p-10 text-center">
            <p className="font-hand text-lg text-gray-400">불러오는 중...</p>
          </div>
        ) : users.length === 0 ? (
          <div className="p-10 text-center">
            <p className="font-hand text-lg text-gray-400">사용자가 없습니다.</p>
          </div>
        ) : (
          <table className="w-full text-sm">
            <thead className="border-b-[3px] border-dark bg-cream">
              <tr>
                <th className="px-4 py-3 text-left font-bold">닉네임</th>
                <th className="px-4 py-3 text-left font-bold">아이디</th>
                <th className="px-4 py-3 text-left font-bold">권한</th>
                <th className="px-4 py-3 text-left font-bold">상태</th>
                <th className="px-4 py-3 text-left font-bold">랭킹점수</th>
                <th className="px-4 py-3 text-left font-bold">가입일</th>
                <th className="px-4 py-3 text-left font-bold">제재</th>
              </tr>
            </thead>
            <tbody>
              {users.map((user, i) => (
                <tr key={user.id} className={`border-b border-gray-100 ${i % 2 === 0 ? "bg-white" : "bg-cream/50"}`}>
                  <td className="px-4 py-3 font-bold">{user.nickname}</td>
                  <td className="px-4 py-3 text-gray-400">@{user.username}</td>
                  <td className="px-4 py-3">
                    <span className={`px-2 py-0.5 rounded-full text-xs font-bold border ${user.role === "ADMIN" ? "bg-primary text-white border-primary" : "bg-cream border-dark"}`}>
                      {user.role}
                    </span>
                  </td>
                  <td className="px-4 py-3">
                    <span className={`px-2 py-0.5 rounded-full text-xs font-bold border ${user.status === "ACTIVE" ? "bg-green-100 text-green-600 border-green-300" : "bg-red-100 text-red-500 border-red-300"}`}>
                      {user.status}
                    </span>
                  </td>
                  <td className="px-4 py-3 font-title">{user.totalRankingScore.toLocaleString()}</td>
                  <td className="px-4 py-3 text-gray-400">{new Date(user.createdAt).toLocaleDateString("ko-KR")}</td>
                  <td className="px-4 py-3">
                    {user.role !== "ADMIN" && (
                      <div className="flex gap-2">
                        <button
                          onClick={() => setSuspendTarget(user)}
                          className="px-3 py-1 bg-yellow-100 text-yellow-600 border-2 border-yellow-300 rounded-lg text-xs font-bold hover:border-yellow-500 transition-all"
                        >
                          정지
                        </button>
                        <button
                          onClick={() => handleDelete(user)}
                          className="px-3 py-1 bg-red-100 text-red-500 border-2 border-red-200 rounded-lg text-xs font-bold hover:border-red-400 transition-all"
                        >
                          탈퇴
                        </button>
                      </div>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>

      {/* 페이지네이션 */}
      <div className="flex justify-center gap-3 mt-6">
        <button
          onClick={() => setPage((p) => Math.max(0, p - 1))}
          disabled={page === 0}
          className="px-4 py-2 bg-white border-[3px] border-dark rounded-xl font-bold text-sm shadow-kitsch-sm hover:shadow-kitsch hover:-translate-y-0.5 transition-all disabled:opacity-40"
        >
          ← 이전
        </button>
        <span className="px-4 py-2 font-bold text-sm">{page + 1} 페이지</span>
        <button
          onClick={() => setPage((p) => p + 1)}
          disabled={!hasNext}
          className="px-4 py-2 bg-white border-[3px] border-dark rounded-xl font-bold text-sm shadow-kitsch-sm hover:shadow-kitsch hover:-translate-y-0.5 transition-all disabled:opacity-40"
        >
          다음 →
        </button>
      </div>

      {/* 정지 모달 */}
      {suspendTarget && (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50">
          <div className="bg-white border-[3px] border-dark rounded-2xl shadow-kitsch-lg p-8 w-full max-w-md">
            <h3 className="font-title text-xl mb-2">사용자 정지</h3>
            <p className="text-sm text-gray-400 mb-6">{suspendTarget.nickname} (@{suspendTarget.username})</p>
            <div className="flex flex-col gap-4">
              <div>
                <label className="block text-sm font-bold mb-2">정지 사유</label>
                <textarea
                  value={suspendReason}
                  onChange={(e) => setSuspendReason(e.target.value)}
                  placeholder="정지 사유를 입력하세요."
                  rows={3}
                  className="w-full px-4 py-3 bg-cream border-[3px] border-dark rounded-xl text-sm focus:border-primary outline-none resize-none"
                />
              </div>
              <div>
                <label className="block text-sm font-bold mb-2">정지 기간 (일)</label>
                <input
                  type="number"
                  min={1}
                  value={suspendDays}
                  onChange={(e) => setSuspendDays(Number(e.target.value))}
                  className="w-full px-4 py-3 bg-cream border-[3px] border-dark rounded-xl text-sm focus:border-primary outline-none"
                />
              </div>
            </div>
            <div className="flex gap-3 mt-6">
              <button
                onClick={handleSuspend}
                disabled={suspending || !suspendReason.trim()}
                className="flex-1 py-3 bg-yellow-400 font-bold border-[3px] border-dark rounded-xl shadow-kitsch hover:shadow-kitsch-lg hover:-translate-y-0.5 transition-all disabled:opacity-50"
              >
                {suspending ? "처리 중..." : "정지하기"}
              </button>
              <button
                onClick={() => { setSuspendTarget(null); setSuspendReason(""); setSuspendDays(1); }}
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