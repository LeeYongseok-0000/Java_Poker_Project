package exception;

//Bet action 에러 처리 로직
public class InvalidBetException extends RuntimeException {
    public InvalidBetException(String message) {
    	super(message); 
    	}
}
