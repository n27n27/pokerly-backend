-- game_sessions 테이블에 earned_point 컬럼 추가
ALTER TABLE game_sessions
  ADD COLUMN earned_point BIGINT NOT NULL DEFAULT 0 AFTER discount;
