package io.agrest.property;

import io.agrest.meta.AgAttribute;
import io.agrest.meta.AgEntity;
import io.agrest.meta.AgPersistentEntity;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class IdPropertyReader implements PropertyReader {

    private AgEntity<?> entity;
    private boolean isPersistent;

    public IdPropertyReader(AgEntity<?> entity) {
        this.entity = entity;
        this.isPersistent = entity instanceof AgPersistentEntity;
    }

    @Override
    public Object value(Object root, String name) {

        Collection<AgAttribute> ids = entity.getIds();
        if (ids.isEmpty()) {
            return Collections.emptyMap();
        }

        // TODO: for Cayenne ids, just use ObjectId internal map and avoid copy
        Map<String, Object> idMap = new HashMap<>((int) (ids.size() / 0.75d) + 1);
        for (AgAttribute id : ids) {
            idMap.put(id.getName(), readPropertyOrId(root, id.getName()));
        }
        return idMap;
    }

    private Object readPropertyOrId(Object object, String name) {

        if (isPersistent) {
            // try normal property first, and if it's absent, assume that it's (a part of) the entity's ID
            Object property = DataObjectPropertyReader.reader().value(object, name);
            return property != null ? property : PersistentObjectIdPropertyReader.reader().value(object, name);
        } else {
            return BeanPropertyReader.reader().value(object, name);
        }
    }
}
