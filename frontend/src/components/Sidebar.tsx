"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { getEmployee } from "@/lib/auth";
import type { Role } from "@/types";

interface NavItem {
  href: string;
  label: string;
  roles: Role[];
}

const NAV_ITEMS: NavItem[] = [
  { href: "/dashboard", label: "ダッシュボード", roles: ["EMPLOYEE", "MANAGER", "ADMIN"] },
  { href: "/time-record", label: "打刻", roles: ["EMPLOYEE", "MANAGER", "ADMIN"] },
  { href: "/attendance/monthly", label: "月次勤怠", roles: ["EMPLOYEE", "MANAGER", "ADMIN"] },
  { href: "/applications", label: "申請一覧", roles: ["EMPLOYEE", "MANAGER", "ADMIN"] },
  { href: "/applications/leave", label: "休暇申請", roles: ["EMPLOYEE", "MANAGER", "ADMIN"] },
  { href: "/applications/overtime", label: "残業申請", roles: ["EMPLOYEE", "MANAGER", "ADMIN"] },
  { href: "/applications/time-correction", label: "打刻修正", roles: ["EMPLOYEE", "MANAGER", "ADMIN"] },
  { href: "/leave-balance", label: "有給残高", roles: ["EMPLOYEE", "MANAGER", "ADMIN"] },
  { href: "/notifications", label: "通知", roles: ["EMPLOYEE", "MANAGER", "ADMIN"] },
  { href: "/approvals", label: "承認一覧", roles: ["MANAGER", "ADMIN"] },
  { href: "/admin/employees", label: "社員管理", roles: ["ADMIN"] },
  { href: "/admin/organizations", label: "組織管理", roles: ["ADMIN"] },
  { href: "/admin/offices", label: "拠点管理", roles: ["ADMIN"] },
];

export default function Sidebar() {
  const pathname = usePathname();
  const employee = getEmployee();
  const role = (employee?.role || "EMPLOYEE") as Role;

  const visibleItems = NAV_ITEMS.filter((item) => item.roles.includes(role));

  return (
    <aside className="w-56 bg-white border-r border-gray-200 min-h-screen p-4">
      <nav className="space-y-1">
        {visibleItems.map((item) => {
          const isActive = pathname === item.href;
          return (
            <Link
              key={item.href}
              href={item.href}
              className={`block px-3 py-2 rounded-md text-sm font-medium ${
                isActive
                  ? "bg-blue-50 text-blue-700"
                  : "text-gray-700 hover:bg-gray-50"
              }`}
            >
              {item.label}
            </Link>
          );
        })}
      </nav>
    </aside>
  );
}
