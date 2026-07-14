"use client";

import { useState } from "react";
import { apiClient, ApiClientError } from "@/lib/api-client";
import AppLayout from "@/components/AppLayout";
import type { TimeRecordResponse, TimeRecordType } from "@/types";

export default function TimeRecordPage() {
  const [loading, setLoading] = useState(false);
  const [lastRecord, setLastRecord] = useState<TimeRecordResponse | null>(null);
  const [error, setError] = useState("");

  async function handleRecord(type: TimeRecordType) {
    setError("");
    setLoading(true);
    try {
      const position = await getCurrentPosition();
      const result = await apiClient<TimeRecordResponse>("/api/v1/time-records", {
        method: "POST",
        body: JSON.stringify({
          type,
          latitude: position.latitude,
          longitude: position.longitude,
        }),
      });
      setLastRecord(result);
    } catch (err) {
      if (err instanceof ApiClientError) {
        setError(err.body.message);
      } else if (err instanceof GeolocationPositionError) {
        setError("位置情報の取得に失敗しました。位置情報の許可を確認してください。");
      } else {
        setError("通信エラーが発生しました");
      }
    } finally {
      setLoading(false);
    }
  }

  function getCurrentPosition(): Promise<{ latitude: number; longitude: number }> {
    return new Promise((resolve, reject) => {
      if (!navigator.geolocation) {
        reject(new Error("位置情報が利用できません"));
        return;
      }
      navigator.geolocation.getCurrentPosition(
        (pos) => resolve({ latitude: pos.coords.latitude, longitude: pos.coords.longitude }),
        (err) => reject(err),
        { enableHighAccuracy: true, timeout: 10000 }
      );
    });
  }

  const RECORD_BUTTONS: { type: TimeRecordType; label: string; color: string }[] = [
    { type: "CLOCK_IN", label: "出勤", color: "bg-blue-600 hover:bg-blue-700" },
    { type: "CLOCK_OUT", label: "退勤", color: "bg-green-600 hover:bg-green-700" },
    { type: "BREAK_START", label: "休憩開始", color: "bg-yellow-500 hover:bg-yellow-600" },
    { type: "BREAK_END", label: "休憩終了", color: "bg-yellow-600 hover:bg-yellow-700" },
  ];

  return (
    <AppLayout>
      <div className="max-w-2xl">
        <h2 className="text-2xl font-bold text-gray-800 mb-6">打刻</h2>

        {error && (
          <div className="p-3 bg-red-50 border border-red-200 rounded text-red-700 text-sm mb-4">
            {error}
          </div>
        )}

        <div className="bg-white rounded-lg shadow p-6 mb-6">
          <div className="grid grid-cols-2 gap-4">
            {RECORD_BUTTONS.map((btn) => (
              <button
                key={btn.type}
                onClick={() => handleRecord(btn.type)}
                disabled={loading}
                className={`py-4 px-6 text-white font-medium rounded-lg ${btn.color} disabled:opacity-50 disabled:cursor-not-allowed`}
              >
                {loading ? "処理中..." : btn.label}
              </button>
            ))}
          </div>
        </div>

        {lastRecord && (
          <div className="bg-white rounded-lg shadow p-6">
            <h3 className="text-lg font-semibold text-gray-800 mb-3">打刻結果</h3>
            <dl className="space-y-2 text-sm">
              <div className="flex justify-between">
                <dt className="text-gray-500">種別</dt>
                <dd className="font-medium">{lastRecord.type}</dd>
              </div>
              <div className="flex justify-between">
                <dt className="text-gray-500">時刻</dt>
                <dd className="font-medium">{new Date(lastRecord.recordedAt).toLocaleTimeString("ja-JP")}</dd>
              </div>
              <div className="flex justify-between">
                <dt className="text-gray-500">エリア内</dt>
                <dd className="font-medium">{lastRecord.withinArea ? "はい" : "いいえ"}</dd>
              </div>
              {lastRecord.officeName && (
                <div className="flex justify-between">
                  <dt className="text-gray-500">拠点</dt>
                  <dd className="font-medium">{lastRecord.officeName}</dd>
                </div>
              )}
              {lastRecord.warning && (
                <div className="mt-3 p-3 bg-yellow-50 border border-yellow-200 rounded text-yellow-800 text-sm">
                  {lastRecord.warning}
                </div>
              )}
            </dl>
          </div>
        )}
      </div>
    </AppLayout>
  );
}
