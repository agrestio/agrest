package io.agrest.cayenne.processor;

import io.agrest.AgException;
import io.agrest.AgObjectId;
import io.agrest.EntityParent;
import io.agrest.RelatedResourceEntity;
import io.agrest.ResourceEntity;
import io.agrest.RootResourceEntity;
import io.agrest.protocol.Direction;
import io.agrest.protocol.Sort;
import io.agrest.cayenne.path.IPathResolver;
import io.agrest.cayenne.path.PathOps;
import io.agrest.cayenne.persister.ICayennePersister;
import io.agrest.cayenne.exp.ICayenneExpParser;
import io.agrest.cayenne.exp.ICayenneExpPostProcessor;
import io.agrest.meta.AgSchema;
import io.agrest.meta.AgEntity;
import io.agrest.meta.AgIdPart;
import io.agrest.runtime.processor.select.SelectContext;
import org.apache.cayenne.di.Inject;
import org.apache.cayenne.di.Provider;
import org.apache.cayenne.exp.Expression;
import org.apache.cayenne.exp.ExpressionFactory;
import org.apache.cayenne.exp.parser.ASTDbPath;
import org.apache.cayenne.exp.parser.ASTPath;
import org.apache.cayenne.exp.property.Property;
import org.apache.cayenne.exp.property.PropertyFactory;
import org.apache.cayenne.map.EntityResolver;
import org.apache.cayenne.map.ObjEntity;
import org.apache.cayenne.map.ObjRelationship;
import org.apache.cayenne.query.ColumnSelect;
import org.apache.cayenne.query.FluentSelect;
import org.apache.cayenne.query.ObjectSelect;
import org.apache.cayenne.query.Ordering;
import org.apache.cayenne.query.SortOrder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

/**
 * @since 3.4
 */
public class CayenneQueryAssembler implements ICayenneQueryAssembler {

    private final Provider<AgSchema> schema;
    private final EntityResolver entityResolver;
    private final IPathResolver pathResolver;
    private final ICayenneExpParser qualifierParser;
    private final ICayenneExpPostProcessor qualifierPostProcessor;

    public CayenneQueryAssembler(

            // AgSchema is not yet available when we are first creating CayenneQueryAssembler,
            // so injecting provider for lazy init
            @Inject Provider<AgSchema> schema,

            @Inject ICayennePersister persister,
            @Inject IPathResolver pathResolver,
            @Inject ICayenneExpParser qualifierParser,
            @Inject ICayenneExpPostProcessor qualifierPostProcessor) {

        this.schema = schema;
        this.entityResolver = persister.entityResolver();
        this.pathResolver = pathResolver;
        this.qualifierParser = qualifierParser;
        this.qualifierPostProcessor = qualifierPostProcessor;
    }

    @Override
    public <T> ObjectSelect<T> createRootQuery(SelectContext<T> context) {

        ObjectSelect<T> query = context.getId() != null
                ? createRootIdQuery(context.getEntity(), context.getId())
                : createBaseQuery(context.getEntity());

        EntityParent<?> parent = context.getParent();
        if (parent != null) {
            query.and(CayenneUtil.parentQualifier(
                    pathResolver,
                    schema.get().getEntity(parent.getType()),
                    parent,
                    entityResolver));
        }

        return query;
    }

    @Override
    public <T> ColumnSelect<Object[]> createQueryWithParentQualifier(RelatedResourceEntity<T> entity) {

        ColumnSelect<Object[]> query = createBaseQuery(entity).columns(queryColumns(entity));

        // Translate expression from parent.
        // Find the closest parent in the chain that has a query of its own, and use that as a base.
        Expression parentQualifier = resolveParentQualifier(entity, null);
        if (parentQualifier != null) {
            query.and(parentQualifier);
        }

        return query;
    }

    /**
     * @since 5.0
     */
    public <T> Property<?>[] queryColumns(RelatedResourceEntity<T> entity) {

        // Use Cayenne metadata for query building. Agrest metadata may be missing some important parts like ids
        // (e.g. see https://github.com/agrestio/agrest/issues/473)

        AgEntity<?> parentEntity = entity.getParent().getAgEntity();
        ObjEntity parentObjEntity = entityResolver.getObjEntity(entity.getParent().getName());
        ObjRelationship objRelationship = parentObjEntity.getRelationship(entity.getIncoming().getName());
        ASTDbPath reversePath = new ASTDbPath(objRelationship.getReverseDbRelationshipPath());

        Property<?>[] columns = new Property<?>[parentEntity.getIdParts().size() + 1];

        Class entityType = entity.getType();
        columns[0] = PropertyFactory.createSelf(entityType);

        // columns must be added in the order of id parts iteration, as this is how they will be read from result
        int i = 1;
        for (AgIdPart idPart : parentEntity.getIdParts()) {
            ASTPath idPartPath = pathResolver.resolve(parentEntity, idPart.getName()).getPathExp();
            Expression propertyExp = PathOps.concatWithDbPath(parentObjEntity, reversePath, idPartPath);
            columns[i++] = PropertyFactory.createBase(propertyExp, idPart.getType());
        }

        return columns;
    }

