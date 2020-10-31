package io.agrest.meta.parser;

import io.agrest.DataResponse;
import io.agrest.annotation.LinkType;
import io.agrest.meta.AgEntity;
import io.agrest.meta.AgResource;
import io.agrest.meta.DefaultAgOperation;
import io.agrest.meta.DefaultAgResource;
import io.agrest.meta.LinkMethodType;
import io.agrest.runtime.meta.IMetadataService;
import org.apache.cayenne.di.Inject;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * @since 1.18
 * @deprecated since 4.1, as Agrest now integrates with OpenAPI 3 / Swagger.
 */
@Deprecated
public class ResourceParser implements IResourceParser {

    private IMetadataService metadataService;

    public ResourceParser(@Inject IMetadataService metadataService) {
        this.metadataService = metadataService;
    }

    private static LinkMethodType getMethodType(Method method) {
        if (method.getAnnotation(GET.class) != null) {
            return LinkMethodType.GET;
        }
        if (method.getAnnotation(POST.class) != null) {
            return LinkMethodType.POST;
        }
        if (method.getAnnotation(PUT.class) != null) {
            return LinkMethodType.PUT;
        }
        if (method.getAnnotation(DELETE.class) != null) {
            return LinkMethodType.DELETE;
        }
        return null;
    }

    private static String getPathSegment(AnnotatedElement element) {
        Path path = element.getAnnotation(Path.class);
        return normalizePathSegment(path == null ? "" : path.value());
    }

    private static String concatPathSegments(String... paths) {
        StringBuilder concat = new StringBuilder();

        for (String p : paths) {
            if (!p.isEmpty()) {
                if (concat.length() > 0) {
                    concat.append('/');
                }

                concat.append(p);
            }
        }

        return concat.toString();
    }

    /**
     * Strips leading and trailing slash from the path for further resolution against the base
     */
    // From @Path javadoc:
    //   Paths are relative. For an annotated class the base URI is the application path, see {@link ApplicationPath}.
    //   For an annotated method the base URI is the effective URI of the containing class. For the purposes of
    //   absolutizing a path against the base URI , a leading '/' in a path is ignored and base URIs are treated as if
    //   they ended in '/'.
    private static String normalizePathSegment(String path) {

        if (path == null || path.length() == 0 || path.equals("/")) {
            return "";
        }

        if (path.startsWith("/")) {
            path = path.substring(1);
        }

        if (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }

        return path;
    }

    @Override
    public <T> Collection<AgResource<?>> parse(Class<T> resourceClass) {

        Path root = resourceClass.getAnnotation(Path.class);
        if (root == null) {
            return Collections.emptySet();
        }

        Method[] methods = resourceClass.getDeclaredMethods();

        // using sorted TreeMap to ensure stable ordering of resources returned
        // from the method. Otherwise ordering differs between Java 8 and 7 ,
        // causing non-deterministic responses (and unit test failures).
        Map<String, Set<Method>> methodsMap = new TreeMap<>();

        for (Method method : methods) {
            if (Modifier.isPublic(method.getModifiers()) && getMethodType(method) != null) {
                String path = concatPathSegments(getPathSegment(resourceClass), getPathSegment(method));
                methodsMap.computeIfAbsent(path, p -> new LinkedHashSet<>()).add(method);
            }
        }

        Collection<AgResource<?>> resources = new ArrayList<>();
        for (Map.Entry<String, Set<Method>> methodsByPath : methodsMap.entrySet()) {
            resources.add(createResource(methodsByPath.getKey(), methodsByPath.getValue()));
        }
        return resources;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private AgResource<?> createResource(String path, Set<Method> methods) {
        DefaultAgResource resource = new DefaultAgResource();

        LinkType resourceType = LinkType.UNDEFINED;
        for (Method method : methods) {

            EndpointMetadata md = EndpointMetadata.fromAnnotation(method);
            AgEntity<?> entity = null;
            if (md != null) {
                LinkType annotatedType = md.getLinkType();
                if (resourceType == LinkType.UNDEFINED) {
                    resourceType = annotatedType;
                } else {
                    if (annotatedType != LinkType.UNDEFINED && annotatedType != resourceType) {
                        throw new IllegalStateException("Conflicting resource type annotations detected for resource: "
                                + path);
                    }
                }
            }

            if (md != null && !md.getEntityClass().equals(Object.class)) {

                Class<?> entityClass = md.getEntityClass();
                entity = metadataService.getAgEntity(entityClass);
                if (entity == null) {
                    throw new IllegalStateException("Unknown entity class: " + entityClass.getName());
                }

            } else if (DataResponse.class.isAssignableFrom(method.getReturnType())) {

                Type returnType = method.getGenericReturnType();
                if (returnType instanceof ParameterizedType) {
                    entity = metadataService.getAgEntity((Class) ((ParameterizedType) returnType)
                            .getActualTypeArguments()[0]);
                }
            }

            if (entity != null) {
                if (resource.getEntity() != null) {
                    if (!resource.getEntity().getName().equals(entity.getName())) {
                        throw new IllegalStateException("Conflicting entity class annotations detected for resource: "
                                + path);
                    }
                }
                resource.setEntity(entity);
            }

            LinkMethodType methodType = getMethodType(method);
            if (methodType == null) {
                continue;
            }
            resource.addOperation(new DefaultAgOperation(methodType));

        }

        resource.setPath(path);
        resource.setType(resourceType);

        return resource;
    }

}
