import Link from "next/link";

export default function Home() {
  return (
    <>
      {/* 히어로 섹션 */}
      <section className="relative text-center py-20 px-4 overflow-hidden">
        {/* 배경 장식 */}
        <span className="absolute top-12 left-12 font-title text-[120px] text-accent opacity-15 select-none -rotate-12">?</span>
        <span className="absolute top-20 right-16 font-title text-[100px] text-primary opacity-15 select-none rotate-12">!</span>
        <span className="absolute bottom-16 left-24 font-title text-[80px] text-secondary opacity-20 select-none rotate-6">?!</span>
        <span className="absolute bottom-12 right-12 font-title text-[90px] text-accent opacity-10 select-none -rotate-6">?</span>

        <h1 className="font-title text-7xl leading-tight mb-4 relative z-10">
          <span className="relative inline-block">
            <span className="relative z-10">답</span>
            <span className="absolute bottom-2 left-[-6px] right-[-6px] h-5 bg-secondary opacity-70 rounded z-0"></span>
          </span>
          은 정해져 있어,
          <br />
          너만 모를 뿐<span className="text-primary">!</span>
        </h1>
        <p className="font-hand text-2xl text-gray-500 mb-10 relative z-10">
          퀴즈로 대결하고, 랭킹으로 증명하자
        </p>

        <div className="flex justify-center gap-4 mb-12 flex-wrap relative z-10">
          <span className="px-6 py-3 border-[3px] border-dark rounded-full text-sm font-bold bg-primary text-white shadow-kitsch-sm">
            ⚡ 실시간 배틀
          </span>
          <span className="px-6 py-3 border-[3px] border-dark rounded-full text-sm font-bold bg-accent text-white shadow-kitsch-sm">
            🤖 AI 문제 생성
          </span>
          <span className="px-6 py-3 border-[3px] border-dark rounded-full text-sm font-bold bg-white shadow-kitsch-sm">
            🏆 랭킹 시스템
          </span>
        </div>

        <Link
          href="/signup"
          className="relative z-10 inline-block px-16 py-5 bg-primary text-white font-bold text-xl border-[3px] border-dark rounded-2xl shadow-kitsch hover:shadow-kitsch-lg hover:-translate-y-1 transition-all"
        >
          시작하기
        </Link>
      </section>

      {/* 기능 소개 */}
      <section className="max-w-5xl mx-auto px-4 pb-16">
        <h2 className="font-title text-3xl text-center mb-3">이런 게 가능해!</h2>
        <p className="font-hand text-lg text-gray-400 text-center mb-10">답정너만의 특별한 기능들</p>
        <div className="grid grid-cols-3 gap-6">
          <div className="bg-white border-[3px] border-dark rounded-2xl p-8 shadow-kitsch hover:shadow-kitsch-lg hover:-translate-y-1 transition-all text-center">
            <div className="w-16 h-16 bg-primary rounded-2xl flex items-center justify-center text-3xl mx-auto mb-5">⚡</div>
            <h3 className="font-title text-xl mb-2">실시간 퀴즈 배틀</h3>
            <p className="text-sm text-gray-500">친구와 실시간으로 퀴즈 대결! 누가 더 빠르고 정확한지 겨뤄보자.</p>
          </div>
          <div className="bg-white border-[3px] border-dark rounded-2xl p-8 shadow-kitsch hover:shadow-kitsch-lg hover:-translate-y-1 transition-all text-center">
            <div className="w-16 h-16 bg-accent rounded-2xl flex items-center justify-center text-3xl mx-auto mb-5">📝</div>
            <h3 className="font-title text-xl mb-2">나만의 퀴즈 제작</h3>
            <p className="text-sm text-gray-500">직접 퀴즈셋을 만들고 공유해봐. 네가 출제자가 되는 거야!</p>
          </div>
          <div className="bg-white border-[3px] border-dark rounded-2xl p-8 shadow-kitsch hover:shadow-kitsch-lg hover:-translate-y-1 transition-all text-center">
            <div className="w-16 h-16 bg-secondary rounded-2xl flex items-center justify-center text-3xl mx-auto mb-5">🤖</div>
            <h3 className="font-title text-xl mb-2">AI가 만드는 문제</h3>
            <p className="text-sm text-gray-500">AI가 자동으로 퀴즈를 생성해줘. 무한한 문제에 도전하자!</p>
          </div>
        </div>
      </section>

      {/* 하단 CTA */}
      <section className="text-center pb-20">
        <Link
          href="/rooms"
          className="inline-block px-12 py-5 bg-accent text-white font-bold text-xl border-[3px] border-dark rounded-2xl shadow-kitsch hover:shadow-kitsch-lg hover:-translate-y-1 transition-all"
        >
          지금 바로 대결하러 가기
        </Link>
      </section>
    </>
  );
}