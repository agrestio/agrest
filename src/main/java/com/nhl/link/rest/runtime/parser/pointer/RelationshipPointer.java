package com.nhl.link.rest.runtime.parser.pointer;

import com.nhl.link.rest.meta.LrEntity;
import com.nhl.link.rest.meta.LrRelationship;

class RelationshipPointer extends SimplePointer {

    private PointerType type;
    private LrRelationship relationship;
    private Object id;

    RelationshipPointer(SimplePointer predecessor, LrEntity<?> entity, LrRelationship relationship, Object id) {
        super(predecessor, entity);
        this.relationship = relationship;
        this.id = id;

        if (relationship.isToMany()) {
            type = PointerType.TO_MANY_RELATIONSHIP;
        } else if (id != null) {
            type = PointerType.EXPLICIT_TO_ONE_RELATIONSHIP;
        } else {
            type = PointerType.IMPLICIT_TO_ONE_RELATIONSHIP;
        }
    }

    @Override
    protected String encodeToString() {

        String encoded;
        switch (type) {
            case TO_MANY_RELATIONSHIP:
            case EXPLICIT_TO_ONE_RELATIONSHIP: {
                encoded = Pointers.buildRelationship(relationship.getName(), id);
                break;
            }
            case IMPLICIT_TO_ONE_RELATIONSHIP: {
                encoded = relationship.getName();
                break;
            }
            default: {
                throw new RuntimeException("Unknown pointer type: " + type.name());
            }
        }

        return encoded;
    }

    @Override
    public PointerType getType() {
        return type;
    }

    @Override
    public Class<?> getTargetType() {
        return relationship.getTargetEntityType();
    }

    LrRelationship getRelationship() {
        return relationship;
    }

    Object getId() {
        return id;
    }
}
