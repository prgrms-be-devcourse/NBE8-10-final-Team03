"use client";

import { useState, useEffect } from "react";
import { useRouter } from "next/navigation";
import api from "@/lib/api";

interface ReportItem {
  id: number;
  quizSetId: number;
  quizSetTitle: string;
  reporterNickname: string;
  reason: string;
  status: "PENDING" | "PROCESSED";
  createdAt: string;
}

export default function AdminReportsPage() {
  const router = useRouter();
  const [reports, setReports] = useState<ReportItem[]>([]);
  const [loading, setLoading] = useState(true);
  const [filter, setFilter] = useState<"ALL" | "PENDING" | "PROCESSED">("ALL");

  useEffect(() => {
    fetchReports();
  }, []);

  const fetchReports = async () => {
    setLoading(true);
    try {
      const res = await api.get("/admin/reports");
      setReports(res.data.data);
    } catch (err: any) {
      if (err.response?.status === 403) {
        alert("관리자 권한이 필요합니다.");
        router.push("/");
      }
    } finally {
      setLoading(false);
    }
  };

  const handleProcess = async (id: number) => {
    if (!confirm("신고를 처리 완료로 변경할까요?")) return;
    try {
      await api.patch(`/admin/reports/${id}/process`);
      setReports((prev) => prev.map((r) => r.id === id ? { ...r, status: "PROCESSED" } : r));
    } catch (err) {
      alert("처리에 실패했습니다.");
    }
  };

  const handleDelete = async (id: number) => {
    if (!confirm("신고를 삭제할까요?")) return;
    try {
      await api.delete(`/admin/reports/${id}`);
      setReports((prev) => prev.filter((r) => r.id !== id));
    } catch (err) {
      alert("삭제에 실패했습니다.");
    }
  };

  const filteredReports = reports.filter((r) => {
    if (filter === "ALL") return true;
    return r.status === filter;
  });

  if (loading) return (
    <div className="text-center py-20">
      <p className="font-hand text-xl text-gray-400">불러오는 중...</p>
    </div>
  );

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <h2 className="font-title text-2xl">🚨 신고 관리</h2>
        <div className="flex gap-2">
          {(["ALL", "PENDING", "PROCESSED"] as const).map((f) => (
            <button
              key={f}
              onClick={() => setFilter(f)}
              className={`px-4 py-2 border-[3px] border-dark rounded-full font-bold text-sm transition-colors ${filter === f ? "bg-secondary" : "bg-white hover:bg-gray-50"}`}
            >
              {f === "ALL" ? "전체" : f === "PENDING" ? "미처리" : "처리완료"}
            </button>
          ))}
        </div>
      </div>

      {filteredReports.length === 0 ? (
        <div className="bg-white border-[3px] border-dark rounded-2xl p-10 text-center shadow-kitsch">
          <p className="font-hand text-lg text-gray-400">신고 내역이 없어요!</p>
        </div>
      ) : (
        <div className="bg-white border-[3px] border-dark rounded-2xl shadow-kitsch overflow-hidden">
          <div className="grid grid-cols-[1fr_1fr_2fr_100px_120px] gap-4 px-6 py-3 bg-cream border-b-[3px] border-dark text-sm font-bold text-gray-500">
            <span>퀴즈셋</span>
            <span>신고자</span>
            <span>사유</span>
            <span>상태</span>
            <span>액션</span>
          </div>
          {filteredReports.map((r) => (
            <div key={r.id} className="grid grid-cols-[1fr_1fr_2fr_100px_120px] gap-4 px-6 py-4 border-b-2 border-dashed border-gray-200 last:border-b-0 items-center">
              <button
                onClick={() => router.push(`/quizsets/${r.quizSetId}`)}
                className="text-sm font-bold text-primary hover:underline text-left truncate"
              >
                {r.quizSetTitle}
              </button>
              <span className="text-sm">{r.reporterNickname}</span>
              <span className="text-sm text-gray-600 truncate">{r.reason}</span>
              <span className={`px-2 py-1 rounded-lg text-xs font-bold text-center ${r.status === "PENDING" ? "bg-red-100 text-red-600" : "bg-green-100 text-green-600"}`}>
                {r.status === "PENDING" ? "미처리" : "처리완료"}
              </span>
              <div className="flex gap-2">
                {r.status === "PENDING" && (
                  <button
                    onClick={() => handleProcess(r.id)}
                    className="px-2 py-1 bg-primary text-white border-2 border-dark rounded-lg text-xs font-bold"
                  >
                    처리
                  </button>
                )}
                <button
                  onClick={() => handleDelete(r.id)}
                  className="px-2 py-1 bg-red-500 text-white border-2 border-dark rounded-lg text-xs font-bold"
                >
                  삭제
                </button>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}