package io.agrest.exp;

public class AgExpressionException  extends RuntimeException {

    public AgExpressionException() {
        super();
    }

    public AgExpressionException(String message) {
        super(message);
    }

    public AgExpressionException(String message, Throwable cause) {
        super(message, cause);
    }

    public AgExpressionException(Throwable cause) {
        super(cause);
    }
}
