-- ============================================
-- V5: Remove discount column from game_sessions
-- 이유: total_cash_in 값에 이미 할인 적용되므로 중복 데이터라 제거
-- ============================================

ALTER TABLE game_sessions
  DROP COLUMN discount;
