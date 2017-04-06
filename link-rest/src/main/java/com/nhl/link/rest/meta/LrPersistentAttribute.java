package com.nhl.link.rest.meta;

/**
 * Represents a persistent attribute.
 * 
 * @since 1.12
 */
public interface LrPersistentAttribute extends LrAttribute {

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
