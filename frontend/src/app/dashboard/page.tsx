"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { getEmployee, isAuthenticated, logout } from "@/lib/auth";

export default function DashboardPage() {
  const router = useRouter();
  const [employee, setEmployee] = useState<{
    name: string;
    role: string;
  } | null>(null);
  const [currentTime, setCurrentTime] = useState(new Date());
  const [clockedIn, setClockedIn] = useState(false);
  const [clockInTime, setClockInTime] = useState<string | null>(null);

  useEffect(() => {
    if (!isAuthenticated()) {
      router.replace("/login");
      return;
    }
    setEmployee(getEmployee());
  }, [router]);

  useEffect(() => {
    const timer = setInterval(() => setCurrentTime(new Date()), 1000);
    return () => clearInterval(timer);
  }, []);

  function handleClockIn() {
    const now = new Date();
    setClockInTime(now.toLocaleTimeString("ja-JP", { hour: "2-digit", minute: "2-digit" }));
    setClockedIn(true);
  }

  function handleClockOut() {
    setClockedIn(false);
    setClockInTime(null);
  }

  function handleLogout() {
    logout();
    router.push("/login");
  }

  if (!employee) return null;

  return (
    <div className="min-h-screen bg-gray-50">
      <header className="bg-white shadow-sm">
        <div className="max-w-7xl mx-auto px-4 py-4 flex items-center justify-between">
          <h1 className="text-xl font-bold text-gray-800">勤怠管理システム</h1>
          <div className="flex items-center gap-4">
            <span className="text-sm text-gray-600">
              {employee.name}（{employee.role}）
            </span>
            <button
              onClick={handleLogout}
              className="text-sm text-gray-500 hover:text-gray-700"
            >
              ログアウト
            </button>
          </div>
        </div>
      </header>

      <main className="max-w-4xl mx-auto px-4 py-8">
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

        <div className="bg-white rounded-lg shadow p-6 mb-6">
          <h2 className="text-lg font-semibold text-gray-800 mb-4">打刻</h2>

          {clockInTime && (
            <p className="text-sm text-gray-600 mb-4">
              出勤時刻: {clockInTime}
            </p>
          )}

          <div className="flex gap-4">
            <button
              onClick={handleClockIn}
              disabled={clockedIn}
              className="flex-1 py-3 px-6 bg-blue-600 text-white font-medium rounded-lg hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed"
            >
              出勤
            </button>
            <button
              onClick={handleClockOut}
              disabled={!clockedIn}
              className="flex-1 py-3 px-6 bg-green-600 text-white font-medium rounded-lg hover:bg-green-700 disabled:opacity-50 disabled:cursor-not-allowed"
            >
              退勤
            </button>
          </div>

          <div className="flex gap-4 mt-4">
            <button className="flex-1 py-2 px-4 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50">
              休憩開始
            </button>
            <button className="flex-1 py-2 px-4 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50">
              休憩終了
            </button>
          </div>
        </div>

        <div className="bg-white rounded-lg shadow p-6">
          <h2 className="text-lg font-semibold text-gray-800 mb-4">
            今日の勤怠状態
          </h2>
          <div className="grid grid-cols-3 gap-4 text-center">
            <div className="p-4 bg-gray-50 rounded-lg">
              <p className="text-sm text-gray-500">出勤</p>
              <p className="text-lg font-semibold">
                {clockInTime || "--:--"}
              </p>
            </div>
            <div className="p-4 bg-gray-50 rounded-lg">
              <p className="text-sm text-gray-500">退勤</p>
              <p className="text-lg font-semibold">--:--</p>
            </div>
            <div className="p-4 bg-gray-50 rounded-lg">
              <p className="text-sm text-gray-500">状態</p>
              <p className="text-lg font-semibold">
                {clockedIn ? "勤務中" : "未出勤"}
              </p>
            </div>
          </div>
        </div>
      </main>
    </div>
  );
}
