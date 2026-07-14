"use client";

import { useEffect, useState } from "react";
import { apiClient } from "@/lib/api-client";
import AppLayout from "@/components/AppLayout";
import type { ApplicationResponse } from "@/types";

const TYPE_LABELS: Record<string, string> = {
  LEAVE: "休暇",
  OVERTIME: "残業",
  TIME_CORRECTION: "打刻修正",
};

const STATUS_STYLES: Record<string, string> = {
  PENDING: "bg-yellow-100 text-yellow-700",
  APPROVED: "bg-green-100 text-green-700",
  REJECTED: "bg-red-100 text-red-700",
};

const STATUS_LABELS: Record<string, string> = {
  PENDING: "審査中",
  APPROVED: "承認済",
  REJECTED: "差戻",
};

export default function ApplicationsPage() {
  const [applications, setApplications] = useState<ApplicationResponse[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    apiClient<ApplicationResponse[]>("/api/v1/applications")
      .then(setApplications)
      .catch(() => {})
      .finally(() => setLoading(false));
  }, []);

  function handleCancel(id: number) {
    if (!confirm("この申請を取消しますか？")) return;
    apiClient(`/api/v1/applications/${id}`, { method: "DELETE" })
      .then(() => setApplications((prev) => prev.filter((a) => a.id !== id)))
      .catch(() => alert("取消に失敗しました"));
  }

  return (
    <AppLayout>
      <div className="max-w-5xl">
        <h2 className="text-2xl font-bold text-gray-800 mb-6">申請一覧</h2>

        <div className="bg-white rounded-lg shadow overflow-hidden">
          <table className="w-full text-sm">
            <thead className="bg-gray-50">
              <tr>
                <th className="px-4 py-3 text-left text-gray-600">種別</th>
                <th className="px-4 py-3 text-left text-gray-600">申請日時</th>
                <th className="px-4 py-3 text-left text-gray-600">理由</th>
                <th className="px-4 py-3 text-left text-gray-600">状態</th>
                <th className="px-4 py-3 text-left text-gray-600">操作</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-100">
              {loading ? (
                <tr>
                  <td colSpan={5} className="px-4 py-8 text-center text-gray-500">読み込み中...</td>
                </tr>
              ) : applications.length === 0 ? (
                <tr>
                  <td colSpan={5} className="px-4 py-8 text-center text-gray-500">申請がありません</td>
                </tr>
              ) : (
                applications.map((app) => (
                  <tr key={app.id} className="hover:bg-gray-50">
                    <td className="px-4 py-3">{TYPE_LABELS[app.type] || app.type}</td>
                    <td className="px-4 py-3">{new Date(app.appliedAt).toLocaleDateString("ja-JP")}</td>
                    <td className="px-4 py-3 max-w-xs truncate">{app.reason || "-"}</td>
                    <td className="px-4 py-3">
                      <span className={`px-2 py-1 rounded text-xs font-medium ${STATUS_STYLES[app.status]}`}>
                        {STATUS_LABELS[app.status]}
                      </span>
                    </td>
                    <td className="px-4 py-3">
                      {app.status === "PENDING" && (
                        <button
                          onClick={() => handleCancel(app.id)}
                          className="text-red-600 hover:text-red-800 text-xs"
                        >
                          取消
                        </button>
                      )}
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
