package io.agrest.jaxrs.pojo.runtime;

import io.agrest.PathConstants;
import io.agrest.meta.AgAttribute;
import io.agrest.meta.AgEntity;
import io.agrest.processor.Processor;
import io.agrest.processor.ProcessorOutcome;
import io.agrest.property.PropertyReader;
import io.agrest.protocol.Sort;
import io.agrest.runtime.processor.select.SelectContext;
import org.apache.cayenne.di.Inject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
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
            context.getEntity().setData(object != null ? Collections.singletonList(object) : Collections.emptyList());
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
        AgAttribute attribute = entity.getAttribute(s.getProperty());
        if (attribute != null) {
            keyReader = t -> (Comparable) attribute.getPropertyReader().value(t);
        }
        else if (PathConstants.ID_PK_ATTRIBUTE.equals(s.getProperty())) {
            keyReader = t -> readId(t, entity.getIdReader());
        } else {
            throw new RuntimeException("Can't find sort property reader for '" + s.getProperty() + "'");
        }

        return (Comparator<T>) Comparator.comparing(keyReader);
    }

    private Comparable readId(Object object, PropertyReader idReader) {
        if (object == null) {
            return null;
        }

        Map<String, Object> id = (Map<String, Object>) idReader.value(object);
        assertEquals(1, id.size(), () -> "Unexpected id size " + id.size() + " for object " + object.getClass());
        return (Comparable) id.values().iterator().next();
    }
}
