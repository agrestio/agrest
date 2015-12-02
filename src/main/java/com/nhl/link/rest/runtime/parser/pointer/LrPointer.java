package com.nhl.link.rest.runtime.parser.pointer;

public interface LrPointer {

    PointerType getType();

    /**
     * @return Target entity class or target attribute's java type
     */
    Class<?> getTargetType();

    /**
     * @param context Base object for this pointer (e.g. instance of some entity E1)
     * @return Instance of related entity, attribute or instance of the same entity
     * @throws Exception If context is null or does not match this pointer's base type
     */
    Object resolve(Object context) throws Exception;

    /**
     * Shortcut for LrPointer#resolve(Object), that is convenient for simple instance pointers
     * @throws Exception If pointer can't be resolved without context (i.e. it's compound and/or is not an instance pointer)
     */
    Object resolve() throws Exception;
}
