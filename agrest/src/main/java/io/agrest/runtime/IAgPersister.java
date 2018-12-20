package io.agrest.runtime;

/**
 *
 *
 */
public interface IAgPersister<C, R> {

    C sharedContext();

    C newContext();

    R entityResolver();
}

