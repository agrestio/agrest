package io.agrest.jpa.pocessor.update.stage;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.agrest.AgException;
import io.agrest.CompoundObjectId;
import io.agrest.EntityParent;
import io.agrest.EntityUpdate;
import io.agrest.jpa.persister.IAgJpaPersister;
import io.agrest.jpa.pocessor.IJpaQueryAssembler;
import io.agrest.jpa.pocessor.JpaUtil;
import io.agrest.meta.AgDataMap;
import io.agrest.meta.AgEntity;
import io.agrest.meta.AgRelationship;
import io.agrest.processor.ProcessorOutcome;
import io.agrest.runtime.processor.update.ChangeOperation;
import io.agrest.runtime.processor.update.ChangeOperationType;
import io.agrest.runtime.processor.update.UpdateContext;
import io.agrest.runtime.processor.update.stage.UpdateMergeChangesStage;
import jakarta.persistence.EntityManager;
import jakarta.persistence.metamodel.Attribute;
import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.Metamodel;
import jakarta.persistence.metamodel.PluralAttribute;
import org.apache.cayenne.di.Inject;

/**
 * A processor invoked for {@link io.agrest.UpdateStage#MERGE_CHANGES} stage.
 *
 * @since 5.0
 */
public class JpaMergeChangesStage extends UpdateMergeChangesStage {

    private final Metamodel metamodel;

    private final IJpaQueryAssembler queryAssembler;

    private final AgDataMap dataMap;

    public JpaMergeChangesStage(@Inject IAgJpaPersister persister,
                                @Inject IJpaQueryAssembler queryAssembler,
                                @Inject AgDataMap dataMap) {
        this.metamodel = persister.metamodel();
        this.queryAssembler = queryAssembler;
        this.dataMap = dataMap;
    }

    @SuppressWarnings("unchecked")
    @Override
    public ProcessorOutcome execute(UpdateContext<?> context) {
        try {
            merge((UpdateContext<Object>) context);
            return ProcessorOutcome.CONTINUE;
        } catch (Exception ex) {
            try {
                JpaUpdateStartStage.entityManager(context)
                        .getTransaction()
                        .rollback();
            } finally {
                JpaUpdateStartStage.entityManager(context).close();
            }
            throw ex;
        }
    }

    protected void merge(UpdateContext<Object> context) {
        Map<ChangeOperationType, List<ChangeOperation<Object>>> ops = context.getChangeOperations();
        if (ops.isEmpty()) {
            return;
        }

        ObjectRelator relator = createRelator(context);
        for (ChangeOperation<Object> op : ops.get(ChangeOperationType.CREATE)) {
            create(context, relator, op.getUpdate());
        }

        for (ChangeOperation<Object> op : ops.get(ChangeOperationType.UPDATE)) {
            update(context, relator, op.getObject(), op.getUpdate());
        }

        for (ChangeOperation<Object> op : ops.get(ChangeOperationType.DELETE)) {
            delete(context, op.getObject());
        }
    }

    protected void delete(UpdateContext<Object> context, Object o) {
        EntityManager entityManager = JpaUpdateStartStage.entityManager(context);
        entityManager.remove(o);
    }

    protected void create(UpdateContext<Object> context, ObjectRelator relator, EntityUpdate<Object> update) {

        EntityManager entityManager = JpaUpdateStartStage.entityManager(context);
        Object o;
        try {
            o = context.getType().getConstructor().newInstance();
        } catch (Exception e) {
            throw AgException.badRequest(e, "Unable to instantiate object of type: %s", context.getType().getName());
        }

        Map<String, Object> idByAgAttribute = update.getId();

        // set explicit ID
        if (idByAgAttribute != null) {
            if (context.isIdUpdatesDisallowed() && update.isExplicitId()) {
                throw AgException.badRequest("Setting ID explicitly is not allowed: %s", idByAgAttribute);
            }

            AgEntity<Object> agEntity = context.getEntity().getAgEntity();
            EntityType<Object> entity = metamodel.entity(context.getType());
            Map<Attribute<?, ?>, Object> idByJpaAttribute = mapToDbAttributes(agEntity, idByAgAttribute);

            // need to make an additional check that the AgId is unique
            checkExisting(entityManager, agEntity, idByAgAttribute);
            createSingleFromIdValues(entity, idByJpaAttribute, idByAgAttribute, o);
        }

        mergeChanges(context, update, o, relator);
        entityManager.persist(o);
        relator.relateToParent(o);
    }

