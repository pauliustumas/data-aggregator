package ai.foodscan.aggregate.db.exception;

import java.io.Serial;

public class NoProductFoundException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 7273456070793365431L;

    public NoProductFoundException(String message) {
        super(message);
    }
}
