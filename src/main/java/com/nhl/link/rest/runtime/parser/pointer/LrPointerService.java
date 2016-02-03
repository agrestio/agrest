package com.nhl.link.rest.runtime.parser.pointer;

import com.nhl.link.rest.LinkRestException;
import com.nhl.link.rest.meta.LrAttribute;
import com.nhl.link.rest.meta.LrEntity;
import com.nhl.link.rest.meta.LrRelationship;
import com.nhl.link.rest.runtime.meta.IMetadataService;

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
        private SimplePointer tail;

        private DefaultLrPointerBuilder(LrEntity<?> entity) {
            this.currentEntity = entity;
        }

        @Override
        public DefaultLrPointerBuilder append(String relationshipName, Object id) {

            ensurePossibleToAddMoreElements();

            LrRelationship relationship = currentEntity.getRelationship(relationshipName);
            if (relationship == null) {
                throw new LinkRestException(Status.BAD_REQUEST,
                        "Unknown relationship '" + relationshipName + "' for '" + currentEntity.getName() + "'");
            }

            // TODO: id type check and fix
            tail = new RelationshipPointer(tail, currentEntity, relationship, id);
            currentEntity = metadataService.getLrEntity(relationship.getTargetEntityType());

            return this;
        }

        @Override
        public DefaultLrPointerBuilder append(String pathElement) {

            ensurePossibleToAddMoreElements();

            LrAttribute attribute = currentEntity.getAttribute(pathElement);
            if (attribute != null) {
                tail = new AttributePointer(tail, currentEntity, attribute);

            } else {
                LrRelationship relationship = currentEntity.getRelationship(pathElement);
                if (relationship != null) {
                    tail = new RelationshipPointer(tail, currentEntity, relationship, null);
                    currentEntity = metadataService.getLrEntity(relationship.getTargetEntityType());

                } else {
                    tail = new ObjectInstancePointer(tail, currentEntity, pathElement);
                }
            }

            return this;
        }

        private void ensurePossibleToAddMoreElements() {

            if (tail != null) {
                PointerType type = tail.getType();
                if (type == PointerType.ATTRIBUTE) {
                    throw new LinkRestException(Status.BAD_REQUEST,
                            "Can't add pointer element: last element was attribute");
                }
            }
        }

        @Override
        public LrPointer build() {

            if (tail == null) {
                return new EntityCollectionPointer(null, currentEntity);
            }

            return tail;
        }
    }

}
