package player;

import card.Card;

public class UserPlayer extends Player {
    private Card selectedDiscardCard = null;
    private Card selectedOpenCard = null;

    public void setSelectedDiscardCard(Card card) { 
    	selectedDiscardCard = card; 
    	}
    
    public Card chooseCardToDiscard() {
        if (selectedDiscardCard == null) throw new IllegalStateException("버릴 카드 미선택");
        hand.remove(selectedDiscardCard);
        openCards.remove(selectedDiscardCard);
        Card discard = selectedDiscardCard;
        selectedDiscardCard = null;
        return discard;
    }
    
    public void setSelectedOpenCard(Card card) {
    	selectedOpenCard = card; 
    	}
    
    public Card chooseCardToOpen() {
        if (selectedOpenCard == null) throw new IllegalStateException("오픈 카드 미선택");
        openCards.add(selectedOpenCard);
        Card opened = selectedOpenCard;
        selectedOpenCard = null;
        return opened;
    }
}
