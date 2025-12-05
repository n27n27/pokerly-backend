CREATE TABLE game_journals (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  journal_date DATE NOT NULL,
  title VARCHAR(200),
  content TEXT,
  mood_score TINYINT,
  focus_score TINYINT,
  tilt_score TINYINT,
  energy_score TINYINT,
  tags VARCHAR(300),
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

  CONSTRAINT ux_game_journals_user_date UNIQUE (user_id, journal_date),
  INDEX idx_game_journals_user_date (user_id, journal_date)
);
