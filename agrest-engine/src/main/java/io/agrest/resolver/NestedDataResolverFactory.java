package io.agrest.resolver;

/**
 * @since 3.4
 */
@FunctionalInterface
public interface NestedDataResolverFactory {

    NestedDataResolver<?> resolver(Class<?> parentEntityType, String relationshipName);
}
