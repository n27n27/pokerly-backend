package com.rolling.pokerly.handreview.application;

import java.util.Locale;
import java.util.Objects;

import org.springframework.stereotype.Service;

import com.rolling.pokerly.handreview.dto.SimpleAnalyzeRequest;
import com.rolling.pokerly.handreview.dto.SimpleAnalyzeResponse;

@Service
public class HandReviewSimpleService {

    public SimpleAnalyzeResponse analyze(SimpleAnalyzeRequest req) {
        var pre = buildPreflopRecommendation(req);
        String postflop = buildPostflopComment(req);
        String overall = buildOverallComment(pre, postflop);

        return SimpleAnalyzeResponse.builder()
                .preflopRecommendation(pre.title)
                .preflopDetail(pre.detail)
                .postflopComment(postflop)
                .overallSimpleComment(overall)
                .build();
    }

    /* ==================== 프리플랍 로직 ==================== */

    private static class PreflopResult {
        final String title;
        final String detail;

        PreflopResult(String title, String detail) {
            this.title = title;
            this.detail = detail;
        }
    }

    private PreflopResult buildPreflopRecommendation(SimpleAnalyzeRequest req) {
        String hand = safeUpper(req.getHeroHand());
        String pos = normalizePosition(req.getPosition());
        int bb = Objects.requireNonNullElse(req.getStackBb(), 40);

        if (hand == null || hand.length() < 2) {
            return new PreflopResult(
                    "정보 부족",
                    "핸드 정보가 부족해 명확한 프리플랍 추천을 하기 어렵습니다."
            );
        }

        HandCategory handCat = categorizeHand(hand);
        String stackLabel = stackLabel(bb);

        String title;
        String detail;

        switch (handCat) {
            case PREMIUM -> {
                title = "강한 오픈/3벳 핸드";
                detail = premiumDetail(pos, stackLabel);
            }
            case STRONG -> {
                title = "일반적인 오픈 핸드";
                detail = strongDetail(hand, pos, stackLabel);
            }
            case MEDIUM -> {
                title = "포지션 의존 핸드";
                detail = mediumDetail(hand, pos, stackLabel);
            }
            case SPECULATIVE -> {
                title = "스펙 핸드 (상황 의존)";
                detail = speculativeDetail(hand, pos, stackLabel);
            }
            case TRASH -> {
                title = "폴드 추천 핸드";
                detail = trashDetail(hand, pos, stackLabel);
            }
            default -> {
                title = "정보 부족";
                detail = "핸드 타입을 인식하지 못해 보수적으로 폴드 쪽을 권장합니다.";
            }
        }

        return new PreflopResult(title, detail);
    }

    private enum HandCategory {
        PREMIUM, STRONG, MEDIUM, SPECULATIVE, TRASH
    }

    private HandCategory categorizeHand(String heroHand) {
        String h = heroHand.toUpperCase(Locale.ROOT).replaceAll("\\s+", "");
        char r1 = h.charAt(0);
        char r2 = h.charAt(1);
        boolean suited = h.endsWith("S");

        boolean pair = (r1 == r2);

        int v1 = rankToValue(r1);
        int v2 = rankToValue(r2);
        int high = Math.max(v1, v2);
        int low = Math.min(v1, v2);

        if (pair) {
            if (v1 >= 12) return HandCategory.PREMIUM;
            if (v1 >= 9)  return HandCategory.STRONG;
            if (v1 >= 6)  return HandCategory.MEDIUM;
            return HandCategory.SPECULATIVE;
        }

        boolean bothBroadway = v1 >= 10 && v2 >= 10;
        if (bothBroadway) {
            if (suited) return HandCategory.PREMIUM;
            return HandCategory.STRONG;
        }

        if ((r1 == 'A' || r2 == 'A') && suited) {
            if (low >= 9) return HandCategory.STRONG;
            if (low >= 5) return HandCategory.MEDIUM;
            return HandCategory.SPECULATIVE;
        }

        if ((r1 == 'K' || r2 == 'K') && suited && low >= 9) return HandCategory.MEDIUM;
        if ((r1 == 'Q' || r2 == 'Q') && suited && low >= 9) return HandCategory.MEDIUM;

        int gap = high - low;
        if (suited && high <= 11 && low >= 5 && gap <= 4) return HandCategory.SPECULATIVE;

        return HandCategory.TRASH;
    }

