import type { Metadata } from "next";
import "./globals.css";
import Header from "@/components/common/Header";

export const metadata: Metadata = {
  title: "답정너 - 퀴즈 배틀",
  description: "실시간 퀴즈 배틀 서비스",
};

export default function RootLayout({ children }: { children: React.ReactNode }) {
  return (
    <html lang="ko">
      <body className="font-body bg-cream text-dark min-h-screen">
        {children}
      </body>
    </html>
  );
}