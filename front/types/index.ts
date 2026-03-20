export interface RecordItem {
  sessionId: number;
  quizSetTitle: string;
  maxPlayers: number;
  sessionRanking: number;
  sessionScore: number;
  earnedRankingScore: number;
  playedAt: string;
}

export interface UserRecordResponse {
  totalGames: number;
  totalWins: number;
  totalRankingScore: number;
  recentRecords: RecordItem[];
  page: number;
  size: number;
  totalElements: number;
}

export interface CommonResponse<T> {
  status: string;
  data: T;
  message: string;
}
