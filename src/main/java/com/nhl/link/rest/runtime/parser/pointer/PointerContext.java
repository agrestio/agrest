package com.nhl.link.rest.runtime.parser.pointer;

import java.util.Collection;

public interface PointerContext {

    /**
     * Apply all changes accumulated in this context so far.
     */
    void commitChanges();

    /**
     * Returns all persistent objects of some type.
     */
    <T> Collection<T> resolveAll(Class<T> type);

    /**
     * Removes all persistent objects of some type.
     */
    void removeAll(Class<?> type);

    /**
     * Creates a new persistent object. Newly created object is logically equal to {@code newObject},
     * but may be not identical.
     *
     * @param newObject Transient object of some persistent type.
     */
    void addObject(Object newObject);

    /**
	 * Finds and returns object of the specified type by its ID.
	 */
    <T> T resolveObject(Class<T> type, Object id);

    /**
     * Finds and updates object of the specified type by its ID.
     * After update the object is logically equal to {@code newObject}.
     *
     * @param newObject Another object of the persistent type denoted by {@code type}.
     */
    void updateObject(Class<?> type, Object id, Object newObject);

    /**
     * Finds and deletes object of the specified type by its ID.
     */
    void deleteObject(Class<?> type, Object id);

    /**
     * Returns value of a property {@code propertyName} for persistent object {@code baseObject}.
     *
	 * @param baseObject Base object for resolving the specified attribute.
     *                   Should be from this PointerContext's Cayenne context
	 */
    Object resolveProperty(Object baseObject, String propertyName);

    /**
     * Sets value of a property {@code propertyName} for persistent object {@code baseObject}
     * if property is attribute or to-one relationship.
     *
     * @param baseObject Base object for resolving the specified property.
     *                   Should be from this PointerContext's Cayenne context
     */
    void updateProperty(Object baseObject, String propertyName, Object value);

    /**
     * Sets value of a property {@code propertyName} to {@code null} for persistent object {@code baseObject}
     * if property is attribute or to-one relationship. Unrelates all related objects if property is to-many
     * relationship.
     *
     * @param baseObject Base object for resolving the specified property.
     *                   Should be from this PointerContext's Cayenne context
     */
    void deleteProperty(Object baseObject, String propertyName);

    /**
     * Sets target for to-one relationship or dds new item into to-many relationship
     *
     * @param baseObject Base object for resolving the specified property.
     *                   Should be from this PointerContext's Cayenne context
     */
    void addRelatedObject(Object baseObject, String propertyName, Object value);

    /**
     * Removes related object from to-one or to-many relationship.
     *
     * @param baseObject Base object for resolving the specified property.
     *                   Should be from this PointerContext's Cayenne context
     */
    void deleteRelatedObject(Object baseObject, String propertyName, Object relatedId);
}
