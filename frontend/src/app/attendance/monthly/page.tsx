"use client";

import { useEffect, useState } from "react";
import { apiClient } from "@/lib/api-client";
import AppLayout from "@/components/AppLayout";
import type { DailyAttendanceResponse, MonthlyAttendanceResponse } from "@/types";

export default function MonthlyAttendancePage() {
  const [yearMonth, setYearMonth] = useState(() => {
    const now = new Date();
    return `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, "0")}`;
  });
  const [monthly, setMonthly] = useState<MonthlyAttendanceResponse | null>(null);
  const [dailyList, setDailyList] = useState<DailyAttendanceResponse[]>([]);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    let cancelled = false;
    Promise.all([
      apiClient<MonthlyAttendanceResponse>(`/api/v1/attendances/monthly?yearMonth=${yearMonth}`).catch(() => null),
      apiClient<DailyAttendanceResponse[]>(`/api/v1/attendances/daily?yearMonth=${yearMonth}`).catch(() => []),
    ]).then(([m, d]) => {
      if (cancelled) return;
      setMonthly(m);
      setDailyList(Array.isArray(d) ? d : []);
      setLoading(false);
    });
    return () => { cancelled = true; };
  }, [yearMonth]);

  function formatMinutes(minutes: number | null): string {
    if (minutes == null) return "--";
    const h = Math.floor(minutes / 60);
    const m = minutes % 60;
    return `${h}h${m > 0 ? `${m}m` : ""}`;
  }

  return (
    <AppLayout>
      <div className="max-w-5xl">
        <div className="flex items-center justify-between mb-6">
          <h2 className="text-2xl font-bold text-gray-800">月次勤怠</h2>
          <input
            type="month"
            value={yearMonth}
            onChange={(e) => setYearMonth(e.target.value)}
            className="px-3 py-2 border border-gray-300 rounded-md"
          />
        </div>

        {monthly && (
          <div className="bg-white rounded-lg shadow p-6 mb-6">
            <div className="grid grid-cols-4 gap-4 text-center">
              <div className="p-3 bg-gray-50 rounded">
                <p className="text-sm text-gray-500">出勤日数</p>
                <p className="text-xl font-bold">{monthly.workingDays}日</p>
              </div>
              <div className="p-3 bg-gray-50 rounded">
                <p className="text-sm text-gray-500">総労働時間</p>
                <p className="text-xl font-bold">{formatMinutes(monthly.totalWorkingMinutes)}</p>
              </div>
              <div className="p-3 bg-gray-50 rounded">
                <p className="text-sm text-gray-500">総残業時間</p>
                <p className="text-xl font-bold">{formatMinutes(monthly.totalOvertimeMinutes)}</p>
              </div>
              <div className="p-3 bg-gray-50 rounded">
                <p className="text-sm text-gray-500">有給取得</p>
                <p className="text-xl font-bold">{monthly.paidLeaveDays}日</p>
              </div>
            </div>
          </div>
        )}

        <div className="bg-white rounded-lg shadow overflow-hidden">
          <table className="w-full text-sm">
            <thead className="bg-gray-50">
              <tr>
                <th className="px-4 py-3 text-left text-gray-600">日付</th>
                <th className="px-4 py-3 text-left text-gray-600">出勤</th>
                <th className="px-4 py-3 text-left text-gray-600">退勤</th>
                <th className="px-4 py-3 text-left text-gray-600">労働時間</th>
                <th className="px-4 py-3 text-left text-gray-600">残業</th>
                <th className="px-4 py-3 text-left text-gray-600">状態</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-100">
              {loading ? (
                <tr>
                  <td colSpan={6} className="px-4 py-8 text-center text-gray-500">読み込み中...</td>
                </tr>
              ) : dailyList.length === 0 ? (
                <tr>
                  <td colSpan={6} className="px-4 py-8 text-center text-gray-500">データがありません</td>
                </tr>
              ) : (
                dailyList.map((d) => (
                  <tr key={d.date} className="hover:bg-gray-50">
                    <td className="px-4 py-3">{d.date}</td>
                    <td className="px-4 py-3">{d.clockIn || "--:--"}</td>
                    <td className="px-4 py-3">{d.clockOut || "--:--"}</td>
                    <td className="px-4 py-3">{formatMinutes(d.workingMinutes)}</td>
                    <td className="px-4 py-3">{formatMinutes(d.overtimeMinutes)}</td>
                    <td className="px-4 py-3">
                      <span className={`px-2 py-1 rounded text-xs font-medium ${
                        d.status === "NORMAL" ? "bg-green-100 text-green-700" :
                        d.status === "PAID_LEAVE" ? "bg-blue-100 text-blue-700" :
                        "bg-gray-100 text-gray-700"
                      }`}>
                        {d.status}
                      </span>
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
