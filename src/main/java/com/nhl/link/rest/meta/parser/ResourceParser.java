package com.nhl.link.rest.meta.parser;

import com.nhl.link.rest.DataResponse;
import com.nhl.link.rest.meta.DefaultLrOperation;
import com.nhl.link.rest.meta.DefaultLrResource;
import com.nhl.link.rest.meta.LinkMethodType;
import com.nhl.link.rest.meta.LrDataMap;
import com.nhl.link.rest.meta.LrEntity;
import com.nhl.link.rest.meta.LrResource;
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ResourceParser implements IResourceParser {

    @Inject
    private LrDataMap dataMap;

    @Override
    public <T> Collection<LrResource> parse(Class<T> resourceClass) {

        Path root = resourceClass.getAnnotation(Path.class);
        if (root == null) {
            return Collections.emptySet();
        }

        Method[] methods = resourceClass.getDeclaredMethods();
        Map<String, Set<Method>> methodsMap = new HashMap<>();
        for (Method method : methods) {
            if (Modifier.isPublic(method.getModifiers()) && getMethodType(method) != null) {
                String path = buildPath(
                        getPath(resourceClass),
                        getPath(method)
                );
                Set<Method> methodsByPath = methodsMap.get(path);
                if (methodsByPath == null) {
                    methodsByPath = new HashSet<>();
                }
                methodsByPath.add(method);
                methodsMap.put(path, methodsByPath);
            }
        }

        Collection<LrResource> resources = new ArrayList<>();
        for (Map.Entry<String, Set<Method>> methodsByPath : methodsMap.entrySet()) {
            resources.add(createResource(methodsByPath.getKey(), methodsByPath.getValue()));
        }
        return resources;
    }

    private LrResource createResource(String path, Set<Method> methods) {
        DefaultLrResource resource = new DefaultLrResource();

        for (Method method : methods) {
            if (DataResponse.class.isAssignableFrom(method.getReturnType())) {
                Type returnType = method.getGenericReturnType();
                if (returnType instanceof ParameterizedType) {
                    LrEntity<?> entity = dataMap.getEntity(
                            (Class) ((ParameterizedType) returnType).getActualTypeArguments()[0]
                    );
                    if (entity != null) {
                        // TODO: Check that entity has been set already and is the same
                        resource.setEntity(entity);
                    }
                }
            }

            LinkMethodType methodType = getMethodType(method);
            if (methodType == null) {
                continue;
            }
            resource.addOperation(
                    new DefaultLrOperation(methodType)
            );

        }

        resource.setPath(path);

        return resource;
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

    private static String getPath(AnnotatedElement element) {
        Path path = element.getAnnotation(Path.class);
        return path == null? "" : path.value();
    }

    private static String buildPath(String root, String suffix) {
        if (isEmpty(root)) {
            if (isEmpty(suffix)) {
                throw new IllegalStateException("Root and suffix cannot both be empty");
            }
            return suffix;
        } else {
            return isEmpty(suffix)? root : root + "/" + suffix;
        }
    }

    private static boolean isEmpty(String s) {
        return s == null || s.isEmpty();
    }

}
