package io.agrest.reader;

import io.agrest.AgObjectId;
import io.agrest.ToOneResourceEntity;

/**
 * @since 4.8
 */
public class ToOneEntityResultReader extends RelatedEntityResultReader<ToOneResourceEntity<?>> {

    public ToOneEntityResultReader(ToOneResourceEntity<?> entity, DataReader parentKeyReader) {
        super(entity, parentKeyReader);
    }

    @Override
    public Object read(Object root) {
        AgObjectId id = readId(root);
        return entity.getData(id);
    }
}
