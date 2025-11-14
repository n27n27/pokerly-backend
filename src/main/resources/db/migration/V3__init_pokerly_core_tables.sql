-- ===========================================
-- Pokerly v3 핵심 테이블 초기 생성
-- venues / game_sessions / point_transactions
-- ===========================================

-- -------------------------
-- 1. venues (매장)
-- -------------------------
CREATE TABLE IF NOT EXISTS venues (
  id          BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id     BIGINT       NOT NULL,
  name        VARCHAR(100) NOT NULL,
  location    VARCHAR(255),
  notes       VARCHAR(1000),

  created_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
                   ON UPDATE CURRENT_TIMESTAMP,

  CONSTRAINT uk_venues_user_name
    UNIQUE (user_id, name)
);

-- -------------------------
-- 2. game_sessions (게임 세션)
-- -------------------------
CREATE TABLE IF NOT EXISTS game_sessions (
  id            BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id       BIGINT      NOT NULL,
  venue_id      BIGINT      NOT NULL,
  play_date     DATE        NOT NULL,
  title         VARCHAR(100),
  game_type     VARCHAR(30),

  -- 세션에서 실제로 투입한 총 금액(현금/포인트)
  total_cash_in  BIGINT     NOT NULL DEFAULT 0,  -- 현금 총 투입
  total_point_in BIGINT     NOT NULL DEFAULT 0,  -- 포인트 총 투입(현금 가치 기준)
  entries        INT        NOT NULL DEFAULT 1,  -- 참가/리엔트리 횟수

  cash_out      BIGINT      NOT NULL DEFAULT 0,  -- 실제 현금 아웃
  discount      BIGINT      NOT NULL DEFAULT 0,  -- 얼리 할인 등 (현금 가치)

  notes         VARCHAR(1000),

  -- 손익 지표
  profit_cash_realized     BIGINT NOT NULL DEFAULT 0, -- 현금 기준 손익
  profit_including_points  BIGINT NOT NULL DEFAULT 0, -- 포인트 포함 EV 손익

  created_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
                      ON UPDATE CURRENT_TIMESTAMP,

  INDEX idx_gs_user_date (user_id, play_date),
  CONSTRAINT fk_gs_venue
    FOREIGN KEY (venue_id) REFERENCES venues(id)
);

-- -------------------------
-- 3. point_transactions (포인트 내역)
-- -------------------------
CREATE TABLE IF NOT EXISTS point_transactions (
  id              BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id         BIGINT      NOT NULL,
  venue_id        BIGINT      NOT NULL,
  game_session_id BIGINT,

  change_amount   BIGINT      NOT NULL,    -- 적립: 양수, 사용: 음수
  balance_after   BIGINT      NOT NULL,
  type            VARCHAR(30) NOT NULL,    -- EARN, USE, ADJUST...
  description     VARCHAR(1000),

  created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

  INDEX idx_pt_user_venue (user_id, venue_id, id),
  CONSTRAINT fk_pt_venue
    FOREIGN KEY (venue_id) REFERENCES venues(id),
  CONSTRAINT fk_pt_session
    FOREIGN KEY (game_session_id) REFERENCES game_sessions(id)
);
