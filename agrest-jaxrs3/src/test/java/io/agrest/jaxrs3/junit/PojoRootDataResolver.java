package io.agrest.jaxrs3.junit;

import io.agrest.PathConstants;
import io.agrest.meta.AgAttribute;
import io.agrest.meta.AgEntity;
import io.agrest.protocol.Sort;
import io.agrest.reader.DataReader;
import io.agrest.resolver.BaseRootDataResolver;
import io.agrest.runtime.processor.select.SelectContext;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PojoRootDataResolver<T> extends BaseRootDataResolver<T> {

    private final PojoStore store;

    public PojoRootDataResolver(PojoStore store) {
        this.store = store;
    }

    @Override
    protected List<T> doFetchData(SelectContext<T> context) {

        Map<Object, T> typeBucket = store.bucket(context.getType());
        if (context.isById()) {
            Map<String, Object> idMap = context.getId().asMap(context.getEntity().getAgEntity());
            Object id = idMap.size() > 1 ? idMap : idMap.values().iterator().next();
            T object = typeBucket.get(id);
            // stores as a result into ResourceEntity
            return object != null ? List.of(object) : List.of();
        }

        // clone the list and then filter/sort it as needed
        List<T> list = new ArrayList<>(typeBucket.values());

        for (Sort s : context.getEntity().getOrderings()) {
            list.sort(toComparator(context.getEntity().getAgEntity(), s));
        }

        return list;
    }

    private <T> Comparator<T> toComparator(AgEntity<T> entity, Sort s) {

        Function<T, ? extends Comparable> keyReader;
        AgAttribute attribute = entity.getAttribute(s.getPath());
        if (attribute != null) {
            keyReader = t -> (Comparable) attribute.getDataReader().read(t);
        } else if (PathConstants.ID_PK_ATTRIBUTE.equals(s.getPath())) {
            keyReader = t -> readId(t, entity.getIdReader());
        } else {
            throw new RuntimeException("Can't find sort property reader for '" + s.getPath() + "'");
        }

        return (Comparator<T>) Comparator.comparing(keyReader);
    }

    private Comparable readId(Object object, DataReader idReader) {
        if (object == null) {
            return null;
        }

        Map<String, Object> id = (Map<String, Object>) idReader.read(object);
        assertEquals(1, id.size(), () -> "Unexpected id size " + id.size() + " for object " + object.getClass());
        return (Comparable) id.values().iterator().next();
    }
}
