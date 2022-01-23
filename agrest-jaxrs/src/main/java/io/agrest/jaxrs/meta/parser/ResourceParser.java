package io.agrest.jaxrs.meta.parser;

import io.agrest.DataResponse;
import io.agrest.annotation.LinkType;
import io.agrest.meta.*;
import io.agrest.meta.parser.EndpointMetadata;
import io.agrest.meta.parser.IResourceParser;
import org.apache.cayenne.di.Inject;

import javax.ws.rs.*;
import java.lang.reflect.*;
import java.util.*;

/**
 * @since 1.18
 * @deprecated since 4.1, as Agrest now integrates with OpenAPI 3 / Swagger.
 */
@Deprecated
public class ResourceParser implements IResourceParser {

    private AgDataMap dataMap;

    public ResourceParser(@Inject AgDataMap dataMap) {
        this.dataMap = dataMap;
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
                entity = dataMap.getEntity(entityClass);
                if (entity == null) {
                    throw new IllegalStateException("Unknown entity class: " + entityClass.getName());
                }

            } else if (DataResponse.class.isAssignableFrom(method.getReturnType())) {

                Type returnType = method.getGenericReturnType();
                if (returnType instanceof ParameterizedType) {
                    entity = dataMap.getEntity((Class) ((ParameterizedType) returnType)
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
