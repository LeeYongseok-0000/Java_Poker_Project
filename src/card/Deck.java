package card;

import java.util.*;

public class Deck {
		
    private List<Card> cards = new ArrayList<>();
    private int pos = 0;
    
    //덱 리셋
    public Deck() { 
    	
    	reset(); 
    	
    }
   
    //초기화(리셋) 로직
    public void reset() {    	
    	
        cards.clear();
       
        for (Card.Suit s : Card.Suit.values())        
        	
            for (int r = 2; r <= 14; r++)
                cards.add(new Card(s, r));        
       
        Collections.shuffle(cards);        
       
        pos = 0;
        
    }    
 
    //덱 소진 로직, 오류 처리 로직
    public Card draw() {    	
    	
        if (pos >= cards.size()) throw new IllegalStateException("덱 소진");        
       
        return cards.get(pos++);
        
    }
}
