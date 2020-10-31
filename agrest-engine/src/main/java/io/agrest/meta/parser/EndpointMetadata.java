package io.agrest.meta.parser;

import io.agrest.annotation.AgResource;
import io.agrest.annotation.LinkType;

import java.lang.reflect.Method;

/**
 * @since 2.10
 * @deprecated since 4.1, as Agrest now integrates with OpenAPI 3 / Swagger.
 */
@Deprecated
class EndpointMetadata {

    public static EndpointMetadata fromAnnotation(Method resourceMethod) {
        AgResource annotation = resourceMethod.getAnnotation(AgResource.class);
        return annotation == null ? null : new EndpointMetadata(annotation.type(), annotation.entityClass());
    }

    private LinkType linkType;
    private Class<?> entityClass;

    private EndpointMetadata(LinkType linkType, Class<?> entityClass) {
        this.linkType = linkType;
        this.entityClass = entityClass;
    }

    public Class<?> getEntityClass() {
        return entityClass;
    }

    public LinkType getLinkType() {
        return linkType;
    }
}
