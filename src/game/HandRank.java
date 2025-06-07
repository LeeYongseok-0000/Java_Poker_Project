// game/HandRank.java
package game;

//포커 족보
//랭크 값 비교
public enum HandRank {
	
    HIGH_CARD(1), 
    ONE_PAIR(2), 
    TWO_PAIR(3), 
    TRIPS(4), 
    STRAIGHT(5), 
    FLUSH(6),
    FULL_HOUSE(7), 
    FOUR_CARD(8), 
    STRAIGHT_FLUSH(9);

    private final int rankValue;
    
    HandRank(int v) { 
    	rankValue = v; 
    	}
    
    public int getRankValue() { 
    	return rankValue; 
    	}
}
