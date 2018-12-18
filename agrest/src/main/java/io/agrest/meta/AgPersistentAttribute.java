package io.agrest.meta;

/**
 * Represents a persistent attribute.
 * 
 * @since 1.12
 */
public interface AgPersistentAttribute<P> extends AgAttribute<P> {

	/**
	 * @since 1.12
     */
	int getJdbcType();

	/**
	 * @since 2.4
     */
	String getColumnName();

	/**
	 * @since 2.4
     */
	boolean isMandatory();
}
