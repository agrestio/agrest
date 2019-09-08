package io.agrest.meta.cayenne;

import io.agrest.ResourceEntity;
import io.agrest.meta.AgEntity;
import io.agrest.meta.AgRelationship;
import io.agrest.property.PropertyReader;
import org.apache.cayenne.exp.Expression;
import org.apache.cayenne.map.ObjRelationship;

import java.util.Objects;
import java.util.function.Function;

/**
 * @since 1.12
 */
public class CayenneAgRelationship implements AgRelationship {

    private ObjRelationship objRelationship;
    private AgEntity<?> targetEntity;
    private Function<ResourceEntity<?>, PropertyReader> readerFactory;

    /**
     * @since 2.10
     */
    public CayenneAgRelationship(
            ObjRelationship objRelationship,
            AgEntity<?> targetEntity,
            Function<ResourceEntity<?>, PropertyReader> readerFactory) {

        this.objRelationship = objRelationship;
        this.targetEntity = Objects.requireNonNull(targetEntity);
        this.readerFactory = readerFactory;
    }

    @Override
    public PropertyReader getPropertyReader(ResourceEntity<?> entity) {
        return readerFactory.apply(entity);
    }

    @Override
    public String getName() {
        return objRelationship.getName();
    }

    @Override
    public AgEntity<?> getTargetEntity() {
        return targetEntity;
    }

    @Override
    public boolean isToMany() {
        return objRelationship.isToMany();
    }

    public ObjRelationship getObjRelationship() {
        return objRelationship;
    }

    public String getReverseDbPath() {
        return objRelationship.getReverseDbRelationshipPath();
    }

    public Expression translateExpressionToSource(Expression expression) {
        return expression != null
                ? objRelationship.getSourceEntity().translateToRelatedEntity(expression, objRelationship.getName())
                : null;
    }
}
