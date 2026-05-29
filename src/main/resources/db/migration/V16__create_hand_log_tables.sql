CREATE TABLE hand_log_events (
  event_id BIGINT NOT NULL AUTO_INCREMENT,
  user_id BIGINT NOT NULL,

  name VARCHAR(100) NOT NULL,
  event_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  venue_id BIGINT NULL,

  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,

  PRIMARY KEY (event_id),
  INDEX idx_hand_log_events_user_event_at (user_id, event_at, created_at),
  INDEX idx_hand_log_events_user_venue (user_id, venue_id)
);

CREATE TABLE hand_log_blind_levels (
  blind_level_id BIGINT NOT NULL AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  event_id BIGINT NOT NULL,

  level_no INT NOT NULL,
  small_blind INT NOT NULL DEFAULT 0,
  big_blind INT NOT NULL DEFAULT 0,
  ante INT NOT NULL DEFAULT 0,

  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,

  PRIMARY KEY (blind_level_id),
  INDEX idx_hand_log_blind_levels_user_event (user_id, event_id, level_no),
  CONSTRAINT fk_hand_log_blind_levels_event
    FOREIGN KEY (event_id)
    REFERENCES hand_log_events (event_id)
    ON DELETE CASCADE
);

CREATE TABLE hand_log_hands (
  hand_id BIGINT NOT NULL AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  event_id BIGINT NOT NULL,
  blind_level_id BIGINT NOT NULL,

  hole_cards VARCHAR(10) NOT NULL,
  first_rank CHAR(1) NULL,
  second_rank CHAR(1) NULL,
  suited TINYINT(1) NOT NULL DEFAULT 0,

  position VARCHAR(20) NULL,

  action_type VARCHAR(30) NULL,
  action_label VARCHAR(30) NULL,
  preflop_all_in TINYINT(1) NOT NULL DEFAULT 0,

  result_type VARCHAR(30) NULL,
  result_label VARCHAR(30) NULL,

  review_required TINYINT(1) NOT NULL DEFAULT 0,
  memo VARCHAR(1000) NULL,

  hand_strength_tier VARCHAR(30) NULL,
  hand_strength_label VARCHAR(30) NULL,
  hand_strength_color VARCHAR(30) NULL,

  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,

  PRIMARY KEY (hand_id),
  INDEX idx_hand_log_hands_user_event (user_id, event_id, created_at),
  INDEX idx_hand_log_hands_user_level (user_id, blind_level_id, created_at),
  CONSTRAINT fk_hand_log_hands_event
    FOREIGN KEY (event_id)
    REFERENCES hand_log_events (event_id)
    ON DELETE CASCADE,
  CONSTRAINT fk_hand_log_hands_blind_level
    FOREIGN KEY (blind_level_id)
    REFERENCES hand_log_blind_levels (blind_level_id)
    ON DELETE CASCADE
);

CREATE TABLE hand_log_reviews (
  review_id BIGINT NOT NULL AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  hand_id BIGINT NOT NULL,

  preflop TEXT NULL,
  flop TEXT NULL,
  turn TEXT NULL,
  river TEXT NULL,
  opponent_hand VARCHAR(50) NULL,
  opponent_type VARCHAR(100) NULL,
  my_thought TEXT NULL,
  review_result TEXT NULL,

  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,

  PRIMARY KEY (review_id),
  UNIQUE KEY uk_hand_log_reviews_hand_id (hand_id),
  INDEX idx_hand_log_reviews_user_hand (user_id, hand_id),
  CONSTRAINT fk_hand_log_reviews_hand
    FOREIGN KEY (hand_id)
    REFERENCES hand_log_hands (hand_id)
    ON DELETE CASCADE
);