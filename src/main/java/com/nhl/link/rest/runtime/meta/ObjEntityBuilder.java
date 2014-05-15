package com.nhl.link.rest.runtime.meta;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.Collection;
import java.util.Map;

import org.apache.cayenne.map.DataMap;
import org.apache.cayenne.map.ObjAttribute;
import org.apache.cayenne.map.ObjEntity;
import org.apache.cayenne.map.ObjRelationship;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// keeping non-public until we add more customizations for the users to call... 
// used exclusively from DataMapBuilder until then
public class ObjEntityBuilder extends DataMapBuilder {

	private static final Logger LOGGER = LoggerFactory.getLogger(ObjEntityBuilder.class);

	private Class<?> type;
	private PropertyDescriptor[] propertyDescriptors;
	private RootDataMapBuilder parent;
	private Package entityPackage;
	private String idProperty;

	ObjEntityBuilder(RootDataMapBuilder parent, Class<?> type) {
		this.parent = parent;
		this.type = type;
		this.entityPackage = type.getPackage();
	}

	public ObjEntityBuilder withId(String idProperty) {
		this.idProperty = idProperty;
		return this;
	}

	@Override
	public DataMapBuilder addEntities(Class<?> type, Class<?>... moreTypes) {
		return parent.addEntities(type, moreTypes);
	}

	@Override
	public ObjEntityBuilder addEntity(Class<?> type) {
		return parent.addEntity(type);
	}

	@Override
	public DataMap toDataMap() {
		return parent.toDataMap();
	}

	ObjEntity toEntity() {
		PojoEntity entity = getOrCreateEntity(type);
		appendProperties(entity);
		return entity;
	}

	private String buildName(Class<?> type) {
		return type.getSimpleName();
	}

	private void appendProperties(PojoEntity entity) {
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

	private boolean addAsAttribute(PojoEntity entity, PropertyDescriptor pd) {
		Class<?> targetType = pd.getPropertyType();
		if (Collection.class.isAssignableFrom(targetType) || Map.class.isAssignableFrom(targetType)) {
			return false;
		}

		ObjAttribute a = new ObjAttribute(pd.getName());
		a.setType(targetType.getName());
		entity.addAttribute(a);

		if (a.getName().equals(idProperty)) {
			entity.addPrimaryKey(a);
		}

		return true;
	}

	private boolean addAsToManyRelationship(ObjEntity entity, PropertyDescriptor pd) {

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

		// create target entity placeholder if needed
		ObjEntity targetEntity = getOrCreateEntity(collectionType);

		ObjRelationship r = new ObjRelationship(pd.getName()) {

			private static final long serialVersionUID = 1L;

			@Override
			public boolean isToMany() {
				return true;
			}
		};

		r.setTargetEntityName(targetEntity.getName());
		entity.addRelationship(r);

		return true;
	}

	private boolean addAsToOneRelationship(ObjEntity entity, PropertyDescriptor pd) {

		Class<?> targetType = pd.getPropertyType();

		if (!isRelationship(targetType)) {
			return false;
		}

		// create target entity placeholder if needed
		ObjEntity targetEntity = getOrCreateEntity(targetType);

		ObjRelationship r = new ObjRelationship(pd.getName()) {

			private static final long serialVersionUID = 1L;

			@Override
			public boolean isToMany() {
				return false;
			}
		};

		r.setTargetEntityName(targetEntity.getName());
		entity.addRelationship(r);
		return true;
	}

	private boolean isRelationship(Class<?> propertyType) {
		// treat classes in the same package as relationships...
		return !propertyType.isPrimitive() && propertyType.getPackage().equals(entityPackage);
	}

	private PojoEntity getOrCreateEntity(Class<?> type) {

		String name = buildName(type);

		// we may have a placeholder entity in the DataMap already as a result
		// of an earlier relationship resolution, so let's ensure we don't
		// create a dupe...
		PojoEntity entity = (PojoEntity) parent.getMap().getObjEntity(name);
		if (entity == null) {
			entity = new PojoEntity(name);
			parent.getMap().addObjEntity(entity);
			entity.setClassName(type.getName());
		} else {
			// sanity check...
			if (!type.getName().equals(entity.getClassName())) {
				throw new IllegalStateException("Entity " + name + " was already created with a different type: "
						+ entity.getClassName());
			}
		}

		return entity;
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
