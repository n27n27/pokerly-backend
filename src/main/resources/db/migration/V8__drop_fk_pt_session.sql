-- V8: point_transactions 에서 game_sessions FK 제거
-- 이유: 기획변경으로 인해 포인트와 게임 세션은 관련이 없음(포인트는 그냥 매장정보의 부수기능일뿐)

-- 1) 기존 FK 제거
ALTER TABLE point_transactions
  DROP FOREIGN KEY fk_pt_session;
-- 2) 컬럼 제거
ALTER TABLE point_transactions
  DROP COLUMN game_session_id;
