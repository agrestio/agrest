package io.agrest.cayenne.processor;

import io.agrest.AgException;
import io.agrest.AgObjectId;
import io.agrest.NestedResourceEntity;
import io.agrest.ResourceEntity;
import io.agrest.RootResourceEntity;
import io.agrest.base.protocol.Dir;
import io.agrest.base.protocol.Sort;
import io.agrest.cayenne.path.IPathResolver;
import io.agrest.cayenne.persister.ICayennePersister;
import io.agrest.cayenne.qualifier.IQualifierParser;
import io.agrest.cayenne.qualifier.IQualifierPostProcessor;
import io.agrest.meta.AgEntity;
import io.agrest.meta.AgIdPart;
import io.agrest.runtime.processor.select.SelectContext;
import org.apache.cayenne.dba.TypesMapping;
import org.apache.cayenne.di.Inject;
import org.apache.cayenne.exp.Expression;
import org.apache.cayenne.exp.ExpressionFactory;
import org.apache.cayenne.exp.parser.ASTPath;
import org.apache.cayenne.exp.property.Property;
import org.apache.cayenne.exp.property.PropertyFactory;
import org.apache.cayenne.map.DbAttribute;
import org.apache.cayenne.map.EntityResolver;
import org.apache.cayenne.map.ObjAttribute;
import org.apache.cayenne.map.ObjEntity;
import org.apache.cayenne.map.ObjRelationship;
import org.apache.cayenne.query.Ordering;
import org.apache.cayenne.query.SelectQuery;
import org.apache.cayenne.query.SortOrder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

import static io.agrest.base.reflect.Types.typeForName;

/**
 * @since 3.4
 */
public class CayenneQueryAssembler implements ICayenneQueryAssembler {

    private final EntityResolver entityResolver;
    private final IPathResolver pathCache;
    private final IQualifierParser qualifierParser;
    private final IQualifierPostProcessor qualifierPostProcessor;

    public CayenneQueryAssembler(
            @Inject ICayennePersister persister,
            @Inject IPathResolver pathCache,
            @Inject IQualifierParser qualifierParser,
            @Inject IQualifierPostProcessor qualifierPostProcessor) {
        this.entityResolver = persister.entityResolver();
        this.pathCache = pathCache;
        this.qualifierParser = qualifierParser;
        this.qualifierPostProcessor = qualifierPostProcessor;
    }

    @Override
    public <T> SelectQuery<T> createRootQuery(SelectContext<T> context) {

        SelectQuery<T> query = context.getId() != null
                ? createRootIdQuery(context.getEntity(), context.getId())
                : createBaseQuery(context.getEntity());

        if (context.getParent() != null) {
            query.andQualifier(CayenneUtil.parentQualifier(context.getParent(), entityResolver));
        }

        return query;
    }

    @Override
    public <T> SelectQuery<T> createQueryWithParentQualifier(NestedResourceEntity<T> entity) {

        SelectQuery<T> query = createBaseQuery(entity);

        // Use Cayenne metadata for query building. Agrest metadata may be missing some important parts like ids
        // (e.g. see https://github.com/agrestio/agrest/issues/473)

        ObjEntity parentObjEntity = entityResolver.getObjEntity(entity.getParent().getName());
        ObjRelationship objRelationship = parentObjEntity.getRelationship(entity.getIncoming().getName());
        String reversePath = objRelationship.getReverseDbRelationshipPath();

        List<Property<?>> properties = new ArrayList<>();
        Class entityType = entity.getType();
        properties.add(PropertyFactory.createSelf(entityType));

        for (DbAttribute pk : parentObjEntity.getDbEntity().getPrimaryKeys()) {
            String path = reversePath + "." + pk.getName();
            Expression propertyExp = ExpressionFactory.dbPathExp(path);
            Class<?> pkType = typeForName(TypesMapping.getJavaBySqlType(pk.getType()));
            properties.add(PropertyFactory.createBase(propertyExp, pkType));
        }

        query.setColumns(properties);

        // Translate expression from parent.
        // Find the closest parent in the chain that has a query of its own, and use that as a base.
        Expression parentQualifier = resolveParentQualifier(entity, null);
        if (parentQualifier != null) {
            query.andQualifier(parentQualifier);
        }

        return query;
    }

