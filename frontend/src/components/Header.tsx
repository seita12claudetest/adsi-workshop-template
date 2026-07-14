"use client";

import { useRouter } from "next/navigation";
import { getEmployee, logout } from "@/lib/auth";

export default function Header() {
  const router = useRouter();
  const employee = getEmployee();

  function handleLogout() {
    logout();
    router.push("/login");
  }

  return (
    <header className="bg-white border-b border-gray-200 px-6 py-3 flex items-center justify-between">
      <h1 className="text-lg font-bold text-gray-800">勤怠管理システム</h1>
      <div className="flex items-center gap-4">
        {employee && (
          <span className="text-sm text-gray-600">
            {employee.name}（{employee.role}）
          </span>
        )}
        <button
          onClick={handleLogout}
          className="text-sm text-gray-500 hover:text-gray-700"
        >
          ログアウト
        </button>
      </div>
    </header>
  );
}
