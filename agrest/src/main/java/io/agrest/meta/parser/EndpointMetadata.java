package io.agrest.meta.parser;

import io.agrest.annotation.AgResource;
import io.agrest.annotation.LinkType;
import io.agrest.meta.annotation.Resource;

import java.lang.reflect.Method;

/**
 * @since 2.10
 */
class EndpointMetadata {

    public static EndpointMetadata fromAnnotation(Method resourceMethod) {

        EndpointMetadata mdLegacy = fromLegacyAnnotation(resourceMethod);
        EndpointMetadata mdNew = fromNewAnnotation(resourceMethod);

        if (mdLegacy != null && mdNew != null) {
            throw new IllegalStateException("Method '" + resourceMethod.getName() + "' is annotated with both @Resource and @AgResource");
        }

        return mdNew != null ? mdNew : mdLegacy;
    }

    @Deprecated
    private static EndpointMetadata fromLegacyAnnotation(Method resourceMethod) {
        Resource annotation = resourceMethod.getAnnotation(Resource.class);
        return annotation == null ? null : new EndpointMetadata(annotation.type().toType(), annotation.entityClass());
    }

    private static EndpointMetadata fromNewAnnotation(Method resourceMethod) {
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
