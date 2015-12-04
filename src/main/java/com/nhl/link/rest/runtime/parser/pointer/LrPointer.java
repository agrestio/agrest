package com.nhl.link.rest.runtime.parser.pointer;

import java.util.List;

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

    /**
     * @return List of this pointer's elements
     */
    List<? extends LrPointer> getElements();

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
}
