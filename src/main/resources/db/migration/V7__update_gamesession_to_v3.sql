-- ============================================
-- V7: Apply new GameSession v3 structure
-- ============================================

-- 1) 복원할 컬럼 (v5에서 삭제된 discount)
ALTER TABLE game_sessions
  ADD COLUMN discount BIGINT NOT NULL DEFAULT 0 AFTER entries;

-- 2) 새 기획 컬럼 추가
ALTER TABLE game_sessions
  ADD COLUMN buy_in_per_entry BIGINT NOT NULL DEFAULT 0 AFTER game_type,
  ADD COLUMN prize BIGINT NOT NULL DEFAULT 0 AFTER discount,
  ADD COLUMN net_profit BIGINT NOT NULL DEFAULT 0 AFTER prize,
  ADD COLUMN total_buy_in BIGINT NOT NULL DEFAULT 0 AFTER net_profit;

-- 3) 사용하지 않는 legacy 필드 제거
ALTER TABLE game_sessions
  DROP COLUMN total_cash_in,
  DROP COLUMN total_point_in,
  DROP COLUMN cash_out,
  DROP COLUMN profit_cash_realized,
  DROP COLUMN profit_including_points;
