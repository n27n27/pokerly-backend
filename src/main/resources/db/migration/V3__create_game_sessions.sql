CREATE TABLE IF NOT EXISTS game_sessions (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  venue_id BIGINT NOT NULL,
  play_date DATE NOT NULL,
  title VARCHAR(100),
  game_type VARCHAR(30),
  buy_in DECIMAL(15,2) NOT NULL DEFAULT 0,
  entries INT NOT NULL DEFAULT 1,
  cash_out DECIMAL(15,2) NOT NULL DEFAULT 0,
  point_used DECIMAL(15,2) NOT NULL DEFAULT 0,
  point_remain_after DECIMAL(15,2) NOT NULL DEFAULT 0,
  discount DECIMAL(15,2) NOT NULL DEFAULT 0,
  notes VARCHAR(1000),
  profit_cash_realized DECIMAL(15,2) NOT NULL DEFAULT 0,
  profit_including_points DECIMAL(15,2) NOT NULL DEFAULT 0,
  INDEX idx_gs_user_date (user_id, play_date)
);
