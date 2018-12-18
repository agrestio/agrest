package io.agrest.runtime.constraints;

import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import io.agrest.EntityConstraint;
import io.agrest.EntityUpdate;
import io.agrest.ResourceEntity;
import io.agrest.annotation.ClientReadable;
import io.agrest.annotation.ClientWritable;
import io.agrest.meta.AgAttribute;
import io.agrest.runtime.processor.update.UpdateContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

	void constrainResponse(ResourceEntity<?, ?> resourceEntity) {
		constrainForRead(resourceEntity);
	}

	void constrainUpdate(UpdateContext<?, ?> context) {

		EntityConstraint c = forWrite.getOrCreate(context.getEntity().getAgEntity());

		if (!c.allowsId()) {
			context.setIdUpdatesDisallowed(true);
		}

		// updates are not hierarchical yet, so simply check attributes...
		// TODO: updates may contain FKs ... need to handle that

		if (!c.allowsAllAttributes()) {
			for (EntityUpdate<?> u : context.getUpdates()) {

				// exclude disallowed attributes
				Iterator<String> it = u.getValues().keySet().iterator();
				while (it.hasNext()) {
					String attr = it.next();
					if (!c.allowsAttribute(attr)) {

						// do not report default properties, as this wasn't a
						// client's fault it go there..
						if (!context.getEntity().isDefault(attr)) {
							LOGGER.info("Attribute not allowed, removing: " + attr + " for id " + u.getId());
						}

						it.remove();
					}
				}
			}
		}

		for (EntityUpdate<?> u : context.getUpdates()) {
			Iterator<String> it = u.getRelatedIds().keySet().iterator();
			while (it.hasNext()) {
				String relationship = it.next();
				if (!c.allowsRelationship(relationship)) {
					LOGGER.info("Relationship not allowed, removing: " + relationship + " for id " + u.getId());
					it.remove();
				}
			}
		}
	}

	void constrainForRead(ResourceEntity<?, ?> entity) {

		EntityConstraint c = forRead.getOrCreate(entity.getAgEntity());

		if (!c.allowsId()) {
			entity.excludeId();
		}

		if (!c.allowsAllAttributes()) {
			Iterator<AgAttribute> ait = entity.getAttributes().values().iterator();
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

		Iterator<Entry<String, ResourceEntity<?, ?>>> rit = entity.getChildren().entrySet().iterator();
		while (rit.hasNext()) {

			Entry<String, ResourceEntity<?, ?>> e = rit.next();

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
