-- VXX__create_hand_reviews.sql
CREATE TABLE IF NOT EXISTS hand_reviews (
  id BIGINT NOT NULL AUTO_INCREMENT,

  -- 소유 유저
  user_id BIGINT NOT NULL,

  -- 연동 세션 (game_sessions.id)
  session_id BIGINT NULL,

  -- 기본 핸드 정보 (현재 프론트 폼 기준)
  title VARCHAR(200) NOT NULL,          -- 핸드 한 줄 요약
  hero_hand VARCHAR(10) NOT NULL,       -- Hero 핸드 (예: A5s, JJ, KTs)
  position VARCHAR(20) NULL,            -- 포지션 (UTG, UTG+1, LJ, HJ, CO, BTN, SB, BB)
  blinds VARCHAR(50) NULL,              -- 블라인드 (예: 1000/2000)
  stack_bb INT NULL,                    -- 스택 (BB 기준)
  description VARCHAR(2000) NULL,       -- 핸드 메모
  question VARCHAR(2000) NULL,          -- 나의 질문 / 메모

  -- Simple 분석에 사용된 태그 입력값 (simpleForm)
  simple_main_street   VARCHAR(20) NULL, -- PREFLOP / FLOP / TURN
  simple_pot_type      VARCHAR(20) NULL, -- HU / MW
  simple_board_texture VARCHAR(20) NULL, -- DRY / SEMI_WET / WET / PAIRED / MONOTONE
  simple_hero_strength VARCHAR(20) NULL, -- STRONG_MADE / MEDIUM_MADE / WEAK_MADE / DRAW / AIR
  simple_hero_line     VARCHAR(20) NULL, -- CBET / XC / XF / XR / BET_CALL / BET_FOLD ...

  -- Simple 분석 결과 전체(JSON 문자열로 저장)
  analysis_simple_json TEXT NULL,

  -- 생성/수정 시각
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

  PRIMARY KEY (id),

  INDEX idx_hand_reviews_user_created (user_id, created_at),
  INDEX idx_hand_reviews_session_created (session_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
