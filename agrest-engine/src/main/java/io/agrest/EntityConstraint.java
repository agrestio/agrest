package io.agrest;

/**
 * @since 1.6
 */
public interface EntityConstraint {

    String getEntityName();

    boolean allowsId();

    /**
     * Tells constraint handler whether there is a need to check individual attributes. This is a shortcut to improve
	 * constraints checking performance.
     */
    boolean allowsAllAttributes();

    boolean allowsAttribute(String name);

    boolean allowsRelationship(String name);

}
