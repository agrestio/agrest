package com.nhl.link.rest.runtime.parser.pointer;

import com.nhl.link.rest.LinkRestException;
import com.nhl.link.rest.runtime.cayenne.ICayennePersister;
import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.exp.ExpressionFactory;
import org.apache.cayenne.map.Entity;
import org.apache.cayenne.map.ObjEntity;
import org.apache.cayenne.query.PrefetchTreeNode;
import org.apache.cayenne.query.SelectById;
import org.apache.cayenne.query.SelectQuery;
import org.apache.cayenne.reflect.PropertyDescriptor;

import java.util.ArrayList;
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
                    case TO_MANY_RELATIONSHIP: {
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
    public Object resolveObject(Class<?> type, Object id) {
        return SelectById.query(type, id).selectOne(delegateContext);
    }

    @Override
    public Object resolveProperty(Class<?> type, String propertyName, Object baseObject) {

        ObjEntity entity = cayenneService.entityResolver().getObjEntity(type);
        PropertyDescriptor property = cayenneService.entityResolver()
                .getClassDescriptor(entity.getName()).getProperty(propertyName);
        return property.readProperty(baseObject);
    }
}
