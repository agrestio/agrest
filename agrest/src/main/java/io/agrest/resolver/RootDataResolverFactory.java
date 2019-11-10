package io.agrest.resolver;

/**
 * @since 3.4
 */
@FunctionalInterface
public interface RootDataResolverFactory {

    <T> RootDataResolver<T> resolver(Class<T> entityType);
}
