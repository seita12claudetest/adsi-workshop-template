"use client";

import { useEffect, useState } from "react";
import { apiClient } from "@/lib/api-client";
import AppLayout from "@/components/AppLayout";
import type { LeaveBalanceSummaryResponse } from "@/types";

export default function LeaveBalancePage() {
  const [summary, setSummary] = useState<LeaveBalanceSummaryResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    apiClient<LeaveBalanceSummaryResponse>("/api/v1/leave-balances")
      .then(setSummary)
      .catch(() => setError("有給残高の取得に失敗しました"))
      .finally(() => setLoading(false));
  }, []);

  return (
    <AppLayout>
      <div className="max-w-4xl">
        <h2 className="text-2xl font-bold text-gray-800 mb-6">有給残高</h2>

        {error && (
          <div className="p-3 bg-red-50 border border-red-200 rounded text-red-700 text-sm mb-4">
            {error}
          </div>
        )}

        {loading ? (
          <p className="text-gray-500">読み込み中...</p>
        ) : summary ? (
          <>
            <div className="bg-white rounded-lg shadow p-6 mb-6">
              <div className="text-center">
                <p className="text-sm text-gray-500">有給残日数（合計）</p>
                <p className="text-4xl font-bold text-blue-600 mt-1">
                  {summary.totalRemainingDays}
                  <span className="text-lg text-gray-500 ml-1">日</span>
                </p>
              </div>
            </div>

            <div className="bg-white rounded-lg shadow overflow-hidden">
              <table className="w-full text-sm">
                <thead className="bg-gray-50">
                  <tr>
                    <th className="px-4 py-3 text-left text-gray-600">年度</th>
                    <th className="px-4 py-3 text-right text-gray-600">付与日数</th>
                    <th className="px-4 py-3 text-right text-gray-600">使用日数</th>
                    <th className="px-4 py-3 text-right text-gray-600">残日数</th>
                    <th className="px-4 py-3 text-left text-gray-600">付与日</th>
                    <th className="px-4 py-3 text-left text-gray-600">有効期限</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-gray-100">
                  {summary.balances.length === 0 ? (
                    <tr>
                      <td colSpan={6} className="px-4 py-8 text-center text-gray-500">
                        データがありません
                      </td>
                    </tr>
                  ) : (
                    summary.balances.map((b) => (
                      <tr key={b.id} className="hover:bg-gray-50">
                        <td className="px-4 py-3 font-medium">{b.fiscalYear}年度</td>
                        <td className="px-4 py-3 text-right">{b.grantedDays}</td>
                        <td className="px-4 py-3 text-right">{b.usedDays}</td>
                        <td className="px-4 py-3 text-right font-bold text-blue-600">
                          {b.remainingDays}
                        </td>
                        <td className="px-4 py-3">{b.grantDate}</td>
                        <td className="px-4 py-3">
                          <span className={isExpiringSoon(b.expiryDate) ? "text-red-600 font-medium" : ""}>
                            {b.expiryDate}
                          </span>
                        </td>
                      </tr>
                    ))
                  )}
                </tbody>
              </table>
            </div>
          </>
        ) : null}
      </div>
    </AppLayout>
  );
}

function isExpiringSoon(expiryDate: string): boolean {
  const expiry = new Date(expiryDate);
  const now = new Date();
  const diffDays = (expiry.getTime() - now.getTime()) / (1000 * 60 * 60 * 24);
  return diffDays <= 30 && diffDays >= 0;
}
