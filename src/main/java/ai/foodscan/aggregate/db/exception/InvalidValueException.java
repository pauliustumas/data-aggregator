package ai.foodscan.aggregate.db.exception;

import java.io.Serial;

public class InvalidValueException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = -2738782089583057191L;

    public InvalidValueException(Throwable cause) {
        super(cause);
    }

    public InvalidValueException(String message, Throwable cause) {
        super(message, cause);
    }
}
