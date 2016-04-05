package com.nhl.link.rest.meta;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.Collection;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nhl.link.rest.annotation.LrAttribute;
import com.nhl.link.rest.annotation.LrId;
import com.nhl.link.rest.annotation.LrRelationship;

/**
 * A helper class to compile custom {@link LrEntity} objects based on
 * annotations. Used for POJOs,etc.
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

	private static final Pattern GETTER = Pattern.compile("^(get|is)([A-Z].*)$");

	private Class<T> type;
	private Package entityPackage;

	LrEntityBuilder(Class<T> type) {
		this.type = type;
		this.entityPackage = type.getPackage();
	}

	public LrEntity<T> build() {
		DefaultLrEntity<T> e = new DefaultLrEntity<>(type);
		appendProperties(e);
		return e;
	}

	private void appendProperties(DefaultLrEntity<T> entity) {

		for (Method method : type.getMethods()) {
			appendProperty(entity, method);
		}
	}

	private void appendProperty(DefaultLrEntity<T> entity, Method m) {

		Class<?> type = m.getReturnType();
		if (type.equals(Void.class) || m.getParameterTypes().length > 0) {
			return;
		}

		String name = toPropertyName(m.getName());
		if (name == null) {
			return;
		}

		if (name.equals("class")) {
			// 'getClass' is not a property we care about
			return;
		}

		if (!addAsAttribute(entity, name, m)) {
			addAsRelationship(entity, name, m);
		}
	}

	String toPropertyName(String methodName) {
		Matcher matcher = GETTER.matcher(methodName);
		if (!matcher.find()) {
			return null;
		}

		String raw = matcher.group(2);
		return Character.toLowerCase(raw.charAt(0)) + raw.substring(1);
	}

	private boolean addAsAttribute(DefaultLrEntity<T> entity, String name, Method m) {

		if (m.getAnnotation(LrAttribute.class) != null) {

			if (checkValidAttributeType(m.getReturnType())) {
				DefaultLrAttribute a = new DefaultLrAttribute(name, m.getReturnType());
				entity.addAttribute(a);
			} else {
				// still return true after validation failure... this is an
				// attribute, just not a proper one
				LOGGER.warn("Invalid attribute type for " + entity.getName() + "." + name + ". Skipping.");
			}

			return true;
		}

		if (m.getAnnotation(LrId.class) != null) {

			if (checkValidAttributeType(m.getReturnType())) {
				DefaultLrAttribute a = new DefaultLrAttribute(name, m.getReturnType());
				entity.addId(a);
			} else {
				// still return true after validation failure... this is an
				// attribute, just not a proper one
				LOGGER.warn("Invalid ID attribute type for " + entity.getName() + "." + name + ". Skipping.");
			}

			return true;
		}

		return false;
	}

	private boolean checkValidAttributeType(Class<?> type) {
		return !Void.class.equals(type) && !void.class.equals(type) &&
				!Collection.class.isAssignableFrom(type) && !Map.class.isAssignableFrom(type);
	}

	private boolean addAsRelationship(DefaultLrEntity<T> entity, String name, Method m) {

		if (m.getAnnotation(LrRelationship.class) != null) {

			Class<?> targetType = m.getReturnType();
			boolean toMany = false;

			if (Collection.class.isAssignableFrom(targetType)) {
				targetType = (Class<?>) ((ParameterizedType) m.getGenericReturnType()).getActualTypeArguments()[0];
				toMany = true;
			}

			if (!isRelationship(targetType)) {
				return false;
			}

			entity.addRelationship(new DefaultLrRelationship(name, targetType, toMany));
		}

		return false;
	}

	private boolean isRelationship(Class<?> propertyType) {
		// treat classes in the same package as relationships...
		// TODO: lame...
		return !propertyType.isPrimitive() && propertyType.getPackage().equals(entityPackage);
	}
}
