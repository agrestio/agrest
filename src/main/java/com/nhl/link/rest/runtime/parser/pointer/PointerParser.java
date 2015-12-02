package com.nhl.link.rest.runtime.parser.pointer;

import com.fasterxml.jackson.databind.JsonNode;
import com.nhl.link.rest.LinkRestException;
import com.nhl.link.rest.meta.LrEntity;

import java.util.StringTokenizer;

import static javax.ws.rs.core.Response.Status;

public class PointerParser {

    private LrPointerService pointerService;

    public PointerParser(LrPointerService pointerService) {
        this.pointerService = pointerService;
    }

    public LrPointer getPointer(JsonNode node) {
        // TODO: Implement me
        return null;
    }

    public LrPointer getPointer(LrEntity<?> rootEntity, String s) {

        if (s == null || s.isEmpty()) {
            throw new LinkRestException(Status.BAD_REQUEST,
                    "Invalid empty pointer for '" + rootEntity.getName() + "'");
        }

        if (s.startsWith(Pointers.PATH_SEPARATOR) || s.startsWith(Pointers.ID_SEPARATOR)
                || s.endsWith(Pointers.PATH_SEPARATOR) || s.endsWith(Pointers.ID_SEPARATOR)) {
            throw new LinkRestException(Status.BAD_REQUEST,
                    "Invalid pointer '" + s + "' for '" + rootEntity.getName() + "'");
        }

        LrPointerBuilder builder = pointerService.forEntity(rootEntity);

        StringTokenizer properties = new StringTokenizer(Pointers.unescape(s), Pointers.PATH_SEPARATOR);
        while (properties.hasMoreTokens()) {

            String property = properties.nextToken();

            if (property.isEmpty()) {
                throw new LinkRestException(Status.BAD_REQUEST,
                        "Invalid pointer '" + s + "' for '" + rootEntity.getName() + "'");
            }
            if (property.startsWith(Pointers.ID_SEPARATOR) || property.endsWith(Pointers.ID_SEPARATOR)) {
                throw new LinkRestException(Status.BAD_REQUEST,
                        "Invalid pointer '" + s + "' for '" + rootEntity.getName() + "'");
            }

            String[] parts = property.split(Pointers.ID_SEPARATOR);
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
