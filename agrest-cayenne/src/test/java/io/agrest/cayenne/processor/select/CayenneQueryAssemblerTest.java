package io.agrest.cayenne.processor.select;

import io.agrest.RootResourceEntity;
import io.agrest.cayenne.unit.CayenneNoDbTest;
import io.agrest.it.fixture.cayenne.E1;
import io.agrest.runtime.processor.select.SelectContext;
import org.apache.cayenne.exp.Expression;
import org.apache.cayenne.query.Ordering;
import org.apache.cayenne.query.SelectQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class CayenneQueryAssemblerTest extends CayenneNoDbTest {

    private CayenneQueryAssembler queryAssembler;

    @BeforeEach
    public void before() {
        this.queryAssembler = new CayenneQueryAssembler(runtime.getChannel().getEntityResolver());
    }

    @Test
    public void testCreateRootQuery_Ordering() {

        Ordering o1 = E1.NAME.asc();
        Ordering o2 = E1.NAME.desc();

        SelectQuery<E1> query = new SelectQuery<>(E1.class);
        query.addOrdering(o1);

        RootResourceEntity<E1> resourceEntity = getResourceEntity(E1.class);
        resourceEntity.getOrderings().add(o2);

        SelectContext<E1> context = new SelectContext<>(E1.class);
        resourceEntity.setSelect(query);
        context.setEntity(resourceEntity);

        SelectQuery<E1> amended = queryAssembler.createRootQuery(context);
        assertSame(query, amended);
        assertEquals(2, amended.getOrderings().size());
        assertSame(o1, amended.getOrderings().get(0));
        assertSame(o2, amended.getOrderings().get(1));
    }

    @Test
    public void testCreateRootQuery_Pagination() {

        RootResourceEntity<E1> resourceEntity = new RootResourceEntity<>(getAgEntity(E1.class), null);
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
        resourceEntity.setSelect(null);

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
        Expression extraQualifier = E1.NAME.eq("X");
        RootResourceEntity<E1> resourceEntity = getResourceEntity(E1.class);

        resourceEntity.andQualifier(extraQualifier);

        SelectContext<E1> c1 = new SelectContext<>(E1.class);
        c1.setEntity(resourceEntity);

        SelectQuery<E1> query = queryAssembler.createRootQuery(c1);
        assertEquals(extraQualifier, query.getQualifier());

        SelectQuery<E1> query2 = new SelectQuery<>(E1.class);
        query2.setQualifier(E1.NAME.in("a", "b"));

        SelectContext<E1> c2 = new SelectContext<>(E1.class);
        resourceEntity.setSelect(query2);
        c2.setEntity(resourceEntity);

        SelectQuery<E1> query2Amended = queryAssembler.createRootQuery(c2);
        assertEquals(E1.NAME.in("a", "b").andExp(E1.NAME.eq("X")), query2Amended.getQualifier());
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
        c.getEntity().setSelect(select);

        SelectQuery<E1> s2 = queryAssembler.createRootQuery(c);
        assertNotNull(s2);
        assertNotSame(select, s2);
        assertSame(E1.class, s2.getRoot());
    }
}