    protected void update(UpdateContext<Object> context, ObjectRelator relator, Object o, EntityUpdate<Object> update) {
        EntityManager entityManager = JpaUpdateStartStage.entityManager(context);
        mergeChanges(context, update, o, relator);
        relator.relateToParent(o);
        entityManager.merge(o);
    }

    // translate "id" expressed in terms on public Ag names to Cayenne DbAttributes
    private Map<Attribute<?, ?>, Object> mapToDbAttributes(AgEntity<?> agEntity, Map<String, Object> idByAgAttribute) {

        Map<Attribute<?, ?>, Object> idByDbAttribute = new HashMap<>((int) (idByAgAttribute.size() / 0.75) + 1);
        for (Map.Entry<String, Object> e : idByAgAttribute.entrySet()) {
            Attribute<?, ?> attribute = attributeForAgAttribute(agEntity, e.getKey());
            if (attribute == null) {
                throw AgException.badRequest("Not a mapped persistent attribute '%s.%s'", agEntity.getName(), e.getKey());
            }
            idByDbAttribute.put(attribute, e.getValue());
        }

        return idByDbAttribute;
    }

    private void checkExisting(
            EntityManager entityManager,
            AgEntity<Object> agEntity,
            Map<String, Object> idByAgAttribute) {

        List<?> resultList = queryAssembler.createByIdQuery(agEntity, idByAgAttribute).build(entityManager).getResultList();
        if(!resultList.isEmpty()) {
            throw AgException.badRequest("Can't create '%s' with id %s - already exists",
                    agEntity.getName(),
                    CompoundObjectId.mapToString(idByAgAttribute));
        }
    }

    private void createSingleFromIdValues(
            EntityType<Object> entity,
            Map<Attribute<?,?>, Object> idByDbAttribute,
            Map<String, Object> idByAgAttribute,
            Object o) {

        for (Map.Entry<Attribute<?,?>, Object> idPart : idByDbAttribute.entrySet()) {
            Attribute<?,?> attribute = idPart.getKey();
            if (attribute == null) {
                throw AgException.badRequest("Can't create '%s' with id %s - not an ID DB attribute: %s",
                        entity.getName(),
                        CompoundObjectId.mapToString(idByAgAttribute),
                        idPart.getKey());
            }

            JpaUtil.writeProperty(o, attribute, idPart.getValue());
        }
    }

    private void mergeChanges(UpdateContext<Object> context, EntityUpdate<Object> entityUpdate, Object o, ObjectRelator relator) {

        EntityManager manager = JpaUpdateStartStage.entityManager(context);
        EntityType<Object> entityType = metamodel.entity(entityUpdate.getEntity().getType());

        // attributes
        for (Map.Entry<String, Object> e : entityUpdate.getValues().entrySet()) {
            JpaUtil.writeProperty(o, entityType.getAttribute(e.getKey()), e.getValue());
        }

        // relationships
        for (Map.Entry<String, Set<Object>> e : entityUpdate.getRelatedIds().entrySet()) {
            Attribute<?, ?> attribute = entityType.getAttribute(e.getKey());
            AgRelationship agRelationship = entityUpdate.getEntity().getRelationship(e.getKey());

            // sanity check
            if (agRelationship == null) {
                continue;
            }

            final Set<Object> relatedIds = e.getValue();
            if (relatedIds == null || relatedIds.isEmpty() || allElementsNull(relatedIds)) {
                relator.unrelateAll(agRelationship, o);
                continue;
            }

            if (!agRelationship.isToMany() && relatedIds.size() > 1) {
                throw AgException.badRequest(
                        "Relationship is to-one, but received update with multiple objects: %s",
                        agRelationship.getName());
            }


            relator.unrelateAll(agRelationship, o, new RelationshipUpdate() {
                @Override
                public boolean containsRelatedObject(Object relatedObject) {
                    Object id = manager.getEntityManagerFactory().getPersistenceUnitUtil().getIdentifier(relatedObject);
                    return relatedIds.contains(id);
                }

                @Override
                public void removeUpdateForRelatedObject(Object relatedObject) {
                    Object id = manager.getEntityManagerFactory().getPersistenceUnitUtil().getIdentifier(relatedObject);
                    relatedIds.remove(id);
                }
            });

            for (Object relatedId : relatedIds) {
                if (relatedId == null) {
                    continue;
                }

                Class<?> type = attribute.getJavaType();
                if(attribute.isCollection() && (attribute.getPersistentAttributeType() == Attribute.PersistentAttributeType.MANY_TO_MANY
                        || attribute.getPersistentAttributeType() == Attribute.PersistentAttributeType.ONE_TO_MANY)) {
                    type = ((PluralAttribute<?,?,?>)attribute).getElementType().getJavaType();
                }
                // TODO: to-many collections will brake here!
                Object related = manager.find(type, relatedId);

                if (related == null) {
                    throw AgException.notFound("Related object '%s' with ID '%s' is not found",
                            metamodel.entity(attribute.getJavaType()).getName(),
                            e.getValue());
                }

                relator.relate(agRelationship, o, related);
            }
        }

        entityUpdate.setMergedTo(o);
    }

