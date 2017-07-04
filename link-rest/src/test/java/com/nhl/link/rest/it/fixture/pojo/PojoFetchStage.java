package com.nhl.link.rest.it.fixture.pojo;

import com.nhl.link.rest.processor2.Processor;
import com.nhl.link.rest.processor2.ProcessorOutcome;
import com.nhl.link.rest.runtime.processor.select.SelectContext;
import org.apache.cayenne.di.Inject;
import org.apache.cayenne.exp.Expression;
import org.apache.cayenne.query.Ordering;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class PojoFetchStage implements Processor<SelectContext<?>> {

    private PojoDB db;

    public PojoFetchStage(@Inject PojoDB db) {
        this.db = db;
    }

    @Override
    public ProcessorOutcome execute(SelectContext<?> context) {
        findObjects(context);
        return ProcessorOutcome.CONTINUE;
    }

    <T> void findObjects(SelectContext<T> context) {

        Map<Object, T> typeBucket = db.bucketForType(context.getType());
        if (context.isById()) {
            T object = typeBucket.get(context.getId().get());
            context.setObjects(object != null ? Collections.singletonList(object) : Collections.<T>emptyList());
            return;
        }

        // clone the list and then filter/sort it as needed
        List<T> list = new ArrayList<>(typeBucket.values());

        Expression filter = context.getEntity().getQualifier();
        if (filter != null) {

            Iterator<T> it = list.iterator();
            while (it.hasNext()) {
                T t = it.next();
                if (!filter.match(t)) {
                    it.remove();
                }
            }
        }

        for (Ordering o : context.getEntity().getOrderings()) {
            o.orderList(list);
        }

        context.setObjects(list);
    }
}