    // using dbpaths for all expression operations on the theory that some object paths can be unidirectional, and
    // hence may be missing for some relationships (although all "incoming" relationships along the parents chain
    // should be present, no?)
    protected Expression resolveParentQualifier(NestedResourceEntity<?> entity, String outgoingDbPath) {

        ResourceEntity<?> parent = entity.getParent();
        CayenneResourceEntityExt<?> parentExt = CayenneProcessor.getCayenneEntity(parent);
        if(parentExt == null) {
            throw AgException.internalServerError("Parent '%s' of entity '%s' is not managed by Cayenne",
                    parent.getName(),
                    entity.getName());
        }

        SelectQuery<?> select = parentExt.getSelect();

        if (select != null) {

            Expression parentQualifier = select.getQualifier();
            if (parentQualifier == null) {
                return null;
            }

            ObjEntity parentObjEntity = entityResolver.getObjEntity(parent.getType());
            ObjRelationship incoming = parentObjEntity.getRelationship(entity.getIncoming().getName());

            if (incoming == null) {
                throw new IllegalStateException("No such relationship: " + parentObjEntity.getName() + "." + entity.getIncoming().getName());
            }

            String fullDbPath = concatWithParentDbPath(incoming, outgoingDbPath);
            Expression dbParentQualifier = parentObjEntity.translateToDbPath(parentQualifier);
            return parentObjEntity.getDbEntity().translateToRelatedEntity(dbParentQualifier, fullDbPath);
        }

        ObjEntity parentObjEntity = entityResolver.getObjEntity(entity.getParent().getType());
        ObjRelationship incoming = parentObjEntity.getRelationship(entity.getIncoming().getName());
        String fullDbPath = concatWithParentDbPath(incoming, outgoingDbPath);

        // shouldn't really happen with any of the current built-in root strategies, but who knows what customizations
        // can be applied
        if (parent instanceof RootResourceEntity) {
            throw new IllegalStateException(
                    "Can't fetch child using parent expression strategy. Root entity '" +
                            parent.getName() +
                            "' has no SelectQuery of its own. DB path to child: " +
                            fullDbPath);
        }

        return resolveParentQualifier((NestedResourceEntity) parent, fullDbPath);
    }

    private String concatWithParentDbPath(ObjRelationship incoming, String outgoingDbPath) {
        String dbPath = incoming.getDbRelationshipPath();
        return outgoingDbPath != null ? dbPath + "." + outgoingDbPath : dbPath;
    }

    @Override
    public <T, P> SelectQuery<T> createQueryWithParentIdsQualifier(NestedResourceEntity<T> entity, Iterator<P> parentData) {

        SelectQuery<T> query = createBaseQuery(entity);

        // Use Cayenne metadata for query building. Agrest metadata may be missing some important parts like ids
        // (e.g. see https://github.com/agrestio/agrest/issues/473)

        ObjEntity parentObjEntity = entityResolver.getObjEntity(entity.getParent().getName());
        ObjRelationship objRelationship = parentObjEntity.getRelationship(entity.getIncoming().getName());
        String reversePath = objRelationship.getReverseDbRelationshipPath();

        List<Property<?>> properties = new ArrayList<>();
        Class entityType = entity.getType();
        properties.add(PropertyFactory.createSelf(entityType));

        for (DbAttribute pk : parentObjEntity.getDbEntity().getPrimaryKeys()) {
            String path = reversePath + "." + pk.getName();
            Expression propertyExp = ExpressionFactory.dbPathExp(path);
            Class<?> pkType = typeForName(TypesMapping.getJavaBySqlType(pk.getType()));
            properties.add(PropertyFactory.createBase(propertyExp, pkType));
        }

        query.setColumns(properties);

        // build id-based qualifier
        List<Expression> qualifiers = new ArrayList<>();

        // if pagination is in effect, we should only fault the requested range. It makes this particular strategy
        // very efficient in case of pagination
        consumeRange(parentData, entity.getParent().getFetchOffset(), entity.getParent().getFetchLimit(),
                // TODO: this only works for single column ids
                p -> qualifiers.add(ExpressionFactory.matchDbExp(reversePath, p)));

        // TODO: There is some functionality in Cayenne that allows to break long OR qualifiers in a series of queries.
        //  How do we use it here?
        query.andQualifier(ExpressionFactory.joinExp(Expression.OR, qualifiers));

        return query;
    }

