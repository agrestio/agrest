package io.agrest.annotation;

/**
 * Classifies endpoints annotated with {@link AgResource}.
 *
 * @since 2.10
 * @deprecated since 4.7, as Agrest now integrates with OpenAPI 3 / Swagger
 */
@Deprecated
public enum LinkType {
    COLLECTION, ITEM, METADATA, UNDEFINED
}