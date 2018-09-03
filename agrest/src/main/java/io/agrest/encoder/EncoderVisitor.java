package io.agrest.encoder;

/**
 * @since 2.0
 */
public interface EncoderVisitor {

	int visit(Object object);

	/**
	 * Called before the Encoder starts processing a collection of entities of a
	 * given type.
	 */
	default void push(String relationship) {
	}

	/**
	 * Called after the Encoder finished processing a relationship.
	 */
	default void pop() {
	}
}
