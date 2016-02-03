package com.nhl.link.rest.runtime.parser.pointer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.nhl.link.rest.LinkRestException;
import com.nhl.link.rest.meta.LrEntity;

import java.util.StringTokenizer;

import static javax.ws.rs.core.Response.Status;

public class PointerParser {

    private static final String JSON_POINTER_PROPERTY_ATTRIBUTE = "property";
    private static final String JSON_POINTER_ID_ATTRIBUTE = "id";

    private LrPointerService pointerService;

    public PointerParser(LrPointerService pointerService) {
        this.pointerService = pointerService;
    }

    public LrPointer getPointer(LrEntity<?> rootEntity, JsonNode node) {

        // TODO: Entity collection pointer
        if (node == null) {
            throw new LinkRestException(Status.BAD_REQUEST,
                    "Invalid empty pointer for '" + rootEntity.getName() + "'");
        }

        LrPointerBuilder builder = pointerService.forEntity(rootEntity);
        try {
            if (node.isArray()) {
                ArrayNode pointerNodes = (ArrayNode) node;
                if (pointerNodes.size() == 0) {
                    throw new LinkRestException(Status.BAD_REQUEST,
                        "Invalid empty pointer for '" + rootEntity.getName() + "'");
                }
                for (JsonNode pointerNode : pointerNodes) {
                    appendSimplePointer(builder, pointerNode);
                }
            } else {
                appendSimplePointer(builder, node);
            }
        } catch (Exception e) {
            throw new LinkRestException(Status.BAD_REQUEST,
                    "Invalid pointer '" + node.toString() + "' for '" + rootEntity.getName() + "'", e);
        }

        return builder.build();
    }

    private void appendSimplePointer(LrPointerBuilder builder, JsonNode pointerNode) throws Exception {

        if (!pointerNode.isObject()) {
            throw new LinkRestException(Status.BAD_REQUEST,
                "Invalid pointer element '" + pointerNode.toString() + "'");
        }

        JsonNode propertyNode = pointerNode.get(JSON_POINTER_PROPERTY_ATTRIBUTE),
                idNode = pointerNode.get(JSON_POINTER_ID_ATTRIBUTE);

        if (propertyNode == null && idNode == null) {
            throw new LinkRestException(Status.BAD_REQUEST,
                "Invalid pointer element '" + pointerNode.toString() + "'");
        }

        if (propertyNode == null) {
            builder.append(idNode.textValue());
        } else if (idNode == null) {
            builder.append(propertyNode.textValue());
        } else {
            builder.append(propertyNode.textValue(), idNode.textValue());
        }
    }

    public LrPointer getPointer(LrEntity<?> rootEntity, String s) {

        if (s == null || s.isEmpty()) {
            throw new LinkRestException(Status.BAD_REQUEST,
                    "Invalid empty pointer for '" + rootEntity.getName() + "'");
        }

        LrPointerBuilder builder = pointerService.forEntity(rootEntity);

        if (s.equals(Pointers.PATH_SEPARATOR)) {
            // entity collection pointer
            return builder.build();
        }

        if (s.startsWith(Pointers.PATH_SEPARATOR) || s.startsWith(Pointers.RELATIONSHIP_SEPARATOR)
                || s.endsWith(Pointers.PATH_SEPARATOR) || s.endsWith(Pointers.RELATIONSHIP_SEPARATOR)) {
            throw new LinkRestException(Status.BAD_REQUEST,
                    "Invalid pointer '" + s + "' for '" + rootEntity.getName() + "'");
        }

        StringTokenizer properties = new StringTokenizer(Pointers.unescape(s), Pointers.PATH_SEPARATOR);
        while (properties.hasMoreTokens()) {

            String property = properties.nextToken();

            if (property.isEmpty()) {
                throw new LinkRestException(Status.BAD_REQUEST,
                        "Invalid pointer '" + s + "' for '" + rootEntity.getName() + "'");
            }
            if (property.startsWith(Pointers.RELATIONSHIP_SEPARATOR) || property.endsWith(Pointers.RELATIONSHIP_SEPARATOR)) {
                throw new LinkRestException(Status.BAD_REQUEST,
                        "Invalid pointer '" + s + "' for '" + rootEntity.getName() + "'");
            }

            String[] parts = property.split(Pointers.RELATIONSHIP_SEPARATOR);
            if (parts.length == 1) {
                // single ID, attribute or to-one relationship
                builder.append(parts[0]);
            } else if (parts.length == 2) {
                // to-many relationship with single ID
                builder.append(parts[0], parts[1]);
            } else {
                throw new LinkRestException(Status.BAD_REQUEST,
                        "Invalid pointer '" + s + "' for '" + rootEntity.getName() + "'");
            }
        }

        return builder.build();
    }
}
