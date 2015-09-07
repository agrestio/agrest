package com.nhl.link.rest.runtime.constraints;

import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nhl.link.rest.DataResponse;
import com.nhl.link.rest.EntityConstraint;
import com.nhl.link.rest.EntityUpdate;
import com.nhl.link.rest.ResourceEntity;
import com.nhl.link.rest.UpdateResponse;
import com.nhl.link.rest.annotation.ClientReadable;
import com.nhl.link.rest.annotation.ClientWritable;
import com.nhl.link.rest.meta.LrAttribute;

/**
 * @since 1.6
 */
class EntityConstraintHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(EntityConstraintHandler.class);

	private EntityConstraintSource forRead;
	private EntityConstraintSource forWrite;

	EntityConstraintHandler(List<EntityConstraint> defaultReadConstraints,
			List<EntityConstraint> defaultWriteConstraints) {

		// note that explicit defaults override annotations
		// annotation-based constraints will be compiled dynamically
		ConcurrentMap<String, EntityConstraint> readMap = new ConcurrentHashMap<>();
		for (EntityConstraint c : defaultReadConstraints) {
			readMap.put(c.getEntityName(), c);
		}

		ConcurrentMap<String, EntityConstraint> writeMap = new ConcurrentHashMap<>();
		for (EntityConstraint c : defaultWriteConstraints) {
			writeMap.put(c.getEntityName(), c);
		}

		this.forRead = new EntityConstraintSource(readMap) {
			@Override
			protected AnnotationData processAnnotation(Class<?> type) {
				ClientReadable a = type.getAnnotation(ClientReadable.class);
				if (a == null) {
					return null;
				}

				return new AnnotationData(a.id(), a.value());
			}
		};

		this.forWrite = new EntityConstraintSource(writeMap) {
			@Override
			protected AnnotationData processAnnotation(Class<?> type) {
				ClientWritable a = type.getAnnotation(ClientWritable.class);
				if (a == null) {
					return null;
				}

				return new AnnotationData(a.id(), a.value());
			}
		};
	}

	void constrainResponse(DataResponse<?> response) {
		constrainForRead(response.getEntity());
	}

	void constrainUpdate(UpdateResponse<?> response) {

		EntityConstraint c = forWrite.getOrCreate(response.getEntity().getLrEntity());

		if (!c.allowsId()) {
			response.disallowIdUpdates();
		}

		// updates are not hierarchical yet, so simply check attributes...
		// TODO: updates may contain FKs ... need to handle that

		if (!c.allowsAllAttributes()) {
			for (EntityUpdate<?> u : response.getUpdates()) {

				// exclude disallowed attributes
				Iterator<Entry<String, Object>> it = u.getValues().entrySet().iterator();
				while (it.hasNext()) {
					Entry<String, Object> e = it.next();
					if (!c.allowsAttribute(e.getKey())) {

						// do not report default properties, as this wasn't a
						// client's fault it go there..
						if (!response.getEntity().isDefault(e.getKey())) {
							LOGGER.info("Attribute not allowed, removing: " + e.getKey() + " for id " + u.getId());
						}

						it.remove();
					}
				}
			}
		}
	}

	void constrainForRead(ResourceEntity<?> entity) {

		EntityConstraint c = forRead.getOrCreate(entity.getLrEntity());

		if (!c.allowsId()) {
			entity.excludeId();
		}

		if (!c.allowsAllAttributes()) {
			Iterator<LrAttribute> ait = entity.getAttributes().values().iterator();
			while (ait.hasNext()) {

				String a = ait.next().getName();
				if (!c.allowsAttribute(a)) {

					// hack: do not report default properties, as this wasn't a
					// client's fault it go there..
					if (!entity.isDefault(a)) {
						LOGGER.info("Attribute not allowed, removing: " + a);
					}

					ait.remove();
				}
			}
		}

		Iterator<Entry<String, ResourceEntity<?>>> rit = entity.getChildren().entrySet().iterator();
		while (rit.hasNext()) {

			Entry<String, ResourceEntity<?>> e = rit.next();

			if (c.allowsRelationship(e.getKey())) {
				constrainForRead(e.getValue());
			} else {

				// do not report default properties, as this wasn't a client's
				// fault it go there..
				if (!entity.isDefault(e.getKey())) {
					LOGGER.info("Relationship not allowed, removing: " + e.getKey());
				}

				rit.remove();
			}
		}

	}

}
