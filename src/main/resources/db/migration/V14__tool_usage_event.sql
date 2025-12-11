CREATE TABLE tool_usage_event (
  id        BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'PK',
  user_id   BIGINT UNSIGNED NOT NULL COMMENT '사용자 PK (users.id)',

  -- 사용한 도구 코드
  --  - CALL_EV       : 콜 EV 계산기
  --  - TOURNAMENT_EV : 토너먼트 EV(GTD/오버레이) 계산기
  --  - REENTRY_EV    : 리엔트리 EV 계산기
  --  - ISO_3BET      : Iso / 3Bet 사이즈 계산기
  --  - ICM           : ICM 계산기
  --  - SPR           : SPR 계산기
  --  - IMPLIED_ODDS  : Implied Odds 계산기
  tool_code VARCHAR(50) NOT NULL COMMENT '사용한 도구 코드',

  -- 동작 유형
  --  - OPEN      : 페이지 진입
  --  - CALCULATE : 계산 실행
  action    VARCHAR(30) NOT NULL COMMENT '동작 유형',

  used_at   DATETIME     NOT NULL COMMENT '도구 사용 시각 (서버 기준 시간)',

  PRIMARY KEY (id),
  KEY idx_tool_usage_user (user_id),
  KEY idx_tool_usage_tool_code (tool_code),
  KEY idx_tool_usage_used_at (used_at)
) ENGINE=InnoDB
  DEFAULT CHARSET = utf8mb4
  COMMENT = '도구 사용 이력 로그';
