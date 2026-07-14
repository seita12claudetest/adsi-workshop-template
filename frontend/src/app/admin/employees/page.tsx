"use client";

import { useEffect, useState } from "react";
import { apiClient } from "@/lib/api-client";
import AppLayout from "@/components/AppLayout";
import type { Employee } from "@/types";

export default function EmployeesPage() {
  const [employees, setEmployees] = useState<Employee[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    apiClient<Employee[]>("/api/v1/employees")
      .then(setEmployees)
      .catch(() => {})
      .finally(() => setLoading(false));
  }, []);

  return (
    <AppLayout>
      <div className="max-w-6xl">
        <h2 className="text-2xl font-bold text-gray-800 mb-6">社員管理</h2>

        <div className="bg-white rounded-lg shadow overflow-hidden">
          <table className="w-full text-sm">
            <thead className="bg-gray-50">
              <tr>
                <th className="px-4 py-3 text-left text-gray-600">社員コード</th>
                <th className="px-4 py-3 text-left text-gray-600">氏名</th>
                <th className="px-4 py-3 text-left text-gray-600">メール</th>
                <th className="px-4 py-3 text-left text-gray-600">ロール</th>
                <th className="px-4 py-3 text-left text-gray-600">入社日</th>
                <th className="px-4 py-3 text-left text-gray-600">状態</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-100">
              {loading ? (
                <tr>
                  <td colSpan={6} className="px-4 py-8 text-center text-gray-500">読み込み中...</td>
                </tr>
              ) : employees.length === 0 ? (
                <tr>
                  <td colSpan={6} className="px-4 py-8 text-center text-gray-500">社員が登録されていません</td>
                </tr>
              ) : (
                employees.map((emp) => (
                  <tr key={emp.id} className="hover:bg-gray-50">
                    <td className="px-4 py-3 font-mono">{emp.employeeCode}</td>
                    <td className="px-4 py-3 font-medium">{emp.name}</td>
                    <td className="px-4 py-3">{emp.email}</td>
                    <td className="px-4 py-3">
                      <span className={`px-2 py-1 rounded text-xs font-medium ${
                        emp.role === "ADMIN" ? "bg-purple-100 text-purple-700" :
                        emp.role === "MANAGER" ? "bg-blue-100 text-blue-700" :
                        "bg-gray-100 text-gray-700"
                      }`}>
                        {emp.role}
                      </span>
                    </td>
                    <td className="px-4 py-3">{emp.hireDate}</td>
                    <td className="px-4 py-3">
                      <span className={`px-2 py-1 rounded text-xs font-medium ${
                        emp.active ? "bg-green-100 text-green-700" : "bg-red-100 text-red-700"
                      }`}>
                        {emp.active ? "有効" : "無効"}
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
