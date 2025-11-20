-- V6__add_type_to_venues.sql
-- venues 테이블에 type 컬럼 추가 (USER_PRIVATE / OFFICIAL)

ALTER TABLE venues
  ADD COLUMN type VARCHAR(20) NOT NULL DEFAULT 'USER_PRIVATE'
  AFTER notes;
