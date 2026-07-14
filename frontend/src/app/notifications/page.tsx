"use client";

import { useEffect, useState } from "react";
import { apiClient } from "@/lib/api-client";
import AppLayout from "@/components/AppLayout";
import type { NotificationResponse, NotificationType, PageResponse } from "@/types";

const TYPE_LABELS: Record<NotificationType, string> = {
  CLOCK_REMINDER: "打刻リマインド",
  APPROVAL_REQUEST: "承認依頼",
  APPROVAL_RESULT: "承認結果",
  LEAVE_BALANCE_ALERT: "有給残日数",
};

const TYPE_COLORS: Record<NotificationType, string> = {
  CLOCK_REMINDER: "bg-yellow-100 text-yellow-700",
  APPROVAL_REQUEST: "bg-blue-100 text-blue-700",
  APPROVAL_RESULT: "bg-green-100 text-green-700",
  LEAVE_BALANCE_ALERT: "bg-purple-100 text-purple-700",
};

export default function NotificationsPage() {
  const [notifications, setNotifications] = useState<NotificationResponse[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [filterType, setFilterType] = useState<NotificationType | "">("");

  useEffect(() => {
    let cancelled = false;
    const params = new URLSearchParams({ page: String(page), size: "20" });
    if (filterType) params.set("type", filterType);
    apiClient<PageResponse<NotificationResponse>>(`/api/v1/notifications?${params}`)
      .then((data) => {
        if (cancelled) return;
        setNotifications(data.content);
        setTotalPages(data.totalPages);
      })
      .catch(() => {
        if (!cancelled) setError("通知の取得に失敗しました");
      })
      .finally(() => {
        if (!cancelled) setLoading(false);
      });
    return () => { cancelled = true; };
  }, [page, filterType]);

  async function markAsRead(id: number) {
    try {
      await apiClient<NotificationResponse>(`/api/v1/notifications/${id}/read`, { method: "PUT" });
      setNotifications((prev) =>
        prev.map((n) => (n.id === id ? { ...n, read: true } : n))
      );
    } catch { /* ignore */ }
  }

  async function markAllAsRead() {
    try {
      await apiClient<void>("/api/v1/notifications/read-all", { method: "PUT" });
      setNotifications((prev) => prev.map((n) => ({ ...n, read: true })));
    } catch { /* ignore */ }
  }

  function formatDate(dateStr: string): string {
    const d = new Date(dateStr);
    return `${d.getFullYear()}/${String(d.getMonth() + 1).padStart(2, "0")}/${String(d.getDate()).padStart(2, "0")} ${String(d.getHours()).padStart(2, "0")}:${String(d.getMinutes()).padStart(2, "0")}`;
  }

  return (
    <AppLayout>
      <div className="max-w-4xl">
        <div className="flex items-center justify-between mb-6">
          <h2 className="text-2xl font-bold text-gray-800">通知</h2>
          <div className="flex gap-3">
            <select
              value={filterType}
              onChange={(e) => { setFilterType(e.target.value as NotificationType | ""); setPage(0); }}
              className="px-3 py-2 border border-gray-300 rounded-md text-sm"
            >
              <option value="">すべて</option>
              {Object.entries(TYPE_LABELS).map(([key, label]) => (
                <option key={key} value={key}>{label}</option>
              ))}
            </select>
            <button
              onClick={markAllAsRead}
              className="px-3 py-2 text-sm bg-gray-100 text-gray-700 rounded-md hover:bg-gray-200"
            >
              すべて既読
            </button>
          </div>
        </div>

        {error && (
          <div className="p-3 bg-red-50 border border-red-200 rounded text-red-700 text-sm mb-4">
            {error}
          </div>
        )}

        <div className="bg-white rounded-lg shadow overflow-hidden">
          {loading ? (
            <p className="p-8 text-center text-gray-500">読み込み中...</p>
          ) : notifications.length === 0 ? (
            <p className="p-8 text-center text-gray-500">通知はありません</p>
          ) : (
            <ul className="divide-y divide-gray-100">
              {notifications.map((n) => (
                <li
                  key={n.id}
                  className={`px-4 py-4 hover:bg-gray-50 cursor-pointer ${!n.read ? "bg-blue-50/50" : ""}`}
                  onClick={() => !n.read && markAsRead(n.id)}
                >
                  <div className="flex items-start gap-3">
                    {!n.read && (
                      <span className="mt-1.5 w-2 h-2 bg-blue-500 rounded-full flex-shrink-0" />
                    )}
                    <div className="flex-1 min-w-0">
                      <div className="flex items-center gap-2 mb-1">
                        <span className={`px-2 py-0.5 rounded text-xs font-medium ${TYPE_COLORS[n.type]}`}>
                          {TYPE_LABELS[n.type]}
                        </span>
                        <span className="text-xs text-gray-400">{formatDate(n.createdAt)}</span>
                      </div>
                      <p className="text-sm font-medium text-gray-800">{n.title}</p>
                      <p className="text-sm text-gray-500 mt-0.5">{n.message}</p>
                    </div>
                  </div>
                </li>
              ))}
            </ul>
          )}
        </div>

        {totalPages > 1 && (
          <div className="flex justify-center gap-2 mt-4">
            <button
              onClick={() => setPage((p) => Math.max(0, p - 1))}
              disabled={page === 0}
              className="px-3 py-1 text-sm border rounded disabled:opacity-50"
            >
              前へ
            </button>
            <span className="px-3 py-1 text-sm text-gray-600">
              {page + 1} / {totalPages}
            </span>
            <button
              onClick={() => setPage((p) => Math.min(totalPages - 1, p + 1))}
              disabled={page >= totalPages - 1}
              className="px-3 py-1 text-sm border rounded disabled:opacity-50"
            >
              次へ
            </button>
          </div>
        )}
      </div>
    </AppLayout>
  );
}
