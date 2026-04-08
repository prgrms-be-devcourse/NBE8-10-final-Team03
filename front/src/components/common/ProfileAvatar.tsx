import { ReactNode } from "react";

interface ProfileAvatarProps {
  profileImage: number;
  size?: number;
}

const avatarConfig: Record<number, { color: string; expression: ReactNode }> = {
  1: {
    color: "#FF6B6B",
    expression: (
      <>
        {/* 눈 - 동그란 눈 */}
        <circle cx="35" cy="45" r="5" fill="#2B2D42" />
        <circle cx="65" cy="45" r="5" fill="#2B2D42" />
        <circle cx="37" cy="43" r="2" fill="white" />
        <circle cx="67" cy="43" r="2" fill="white" />
        {/* 입 - 활짝 웃음 */}
        <path d="M35 62 Q50 75 65 62" stroke="#2B2D42" strokeWidth="3" fill="none" strokeLinecap="round" />
      </>
    ),
  },
  2: {
    color: "#4D9DE0",
    expression: (
      <>
        {/* 눈 - 반달눈 */}
        <path d="M30 45 Q35 38 40 45" stroke="#2B2D42" strokeWidth="3" fill="none" strokeLinecap="round" />
        <path d="M60 45 Q65 38 70 45" stroke="#2B2D42" strokeWidth="3" fill="none" strokeLinecap="round" />
        {/* 입 - 옅은 미소 */}
        <path d="M38 63 Q50 70 62 63" stroke="#2B2D42" strokeWidth="3" fill="none" strokeLinecap="round" />
      </>
    ),
  },
  3: {
    color: "#7BC67E",
    expression: (
      <>
        {/* 눈 - 별눈 */}
        <circle cx="35" cy="45" r="5" fill="#2B2D42" />
        <circle cx="65" cy="45" r="5" fill="#2B2D42" />
        <circle cx="37" cy="43" r="2" fill="white" />
        <circle cx="67" cy="43" r="2" fill="white" />
        {/* 입 - 놀란 O */}
        <ellipse cx="50" cy="65" rx="8" ry="6" fill="#2B2D42" />
      </>
    ),
  },
  4: {
    color: "#FFD93D",
    expression: (
      <>
        {/* 눈 - 윙크 */}
        <circle cx="35" cy="45" r="5" fill="#2B2D42" />
        <circle cx="37" cy="43" r="2" fill="white" />
        <path d="M60 43 Q65 48 70 43" stroke="#2B2D42" strokeWidth="3" fill="none" strokeLinecap="round" />
        {/* 입 - 씩 웃음 */}
        <path d="M38 63 Q50 73 65 63" stroke="#2B2D42" strokeWidth="3" fill="none" strokeLinecap="round" />
      </>
    ),
  },
};

export default function ProfileAvatar({ profileImage, size = 96 }: ProfileAvatarProps) {
  const config = avatarConfig[profileImage] || avatarConfig[1];

  return (
    <svg
      width={size}
      height={size}
      viewBox="0 0 100 100"
      xmlns="http://www.w3.org/2000/svg"
    >
      {/* 얼굴 */}
      <circle cx="50" cy="50" r="46" fill={config.color} stroke="#2B2D42" strokeWidth="4" />
      {/* 표정 */}
      {config.expression}
    </svg>
  );
}