package io.agrest.cayenne.processor;

import io.agrest.RootResourceEntity;
import io.agrest.base.protocol.Exp;
import io.agrest.base.protocol.Dir;
import io.agrest.base.protocol.Sort;
import io.agrest.cayenne.cayenne.main.E1;
import io.agrest.cayenne.unit.CayenneNoDbTest;
import io.agrest.runtime.processor.select.SelectContext;
import org.apache.cayenne.query.SelectQuery;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class CayenneQueryAssemblerTest extends CayenneNoDbTest {

    @Test
    public void testCreateRootQuery_Ordering() {

        RootResourceEntity<E1> entity = getResourceEntity(E1.class);
        entity.getOrderings().add(new Sort("name", Dir.ASC));
        SelectContext<E1> c = new SelectContext<>(E1.class);
        c.setEntity(entity);

        SelectQuery<E1> q1 = queryAssembler.createRootQuery(c);
        assertEquals(1, q1.getOrderings().size());
        assertEquals(E1.NAME.asc(), q1.getOrderings().get(0));

        entity.getOrderings().add(new Sort("name", Dir.DESC));
        SelectQuery<E1> q2 = queryAssembler.createRootQuery(c);
        assertEquals(2, q2.getOrderings().size());
        assertEquals(E1.NAME.asc(), q2.getOrderings().get(0));
        assertEquals(E1.NAME.desc(), q2.getOrderings().get(1));
    }

    @Test
    public void testCreateRootQuery_Pagination() {

        RootResourceEntity<E1> resourceEntity = new RootResourceEntity<>(getAgEntity(E1.class));
        resourceEntity.setFetchLimit(10);
        resourceEntity.setFetchOffset(0);

        SelectContext<E1> c = new SelectContext<>(E1.class);
        c.setEntity(resourceEntity);

        SelectQuery<E1> q1 = queryAssembler.createRootQuery(c);

        assertEquals(10, q1.getPageSize(), "Pagination in the query for paginated request is expected");
        assertEquals(0, q1.getFetchOffset());
        assertEquals(0, q1.getFetchLimit());

        resourceEntity.setFetchLimit(0);
        resourceEntity.setFetchOffset(0);
        CayenneProcessor.setQuery(resourceEntity, null);

        SelectQuery<E1> q2 = queryAssembler.createRootQuery(c);
        assertEquals(0, q2.getPageSize());
        assertEquals(0, q2.getFetchOffset());
        assertEquals(0, q2.getFetchLimit());

        resourceEntity.setFetchLimit(0);
        resourceEntity.setFetchOffset(5);

        SelectQuery<E1> q3 = queryAssembler.createRootQuery(c);
        assertEquals(0, q3.getPageSize());
        assertEquals(0, q3.getFetchOffset());
        assertEquals(0, q3.getFetchLimit());
    }

    @Test
    public void testCreateRootQuery_Qualifier() {
        RootResourceEntity<E1> entity = getResourceEntity(E1.class);

        SelectContext<E1> c = new SelectContext<>(E1.class);
        c.setEntity(entity);

        entity.andQualifier(Exp.simple("name = 'X'"));
        SelectQuery<E1> q1 = queryAssembler.createRootQuery(c);
        assertEquals(E1.NAME.eq("X"), q1.getQualifier());

        entity.andQualifier(Exp.simple("name in ('a', 'b')"));
        SelectQuery<E1> q2 = queryAssembler.createRootQuery(c);
        assertEquals(E1.NAME.eq("X").andExp(E1.NAME.in("a", "b")), q2.getQualifier());
    }

    @Test
    public void testCreateRootQuery_ById() {

        SelectContext<E1> c = new SelectContext<>(E1.class);
        c.setId(1);
        c.setEntity(getResourceEntity(E1.class));

        SelectQuery<E1> s1 = queryAssembler.createRootQuery(c);
        assertNotNull(s1);
        assertSame(E1.class, s1.getRoot());
    }

    @Test
    public void testCreateRootQuery_ById_WithQuery() {
        SelectQuery<E1> select = new SelectQuery<>(E1.class);

        SelectContext<E1> c = new SelectContext<>(E1.class);
        c.setId(1);
        c.setEntity(getResourceEntity(E1.class));

        CayenneProcessor.setQuery(c.getEntity(), select);

        SelectQuery<E1> s2 = queryAssembler.createRootQuery(c);
        assertNotNull(s2);
        assertNotSame(select, s2);
        assertSame(E1.class, s2.getRoot());
    }
}
