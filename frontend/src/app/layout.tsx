import type { Metadata } from "next";
import "./globals.css";

export const metadata: Metadata = {
  title: "勤怠管理システム",
  description: "出退勤打刻・休暇申請・勤怠管理",
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="ja" className="h-full">
      <body className="min-h-full bg-gray-50">{children}</body>
    </html>
  );
}
