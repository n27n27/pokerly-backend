ALTER TABLE game_sessions
  ADD COLUMN is_collab TINYINT(1) NOT NULL DEFAULT 0 AFTER venue_id,
  ADD COLUMN collab_label VARCHAR(50) NULL AFTER is_collab;

CREATE INDEX idx_game_session_month_collab
  ON game_sessions (user_id, play_date, session_type, is_collab, venue_id);