    private int rankToValue(char r) {
        return switch (r) {
            case 'A' -> 14;
            case 'K' -> 13;
            case 'Q' -> 12;
            case 'J' -> 11;
            case 'T' -> 10;
            default -> Character.isDigit(r) ? r - '0' : 0;
        };
    }

    private String stackLabel(int bb) {
        if (bb <= 15) return "숏스택(≤15bb)";
        if (bb <= 30) return "미들스택(16~30bb)";
        if (bb <= 50) return "딥스택(31~50bb)";
        return "슈퍼 딥스택(>50bb)";
    }

    private String normalizePosition(String pos) {
        if (pos == null) return "UNKNOWN";
        String p = pos.toUpperCase(Locale.ROOT).trim();
        if (p.startsWith("UTG+1")) return "UTG+1";
        if (p.startsWith("UTG")) return "UTG";
        if (p.startsWith("LJ")) return "LJ";
        if (p.startsWith("HJ")) return "HJ";
        if (p.startsWith("CO")) return "CO";
        if (p.startsWith("BTN")) return "BTN";
        if (p.startsWith("SB")) return "SB";
        if (p.startsWith("BB")) return "BB";
        return p;
    }

    /* ==================== 플랍/턴/라인 코멘트 ==================== */

    private String buildPostflopComment(SimpleAnalyzeRequest req) {
        String street = safeUpper(req.getSimpleMainStreet());
        if (street == null || street.equals("PREFLOP")) {
            return "플랍/턴 태그가 없어 프리플랍 기준 가이드만 제공합니다.";
        }

        String board = safeUpper(req.getSimpleBoardTexture());
        String strength = safeUpper(req.getSimpleHeroStrength());
        String potType = safeUpper(req.getSimplePotType());
        String line = safeUpper(req.getSimpleHeroLine());

        if (board == null || strength == null || potType == null || line == null) {
            return "플랍/턴 상황 태그가 충분히 입력되지 않아, 자세한 스팟 코멘트 대신 프리플랍 기준으로만 판단합니다.";
        }

        StringBuilder sb = new StringBuilder();

        if (street.equals("FLOP")) sb.append("[플랍 스팟 요약] ");
        if (street.equals("TURN")) sb.append("[턴 스팟 요약] ");

        // 예시 조건들 그대로 유지 (너가 준 방식)
        if (board.equals("DRY") && strength.equals("STRONG_MADE") && potType.equals("HU")) {
            if (line.equals("CBET")) {
                sb.append("헤즈업 드라이 보드에서 강한 메이드 핸드로 c-bet 하는 것은 표준적인 라인입니다. ");
                sb.append("1/3~1/2 사이즈로 넓게 압박하는 방향이 좋습니다.");
            }
        }

        if (sb.length() == 0) {
            sb.append("입력한 태그 조합은 특별히 위험한 스팟은 아니며, 상대 스타일에 따라 전략이 크게 달라질 수 있습니다.");
        }

        return sb.toString();
    }

    /* ==================== 전체 요약 ==================== */

    private String buildOverallComment(PreflopResult preflop, String postflop) {
        StringBuilder sb = new StringBuilder();
        sb.append("프리플랍 기준으로는 [")
          .append(preflop.title)
          .append("]에 해당하는 핸드입니다. ");

        if (!postflop.startsWith("플랍/턴 태그가") && !postflop.startsWith("플랍/턴 상황")) {
            sb.append("플랍/턴 태그를 기반으로 한 스팟 코멘트를 참고해 라인을 조정해 보세요. ");
        } else {
            sb.append("이번 핸드는 플랍/턴 태그 정보가 부족하므로 프리플랍 레인지 구성 규칙을 중심으로 복기하는 것이 좋습니다. ");
        }

        sb.append("전체적으로는 '큰 실수를 줄이는 것'을 우선순위로 두면서 스택 구조에 맞춰 공격적 선택을 늘리는 방향이 좋습니다.");

        return sb.toString();
    }

