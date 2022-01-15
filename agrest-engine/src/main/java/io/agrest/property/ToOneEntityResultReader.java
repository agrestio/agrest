package io.agrest.property;

import io.agrest.AgObjectId;
import io.agrest.ToOneResourceEntity;

/**
 * @since 4.8
 */
public class ToOneEntityResultReader extends NestedEntityResultReader<ToOneResourceEntity<?>> {

    public ToOneEntityResultReader(ToOneResourceEntity<?> entity, PropertyReader parentKeyReader) {
        super(entity, parentKeyReader);
    }

    @Override
    public Object value(Object root) {
        AgObjectId id = readId(root);
        return entity.getData(id);
    }
}
