-- V9: 미사용 칼럼 삭제

ALTER TABLE game_sessions
  DROP COLUMN title;

ALTER TABLE game_sessions
  DROP COLUMN earned_point;
