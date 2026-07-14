"use client";

import { useEffect, useState } from "react";
import { apiClient, ApiClientError } from "@/lib/api-client";
import AppLayout from "@/components/AppLayout";
import type { ApplicationResponse } from "@/types";

const TYPE_LABELS: Record<string, string> = {
  LEAVE: "休暇",
  OVERTIME: "残業",
  TIME_CORRECTION: "打刻修正",
};

export default function ApprovalsPage() {
  const [pending, setPending] = useState<ApplicationResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [processingId, setProcessingId] = useState<number | null>(null);

  useEffect(() => {
    let cancelled = false;
    apiClient<ApplicationResponse[]>("/api/v1/approvals/pending")
      .then((data) => { if (!cancelled) setPending(data); })
      .catch(() => {})
      .finally(() => { if (!cancelled) setLoading(false); });
    return () => { cancelled = true; };
  }, []);

  async function handleApprove(applicationId: number) {
    setProcessingId(applicationId);
    try {
      await apiClient(`/api/v1/approvals/${applicationId}/approve`, {
        method: "POST",
        body: JSON.stringify({ comment: "" }),
      });
      setPending((prev) => prev.filter((a) => a.id !== applicationId));
    } catch (err) {
      if (err instanceof ApiClientError) {
        alert(err.body.message);
      }
    } finally {
      setProcessingId(null);
    }
  }

  async function handleReject(applicationId: number) {
    const comment = prompt("差戻コメントを入力してください");
    if (comment === null) return;
    if (!comment.trim()) {
      alert("差戻時はコメントが必須です");
      return;
    }
    setProcessingId(applicationId);
    try {
      await apiClient(`/api/v1/approvals/${applicationId}/reject`, {
        method: "POST",
        body: JSON.stringify({ comment }),
      });
      setPending((prev) => prev.filter((a) => a.id !== applicationId));
    } catch (err) {
      if (err instanceof ApiClientError) {
        alert(err.body.message);
      }
    } finally {
      setProcessingId(null);
    }
  }

  return (
    <AppLayout>
      <div className="max-w-5xl">
        <h2 className="text-2xl font-bold text-gray-800 mb-6">承認一覧</h2>

        <div className="bg-white rounded-lg shadow overflow-hidden">
          <table className="w-full text-sm">
            <thead className="bg-gray-50">
              <tr>
                <th className="px-4 py-3 text-left text-gray-600">申請者</th>
                <th className="px-4 py-3 text-left text-gray-600">種別</th>
                <th className="px-4 py-3 text-left text-gray-600">申請日</th>
                <th className="px-4 py-3 text-left text-gray-600">理由</th>
                <th className="px-4 py-3 text-left text-gray-600">操作</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-100">
              {loading ? (
                <tr>
                  <td colSpan={5} className="px-4 py-8 text-center text-gray-500">読み込み中...</td>
                </tr>
              ) : pending.length === 0 ? (
                <tr>
                  <td colSpan={5} className="px-4 py-8 text-center text-gray-500">未処理の承認はありません</td>
                </tr>
              ) : (
                pending.map((app) => (
                  <tr key={app.id} className="hover:bg-gray-50">
                    <td className="px-4 py-3 font-medium">{app.applicantName}</td>
                    <td className="px-4 py-3">{TYPE_LABELS[app.type] || app.type}</td>
                    <td className="px-4 py-3">{new Date(app.appliedAt).toLocaleDateString("ja-JP")}</td>
                    <td className="px-4 py-3 max-w-xs truncate">{app.reason || "-"}</td>
                    <td className="px-4 py-3">
                      <div className="flex gap-2">
                        <button
                          onClick={() => handleApprove(app.id)}
                          disabled={processingId === app.id}
                          className="px-3 py-1 bg-green-600 text-white text-xs rounded hover:bg-green-700 disabled:opacity-50"
                        >
                          承認
                        </button>
                        <button
                          onClick={() => handleReject(app.id)}
                          disabled={processingId === app.id}
                          className="px-3 py-1 bg-red-600 text-white text-xs rounded hover:bg-red-700 disabled:opacity-50"
                        >
                          差戻
                        </button>
                      </div>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>
    </AppLayout>
  );
}
