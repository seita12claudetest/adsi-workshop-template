"use client";

import { useEffect, useState } from "react";
import { getEmployee } from "@/lib/auth";
import { apiClient } from "@/lib/api-client";
import AppLayout from "@/components/AppLayout";
import type { DailyAttendanceResponse } from "@/types";

export default function DashboardPage() {
  const [employee] = useState(() => getEmployee());
  const [currentTime, setCurrentTime] = useState(new Date());
  const [daily, setDaily] = useState<DailyAttendanceResponse | null>(null);

  useEffect(() => {
    const timer = setInterval(() => setCurrentTime(new Date()), 1000);
    return () => clearInterval(timer);
  }, []);

  useEffect(() => {
    const today = new Date().toISOString().split("T")[0];
    apiClient<DailyAttendanceResponse>(`/api/v1/attendances/daily?date=${today}`)
      .then(setDaily)
      .catch(() => {});
  }, []);

  if (!employee) return null;

  return (
    <AppLayout>
      <div className="max-w-4xl">
        <div className="bg-white rounded-lg shadow p-6 mb-6">
          <p className="text-gray-600 mb-2">
            おはようございます、{employee.name}さん
          </p>
          <p className="text-4xl font-mono font-bold text-gray-800">
            {currentTime.toLocaleTimeString("ja-JP")}
          </p>
          <p className="text-sm text-gray-500 mt-1">
            {currentTime.toLocaleDateString("ja-JP", {
              year: "numeric",
              month: "long",
              day: "numeric",
              weekday: "long",
            })}
          </p>
        </div>

        <div className="bg-white rounded-lg shadow p-6">
          <h2 className="text-lg font-semibold text-gray-800 mb-4">今日の勤怠</h2>
          <div className="grid grid-cols-4 gap-4 text-center">
            <div className="p-4 bg-gray-50 rounded-lg">
              <p className="text-sm text-gray-500">出勤</p>
              <p className="text-lg font-semibold">{daily?.clockIn || "--:--"}</p>
            </div>
            <div className="p-4 bg-gray-50 rounded-lg">
              <p className="text-sm text-gray-500">退勤</p>
              <p className="text-lg font-semibold">{daily?.clockOut || "--:--"}</p>
            </div>
            <div className="p-4 bg-gray-50 rounded-lg">
              <p className="text-sm text-gray-500">労働時間</p>
              <p className="text-lg font-semibold">
                {daily?.workingMinutes != null
                  ? `${Math.floor(daily.workingMinutes / 60)}h${daily.workingMinutes % 60}m`
                  : "--"}
              </p>
            </div>
            <div className="p-4 bg-gray-50 rounded-lg">
              <p className="text-sm text-gray-500">状態</p>
              <p className="text-lg font-semibold">{daily?.status || "未出勤"}</p>
            </div>
          </div>
        </div>
      </div>
    </AppLayout>
  );
}
