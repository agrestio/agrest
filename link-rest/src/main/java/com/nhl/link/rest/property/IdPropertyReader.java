package com.nhl.link.rest.property;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.nhl.link.rest.meta.LrAttribute;
import com.nhl.link.rest.meta.LrEntity;
import com.nhl.link.rest.meta.LrPersistentEntity;

public class IdPropertyReader implements PropertyReader {

    private LrEntity<?> entity;
    private boolean isPersistent;

    public IdPropertyReader(LrEntity<?> entity) {
        this.entity = entity;
        this.isPersistent = entity instanceof LrPersistentEntity;
    }

    @Override
    public Object value(Object root, String name) {

        Collection<LrAttribute> ids = entity.getIds();
        if (ids.size() == 0) {
            return Collections.emptyMap();
        } else {
            Map<String, Object> idMap = new HashMap<>((int)(ids.size() / 0.75d) + 1);
            for (LrAttribute id : ids) {
                idMap.put(id.getName(), readPropertyOrId(root, id.getName()));
            }
            return idMap;
        }
    }

    private Object readPropertyOrId(Object object, String name) {

        if (isPersistent) {
            // try normal property first, and if it's absent,
            // assume that it's (a part of) the entity's ID
            Object property = DataObjectPropertyReader.reader().value(object, name);
            return property == null ?
                    PersistentObjectIdPropertyReader.reader().value(object, name) : property;
        } else {
            return BeanPropertyReader.reader().value(object, name);
        }
    }
}
