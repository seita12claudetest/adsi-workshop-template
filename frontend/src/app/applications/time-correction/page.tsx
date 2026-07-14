"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import { apiClient, ApiClientError } from "@/lib/api-client";
import AppLayout from "@/components/AppLayout";

export default function TimeCorrectionApplicationPage() {
  const router = useRouter();
  const [date, setDate] = useState("");
  const [correctedClockIn, setCorrectedClockIn] = useState("");
  const [correctedClockOut, setCorrectedClockOut] = useState("");
  const [reason, setReason] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    setError("");
    setLoading(true);

    try {
      await apiClient("/api/v1/applications/time-correction", {
        method: "POST",
        body: JSON.stringify({
          date,
          correctedClockIn: correctedClockIn || null,
          correctedClockOut: correctedClockOut || null,
          reason,
        }),
      });
      router.push("/applications");
    } catch (err) {
      if (err instanceof ApiClientError) {
        setError(err.body.message);
      } else {
        setError("通信エラーが発生しました");
      }
    } finally {
      setLoading(false);
    }
  }

  return (
    <AppLayout>
      <div className="max-w-2xl">
        <h2 className="text-2xl font-bold text-gray-800 mb-6">打刻修正申請</h2>

        <form onSubmit={handleSubmit} className="bg-white rounded-lg shadow p-6 space-y-4">
          {error && (
            <div className="p-3 bg-red-50 border border-red-200 rounded text-red-700 text-sm">
              {error}
            </div>
          )}

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">対象日</label>
            <input
              type="date"
              value={date}
              onChange={(e) => setDate(e.target.value)}
              required
              className="w-full px-3 py-2 border border-gray-300 rounded-md"
            />
          </div>

          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">修正後出勤時刻</label>
              <input
                type="time"
                value={correctedClockIn}
                onChange={(e) => setCorrectedClockIn(e.target.value)}
                className="w-full px-3 py-2 border border-gray-300 rounded-md"
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">修正後退勤時刻</label>
              <input
                type="time"
                value={correctedClockOut}
                onChange={(e) => setCorrectedClockOut(e.target.value)}
                className="w-full px-3 py-2 border border-gray-300 rounded-md"
              />
            </div>
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">理由</label>
            <textarea
              value={reason}
              onChange={(e) => setReason(e.target.value)}
              rows={3}
              required
              className="w-full px-3 py-2 border border-gray-300 rounded-md"
              placeholder="修正理由を入力してください（例: 退勤打刻忘れ）"
            />
          </div>

          <button
            type="submit"
            disabled={loading}
            className="w-full py-2 px-4 bg-blue-600 text-white font-medium rounded-md hover:bg-blue-700 disabled:opacity-50"
          >
            {loading ? "申請中..." : "申請する"}
          </button>
        </form>
      </div>
    </AppLayout>
  );
}
