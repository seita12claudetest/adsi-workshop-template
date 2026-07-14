"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import { apiClient, ApiClientError } from "@/lib/api-client";
import AppLayout from "@/components/AppLayout";

const LEAVE_TYPES = [
  { value: "ANNUAL", label: "年次有給休暇" },
  { value: "HALF_AM", label: "半休（午前）" },
  { value: "HALF_PM", label: "半休（午後）" },
  { value: "HOURLY", label: "時間有給" },
  { value: "CONDOLENCE", label: "慶弔休暇" },
  { value: "CHILDCARE", label: "育児休暇" },
  { value: "NURSING", label: "介護休暇" },
  { value: "MENSTRUAL", label: "生理休暇" },
];

export default function LeaveApplicationPage() {
  const router = useRouter();
  const [leaveType, setLeaveType] = useState("ANNUAL");
  const [startDate, setStartDate] = useState("");
  const [endDate, setEndDate] = useState("");
  const [hours, setHours] = useState("");
  const [reason, setReason] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    setError("");
    setLoading(true);

    try {
      await apiClient("/api/v1/applications/leave", {
        method: "POST",
        body: JSON.stringify({
          leaveType,
          startDate,
          endDate: endDate || startDate,
          hours: leaveType === "HOURLY" ? parseFloat(hours) : null,
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
        <h2 className="text-2xl font-bold text-gray-800 mb-6">休暇申請</h2>

        <form onSubmit={handleSubmit} className="bg-white rounded-lg shadow p-6 space-y-4">
          {error && (
            <div className="p-3 bg-red-50 border border-red-200 rounded text-red-700 text-sm">
              {error}
            </div>
          )}

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">休暇種別</label>
            <select
              value={leaveType}
              onChange={(e) => setLeaveType(e.target.value)}
              className="w-full px-3 py-2 border border-gray-300 rounded-md"
            >
              {LEAVE_TYPES.map((t) => (
                <option key={t.value} value={t.value}>{t.label}</option>
              ))}
            </select>
          </div>

          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">開始日</label>
              <input
                type="date"
                value={startDate}
                onChange={(e) => setStartDate(e.target.value)}
                required
                className="w-full px-3 py-2 border border-gray-300 rounded-md"
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">終了日</label>
              <input
                type="date"
                value={endDate}
                onChange={(e) => setEndDate(e.target.value)}
                className="w-full px-3 py-2 border border-gray-300 rounded-md"
              />
            </div>
          </div>

          {leaveType === "HOURLY" && (
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">時間数</label>
              <input
                type="number"
                step="0.5"
                min="0.5"
                max="8"
                value={hours}
                onChange={(e) => setHours(e.target.value)}
                required
                className="w-full px-3 py-2 border border-gray-300 rounded-md"
              />
            </div>
          )}

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">理由</label>
            <textarea
              value={reason}
              onChange={(e) => setReason(e.target.value)}
              rows={3}
              className="w-full px-3 py-2 border border-gray-300 rounded-md"
              placeholder="申請理由を入力してください"
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
