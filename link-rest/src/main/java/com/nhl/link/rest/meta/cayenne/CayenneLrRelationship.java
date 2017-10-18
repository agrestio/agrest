package com.nhl.link.rest.meta.cayenne;

import com.fasterxml.jackson.databind.JsonNode;
import com.nhl.link.rest.LrObjectId;
import com.nhl.link.rest.meta.LrEntity;
import com.nhl.link.rest.meta.LrPersistentRelationship;
import com.nhl.link.rest.parser.converter.JsonValueConverter;
import com.nhl.link.rest.property.PropertyReader;
import org.apache.cayenne.map.DbJoin;
import org.apache.cayenne.map.DbRelationship;
import org.apache.cayenne.map.ObjRelationship;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

/**
 * @since 1.12
 */
public class CayenneLrRelationship implements LrPersistentRelationship {

    private ObjRelationship objRelationship;
    private LrEntity<?> targetEntity;
    private JsonValueConverter<?> converter;
    private PropertyReader propertyReader;

    public CayenneLrRelationship(ObjRelationship objRelationship, LrEntity<?> targetEntity, JsonValueConverter<?> converter) {
        this(objRelationship, targetEntity, converter, null);
    }

    /**
     * @since 2.10
     */
    public CayenneLrRelationship(
            ObjRelationship objRelationship,
            LrEntity<?> targetEntity,
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
    public LrEntity<?> getTargetEntity() {
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

    @Override
    public boolean isPrimaryKey() {
        return getDbRelationship().getReverseRelationship().isToDependentPK();
    }

    private DbRelationship getDbRelationship() {
        return objRelationship.getDbRelationships().get(0);
    }

    @Override
    public Map<String, Object> extractId(LrObjectId id) {
        return extractId(id::get);
    }

    @Override
    public Map<String, Object> extractId(JsonNode id) {
        if (isMultiJoin()) {
            if (!id.isObject()) {
                throw new IllegalArgumentException("Relationship has multiple joins, but only a scalar value was provided");
            }
            return extractId(id::get);
        } else if (id.isObject()) {
            return extractId(id::get);
        } else {
            return Collections.singletonMap(
                    getDbRelationship().getReverseRelationship().getJoins().iterator().next().getTargetName(),
                    converter.value(id));
        }
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

    private boolean isMultiJoin() {
        return getDbRelationship().getReverseRelationship().getJoins().size() > 1;
    }
}
