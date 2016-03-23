package com.nhl.link.rest.meta.parser;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;

import org.apache.cayenne.di.Inject;

import com.nhl.link.rest.DataResponse;
import com.nhl.link.rest.meta.DefaultLrOperation;
import com.nhl.link.rest.meta.DefaultLrResource;
import com.nhl.link.rest.meta.LinkMethodType;
import com.nhl.link.rest.meta.LinkType;
import com.nhl.link.rest.meta.LrEntity;
import com.nhl.link.rest.meta.LrResource;
import com.nhl.link.rest.meta.annotation.Resource;
import com.nhl.link.rest.runtime.meta.IMetadataService;

/**
 * @since 1.18
 */
public class ResourceParser implements IResourceParser {

	private IMetadataService metadataService;

	public ResourceParser(@Inject IMetadataService metadataService) {
		this.metadataService = metadataService;
	}

	@Override
	public <T> Collection<LrResource<?>> parse(Class<T> resourceClass) {

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
				String path = buildPath(getPath(resourceClass), getPath(method));
				Set<Method> methodsByPath = methodsMap.get(path);
				if (methodsByPath == null) {
					methodsByPath = new HashSet<>();
				}
				methodsByPath.add(method);
				methodsMap.put(path, methodsByPath);
			}
		}

		Collection<LrResource<?>> resources = new ArrayList<>();
		for (Map.Entry<String, Set<Method>> methodsByPath : methodsMap.entrySet()) {
			resources.add(createResource(methodsByPath.getKey(), methodsByPath.getValue()));
		}
		return resources;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private LrResource<?> createResource(String path, Set<Method> methods) {
		DefaultLrResource resource = new DefaultLrResource();

		LinkType resourceType = LinkType.UNDEFINED;
		for (Method method : methods) {

			Resource annotation = method.getAnnotation(Resource.class);
			LrEntity<?> entity = null;
			if (annotation != null) {
				LinkType annotatedType = annotation.type();
				if (resourceType == LinkType.UNDEFINED) {
					resourceType = annotatedType;
				} else {
					if (annotatedType != LinkType.UNDEFINED && annotatedType != resourceType) {
						throw new IllegalStateException("Conflicting resource type annotations detected for resource: "
								+ path);
					}
				}
			}

			if (annotation != null && !annotation.entityClass().equals(Object.class)) {

				Class<?> entityClass = annotation.entityClass();
				entity = metadataService.getLrEntity(entityClass);
				if (entity == null) {
					throw new IllegalStateException("Unknown entity class: " + entityClass.getName());
				}

			} else if (DataResponse.class.isAssignableFrom(method.getReturnType())) {

				Type returnType = method.getGenericReturnType();
				if (returnType instanceof ParameterizedType) {
					entity = metadataService.getLrEntity((Class) ((ParameterizedType) returnType)
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
			resource.addOperation(new DefaultLrOperation(methodType));

		}

		resource.setPath(path);
		resource.setType(resourceType);

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
		return path == null ? "" : path.value();
	}

	private static String buildPath(String root, String suffix) {
		if (isEmpty(root)) {
			if (isEmpty(suffix)) {
				throw new IllegalStateException("Root and suffix cannot both be empty");
			}
			return suffix;
		} else {
			return isEmpty(suffix) ? root : root + "/" + suffix;
		}
	}

	private static boolean isEmpty(String s) {
		return s == null || s.isEmpty();
	}

}
