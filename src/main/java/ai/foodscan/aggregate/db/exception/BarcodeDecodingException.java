package ai.foodscan.aggregate.db.exception;

public class BarcodeDecodingException extends Exception {
    public BarcodeDecodingException(String message) {
        super(message);
    }

    public BarcodeDecodingException(String message, Throwable cause) {
        super(message, cause);
    }
}
