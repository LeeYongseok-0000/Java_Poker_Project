package player;

import card.Card;
import java.util.*;

public abstract class Player implements IPlayerAction{
	
    protected List<Card> hand = new ArrayList<>();         // 실제 유저 패(히든카드 포함)
    protected List<Card> openCards = new ArrayList<>();    // 공개된 오픈카드
    protected Card lastHiddenCard = null;                  // 마지막 히든카드(7번째)
    protected int coin = 1000000;
    protected int currentBet = 0;                          // 라운드 내 현재 베팅 금액
    protected boolean folded = false;
    protected boolean actedThisRound = false;
    

    public void receiveCard(Card card) { hand.add(card); }
    public void receiveOpenCard(Card card) { hand.add(card); openCards.add(card); }
    public void receiveHiddenCard(Card card) { hand.add(card); lastHiddenCard = card; }
    public void payAnte(int anteAmount) {//엔티 코인 설정
        if (anteAmount > coin) {
            this.coin = 0; // 예시: 코인 전부 지불
        } else {
            this.coin -= anteAmount;
        }        
    }
    public List<Card> getHand() { 
    	return new ArrayList<>(hand); 
    	}
    
    public List<Card> getOpenCards() {
    	return new ArrayList<>(openCards); 
    	}
    
    public Card getLastHiddenCard() { 
    	return lastHiddenCard; 
    	}
    
    public int getCoin() { 
    	return coin; 
    	}
    
    public int getCurrentBet() { 
    	return currentBet; 
    }
    
    public boolean isFolded() { 
    	return folded; 
    }
    
    public boolean hasActedThisRound() { 
    	return actedThisRound; 
    }
    
    public void setCurrentBet(int value) { 
    	this.currentBet = value; 
    	}
    
    public void setActedThisRound(boolean acted) {
        this.actedThisRound = acted;
    }
    
    public void bet(int amount) { 
    	coin -= amount; currentBet += amount; 
    	}
    
    public void fold() { 
    	folded = true; 
    	}
    
    public void receivePot(int amount) { 
    	coin += amount; 
    	}
    
    public void resetBet() { 
    	currentBet = 0; folded = false; actedThisRound = false; 
    	}
    
    public void resetCoin(int v) { 
    	coin = v; 
    	}
    
    public void clearAll() {
        hand.clear(); openCards.clear(); lastHiddenCard = null;
        currentBet = 0; folded = false; actedThisRound = false;
    }
    
    public List<Card> getAllCards() { // 오픈+히든 전부 반환
        return new ArrayList<>(hand);
    }
}
