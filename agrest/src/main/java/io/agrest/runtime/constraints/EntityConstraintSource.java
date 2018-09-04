package io.agrest.runtime.constraints;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

import javax.ws.rs.core.Response.Status;

import io.agrest.AgRESTException;
import io.agrest.EntityConstraint;
import io.agrest.meta.AgAttribute;
import io.agrest.meta.AgEntity;
import io.agrest.meta.AgRelationship;

/**
 * @since 1.6
 */
abstract class EntityConstraintSource {

	private ConcurrentMap<String, EntityConstraint> constraints;

	EntityConstraintSource(ConcurrentMap<String, EntityConstraint> constraints) {
		this.constraints = constraints;
	}

	EntityConstraint getOrCreate(AgEntity<?> entity) {

		EntityConstraint c = constraints.get(entity.getName());

		if (c == null) {

			// even though we are using a concurrent map, use key
			// synchronization on write to avoid double compilation.. after all
			// this is an amortized cost (done only once)

			synchronized (entity) {
				c = constraints.get(entity.getName());
				if (c == null) {
					c = create(entity);
					constraints.put(entity.getName(), c);
				}
			}
		}

		return c;
	}

	protected abstract AnnotationData processAnnotation(Class<?> type);

	private EntityConstraint create(AgEntity<?> entity) {
		AnnotationData ad = processAnnotation(entity.getType());

		if (ad == null) {
			return AllowAllEntityConstraint.instance();
		}

		Set<String> attributes = new HashSet<>();
		Set<String> relationships = new HashSet<>();

		for (String p : ad.properties) {
			AgAttribute a = entity.getAttribute(p);
			if (a != null) {
				attributes.add(p);
				continue;
			}

			AgRelationship r = entity.getRelationship(p);
			if (r != null) {
				relationships.add(p);
				continue;
			}

			throw new AgRESTException(Status.INTERNAL_SERVER_ERROR, "Invalid property: " + entity.getName() + "." + p);

		}

		boolean allowsAllAttributes = attributes.size() == entity.getAttributes().size();
		return new DefaultEntityConstraint(entity.getName(), ad.id, allowsAllAttributes, attributes, relationships);
	}

	class AnnotationData {
		boolean id;
		String[] properties;

		AnnotationData(boolean id, String[] properties) {
			this.id = id;
			this.properties = properties;
		}
	}

}
