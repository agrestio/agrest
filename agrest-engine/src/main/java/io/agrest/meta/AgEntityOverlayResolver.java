package io.agrest.meta;

import io.agrest.PathConstants;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

class AgEntityOverlayResolver {

    final AgSchema schema;
    final Map<String, AgIdPart> ids;
    final Map<String, AgAttribute> attributes;
    final Map<String, AgRelationship> relationships;

    AgEntityOverlayResolver(AgSchema schema, AgEntity<?> sourceEntity) {
        this.schema = schema;

        this.ids = new HashMap<>();
        sourceEntity.getIdParts().forEach(p -> ids.put(p.getName(), p));

        this.attributes = new HashMap<>();
        sourceEntity.getAttributes().forEach(a -> attributes.put(a.getName(), a));

        this.relationships = new HashMap<>();
        sourceEntity.getRelationships().forEach(r -> relationships.put(r.getName(), r));
    }

    void loadAttributeOverlay(AgAttributeOverlay overlay) {
        attributes.put(overlay.getName(), overlay.resolve(attributes.get(overlay.getName())));
    }

    void loadRelationshipOverlay(AgRelationshipOverlay overlay) {
        relationships.put(overlay.getName(), overlay.resolve(relationships.get(overlay.getName()), schema));
    }

    void makeUnreadable(String name) {
        if (PathConstants.ID_PK_ATTRIBUTE.equals(name)) {

            Iterator<Map.Entry<String, AgIdPart>> it = ids.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, AgIdPart> e = it.next();
                if (e.getValue().isReadable()) {
                    e.setValue(new DefaultIdPart(name, e.getValue().getType(), false, e.getValue().isWritable(), e.getValue().getDataReader()));
                }
            }

            return;
        }

        AgIdPart id = ids.get(name);
        if (id != null) {
            if (id.isReadable()) {
                ids.put(name, new DefaultIdPart(name, id.getType(), false, id.isWritable(), id.getDataReader()));
            }

            return;
        }

        AgAttribute a = attributes.get(name);
        if (a != null) {
            if (a.isReadable()) {
                attributes.put(name, new DefaultAttribute(name, a.getType(), false, a.isWritable(), a.getDataReader()));
            }

            return;
        }

        AgRelationship r = relationships.get(name);
        if (r != null) {
            if (r.isReadable()) {
                relationships.put(name, new DefaultRelationship(name, r.getTargetEntity(), r.isToMany(), false, r.isWritable(), r.getDataResolver()));
            }

            return;
        }
    }

    void makeUnwritable(String name) {

        if (PathConstants.ID_PK_ATTRIBUTE.equals(name)) {

            Iterator<Map.Entry<String, AgIdPart>> it = ids.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, AgIdPart> e = it.next();
                if (e.getValue().isWritable()) {
                    e.setValue(new DefaultIdPart(name, e.getValue().getType(), e.getValue().isReadable(), false, e.getValue().getDataReader()));
                }
            }

            return;
        }

        AgIdPart id = ids.get(name);
        if (id != null) {
            if (id.isWritable()) {
                ids.put(name, new DefaultIdPart(name, id.getType(), id.isReadable(), false, id.getDataReader()));
            }

            return;
        }

        AgAttribute a = attributes.get(name);
        if (a != null) {
            if (a.isWritable()) {
                attributes.put(name, new DefaultAttribute(name, a.getType(), a.isReadable(), false, a.getDataReader()));
            }

            return;
        }

        AgRelationship r = relationships.get(name);
        if (r != null) {
            if (r.isWritable()) {
                relationships.put(name, new DefaultRelationship(name, r.getTargetEntity(), r.isToMany(), r.isReadable(), false, r.getDataResolver()));
            }

            return;
        }
    }
}