    // using dbpaths for all expression operations on the theory that some object paths can be unidirectional, and
    // hence may be missing for some relationships (although all "incoming" relationships along the parents chain
    // should be present, no?)
    protected Expression resolveParentQualifier(RelatedResourceEntity<?> entity, String outgoingDbPath) {

        ResourceEntity<?> parent = entity.getParent();
        CayenneResourceEntityExt parentExt = CayenneProcessor.getEntity(parent);
        if (parentExt == null) {
            throw AgException.internalServerError("Parent '%s' of entity '%s' is not managed by Cayenne",
                    parent.getName(),
                    entity.getName());
        }

        FluentSelect<?> parentSelect = parentExt.getSelect();

        if (parentSelect != null) {

            Expression parentQualifier = parentSelect.getWhere();
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

        return resolveParentQualifier((RelatedResourceEntity) parent, fullDbPath);
    }

    private String concatWithParentDbPath(ObjRelationship incoming, String outgoingDbPath) {
        String dbPath = incoming.getDbRelationshipPath();
        return outgoingDbPath != null ? dbPath + "." + outgoingDbPath : dbPath;
    }

    @Override
    public <T, P> ColumnSelect<Object[]> createQueryWithParentIdsQualifier(RelatedResourceEntity<T> entity, Iterator<P> parentData) {

        ColumnSelect<Object[]> query = createBaseQuery(entity).columns(queryColumns(entity));

        // build id-based qualifier
        List<Expression> qualifiers = new ArrayList<>();

        // if pagination is in effect, we should only fault the requested range. It makes this particular strategy
        // very efficient in case of pagination

        ObjEntity parentObjEntity = entityResolver.getObjEntity(entity.getParent().getName());
        ObjRelationship objRelationship = parentObjEntity.getRelationship(entity.getIncoming().getName());
        String reversePath = objRelationship.getReverseDbRelationshipPath();

        consumeRange(parentData, entity.getParent().getStart(), entity.getParent().getLimit(),
                // TODO: this only works for single column ids
                p -> qualifiers.add(ExpressionFactory.matchDbExp(reversePath, p)));

        // TODO: There is some functionality in Cayenne that allows to break long OR qualifiers in a series of queries.
        //  How do we use it here?
        return query.and(ExpressionFactory.or(qualifiers));
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

    protected <T> ObjectSelect<T> createRootIdQuery(ResourceEntity<T> entity, AgObjectId rootId) {
        return ObjectSelect.query(entity.getType())
                .where(buildIdQualifier(entity.getAgEntity(), rootId));
    }

    protected <T> ObjectSelect<T> createBaseQuery(ResourceEntity<T> entity) {

        ObjectSelect<T> query = ObjectSelect.query(entity.getType());

        if (!entity.isFiltered()) {
            int limit = entity.getLimit();
            if (limit > 0) {
                query.pageSize(limit);
            }
        }

        Expression parsedExp = qualifierParser.parse(entity.getExp());
        Expression finalExp = qualifierPostProcessor.process(entity.getAgEntity(), parsedExp);
        query.where(finalExp);

        for (Sort o : entity.getOrderings()) {
            query.orderBy(toOrdering(entity, o));
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

            ASTPath path = pathResolver.resolve(entity, idAttribute.getName()).getPathExp();
            qualifiers.add(ExpressionFactory.matchExp(path, idValue));
        }
        return ExpressionFactory.and(qualifiers);
    }

    protected Ordering toOrdering(ResourceEntity<?> entity, Sort sort) {
        return new Ordering(
                pathResolver.resolve(entity.getAgEntity(), sort.getPath()).getPathExp(),
                toSortOrder(sort.getDirection()));
    }

    private SortOrder toSortOrder(Direction direction) {
        switch (direction) {
            case asc:
                return SortOrder.ASCENDING;
            case asc_ci:
                return SortOrder.ASCENDING_INSENSITIVE;
            case desc_ci:
                return SortOrder.DESCENDING_INSENSITIVE;
            case desc:
                return SortOrder.DESCENDING;
            default:
                throw new IllegalArgumentException("Missing or unexpected sort direction: " + direction);
        }
    }
}
