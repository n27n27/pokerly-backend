-- 1) venue_id 를 NULL 허용으로 변경
ALTER TABLE game_sessions
  MODIFY venue_id BIGINT(20) NULL COMMENT 'VENUE 세션일 때만 사용';

-- 2) session_type 컬럼 추가 (기본값 VENUE)
ALTER TABLE game_sessions
  ADD COLUMN session_type VARCHAR(30) NOT NULL DEFAULT 'VENUE'
    COMMENT '세션 타입: VENUE / MAJOR / ONLINE / OTHER'
    AFTER play_date;

-- 기존 데이터는 모두 VENUE로 설정
UPDATE game_sessions
SET session_type = 'VENUE'
WHERE session_type IS NULL;

-- 3) GTD/필드 정보 컬럼 추가 (옵션 입력용)
ALTER TABLE game_sessions
  ADD COLUMN gtd_amount BIGINT(20) NULL
    COMMENT '광고된 GTD 금액',
  ADD COLUMN field_entries INT(11) NULL
    COMMENT '토너 전체 엔트리 수(리바인 포함)';
