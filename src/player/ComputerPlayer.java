package player;

import card.Card;
import java.util.Comparator;

public class ComputerPlayer extends Player {
	
    private Card selectedOpenCard = null;
    
    //가장 높은 랭크 선택
    public Card chooseHighestCard() {
        return hand.stream().max(Comparator.comparingInt(Card::getRank)).orElse(null);
    }
    
    //오픈 카드 설정
    public void setSelectedOpenCard(Card card) {
    	selectedOpenCard = card;
    }
    
    //버리는 카드 선택
    public Card chooseCardToDiscard() {
        Card discard = hand.get(0); // 제일 앞 카드 단순 discard
        hand.remove(discard); openCards.remove(discard);
        return discard;
    }
    
    //오픈 카드 선택
    public Card chooseCardToOpen() {
        if (selectedOpenCard == null) selectedOpenCard = chooseHighestCard();
        openCards.add(selectedOpenCard);
        Card opened = selectedOpenCard;
        selectedOpenCard = null;
        return opened;
    }
}
