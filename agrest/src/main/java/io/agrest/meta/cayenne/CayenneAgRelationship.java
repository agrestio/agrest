package io.agrest.meta.cayenne;

import io.agrest.AgObjectId;
import io.agrest.meta.AgEntity;
import io.agrest.meta.AgPersistentRelationship;
import io.agrest.parser.converter.JsonValueConverter;
import io.agrest.property.PropertyReader;
import org.apache.cayenne.exp.Expression;
import org.apache.cayenne.map.DbJoin;
import org.apache.cayenne.map.DbRelationship;
import org.apache.cayenne.map.ObjRelationship;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

/**
 * @since 1.12
 */
public class CayenneAgRelationship implements AgPersistentRelationship {

    private ObjRelationship objRelationship;
    private AgEntity<?> targetEntity;
    private JsonValueConverter<?> converter;
    private PropertyReader propertyReader;

    public CayenneAgRelationship(
            ObjRelationship objRelationship,
            AgEntity<?> targetEntity,
            JsonValueConverter<?> converter) {

        this(objRelationship, targetEntity, converter, null);
    }

    /**
     * @since 2.10
     */
    public CayenneAgRelationship(
            ObjRelationship objRelationship,
            AgEntity<?> targetEntity,
            JsonValueConverter<?> converter,
            PropertyReader propertyReader) {

        this.objRelationship = objRelationship;
        this.targetEntity = Objects.requireNonNull(targetEntity);
        this.converter = converter;
        this.propertyReader = propertyReader;
    }

    /**
     * @since 2.10
     */
    @Override
    public PropertyReader getPropertyReader() {
        return propertyReader;
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

    @Override
    public boolean isToDependentEntity() {
        return getDbRelationship().isToDependentPK();
    }

    public ObjRelationship getObjRelationship() {
        return objRelationship;
    }

    private DbRelationship getDbRelationship() {
        return objRelationship.getDbRelationships().get(0);
    }

    @Override
    public Map<String, Object> extractId(AgObjectId id) {
        return extractId(id::get);
    }

    public String getReverseDbPath() {
        return objRelationship.getReverseDbRelationshipPath();
    }

    public Expression translateExpressionToSource(Expression expression) {
        return expression != null
                ? objRelationship.getSourceEntity().translateToRelatedEntity(expression, objRelationship.getName())
                : null;
    }

    private Map<String, Object> extractId(Function<String, Object> idPartSupplier) {
        Map<String, Object> parentIdMap = new HashMap<>();
        for (DbRelationship dbRelationship : objRelationship.getDbRelationships()) {
            DbRelationship reverseRelationship = dbRelationship.getReverseRelationship();
            for (DbJoin join : reverseRelationship.getJoins()) {
                parentIdMap.put(join.getSourceName(), idPartSupplier.apply(join.getTargetName()));
            }
        }
        return parentIdMap;
    }
}
