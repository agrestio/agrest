package io.agrest.backend.exp;

import io.agrest.AgException;

/**
 * RuntimeException thrown on errors during expressions creation/parsing.
 */
public class ExpressionException extends AgException {

	private static final long serialVersionUID = -4933472762330859309L;
	
	protected String expressionString;

	public ExpressionException() {
		super();
	}

	public ExpressionException(String messageFormat, Object... messageArgs) {
		super(messageFormat, messageArgs);
	}

	public ExpressionException(String messageFormat, Throwable cause, Object... messageArgs) {
		super(messageFormat, cause, messageArgs);
	}

	public ExpressionException(Throwable cause) {
		super(cause);
	}

	/**
	 * Constructor for ExpressionException.
	 */
	public ExpressionException(String messageFormat, String expressionString, Throwable th, Object... messageArgs) {
		super(messageFormat, th, messageArgs);
		this.expressionString = expressionString;
	}

	public String getExpressionString() {
		return expressionString;
	}
}
