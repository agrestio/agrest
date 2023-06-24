package io.agrest.cayenne.processor;

import io.agrest.access.PathChecker;
import io.agrest.id.AgObjectId;
import io.agrest.AgRequestBuilder;
import io.agrest.RootResourceEntity;
import io.agrest.cayenne.cayenne.main.E1;
import io.agrest.cayenne.unit.main.MainNoDbTest;
import io.agrest.protocol.Direction;
import io.agrest.protocol.Exp;
import io.agrest.protocol.Sort;
import io.agrest.runtime.processor.select.SelectContext;
import org.apache.cayenne.di.Injector;
import org.apache.cayenne.query.ObjectSelect;
import org.junit.jupiter.api.Test;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

public class CayenneQueryAssemblerTest extends MainNoDbTest {

    @Test
    public void createRootQuery_Ordering() {

        RootResourceEntity<E1> entity = getResourceEntity(E1.class);
        entity.getOrderings().add(new Sort("name", Direction.asc));
        SelectContext<E1> c = new SelectContext<>(E1.class,
                mock(AgRequestBuilder.class),
                PathChecker.ofDefault(),
                mock(Injector.class));
        c.setEntity(entity);

        ObjectSelect<E1> q1 = queryAssembler.createRootQuery(c);
        assertEquals(asList(E1.NAME.asc()), q1.getOrderings());

        entity.getOrderings().add(new Sort("name", Direction.desc));
        ObjectSelect<E1> q2 = queryAssembler.createRootQuery(c);
        assertEquals(asList(E1.NAME.asc(), E1.NAME.desc()), q2.getOrderings());
    }

    @Test
    public void createRootQuery_Pagination() {

        RootResourceEntity<E1> entity = new RootResourceEntity<>(getAgEntity(E1.class));
        entity.setLimit(10);
        entity.setStart(0);

        SelectContext<E1> c = new SelectContext<>(
                E1.class,
                mock(AgRequestBuilder.class),
                PathChecker.ofDefault(),
                mock(Injector.class));
        c.setEntity(entity);

        ObjectSelect<E1> q1 = queryAssembler.createRootQuery(c);

        assertEquals(10, q1.getPageSize(), "Pagination in the query for paginated request is expected");
        assertEquals(0, q1.getOffset());
        assertEquals(0, q1.getLimit());

        entity.setLimit(0);
        entity.setStart(0);
        CayenneProcessor.getOrCreateRootEntity(entity).setSelect(null);

        ObjectSelect<E1> q2 = queryAssembler.createRootQuery(c);
        assertEquals(0, q2.getPageSize());
        assertEquals(0, q2.getOffset());
        assertEquals(0, q2.getLimit());

        entity.setLimit(0);
        entity.setStart(5);

        ObjectSelect<E1> q3 = queryAssembler.createRootQuery(c);
        assertEquals(0, q3.getPageSize());
        assertEquals(0, q3.getOffset());
        assertEquals(0, q3.getLimit());
    }

    @Test
    public void createRootQuery_Qualifier() {
        RootResourceEntity<E1> entity = getResourceEntity(E1.class);

        SelectContext<E1> c = new SelectContext<>(
                E1.class,
                mock(AgRequestBuilder.class),
                PathChecker.ofDefault(),
                mock(Injector.class));

        c.setEntity(entity);

        entity.andExp(Exp.simple("name = 'X'"));
        ObjectSelect<E1> q1 = queryAssembler.createRootQuery(c);
        assertEquals(E1.NAME.eq("X"), q1.getWhere());

        entity.andExp(Exp.simple("name in ('a', 'b')"));
        ObjectSelect<E1> q2 = queryAssembler.createRootQuery(c);
        assertEquals(E1.NAME.eq("X").andExp(E1.NAME.in("a", "b")), q2.getWhere());
    }

    @Test
    public void createRootQuery_ById() {

        SelectContext<E1> c = new SelectContext<>(
                E1.class,
                mock(AgRequestBuilder.class),
                PathChecker.ofDefault(),
                mock(Injector.class));
        c.setId(AgObjectId.of(1));
        c.setEntity(getResourceEntity(E1.class));

        ObjectSelect<E1> s1 = queryAssembler.createRootQuery(c);
        assertNotNull(s1);
        assertSame(E1.class, s1.getEntityType());
    }

    @Test
    public void createRootQuery_ById_WithQuery() {
        ObjectSelect<E1> select = ObjectSelect.query(E1.class);

        SelectContext<E1> c = new SelectContext<>(
                E1.class,
                mock(AgRequestBuilder.class),
                PathChecker.ofDefault(),
                mock(Injector.class));
        c.setId(AgObjectId.of(1));
        c.setEntity(getResourceEntity(E1.class));

        CayenneProcessor.getOrCreateRootEntity(c.getEntity()).setSelect(select);

        ObjectSelect<E1> s2 = queryAssembler.createRootQuery(c);
        assertNotNull(s2);
        assertNotSame(select, s2);
        assertSame(E1.class, s2.getEntityType());
    }
}
