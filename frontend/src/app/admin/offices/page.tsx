"use client";

import { useEffect, useState } from "react";
import { apiClient } from "@/lib/api-client";
import AppLayout from "@/components/AppLayout";
import type { Office } from "@/types";

export default function OfficesPage() {
  const [offices, setOffices] = useState<Office[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    apiClient<Office[]>("/api/v1/offices")
      .then(setOffices)
      .catch(() => {})
      .finally(() => setLoading(false));
  }, []);

  return (
    <AppLayout>
      <div className="max-w-5xl">
        <h2 className="text-2xl font-bold text-gray-800 mb-6">拠点管理</h2>

        <div className="bg-white rounded-lg shadow overflow-hidden">
          <table className="w-full text-sm">
            <thead className="bg-gray-50">
              <tr>
                <th className="px-4 py-3 text-left text-gray-600">拠点名</th>
                <th className="px-4 py-3 text-left text-gray-600">住所</th>
                <th className="px-4 py-3 text-left text-gray-600">緯度</th>
                <th className="px-4 py-3 text-left text-gray-600">経度</th>
                <th className="px-4 py-3 text-left text-gray-600">許可半径</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-100">
              {loading ? (
                <tr>
                  <td colSpan={5} className="px-4 py-8 text-center text-gray-500">読み込み中...</td>
                </tr>
              ) : offices.length === 0 ? (
                <tr>
                  <td colSpan={5} className="px-4 py-8 text-center text-gray-500">拠点が登録されていません</td>
                </tr>
              ) : (
                offices.map((office) => (
                  <tr key={office.id} className="hover:bg-gray-50">
                    <td className="px-4 py-3 font-medium">{office.name}</td>
                    <td className="px-4 py-3">{office.address}</td>
                    <td className="px-4 py-3 font-mono text-xs">{office.latitude}</td>
                    <td className="px-4 py-3 font-mono text-xs">{office.longitude}</td>
                    <td className="px-4 py-3">{office.radiusMeters}m</td>
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
