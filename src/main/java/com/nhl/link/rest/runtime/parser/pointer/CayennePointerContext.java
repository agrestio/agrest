package com.nhl.link.rest.runtime.parser.pointer;

import com.nhl.link.rest.LinkRestException;
import com.nhl.link.rest.runtime.cayenne.ICayennePersister;
import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.PersistenceState;
import org.apache.cayenne.Persistent;
import org.apache.cayenne.exp.ExpressionFactory;
import org.apache.cayenne.map.Entity;
import org.apache.cayenne.map.ObjEntity;
import org.apache.cayenne.query.ObjectSelect;
import org.apache.cayenne.query.PrefetchTreeNode;
import org.apache.cayenne.query.SelectById;
import org.apache.cayenne.query.SelectQuery;
import org.apache.cayenne.reflect.AttributeProperty;
import org.apache.cayenne.reflect.ClassDescriptor;
import org.apache.cayenne.reflect.PropertyDescriptor;
import org.apache.cayenne.reflect.PropertyVisitor;
import org.apache.cayenne.reflect.ToManyProperty;
import org.apache.cayenne.reflect.ToOneProperty;
import org.apache.cayenne.util.PersistentObjectCollection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static javax.ws.rs.core.Response.Status;

public class CayennePointerContext implements PointerContext {

    private ICayennePersister cayenneService;
    private ObjectContext delegateContext;

    // this map holds references to all objects
    // that will be used as roots for this context's pointers
    // (needed because they might get pushed out
    //  of Cayenne's context internal cache)
    private final Map<Class<?>, List<?>> objectStore;

    CayennePointerContext(ICayennePersister cayenneService, List<? extends LrPointer> pointers) {

        this.cayenneService = cayenneService;

        Map<Class<?>, SelectQuery<?>> queries = getQueries(pointers);
        objectStore = new HashMap<>((int)(queries.size() / 0.75));

        ObjectContext context = cayenneService.newContext();
        for (Map.Entry<Class<?>, SelectQuery<?>> entry : queries.entrySet()) {
            objectStore.put(entry.getKey(), context.select(entry.getValue()));
        }
        this.delegateContext = context;
    }

    private Map<Class<?>, SelectQuery<?>> getQueries(List<? extends LrPointer> pointers) {

        Map<Class<?>, SelectQuery<?>> queries = new HashMap<>(2); // usually there's only one base entity per request
        for (LrPointer pointer : pointers) {

            Class<?> baseEntityClass = pointer.getBaseType();
            SelectQuery<?> query = queries.get(baseEntityClass);
            if (query == null) {
                query = SelectQuery.query(baseEntityClass);
                queries.put(baseEntityClass, query);
            }

            // TODO: detect and optimize loops ( e.g. "e3s:3.e5.e2s:2.e3s:33" )
            ObjEntity baseEntity = cayenneService.entityResolver().getObjEntity(baseEntityClass);
            String currentPath = "";
            List<LrPointer> parts = collectParts(pointer);
            for (int i = parts.size() - 1; i >= 0; i--) {
                LrPointer part = parts.get(i);
                switch (part.getType()) {
                    case ENTITY_COLLECTION: {
                        // here we could remove all query conditions that we've added previously
                        // in order to get a "select all" query and then terminate the loop.
                        // but we won't do that now for reasons below:
                        // - for entities with a big number of instances, especially when there are several such
                        //   operations in one context, naive cache can exhaust server memory; we rather distribute
                        //   the load in time, making query only when operation is performed
                        // - batch optimizations will help to reduce the instantenous load for trivial
                        //   select all / delete all operations
                        // - if we allow pointers to include "entity collection" path segments, then we can't
                        //   be sure if all objects will be needed at all
                        continue;
                    }
                    case INSTANCE: {
                        if (!baseEntityClass.equals(part.getTargetType())) {
                            continue;
                        }
                        if (baseEntity.getPrimaryKeys().size() > 1) {
                            throw new LinkRestException(Status.INTERNAL_SERVER_ERROR, "Multi-attribute IDs not supported");
                        }
                        // this can be when pointer is resolving another instance of the same type as it's predecessor
                        // ( e.g. "e3s:1.3" )
                        query.andQualifier(ExpressionFactory.matchDbExp(
                                baseEntity.getPrimaryKeyNames().iterator().next(),
                                ((ObjectInstancePointer)part).getId()
                        ));
                        break;
                    }
                    case ATTRIBUTE: {
                        // no need to do anything for attribute pointer
                        break;
                    }
                    case EXPLICIT_TO_ONE_RELATIONSHIP:
                    case IMPLICIT_TO_ONE_RELATIONSHIP:
                    case TO_MANY_RELATIONSHIP:
                    case TO_MANY_LIST_RELATIONSHIP: {
                        String relationshipName = ((RelationshipPointer)part).getRelationship().getName();
                        currentPath += Entity.PATH_SEPARATOR + relationshipName;
                        PrefetchTreeNode prefetch = PrefetchTreeNode.withPath(
                                currentPath, PrefetchTreeNode.JOINT_PREFETCH_SEMANTICS);
                        query.addPrefetch(prefetch);
                        break;
                    }
                    default: {
                        throw new LinkRestException(Status.INTERNAL_SERVER_ERROR,
                                "Unknown pointer type: " + part.getType().name());
                    }
                }
            }
        }

        return queries;
    }