    private String premiumDetail(String pos, String stackLabel) {
        StringBuilder sb = new StringBuilder();
        sb.append(stackLabel)
        .append(" 기준 ")
        .append(pos)
        .append("에서 프리미엄 핸드는 거의 항상 오픈/3벳으로 플레이하는 것이 기본입니다. ");
        sb.append("특별히 ICM이 강하게 걸려 있지 않다면, 상대 스타일에 맞춰 공격적으로 칩을 쌓는 스팟입니다.");
        return sb.toString();
    }

    private String strongDetail(String hand, String pos, String stackLabel) {
        StringBuilder sb = new StringBuilder();
        sb.append(stackLabel)
        .append(" 기준 ")
        .append(pos)
        .append("에서 ")
        .append(hand)
        .append("는 대부분 상황에서 오픈 가능한 핸드입니다. ");

        if (pos.startsWith("UTG")) {
            sb.append("초반 포지션에서는 너무 루즈하게 3벳/콜을 넓히기보다는 기본 오픈 라인 위주로 플레이하는 것이 좋습니다.");
        } else if (pos.equals("CO") || pos.equals("BTN")) {
            sb.append("후반 포지션이라면 3벳 블러프나 조금 더 루즈한 오픈도 허용되는 구간입니다.");
        } else {
            sb.append("상대들의 스타일에 따라 3벳/콜 레인지 조절이 필요합니다.");
        }
        return sb.toString();
    }

    private String mediumDetail(String hand, String pos, String stackLabel) {
        StringBuilder sb = new StringBuilder();
        sb.append(stackLabel)
        .append(" 기준 ")
        .append(pos)
        .append("에서 ")
        .append(hand)
        .append("는 포지션과 상황에 따라 오픈/폴드가 갈릴 수 있는 핸드입니다. ");

        if (pos.startsWith("UTG") || pos.equals("LJ")) {
            sb.append("전반적으로는 폴드 쪽이 무난하며, 테이블이 매우 타이트할 때만 선택적으로 오픈을 고려할 수 있습니다.");
        } else if (pos.equals("HJ") || pos.equals("CO")) {
            sb.append("중후반 포지션이라면 오픈을 섞어도 괜찮지만, 블라인드 디펜스가 강하면 폴드 빈도를 높이는 것이 좋습니다.");
        } else if (pos.equals("BTN") || pos.equals("SB") || pos.equals("BB")) {
            sb.append("블라인드 싸움/스틸 상황이면 조금 더 공격적으로 사용해도 되는 핸드입니다.");
        }
        return sb.toString();
    }

    private String speculativeDetail(String hand, String pos, String stackLabel) {
        StringBuilder sb = new StringBuilder();
        sb.append(stackLabel)
        .append(" 기준 ")
        .append(pos)
        .append("에서 ")
        .append(hand)
        .append("는 스펙 핸드로, 깊은 스택/후반 포지션일수록 가치가 올라갑니다. ");

        if (stackLabel.startsWith("숏스택")) {
            sb.append("숏스택에서는 세컨드 베스트 핸드가 되기 쉬워 대부분 폴드하는 것이 좋습니다.");
        } else {
            sb.append("딥스택에서 후반 포지션이라면 멀티웨이 팟을 노리는 콜/오픈 정도로 섞어도 괜찮습니다.");
        }
        return sb.toString();
    }

    private String trashDetail(String hand, String pos, String stackLabel) {
        StringBuilder sb = new StringBuilder();
        sb.append(stackLabel)
        .append(" 기준 ")
        .append(pos)
        .append("에서 ")
        .append(hand)
        .append("는 대부분의 상황에서 폴드가 권장되는 핸드입니다. ");
        sb.append("특별한 상황(블라인드 vs 블라인드 등)이 아니라면 굳이 싸움을 만들 필요는 없습니다.");
        return sb.toString();
    }

    private String safeUpper(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t.toUpperCase(Locale.ROOT);
    }
}
