package com.nhl.link.rest.runtime.parser.pointer;

import com.nhl.link.rest.LinkRestException;
import com.nhl.link.rest.meta.LrAttribute;
import com.nhl.link.rest.meta.LrEntity;
import com.nhl.link.rest.meta.LrRelationship;
import com.nhl.link.rest.runtime.cayenne.ICayennePersister;
import com.nhl.link.rest.runtime.meta.IMetadataService;
import org.apache.cayenne.query.SelectById;
import org.apache.cayenne.reflect.PropertyDescriptor;

import java.util.ArrayList;
import java.util.List;

import static javax.ws.rs.core.Response.Status;

public class LrPointerService {

    private IMetadataService metadataService;
    private ICayennePersister cayenneService;

    public LrPointerService(IMetadataService metadataService, ICayennePersister cayenneService) {
        this.metadataService = metadataService;
        this.cayenneService = cayenneService;
    }

    DefaultLrPointerBuilder forEntity(LrEntity<?> entity) {
        return new DefaultLrPointerBuilder(entity);
    }

    private class DefaultLrPointerBuilder implements LrPointerBuilder {

        private LrEntity<?> currentEntity;
        private List<LrPointer> parts;
        private BasePointer previousElement;

        private DefaultLrPointerBuilder(LrEntity<?> entity) {
            this.currentEntity = entity;
            parts = new ArrayList<>();
        }

        @Override
        public DefaultLrPointerBuilder append(String relationshipName, Object id) {

            ensurePossibleToAddMoreElements();

            LrRelationship relationship = currentEntity.getRelationship(relationshipName);
            if (relationship == null) {
                throw new LinkRestException(Status.BAD_REQUEST,
                        "Unknown relationship '" + relationshipName + "' for '" + currentEntity.getName() + "'");
            }

            previousElement = new RelationshipPointer(previousElement, currentEntity, relationship, id);
            parts.add(previousElement);
            currentEntity = metadataService.getLrEntity(relationship.getTargetEntityType());

            return this;
        }

        @Override
        public DefaultLrPointerBuilder append(String pathElement) {

            ensurePossibleToAddMoreElements();

            LrAttribute attribute = currentEntity.getAttribute(pathElement);
            if (attribute != null) {
                previousElement = new AttributePointer(previousElement, currentEntity, attribute);
                parts.add(previousElement);

            } else {
                LrRelationship relationship = currentEntity.getRelationship(pathElement);
                if (relationship != null) {
                    if (relationship.isToMany()) {
                        throw new LinkRestException(Status.BAD_REQUEST,
                                "Invalid pointer element: to-many relationship '" + pathElement +
                                        "' without explicit ID");
                    }
                    previousElement = new RelationshipPointer(previousElement, currentEntity, relationship, null);
                    parts.add(previousElement);
                    currentEntity = metadataService.getLrEntity(relationship.getTargetEntityType());

                } else {
                    previousElement = new ObjectInstancePointer(previousElement, currentEntity, pathElement);
                    parts.add(previousElement);
                }
            }

            return this;
        }

        private void ensurePossibleToAddMoreElements() {

            if (parts.size() > 0) {
                PointerType type = parts.get(parts.size() - 1).getType();
                if (type == PointerType.ATTRIBUTE) {
                    throw new LinkRestException(Status.BAD_REQUEST,
                            "Can't add pointer element: last element was attribute");
                }
            }
        }

        @Override
        public LrPointer build() {
            return new CompoundPointer(parts);
        }
    }

    private class RelationshipPointer extends BasePointer {

        private PointerType type;
        private LrRelationship relationship;
        private Object id;
        private PropertyDescriptor propertyDescriptor;

        RelationshipPointer(BasePointer predecessor, LrEntity<?> entity, LrRelationship relationship, Object id) {
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

            propertyDescriptor = cayenneService.entityResolver().getClassDescriptor(entity.getName())
                    .getProperty(relationship.getName());
        }

        @Override
        protected Object doResolve(Object context) {
            return propertyDescriptor.readProperty(context);
        }

        @Override
        protected String encodeToString() {

            String encoded;
            switch (type) {
                case TO_MANY_RELATIONSHIP:
                case EXPLICIT_TO_ONE_RELATIONSHIP: {
                    encoded = Pointers.concatRelationship(relationship.getName(), id);
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
    }

    private class AttributePointer extends BasePointer {

        private LrAttribute attribute;
        private PropertyDescriptor propertyDescriptor;

        AttributePointer(BasePointer predecessor, LrEntity<?> entity, LrAttribute attribute) {
            super(predecessor, entity);
            this.attribute = attribute;

            propertyDescriptor = cayenneService.entityResolver().getClassDescriptor(entity.getName())
                    .getProperty(attribute.getName());
        }

        @Override
        protected Object doResolve(Object context) {
            return propertyDescriptor.readProperty(context);
        }

        @Override
        protected String encodeToString() {
            return attribute.getName();
        }

        @Override
        public PointerType getType() {
            return PointerType.ATTRIBUTE;
        }

        @Override
        public Class<?> getTargetType() {
            try {
                return Class.forName(attribute.getJavaType());
            } catch (ClassNotFoundException e) {
                throw new LinkRestException(Status.INTERNAL_SERVER_ERROR,
                        "Unknown attribute type: " + attribute.getJavaType());
            }
        }
    }

    private class ObjectInstancePointer extends BasePointer {

        private SelectById<?> query;
        private LrEntity<?> entity;
        private Object id;

        ObjectInstancePointer(BasePointer predecessor, LrEntity<?> entity, Object id) {
            super(predecessor, entity);
            this.entity = entity;
            this.id = id;

            query = SelectById.query(entity.getType(), id);
        }

        @Override
        protected Object doResolve(Object context) {
            // TODO: not sure that we don't actually need specific request context
            return query.selectOne(cayenneService.sharedContext());
        }

        @Override
        protected String encodeToString() {
            return id.toString();
        }

        @Override
        public PointerType getType() {
            return PointerType.INSTANCE;
        }

        @Override
        public Class<?> getTargetType() {
            return entity.getType();
        }
    }

    private static abstract class BasePointer implements LrPointer {

        private BasePointer predecessor;
        private LrEntity<?> entity;

        BasePointer(BasePointer predecessor, LrEntity<?> entity) {
            this.predecessor = predecessor;
            this.entity = entity;
        }

        @Override
        public final Object resolve() throws Exception {
            return resolve(null);
        }

        @Override
        public final Object resolve(Object context) throws Exception {

            if (getType() != PointerType.INSTANCE) {
                if (context == null) {
                    throw new NullPointerException("Null context passed to pointer: " + toString());
                }

                if (!entity.getType().equals(context.getClass())) {
                    throw new IllegalArgumentException("Wrong context type: expected '" + entity.getType().getName() +
                            "', but actual was '" + context.getClass().getName() + "'");
                }
            }

            return doResolve(context);
        }

        protected abstract Object doResolve(Object context);
        protected abstract String encodeToString();

        @Override
        public final String toString() {
            return predecessor == null?
                    this.encodeToString() :
                    Pointers.concat(
                            Pointers.escape(predecessor.encodeToString()),
                            Pointers.escape(this.encodeToString())
                    );
        }
    }
}
