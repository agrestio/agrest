package io.agrest.meta.cayenne;

import io.agrest.meta.AgEntity;
import io.agrest.meta.AgRelationship;
import io.agrest.resolver.NestedDataResolver;
import org.apache.cayenne.exp.Expression;
import org.apache.cayenne.map.ObjRelationship;

import java.util.Objects;

/**
 * @since 1.12
 */
public class CayenneAgRelationship implements AgRelationship {

    private ObjRelationship objRelationship;
    private AgEntity<?> targetEntity;
    private NestedDataResolver<?> dataResolver;

    public CayenneAgRelationship(
            ObjRelationship objRelationship,
            AgEntity<?> targetEntity,
            NestedDataResolver<?> dataResolver) {

        this.objRelationship = Objects.requireNonNull(objRelationship);
        this.targetEntity = Objects.requireNonNull(targetEntity);
        this.dataResolver = Objects.requireNonNull(dataResolver);
    }

    @Override
    public NestedDataResolver<?> getResolver() {
        return dataResolver;
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
