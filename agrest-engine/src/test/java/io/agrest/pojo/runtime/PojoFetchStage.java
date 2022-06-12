package io.agrest.pojo.runtime;

import io.agrest.PathConstants;
import io.agrest.protocol.Sort;
import io.agrest.meta.AgAttribute;
import io.agrest.meta.AgEntity;
import io.agrest.processor.Processor;
import io.agrest.processor.ProcessorOutcome;
import io.agrest.reader.DataReader;
import io.agrest.runtime.processor.select.SelectContext;
import org.apache.cayenne.di.Inject;

import java.util.*;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PojoFetchStage implements Processor<SelectContext<?>> {

    private final PojoStore db;

    public PojoFetchStage(@Inject PojoStore db) {
        this.db = db;
    }

    @Override
    public ProcessorOutcome execute(SelectContext<?> context) {
        findObjects(context);
        return ProcessorOutcome.CONTINUE;
    }

    <T> void findObjects(SelectContext<T> context) {

        Map<Object, T> typeBucket = db.bucket(context.getType());
        if (context.isById()) {
            Map<String, Object> idMap = context.getId().asMap(context.getEntity().getAgEntity());
            Object id = idMap.size() > 1 ? idMap : idMap.values().iterator().next();
            T object = typeBucket.get(id);
            // stores as a result into ResourceEntity
            context.getEntity().setData(object != null ? List.of(object) : List.of());
            return;
        }

        // clone the list and then filter/sort it as needed
        List<T> list = new ArrayList<>(typeBucket.values());

        for (Sort s : context.getEntity().getOrderings()) {
            list.sort(toComparator(context.getEntity().getAgEntity(), s));
        }

        // stores as a result into ResourceEntity
        context.getEntity().setData(list);
    }

    private <T> Comparator<T> toComparator(AgEntity<T> entity, Sort s) {

        Function<T, ? extends Comparable> keyReader;
        AgAttribute attribute = entity.getAttribute(s.getPath());
        if (attribute != null) {
            keyReader = t -> (Comparable) attribute.getDataReader().read(t);
        }
        else if (PathConstants.ID_PK_ATTRIBUTE.equals(s.getPath())) {
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
