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

    void setReadAccess(String name, boolean readable) {
        if (PathConstants.ID_PK_ATTRIBUTE.equals(name)) {

            Iterator<Map.Entry<String, AgIdPart>> it = ids.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, AgIdPart> e = it.next();

                boolean wasReadable = e.getValue().isReadable();
                if (wasReadable && !readable) {
                    e.setValue(new DefaultIdPart(name, e.getValue().getType(), false, e.getValue().isWritable(), e.getValue().getDataReader()));
                } else if (!wasReadable && readable) {
                    e.setValue(new DefaultIdPart(name, e.getValue().getType(), true, e.getValue().isWritable(), e.getValue().getDataReader()));
                }
            }

            return;
        }

        AgIdPart id = ids.get(name);
        if (id != null) {
            boolean wasReadable = id.isReadable();
            if (wasReadable && !readable) {
                ids.put(name, new DefaultIdPart(name, id.getType(), false, id.isWritable(), id.getDataReader()));
            } else if (!wasReadable && readable) {
                ids.put(name, new DefaultIdPart(name, id.getType(), true, id.isWritable(), id.getDataReader()));
            }

            return;
        }

        AgAttribute a = attributes.get(name);
        if (a != null) {
            boolean wasReadable = a.isReadable();
            if (wasReadable && !readable) {
                attributes.put(name, new DefaultAttribute(name, a.getType(), false, a.isWritable(), a.getDataReader()));
            } else if (!wasReadable && readable) {
                attributes.put(name, new DefaultAttribute(name, a.getType(), true, a.isWritable(), a.getDataReader()));
            }

            return;
        }

        AgRelationship r = relationships.get(name);
        if (r != null) {
            boolean wasReadable = r.isReadable();

            if (wasReadable && !readable) {
                relationships.put(name, new DefaultRelationship(name, r.getTargetEntity(), r.isToMany(), false, r.isWritable(), r.getDataResolver()));
            } else if (!wasReadable && readable) {
                relationships.put(name, new DefaultRelationship(name, r.getTargetEntity(), r.isToMany(), true, r.isWritable(), r.getDataResolver()));
            }

            return;
        }
    }

    void setWriteAccess(String name, boolean writable) {

        if (PathConstants.ID_PK_ATTRIBUTE.equals(name)) {

            Iterator<Map.Entry<String, AgIdPart>> it = ids.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, AgIdPart> e = it.next();

                boolean wasWritable = e.getValue().isWritable();
                if (wasWritable && !writable) {
                    e.setValue(new DefaultIdPart(name, e.getValue().getType(), e.getValue().isReadable(), false, e.getValue().getDataReader()));
                } else if (!wasWritable && writable) {
                    e.setValue(new DefaultIdPart(name, e.getValue().getType(), e.getValue().isReadable(), true, e.getValue().getDataReader()));
                }
            }

            return;
        }

        AgIdPart id = ids.get(name);
        if (id != null) {
            boolean wasWritable = id.isWritable();
            if (wasWritable && !writable) {
                ids.put(name, new DefaultIdPart(name, id.getType(), id.isReadable(), false, id.getDataReader()));
            } else if (!wasWritable && writable) {
                ids.put(name, new DefaultIdPart(name, id.getType(), id.isReadable(), true, id.getDataReader()));
            }

            return;
        }

        AgAttribute a = attributes.get(name);
        if (a != null) {
            boolean wasWritable = a.isWritable();

            if (wasWritable && !writable) {
                attributes.put(name, new DefaultAttribute(name, a.getType(), a.isReadable(), false, a.getDataReader()));
            } else if (!wasWritable && writable) {
                attributes.put(name, new DefaultAttribute(name, a.getType(), a.isReadable(), true, a.getDataReader()));
            }

            return;
        }

        AgRelationship r = relationships.get(name);
        if (r != null) {

            boolean wasWritable = r.isWritable();
            if (wasWritable && !writable) {
                relationships.put(name, new DefaultRelationship(name, r.getTargetEntity(), r.isToMany(), r.isReadable(), false, r.getDataResolver()));
            } else if (!wasWritable && writable) {
                relationships.put(name, new DefaultRelationship(name, r.getTargetEntity(), r.isToMany(), r.isReadable(), true, r.getDataResolver()));
            }

            return;
        }
    }
}