    /**
     * @return Pointer's chain of predecessors, in reverse order (from last to first)
     */
    private List<LrPointer> collectParts(LrPointer pointer) {

        List<LrPointer> parts = new ArrayList<>();
        LrPointer part = pointer;
        do {
            parts.add(part);

        } while ((part = part.getParent()) != null);

        return parts;
    }


    @Override
    public void commitChanges() {
        delegateContext.commitChanges();
    }

    @Override
    public <T> Collection<T> resolveAll(Class<T> type) {
        return ObjectSelect.query(type).select(delegateContext);
    }

    @Override
    public void removeAll(Class<?> type) {

        Collection<?> objects = resolveAll(type);
        for (Object object : objects) {
            delegateContext.deleteObject(object);
        }
    }

    @Override
    public void addObject(Object newObject) {

        int state = ((Persistent) newObject).getPersistenceState();
        if (state != PersistenceState.TRANSIENT) {
            throw new LinkRestException(Status.INTERNAL_SERVER_ERROR,
                    "Expected transient object, but got persistence state: " +
                            PersistenceState.persistenceStateName(state));
        }

        Object persistent = delegateContext.newObject(newObject.getClass());
        merge((Persistent) newObject, (Persistent) persistent);
    }

    @Override
    public <T> T resolveObject(Class<T> type, Object id) {
        return getObject(type, id);
    }

    @Override
    public void updateObject(Class<?> type, Object id, Object newObject) {

        Persistent persistent = (Persistent) getObject(type, id);
        if (persistent == null) {
            throw new LinkRestException(Status.INTERNAL_SERVER_ERROR,
                    String.format("Object not found. Class: %s, id: %s", type.getName(), id));
        }
        merge((Persistent) newObject, persistent);
    }

    // only scalar attributes can be updated for now...
    // will need to implement JSON merge in order to support relationships
    private Object merge(Persistent source, final Persistent destination) {

        if (!destination.getClass().equals(source.getClass())) {
            throw new LinkRestException(Status.INTERNAL_SERVER_ERROR,
                    String.format("Expected object of class: %s, but got: %s",
                            destination.getClass().getName(), source.getClass().getName()));
        }

        final Object localSourceObject;
        if (source.getPersistenceState() == PersistenceState.TRANSIENT) {
            localSourceObject = source;
        } else {
            localSourceObject = delegateContext.localObject(source);
        }

        final ClassDescriptor classDescriptor = getClassDescriptor(destination.getClass());
        classDescriptor.visitAllProperties(new PropertyVisitor() {
            @Override
            public boolean visitAttribute(AttributeProperty property) {
                property.writeProperty(destination,
                        property.readProperty(destination), property.readPropertyDirectly(localSourceObject));
                return true;
            }

            @Override
            public boolean visitToOne(ToOneProperty property) {
                return false;
            }

            @Override
            public boolean visitToMany(ToManyProperty property) {
                return false;
            }
        });

        return destination;
    }

	@Override
    public Object resolveProperty(Object baseObject, String propertyName) {

        PropertyDescriptor property = getProperty(baseObject.getClass(), propertyName);
        return property.readProperty(baseObject);
    }

    @Override
    public void updateProperty(Object baseObject, final String propertyName, final Object value) {

        final Object localObject = localizeObject((Persistent) baseObject);

        PropertyDescriptor property = getProperty(baseObject.getClass(), propertyName);
        property.visit(new PropertyVisitor() {
            @Override
            public boolean visitAttribute(AttributeProperty property) {
                property.writeProperty(localObject, property.readProperty(localObject), value);
                return true;
            }

            @Override
            public boolean visitToOne(ToOneProperty property) {
                property.writeProperty(localObject, property.readProperty(localObject), value);
                return true;
            }

            @Override
            public boolean visitToMany(ToManyProperty property) {
                throw new LinkRestException(Status.INTERNAL_SERVER_ERROR,
                        "Can't update to-many relationship: " + propertyName);
            }
        });
    }

