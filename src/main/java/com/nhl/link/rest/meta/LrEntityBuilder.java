package com.nhl.link.rest.meta;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.Collection;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A helper class to assemble custom {@link LrPersistentEntity} objects. Used
 * for POJOs,etc.
 * 
 * @since 1.12
 */
public class LrEntityBuilder<T> {

	private static final Logger LOGGER = LoggerFactory.getLogger(LrEntityBuilder.class);

	public static <T> LrEntityBuilder<T> builder(Class<T> type) {
		return new LrEntityBuilder<T>(type);
	}

	public static <T> LrEntity<T> build(Class<T> type) {
		return builder(type).build();
	}

	private Class<T> type;
	private PropertyDescriptor[] propertyDescriptors;
	private Package entityPackage;
	private String idProperty;

	LrEntityBuilder(Class<T> type) {
		this.type = type;

		// TODO: pretty lame - only allowing relationships within the same
		// package...
		this.entityPackage = type.getPackage();
	}

	public LrEntity<T> build() {
		DefaultLrEntity<T> e = new DefaultLrEntity<>(type);
		appendProperties(e);
		return e;
	}

	public LrEntityBuilder<T> id(String idProperty) {
		this.idProperty = idProperty;
		return this;
	}

	private void appendProperties(DefaultLrEntity<T> entity) {

		for (PropertyDescriptor pd : propertyDescriptors()) {
			Method reader = pd.getReadMethod();
			if (reader == null) {
				// don't care about write-only props
				continue;
			}

			if (reader.getDeclaringClass().equals(Object.class)) {
				// 'getClass' is not a property we care about
				continue;
			}

			if (!addAsToOneRelationship(entity, pd)) {
				if (!addAsToManyRelationship(entity, pd)) {
					if (!addAsAttribute(entity, pd)) {
						LOGGER.info("Skipping unsupported property: " + entity.getName() + "." + pd.getName());
					}
				}
			}
		}
	}

	private boolean addAsAttribute(DefaultLrEntity<T> entity, PropertyDescriptor pd) {

		Class<?> targetType = pd.getPropertyType();
		if (Collection.class.isAssignableFrom(targetType) || Map.class.isAssignableFrom(targetType)) {
			return false;
		}

		DefaultLrAttribute a = new DefaultLrAttribute(pd.getName(), targetType.getName());

		if (a.getName().equals(idProperty)) {
			entity.addId(a);
		} else {
			entity.addAttribute(a);
		}
		
		return true;
	}

	private boolean addAsToManyRelationship(DefaultLrEntity<T> entity, PropertyDescriptor pd) {

		Method propertyReader = pd.getReadMethod();
		if (propertyReader == null) {
			return false;
		}

		Class<?> returnType = propertyReader.getReturnType();
		if (!Collection.class.isAssignableFrom(returnType)) {
			return false;
		}

		ParameterizedType genericReturnType = (ParameterizedType) propertyReader.getGenericReturnType();
		Class<?> collectionType = (Class<?>) genericReturnType.getActualTypeArguments()[0];
		if (!isRelationship(collectionType)) {
			return false;
		}

		entity.addRelationship(new DefaultLrRelationship(pd.getName(), collectionType, true));
		return true;
	}

	private boolean addAsToOneRelationship(DefaultLrEntity<T> entity, PropertyDescriptor pd) {

		Class<?> targetType = pd.getPropertyType();
		if (!isRelationship(targetType)) {
			return false;
		}

		entity.addRelationship(new DefaultLrRelationship(pd.getName(), targetType, false));
		return true;
	}

	private boolean isRelationship(Class<?> propertyType) {
		// treat classes in the same package as relationships...
		return !propertyType.isPrimitive() && propertyType.getPackage().equals(entityPackage);
	}

	private PropertyDescriptor[] propertyDescriptors() {
		if (propertyDescriptors == null) {
			BeanInfo info;
			try {
				info = Introspector.getBeanInfo(type);
			} catch (IntrospectionException e) {
				throw new RuntimeException("Error getting bean properties from " + type.getName(), e);
			}
			return info.getPropertyDescriptors();
		}
		return propertyDescriptors;
	}
}
