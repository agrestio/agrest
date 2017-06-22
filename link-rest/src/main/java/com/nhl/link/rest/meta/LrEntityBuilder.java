package com.nhl.link.rest.meta;

import com.nhl.link.rest.annotation.LrAttribute;
import com.nhl.link.rest.annotation.LrId;
import com.nhl.link.rest.annotation.LrRelationship;
import com.nhl.link.rest.runtime.parser.converter.IJsonValueConverterFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A helper class to compile custom {@link LrEntity} objects based on
 * annotations. Used for POJOs,etc.
 * 
 * @since 1.12
 */
public class LrEntityBuilder<T> {

	private static final Logger LOGGER = LoggerFactory.getLogger(LrEntityBuilder.class);

	private static final Pattern GETTER = Pattern.compile("^(get|is)([A-Z].*)$");

	private Class<T> type;
	private LrDataMap dataMap;
	private IJsonValueConverterFactory converterFactory;

	public LrEntityBuilder(Class<T> type, LrDataMap dataMap, IJsonValueConverterFactory converterFactory) {
		this.type = type;
		this.dataMap = dataMap;
		this.converterFactory = converterFactory;
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

		Class<?> type = m.getReturnType();

		if (m.getAnnotation(LrAttribute.class) != null) {

			if (checkValidAttributeType(type, m.getGenericReturnType())) {
				DefaultLrAttribute a = new DefaultLrAttribute(name, type);
				entity.addAttribute(a);
			} else {
				// still return true after validation failure... this is an
				// attribute, just not a proper one
				LOGGER.warn("Invalid attribute type for " + entity.getName() + "." + name + ". Skipping.");
			}

			return true;
		}

		if (m.getAnnotation(LrId.class) != null) {

			if (checkValidIdType(type)) {
				DefaultLrAttribute a = new DefaultLrAttribute(name, type);
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

	private boolean checkValidAttributeType(Class<?> type, Type genericType) {
		return !Void.class.equals(type) && !void.class.equals(type) && !Map.class.isAssignableFrom(type)
				&& !isCollectionOfSimpleType(type, genericType);
	}

	private boolean isCollectionOfSimpleType(Class<?> type, Type genericType) {
		if (Collection.class.isAssignableFrom(type) && genericType instanceof ParameterizedType) {
			ParameterizedType pt = (ParameterizedType) genericType;
			return isSimpleType(pt.getRawType());
		}
		return false;
	}

	private boolean isSimpleType(Type rawType) {
		if (rawType instanceof Class) {
			Class<?> cls = (Class<?>) rawType;
			return String.class.isAssignableFrom(cls)
					|| Number.class.isAssignableFrom(cls)
					|| Boolean.class.isAssignableFrom(cls)
					|| Character.class.isAssignableFrom(cls);
		}
		return false;
	}

	private boolean checkValidIdType(Class<?> type) {
		return !Void.class.equals(type) && !void.class.equals(type) && !Map.class.isAssignableFrom(type)
				&& !Collection.class.isAssignableFrom(type);
	}

	private boolean addAsRelationship(DefaultLrEntity<T> entity, String name, Method m) {

		if (m.getAnnotation(LrRelationship.class) != null) {

			Class<?> targetType = m.getReturnType();
			boolean toMany = false;

			if (Collection.class.isAssignableFrom(targetType)) {
				targetType = (Class<?>) ((ParameterizedType) m.getGenericReturnType()).getActualTypeArguments()[0];
				toMany = true;
			}

			LrEntity<?> targetEntity = dataMap.getEntity(targetType);
			entity.addRelationship(new DefaultLrRelationship(name, targetEntity, toMany));
		}

		return false;
	}
}