    @Override
    public void deleteProperty(Object baseObject, final String propertyName) {

        final Object localObject = localizeObject((Persistent) baseObject);

        PropertyDescriptor property = getProperty(baseObject.getClass(), propertyName);
        property.visit(new PropertyVisitor() {
            @Override
            public boolean visitAttribute(AttributeProperty property) {
                property.writeProperty(localObject, property.readProperty(localObject), null);
                return true;
            }

            @Override
            public boolean visitToOne(ToOneProperty property) {
                property.setTarget(localObject, null, true);
                return true;
            }

            @Override
            public boolean visitToMany(ToManyProperty property) {
                Object[] relatedObjects =
                        ((PersistentObjectCollection) property.readProperty(localObject)).toArray();
                for (Object relatedObject : relatedObjects) {
                    property.removeTarget(localObject, relatedObject, true);
                }
                return true;
            }
        });
    }

    @Override
    public void addRelatedObject(Object baseObject, final String propertyName, final Object value) {

        final Object localObject = localizeObject((Persistent) baseObject);

        PropertyDescriptor property = getProperty(baseObject.getClass(), propertyName);
        property.visit(new PropertyVisitor() {
            @Override
            public boolean visitAttribute(AttributeProperty property) {
                throw new LinkRestException(Status.INTERNAL_SERVER_ERROR,
                        "Expected relationship, got attribute: " + propertyName);
            }

            @Override
            public boolean visitToOne(ToOneProperty property) {
                property.writeProperty(localObject, property.readProperty(localObject),
                        localizeObject((Persistent) value));
                return true;
            }

            @Override
            public boolean visitToMany(ToManyProperty property) {
                property.addTarget(localObject, localizeObject((Persistent) value), true);
                return true;
            }
        });
    }

    @Override
    public void deleteRelatedObject(Object baseObject, final String propertyName, final Object relatedId) {

        final Object localObject = localizeObject((Persistent) baseObject);

        PropertyDescriptor property = getProperty(baseObject.getClass(), propertyName);
        property.visit(new PropertyVisitor() {
            @Override
            public boolean visitAttribute(AttributeProperty property) {
                throw new LinkRestException(Status.INTERNAL_SERVER_ERROR,
                        "Expected to-many relationship, but got attribute: " + propertyName);
            }

            @Override
            public boolean visitToOne(ToOneProperty property) {
                Persistent relatedObject = (Persistent) property.readProperty(localObject);
                if (relatedObject != null) {
                    Object id = relatedObject.getObjectId().getIdSnapshot().values().iterator().next();
                    // TODO: types normalization
                    // TODO: compound ids (i.e. when relatedId is Map)
                    if (id.equals(relatedId)) {
                        throw new LinkRestException(Status.INTERNAL_SERVER_ERROR,
                                String.format("Related object (%s) has different id. Expected %s, actual: %s",
                                        propertyName, relatedId, id));
                    } else {
                        property.setTarget(localObject, null, true);
                    }
                }
                return true;
            }

            @Override
            public boolean visitToMany(ToManyProperty property) {
                Class<?> targetClass = property.getTargetDescriptor().getObjectClass();
                Object relatedObject = resolveObject(targetClass, relatedId);
                if (relatedObject != null) {
                    property.removeTarget(localObject, relatedObject, true);
                }
                return true;
            }
        });
    }

    private <T> T getObject(Class<T> type, Object id) {
        return SelectById.query(type, id).selectOne(delegateContext);
    }

    private Persistent localizeObject(Persistent object) {

        if (object.getPersistenceState() == PersistenceState.TRANSIENT) {
            throw new LinkRestException(Status.INTERNAL_SERVER_ERROR,
                    "Expected persistent object, but got transient");
        }
        return delegateContext.localObject(object);
    }

    @Override
    public void deleteObject(Class<?> type, Object id) {

        Object object = getObject(type, id);
        if (object != null) {
            delegateContext.deleteObject(object);
        }
    }

    private PropertyDescriptor getProperty(Class<?> type, String propertyName) {
        return getClassDescriptor(type).getProperty(propertyName);
    }

    private ClassDescriptor getClassDescriptor(Class<?> type) {

        ObjEntity entity = delegateContext.getEntityResolver().getObjEntity(type);
        ClassDescriptor classDescriptor = delegateContext.getEntityResolver().getClassDescriptor(entity.getName());
        if (classDescriptor == null) {
            throw new LinkRestException(Status.INTERNAL_SERVER_ERROR,
                        "Cayenne descriptor not found for class: " + type.getName());
        }
        return classDescriptor;
    }
}