    private boolean allElementsNull(Collection<?> elements) {

        for (Object element : elements) {
            if (element != null) {
                return false;
            }
        }

        return true;
    }

    protected ObjectRelator createRelator(UpdateContext<Object> context) {
        final EntityParent<?> parent = context.getParent();
        EntityManager manager = JpaUpdateStartStage.entityManager(context);
        EntityType<Object> entityType = metamodel.entity(context.getEntity().getType());
        if (parent == null) {
            return new ObjectRelator(entityType, manager);
        }

        EntityType<?> parentEntityType = metamodel.entity(parent.getType());
        AgEntity<?> parentAgEntity = dataMap.getEntity(context.getParent().getType());
        List<?> resultList = queryAssembler.createByIdQuery(parentAgEntity, parent.getId())
                .build(manager)
                .getResultList();
        if (resultList.isEmpty()) {
            throw AgException.notFound("No parent object for ID '%s' and entity '%s'", parent.getId(), entityType.getName());
        }

        Object parentObject = resultList.get(0);

        Attribute<?, ?> attribute = parentEntityType.getAttribute(parent.getRelationship());
        if (attribute.getPersistentAttributeType() == Attribute.PersistentAttributeType.MANY_TO_MANY
                || attribute.getPersistentAttributeType() == Attribute.PersistentAttributeType.ONE_TO_MANY) {
            return new ObjectRelator(entityType, manager) {
                @Override
                public void relateToParent(Object object) {
                    JpaUtil.setToManyTarget(parentObject, (PluralAttribute<?,?,?>)attribute, object);
                    manager.merge(parentObject);
                }
            };
        } else {
            return new ObjectRelator(entityType, manager) {
                @Override
                public void relateToParent(Object object) {
                    JpaUtil.setToOneTarget(parentObject, attribute, object);
                    manager.merge(parentObject);
                }
            };
        }
    }

    protected Attribute<?, ?> attributeForAgAttribute(AgEntity<?> agEntity, String attributeName) {
        return metamodel.entity(agEntity.getType()).getAttribute(attributeName);
    }

    interface RelationshipUpdate {
        boolean containsRelatedObject(Object o);

        void removeUpdateForRelatedObject(Object o);
    }

    static class ObjectRelator {

        private final EntityType<Object> entityType;
        private final EntityManager manager;

        ObjectRelator(EntityType<Object> entityType, EntityManager manager) {
            this.entityType = entityType;
            this.manager = manager;
        }

        void relateToParent(Object object) {
            // do nothing
        }

        void relate(AgRelationship agRelationship, Object object, Object relatedObject) {
            Attribute<? super Object, ?> attribute = entityType.getAttribute(agRelationship.getName());
            if (agRelationship.isToMany()) {
                JpaUtil.setToManyTarget(object, (PluralAttribute<?, ?, ?>) attribute, relatedObject);
            } else {
                JpaUtil.setToOneTarget(object, attribute, relatedObject);
            }
        }

        void unrelateAll(AgRelationship agRelationship, Object object) {
            unrelateAll(agRelationship, object, null);
        }

        void unrelateAll(AgRelationship agRelationship, Object object, RelationshipUpdate relationshipUpdate) {
            Attribute<? super Object, ?> attribute = entityType.getAttribute(agRelationship.getName());
            if (agRelationship.isToMany()) {
                @SuppressWarnings("unchecked")
                List<Object> relatedObjects = (List<Object>) JpaUtil.readProperty(object, attribute);

                for (int i = 0; i < relatedObjects.size(); i++) {
                    Object relatedObject = relatedObjects.get(i);
                    if (relationshipUpdate == null || !relationshipUpdate.containsRelatedObject(relatedObject)) {
                        JpaUtil.removeToManyTarget(object, (PluralAttribute<?, ?, ?>) attribute, relatedObject);
                        i--;
                    } else {
                        relationshipUpdate.removeUpdateForRelatedObject(relatedObject);
                    }
                }
            } else {
                JpaUtil.setToOneTarget(object, attribute, null);
            }
        }
    }
}