    static <P> void consumeRange(Iterator<P> parentData, int offset, int len, Consumer<P> consumer) {

        int from = Math.max(0, offset);
        int to = len > 0 ? Math.min(from + len, Integer.MAX_VALUE) : Integer.MAX_VALUE;

        for (int i = 0; i < from && parentData.hasNext(); i++) {
            parentData.next();
        }

        for (int i = from; i < to && parentData.hasNext(); i++) {
            consumer.accept(parentData.next());
        }
    }

    @Override
    public <T> SelectQuery<T> createRootIdQuery(ResourceEntity<T> entity, AgObjectId rootId) {
        SelectQuery<T> query = new SelectQuery<>(entity.getType());
        query.andQualifier(buildIdQualifier(entity.getAgEntity(), rootId));
        return query;
    }

    protected <T> SelectQuery<T> createBaseQuery(ResourceEntity<T> entity) {

        SelectQuery<T> query = SelectQuery.query(entity.getType());

        if (!entity.isFiltered()) {
            int limit = entity.getFetchLimit();
            if (limit > 0) {
                query.setPageSize(limit);
            }
        }

        Expression parsedExp = qualifierParser.parse(entity.getQualifier());
        Expression finalExp = qualifierPostProcessor.process(entity.getAgEntity(), parsedExp);
        query.setQualifier(finalExp);

        for (Sort o : entity.getOrderings()) {
            query.addOrdering(toOrdering(entity, o));
        }

        return query;
    }

    protected Expression buildIdQualifier(AgEntity<?> entity, AgObjectId id) {

        Collection<AgIdPart> idAttributes = entity.getIdParts();
        if (idAttributes.size() != id.size()) {
            throw AgException.badRequest("Wrong ID size: expected %s, got: %s", idAttributes.size(), id.size());
        }

        Collection<Expression> qualifiers = new ArrayList<>();
        for (AgIdPart idAttribute : idAttributes) {
            Object idValue = id.get(idAttribute.getName());
            if (idValue == null) {
                throw AgException.badRequest(
                        "Failed to build a Cayenne qualifier for entity %s: one of the entity's ID parts is missing in this ID: %s",
                        entity.getName(),
                        idAttribute.getName());
            }

            DbAttribute dbAttribute = dbAttributeForAgIdPart(entity, idAttribute);

            if (dbAttribute == null) {
                throw AgException.internalServerError("ID attribute '%s' has no mapping to a column name", idAttribute.getName());
            }

            qualifiers.add(ExpressionFactory.matchDbExp(dbAttribute.getName(), idValue));
        }
        return ExpressionFactory.and(qualifiers);
    }

    protected DbAttribute dbAttributeForAgIdPart(AgEntity<?> agEntity, AgIdPart agIdPart) {

        ObjEntity entity = entityResolver.getObjEntity(agEntity.getName());
        ObjAttribute objAttribute = entity.getAttribute(agIdPart.getName());
        return objAttribute != null
                ? objAttribute.getDbAttribute()
                // this is suspect.. don't see how we would allow DbAttribute names to leak in the Ag model
                : entity.getDbEntity().getAttribute(agIdPart.getName());
    }

    protected Ordering toOrdering(ResourceEntity<?> entity, Sort sort) {
        return new Ordering(toCayennePath(entity, sort.getProperty()), toSortOrder(sort.getDirection()));
    }

    private ASTPath toCayennePath(ResourceEntity<?> entity, String agPath) {
        return pathCache.resolve(entity.getAgEntity(), agPath).getPathExp();
    }

    private SortOrder toSortOrder(Dir direction) {
        switch (direction) {
            case ASC:
                return SortOrder.ASCENDING;
            case ASC_CI:
                return SortOrder.ASCENDING_INSENSITIVE;
            case DESC_CI:
                return SortOrder.DESCENDING_INSENSITIVE;
            case DESC:
                return SortOrder.DESCENDING;
            default:
                throw new IllegalArgumentException("Missing or unexpected sort direction: " + direction);
        }
    }
}
