package game;

import card.Card;
import java.util.List; // 추가
import java.util.ArrayList; // 추가

public class HandEvaluationResult {
	
    private final HandRank handRank;
    private final List<Card> determiningCards; // 족보를 결정한 카드
                                          

    public HandEvaluationResult(HandRank handRank, List<Card> determiningCards) {
        this.handRank = handRank;
        //determiningCards가 null일 경우 빈 리스트로 초기화
        this.determiningCards = (determiningCards != null) ? new ArrayList<>(determiningCards) : new ArrayList<>();
    }
    
    //handRank 반환
    public HandRank getHandRank() {
    	return handRank;
    	}

    //족보를 결정하는 주요 카드 리스트를 반환
    public List<Card> getDeterminingCards() {
    	return new ArrayList<>(determiningCards);
    	}

    //족보를 대표하는 가장 높은 카드를 반환     
    public Card getHighCard() {
        return determiningCards.isEmpty() ? null : determiningCards.get(0);
    }

    //족보 설명을 위한 문자열
    public String getHighCardDescription() {
        if (determiningCards.isEmpty()) return "";    
        return determiningCards.get(0).toString();
    }

    //족보 결과 비교
    public static int compare(HandEvaluationResult res1, HandEvaluationResult res2) {
        if (res1.getHandRank().getRankValue() != res2.getHandRank().getRankValue()) {
            return Integer.compare(res1.getHandRank().getRankValue(), res2.getHandRank().getRankValue());
        }
        // 족보가 같으면 determiningCards로 비교
        List<Card> cards1 = res1.getDeterminingCards();
        List<Card> cards2 = res2.getDeterminingCards();
        for (int i = 0; i < Math.min(cards1.size(), cards2.size()); i++) {
            if (cards1.get(i).getRank() != cards2.get(i).getRank()) {
                return Integer.compare(cards1.get(i).getRank(), cards2.get(i).getRank());
            }
            // 랭크가 같으면 슈트는 일반적으로 비교하지 않지만, 게임 규칙에 따라 추가 가능
        }
        return 0; // 완전히 동일
    }
}