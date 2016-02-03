package com.nhl.link.rest.runtime.parser.pointer;

public interface LrPointer {

    PointerType getType();

    /**
     * @return Base entity class for this pointer (i.e. class of objects that this pointer can be resolved for)
     */
    Class<?> getBaseType();

    /**
     * @return Target entity class or target attribute's java type
     */
    Class<?> getTargetType();

    LrPointer getParent();

    /**
     * @param baseObject Base object for this pointer (e.g. instance of some entity E1)
     * @return Instance of related entity, attribute or instance of the same entity
     * @throws Exception If context is null or does not match this pointer's base type
     */
    Object resolve(PointerContext context, Object baseObject) throws Exception;

    /**
     * Shortcut for LrPointer#resolve(PointerContext, Object), that is convenient for simple instance pointers
     * @throws Exception If pointer can't be resolved without base object (i.e. it is/contains not an instance pointer)
     */
    Object resolve(PointerContext context) throws Exception;

    void update(PointerContext context, Object baseObject, Object value) throws Exception;

    void update(PointerContext context, Object value) throws Exception;

    void delete(PointerContext context, Object baseObject) throws Exception;

    /**
     * Shortcut for LrPointer#delete(PointerContext, Object), that is convenient for simple instance pointers
     * @throws Exception If pointer can't be deleted without base object (i.e. it is/contains not an instance pointer)
     */
    void delete(PointerContext context) throws Exception;
}
