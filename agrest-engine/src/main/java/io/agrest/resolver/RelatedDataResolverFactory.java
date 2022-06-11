package io.agrest.resolver;

/**
 * @since 3.4
 */
@FunctionalInterface
public interface RelatedDataResolverFactory {

    RelatedDataResolver<?> resolver(Class<?> parentEntityType, String relationshipName);
}
