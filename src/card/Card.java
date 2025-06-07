package card;

public class Card {
	
	//카드 문양 열거형
    public enum Suit { CLUBS, DIAMONDS, HEARTS, SPADES }
    
    //카드 문양, 랭크 (2~14) final: 재정의 불가
    private final Suit suit;
    private final int rank;
    
    //생성자 함수 (문양, 랭크)
    public Card(Suit suit, int rank) {
    	
        this.suit = suit;
        this.rank = rank;
        
    }
    
    //Suit(문양) 반환 //public Suit getSuit()
    public Suit getSuit() { 
    	
    	return suit; 
    	
    	}
    
    //rank(카드 랭크) 반환
    public int getRank() { 
    	
    	return rank; 
    	
    	}

    
    @Override
    public String toString() {
    	
    	//카드 덱 랭크 정의 => J, Q, K Suit => rank 11, 12, 13으로  A=14로 정의
    	String rankStr;
        if (rank == 11) rankStr = "J";
        else if (rank == 12) rankStr = "Q";
        else if (rank == 13) rankStr = "K";
        else if (rank == 14) rankStr = "A";
        
        //나머지 숫자 처리
        else rankStr = String.valueOf(rank);
        String suitStr = "";
        
        //카드 문자열 -> 문양으로 전환
        switch (suit) {
        
            case CLUBS: suitStr = "♣"; break;
            case DIAMONDS: suitStr = "♦"; break;
            case HEARTS: suitStr = "♥"; break;
            case SPADES: suitStr = "♠"; break;
            
        }
        
        return suitStr + rankStr;
        
    }
}
