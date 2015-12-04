package com.nhl.link.rest.runtime.parser.pointer;

import com.nhl.link.rest.LinkRestException;
import com.nhl.link.rest.meta.LrAttribute;
import com.nhl.link.rest.meta.LrEntity;
import com.nhl.link.rest.meta.LrRelationship;
import com.nhl.link.rest.runtime.meta.IMetadataService;

import java.util.ArrayList;
import java.util.List;

import static javax.ws.rs.core.Response.Status;

public class LrPointerService {

    private IMetadataService metadataService;

    public LrPointerService(IMetadataService metadataService) {
        this.metadataService = metadataService;
    }

    DefaultLrPointerBuilder forEntity(LrEntity<?> entity) {
        return new DefaultLrPointerBuilder(entity);
    }

    private class DefaultLrPointerBuilder implements LrPointerBuilder {

        private LrEntity<?> currentEntity;
        private List<SimplePointer> parts;
        private SimplePointer previousElement;

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

            if (parts.size() == 0) {
                throw new IllegalStateException("Can't build an empty pointer");
            } else if (parts.size() == 1) {
                return parts.get(0);
            } else {
                return new CompoundPointer(parts);
            }
        }
    }

}
