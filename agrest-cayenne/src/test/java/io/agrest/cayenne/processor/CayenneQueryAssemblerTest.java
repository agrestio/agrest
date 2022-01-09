package io.agrest.cayenne.processor;

import io.agrest.RootResourceEntity;
import io.agrest.protocol.Dir;
import io.agrest.protocol.Exp;
import io.agrest.protocol.Sort;
import io.agrest.cayenne.cayenne.main.E1;
import io.agrest.cayenne.unit.CayenneNoDbTest;
import io.agrest.runtime.processor.select.SelectContext;
import org.apache.cayenne.query.ObjectSelect;
import org.junit.jupiter.api.Test;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.*;

public class CayenneQueryAssemblerTest extends CayenneNoDbTest {

    @Test
    public void testCreateRootQuery_Ordering() {

        RootResourceEntity<E1> entity = getResourceEntity(E1.class);
        entity.getOrderings().add(new Sort("name", Dir.ASC));
        SelectContext<E1> c = new SelectContext<>(E1.class);
        c.setEntity(entity);

        ObjectSelect<E1> q1 = queryAssembler.createRootQuery(c);
        assertEquals(asList(E1.NAME.asc()), q1.getOrderings());

        entity.getOrderings().add(new Sort("name", Dir.DESC));
        ObjectSelect<E1> q2 = queryAssembler.createRootQuery(c);
        assertEquals(asList(E1.NAME.asc(), E1.NAME.desc()), q2.getOrderings());
    }

    @Test
    public void testCreateRootQuery_Pagination() {

        RootResourceEntity<E1> entity = new RootResourceEntity<>(getAgEntity(E1.class));
        entity.setFetchLimit(10);
        entity.setFetchOffset(0);

        SelectContext<E1> c = new SelectContext<>(E1.class);
        c.setEntity(entity);

        ObjectSelect<E1> q1 = queryAssembler.createRootQuery(c);

        assertEquals(10, q1.getPageSize(), "Pagination in the query for paginated request is expected");
        assertEquals(0, q1.getOffset());
        assertEquals(0, q1.getLimit());

        entity.setFetchLimit(0);
        entity.setFetchOffset(0);
        CayenneProcessor.getOrCreateRootEntity(entity).setSelect(null);

        ObjectSelect<E1> q2 = queryAssembler.createRootQuery(c);
        assertEquals(0, q2.getPageSize());
        assertEquals(0, q2.getOffset());
        assertEquals(0, q2.getLimit());

        entity.setFetchLimit(0);
        entity.setFetchOffset(5);

        ObjectSelect<E1> q3 = queryAssembler.createRootQuery(c);
        assertEquals(0, q3.getPageSize());
        assertEquals(0, q3.getOffset());
        assertEquals(0, q3.getLimit());
    }

    @Test
    public void testCreateRootQuery_Qualifier() {
        RootResourceEntity<E1> entity = getResourceEntity(E1.class);

        SelectContext<E1> c = new SelectContext<>(E1.class);
        c.setEntity(entity);

        entity.andQualifier(Exp.simple("name = 'X'"));
        ObjectSelect<E1> q1 = queryAssembler.createRootQuery(c);
        assertEquals(E1.NAME.eq("X"), q1.getWhere());

        entity.andQualifier(Exp.simple("name in ('a', 'b')"));
        ObjectSelect<E1> q2 = queryAssembler.createRootQuery(c);
        assertEquals(E1.NAME.eq("X").andExp(E1.NAME.in("a", "b")), q2.getWhere());
    }

    @Test
    public void testCreateRootQuery_ById() {

        SelectContext<E1> c = new SelectContext<>(E1.class);
        c.setId(1);
        c.setEntity(getResourceEntity(E1.class));

        ObjectSelect<E1> s1 = queryAssembler.createRootQuery(c);
        assertNotNull(s1);
        assertSame(E1.class, s1.getEntityType());
    }

    @Test
    public void testCreateRootQuery_ById_WithQuery() {
        ObjectSelect<E1> select = ObjectSelect.query(E1.class);

        SelectContext<E1> c = new SelectContext<>(E1.class);
        c.setId(1);
        c.setEntity(getResourceEntity(E1.class));

        CayenneProcessor.getOrCreateRootEntity(c.getEntity()).setSelect(select);

        ObjectSelect<E1> s2 = queryAssembler.createRootQuery(c);
        assertNotNull(s2);
        assertNotSame(select, s2);
        assertSame(E1.class, s2.getEntityType());
    }
}
