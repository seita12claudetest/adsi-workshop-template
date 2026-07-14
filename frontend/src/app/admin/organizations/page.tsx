"use client";

import { useEffect, useState } from "react";
import { apiClient } from "@/lib/api-client";
import AppLayout from "@/components/AppLayout";
import type { Organization, Department, Section } from "@/types";

export default function OrganizationsPage() {
  const [organizations, setOrganizations] = useState<Organization[]>([]);
  const [departments, setDepartments] = useState<Department[]>([]);
  const [sections, setSections] = useState<Section[]>([]);
  const [selectedOrgId, setSelectedOrgId] = useState<number | null>(null);
  const [selectedDeptId, setSelectedDeptId] = useState<number | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    apiClient<Organization[]>("/api/v1/organizations")
      .then(setOrganizations)
      .catch(() => {})
      .finally(() => setLoading(false));
  }, []);

  useEffect(() => {
    if (selectedOrgId) {
      apiClient<Department[]>(`/api/v1/departments?organizationId=${selectedOrgId}`)
        .then((data) => {
          setDepartments(data);
          setSelectedDeptId(null);
          setSections([]);
        })
        .catch(() => {
          setDepartments([]);
          setSelectedDeptId(null);
          setSections([]);
        });
    }
  }, [selectedOrgId]);

  useEffect(() => {
    if (selectedDeptId) {
      apiClient<Section[]>(`/api/v1/sections?departmentId=${selectedDeptId}`)
        .then(setSections)
        .catch(() => setSections([]));
    }
  }, [selectedDeptId]);

  return (
    <AppLayout>
      <div className="max-w-6xl">
        <h2 className="text-2xl font-bold text-gray-800 mb-6">組織管理</h2>

        <div className="grid grid-cols-3 gap-6">
          <div className="bg-white rounded-lg shadow p-4">
            <h3 className="font-semibold text-gray-800 mb-3">本部</h3>
            {loading ? (
              <p className="text-gray-500 text-sm">読み込み中...</p>
            ) : (
              <ul className="space-y-1">
                {organizations.map((org) => (
                  <li key={org.id}>
                    <button
                      onClick={() => setSelectedOrgId(org.id)}
                      className={`w-full text-left px-3 py-2 rounded text-sm ${
                        selectedOrgId === org.id ? "bg-blue-50 text-blue-700" : "hover:bg-gray-50"
                      }`}
                    >
                      {org.name} ({org.code})
                    </button>
                  </li>
                ))}
              </ul>
            )}
          </div>

          <div className="bg-white rounded-lg shadow p-4">
            <h3 className="font-semibold text-gray-800 mb-3">部</h3>
            {departments.length === 0 ? (
              <p className="text-gray-500 text-sm">本部を選択してください</p>
            ) : (
              <ul className="space-y-1">
                {departments.map((dept) => (
                  <li key={dept.id}>
                    <button
                      onClick={() => setSelectedDeptId(dept.id)}
                      className={`w-full text-left px-3 py-2 rounded text-sm ${
                        selectedDeptId === dept.id ? "bg-blue-50 text-blue-700" : "hover:bg-gray-50"
                      }`}
                    >
                      {dept.name} ({dept.code})
                    </button>
                  </li>
                ))}
              </ul>
            )}
          </div>

          <div className="bg-white rounded-lg shadow p-4">
            <h3 className="font-semibold text-gray-800 mb-3">課</h3>
            {sections.length === 0 ? (
              <p className="text-gray-500 text-sm">部を選択してください</p>
            ) : (
              <ul className="space-y-1">
                {sections.map((sec) => (
                  <li key={sec.id} className="px-3 py-2 text-sm border-b border-gray-100 last:border-0">
                    <span className="font-medium">{sec.name}</span>
                    <span className="text-gray-500 ml-2">({sec.code})</span>
                  </li>
                ))}
              </ul>
            )}
          </div>
        </div>
      </div>
    </AppLayout>
  );
}
