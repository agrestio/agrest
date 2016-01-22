package com.nhl.link.rest.runtime.parser.pointer;

import com.nhl.link.rest.it.fixture.JerseyTestOnDerby;
import com.nhl.link.rest.it.fixture.cayenne.E2;
import com.nhl.link.rest.it.fixture.cayenne.E3;
import com.nhl.link.rest.it.fixture.cayenne.E4;
import com.nhl.link.rest.it.fixture.cayenne.E5;
import com.nhl.link.rest.meta.LrEntity;
import com.nhl.link.rest.runtime.LinkRestRuntime;
import com.nhl.link.rest.runtime.cayenne.ICayennePersister;
import com.nhl.link.rest.runtime.meta.IMetadataService;
import org.apache.cayenne.query.SQLTemplate;
import org.apache.cayenne.query.SelectQuery;
import org.junit.Test;

import javax.ws.rs.core.FeatureContext;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class PointerResolveTest extends JerseyTestOnDerby {

    private IMetadataService metadataService;
    private ICayennePersister cayenneService;
    private LrPointerService pointerService;

    @Test
    public void testResolving_InstancePointer() throws Exception {

        SQLTemplate insert = new SQLTemplate(E4.class,
				"INSERT INTO utest.e4 (id, c_varchar, c_int) values (1, 'xxx', 5)");
		newContext().performGenericQuery(insert);

        LrEntity<E4> e4 = metadataService.getLrEntity(E4.class);

        LrPointer pointer = new PointerParser(pointerService).getPointer(e4, "1");
        PointerContext context = new CayennePointerContext(cayenneService, Collections.singletonList(pointer));

        Object object = pointer.resolve(context);
        assertNotNull(object);
    }

    @Test
    public void testResolving_AttributePointer() throws Exception {

        SQLTemplate insert = new SQLTemplate(E4.class,
				"INSERT INTO utest.e4 (id, c_varchar, c_int) values (1, 'xxx', 5)");
		newContext().performGenericQuery(insert);

        E4 instance = newContext().selectOne(SelectQuery.query(E4.class, E4.C_VARCHAR.eq("xxx")));
        assertNotNull(instance);

        LrEntity<E4> e4 = metadataService.getLrEntity(E4.class);

        LrPointer pointer = new PointerParser(pointerService).getPointer(e4, E4.C_VARCHAR.getName());
        PointerContext context = new CayennePointerContext(cayenneService, Collections.singletonList(pointer));

        Object object = pointer.resolve(context, instance);
        assertEquals("xxx", object);
    }

    @Test
    public void testResolving_ToOneRelationshipPointer_Implicit() throws Exception {

        SQLTemplate insertE2 = new SQLTemplate(E2.class,
				"INSERT INTO utest.e2 (id, name) values (2, 'yyy')");
        SQLTemplate insertE3 = new SQLTemplate(E3.class,
				"INSERT INTO utest.e3 (id, name, e2_id) values (1, 'xxx', 2)");
		newContext().performGenericQuery(insertE2);
        newContext().performGenericQuery(insertE3);

        E3 instanceE3 = newContext().selectOne(SelectQuery.query(E3.class, E3.NAME.eq("xxx")));
        E2 instanceE2 = newContext().selectOne(SelectQuery.query(E2.class, E2.NAME.eq("yyy")));
        assertNotNull(instanceE3);
        assertNotNull(instanceE2);

        LrEntity<E3> e3 = metadataService.getLrEntity(E3.class);

        LrPointer pointer = new PointerParser(pointerService).getPointer(e3, E3.E2.getName());
        PointerContext context = new CayennePointerContext(cayenneService, Collections.singletonList(pointer));

        E2 object = (E2) pointer.resolve(context, instanceE3);
        assertEquals(instanceE2.getName(), object.getName());
    }

    @Test
    public void testResolving_ToOneRelationshipPointer_Explicit() throws Exception {

        SQLTemplate insertE2 = new SQLTemplate(E2.class,
				"INSERT INTO utest.e2 (id, name) values (2, 'yyy')");
        SQLTemplate insertE3 = new SQLTemplate(E3.class,
				"INSERT INTO utest.e3 (id, name, e2_id) values (1, 'xxx', 2)");
		newContext().performGenericQuery(insertE2);
        newContext().performGenericQuery(insertE3);

        E3 instanceE3 = newContext().selectOne(SelectQuery.query(E3.class, E3.NAME.eq("xxx")));
        E2 instanceE2 = newContext().selectOne(SelectQuery.query(E2.class, E2.NAME.eq("yyy")));
        assertNotNull(instanceE3);
        assertNotNull(instanceE2);

        LrEntity<E3> e3 = metadataService.getLrEntity(E3.class);

        String path = Pointers.buildRelationship(E3.E2.getName(), 2);
        LrPointer pointer = new PointerParser(pointerService).getPointer(e3, path);
        PointerContext context = new CayennePointerContext(cayenneService, Collections.singletonList(pointer));

        E2 object = (E2) pointer.resolve(context, instanceE3);
        assertEquals(instanceE2.getName(), object.getName());
    }

    @Test
    public void testResolving_ToManyRelationshipPointer() throws Exception {

        SQLTemplate insertE2 = new SQLTemplate(E2.class,
				"INSERT INTO utest.e2 (id, name) values (2, 'yyy')");
        SQLTemplate insertE3_1 = new SQLTemplate(E3.class,
				"INSERT INTO utest.e3 (id, name, e2_id) values (1, 'xxx', 2)");
        SQLTemplate insertE3_2 = new SQLTemplate(E3.class,
				"INSERT INTO utest.e3 (id, name, e2_id) values (3, 'zzz', 2)");
		newContext().performGenericQuery(insertE2);
        newContext().performGenericQuery(insertE3_1);
        newContext().performGenericQuery(insertE3_2);

        E3 instanceE3 = newContext().selectOne(SelectQuery.query(E3.class, E3.NAME.eq("zzz")));
        E2 instanceE2 = newContext().selectOne(SelectQuery.query(E2.class, E2.NAME.eq("yyy")));
        assertNotNull(instanceE3);
        assertNotNull(instanceE2);

        LrEntity<E2> e2 = metadataService.getLrEntity(E2.class);

        String path = Pointers.buildRelationship(E2.E3S.getName(), 3);
        LrPointer pointer = new PointerParser(pointerService).getPointer(e2, path);
        PointerContext context = new CayennePointerContext(cayenneService, Collections.singletonList(pointer));

        E3 object = (E3) pointer.resolve(context, instanceE2);
        assertEquals(instanceE3.getName(), object.getName());
    }

    @Test
    public void testResolving_CompoundInstancePointer() throws Exception {

        SQLTemplate insertE2 = new SQLTemplate(E2.class,
				"INSERT INTO utest.e2 (id, name) values (2, 'yyy')");
        SQLTemplate insertE3_1 = new SQLTemplate(E3.class,
				"INSERT INTO utest.e3 (id, name, e2_id) values (1, 'xxx', 2)");
        SQLTemplate insertE3_2 = new SQLTemplate(E3.class,
				"INSERT INTO utest.e3 (id, name, e2_id) values (3, 'zzz', 2)");
		newContext().performGenericQuery(insertE2);
        newContext().performGenericQuery(insertE3_1);
        newContext().performGenericQuery(insertE3_2);

        E3 instanceE3 = newContext().selectOne(SelectQuery.query(E3.class, E3.NAME.eq("zzz")));
        E2 instanceE2 = newContext().selectOne(SelectQuery.query(E2.class, E2.NAME.eq("yyy")));
        assertNotNull(instanceE3);
        assertNotNull(instanceE2);

        LrEntity<E2> e2 = metadataService.getLrEntity(E2.class);

        String path = Pointers.buildPath(Pointers.buildRelationship(E2.E3S.getName(), 1), "3");
        LrPointer pointer = new PointerParser(pointerService).getPointer(e2, path);
        PointerContext context = new CayennePointerContext(cayenneService, Collections.singletonList(pointer));

        E3 object = (E3) pointer.resolve(context, instanceE2);
        assertEquals(instanceE3.getName(), object.getName());
    }

    @Test
    public void testResolving_CompoundAttributePointer() throws Exception {

        SQLTemplate insertE2 = new SQLTemplate(E2.class,
				"INSERT INTO utest.e2 (id, name) values (2, 'yyy')");
        SQLTemplate insertE3_1 = new SQLTemplate(E3.class,
				"INSERT INTO utest.e3 (id, name, e2_id) values (1, 'xxx', 2)");
        SQLTemplate insertE3_2 = new SQLTemplate(E3.class,
				"INSERT INTO utest.e3 (id, name, e2_id) values (3, 'zzz', 2)");
		newContext().performGenericQuery(insertE2);
        newContext().performGenericQuery(insertE3_1);
        newContext().performGenericQuery(insertE3_2);

        E3 instanceE3 = newContext().selectOne(SelectQuery.query(E3.class, E3.NAME.eq("xxx")));
        E2 instanceE2 = newContext().selectOne(SelectQuery.query(E2.class, E2.NAME.eq("yyy")));
        assertNotNull(instanceE3);
        assertNotNull(instanceE2);

        LrEntity<E2> e2 = metadataService.getLrEntity(E2.class);

        String path = Pointers.buildPath(Pointers.buildRelationship(E2.E3S.getName(), 1), E3.NAME.getName());
        LrPointer pointer = new PointerParser(pointerService).getPointer(e2, path);
        PointerContext context = new CayennePointerContext(cayenneService, Collections.singletonList(pointer));

        Object object = pointer.resolve(context, instanceE2);
        assertEquals(instanceE3.getName(), object);
    }

    @Test
    public void testResolving_CompoundToOneRelationshipPointer_Implicit() throws Exception {

        SQLTemplate insertE2 = new SQLTemplate(E2.class,
				"INSERT INTO utest.e2 (id, name) values (2, 'yyy')");
        SQLTemplate insertE5 = new SQLTemplate(E5.class,
				"INSERT INTO utest.e5 (id, name) values (5, 'zzz')");
        SQLTemplate insertE3 = new SQLTemplate(E3.class,
				"INSERT INTO utest.e3 (id, name, e2_id, e5_id) values (3, 'xxx', 2, 5)");
		newContext().performGenericQuery(insertE2);
        newContext().performGenericQuery(insertE5);
        newContext().performGenericQuery(insertE3);


        E5 instanceE5 = newContext().selectOne(SelectQuery.query(E5.class, E5.NAME.eq("zzz")));
        E2 instanceE2 = newContext().selectOne(SelectQuery.query(E2.class, E2.NAME.eq("yyy")));
        assertNotNull(instanceE5);
        assertNotNull(instanceE2);

        LrEntity<E2> e2 = metadataService.getLrEntity(E2.class);

        String path = Pointers.buildPath(Pointers.buildRelationship(E2.E3S.getName(), 3), E3.E5.getName());
        LrPointer pointer = new PointerParser(pointerService).getPointer(e2, path);
        PointerContext context = new CayennePointerContext(cayenneService, Collections.singletonList(pointer));

        E5 object = (E5) pointer.resolve(context, instanceE2);
        assertEquals(instanceE5.getName(), object.getName());
    }

    @Test
    public void testResolving_CompoundToOneRelationshipPointer_Explicit() throws Exception {

        SQLTemplate insertE2 = new SQLTemplate(E2.class,
				"INSERT INTO utest.e2 (id, name) values (2, 'yyy')");
        SQLTemplate insertE5 = new SQLTemplate(E5.class,
				"INSERT INTO utest.e5 (id, name) values (5, 'zzz')");
        SQLTemplate insertE3 = new SQLTemplate(E3.class,
				"INSERT INTO utest.e3 (id, name, e2_id, e5_id) values (3, 'xxx', 2, 5)");
		newContext().performGenericQuery(insertE2);
        newContext().performGenericQuery(insertE5);
        newContext().performGenericQuery(insertE3);


        E5 instanceE5 = newContext().selectOne(SelectQuery.query(E5.class, E5.NAME.eq("zzz")));
        E2 instanceE2 = newContext().selectOne(SelectQuery.query(E2.class, E2.NAME.eq("yyy")));
        assertNotNull(instanceE5);
        assertNotNull(instanceE2);

        LrEntity<E2> e2 = metadataService.getLrEntity(E2.class);

        String path = Pointers.buildPath(
                Pointers.buildRelationship(E2.E3S.getName(), 3), Pointers.buildRelationship(E3.E5.getName(), "5"));
        LrPointer pointer = new PointerParser(pointerService).getPointer(e2, path);
        PointerContext context = new CayennePointerContext(cayenneService, Collections.singletonList(pointer));

        E5 object = (E5) pointer.resolve(context, instanceE2);
        assertEquals(instanceE5.getName(), object.getName());
    }

    @Test
    public void testResolving_CompoundToManyRelationshipPointer() throws Exception {

        SQLTemplate insertE2 = new SQLTemplate(E2.class,
				"INSERT INTO utest.e2 (id, name) values (2, 'yyy')");
        SQLTemplate insertE5 = new SQLTemplate(E5.class,
				"INSERT INTO utest.e5 (id, name) values (5, 'zzz')");
        SQLTemplate insertE3_1 = new SQLTemplate(E3.class,
				"INSERT INTO utest.e3 (id, name, e2_id, e5_id) values (3, 'xxx', 2, 5)");
        SQLTemplate insertE3_2 = new SQLTemplate(E3.class,
				"INSERT INTO utest.e3 (id, name, e5_id) values (33, 'www', 5)");
		newContext().performGenericQuery(insertE2);
        newContext().performGenericQuery(insertE5);
        newContext().performGenericQuery(insertE3_1);
        newContext().performGenericQuery(insertE3_2);


        E3 instanceE3 = newContext().selectOne(SelectQuery.query(E3.class, E3.NAME.eq("www")));
        E2 instanceE2 = newContext().selectOne(SelectQuery.query(E2.class, E2.NAME.eq("yyy")));
        assertNotNull(instanceE3);
        assertNotNull(instanceE2);

        LrEntity<E2> e2 = metadataService.getLrEntity(E2.class);

        String path = Pointers.buildPath(
                Pointers.buildRelationship(E2.E3S.getName(), 3),
                E3.E5.getName(),
                Pointers.buildRelationship(E5.E2S.getName(), "33")
        );
        LrPointer pointer = new PointerParser(pointerService).getPointer(e2, path);
        PointerContext context = new CayennePointerContext(cayenneService, Collections.singletonList(pointer));

        E3 object = (E3) pointer.resolve(context, instanceE2);
        assertEquals(instanceE3.getName(), object.getName());
    }

    @Test
    public void testResolving_ToOneRelationshipPointer_NoResult() throws Exception {

        SQLTemplate insertE2 = new SQLTemplate(E2.class,
				"INSERT INTO utest.e2 (id, name) values (2, 'yyy')");
        SQLTemplate insertE3 = new SQLTemplate(E3.class,
				"INSERT INTO utest.e3 (id, name, e2_id) values (3, 'xxx', 2)");
		newContext().performGenericQuery(insertE2);
        newContext().performGenericQuery(insertE3);

        E2 instanceE2 = newContext().selectOne(SelectQuery.query(E2.class, E2.NAME.eq("yyy")));
        assertNotNull(instanceE2);

        LrEntity<E2> e2 = metadataService.getLrEntity(E2.class);

        String path = Pointers.buildPath(Pointers.buildRelationship(E2.E3S.getName(), 3), E3.E5.getName());
        LrPointer pointer = new PointerParser(pointerService).getPointer(e2, path);
        PointerContext context = new CayennePointerContext(cayenneService, Collections.singletonList(pointer));

        E5 object = (E5) pointer.resolve(context, instanceE2);
        assertNull(object);
    }

    @Test(expected = Exception.class)
    public void testResolving_CompoundToManyRelationshipPointer_Exception() throws Exception {

        SQLTemplate insertE2 = new SQLTemplate(E2.class,
				"INSERT INTO utest.e2 (id, name) values (2, 'yyy')");
        SQLTemplate insertE5 = new SQLTemplate(E5.class,
				"INSERT INTO utest.e5 (id, name) values (5, 'zzz')");
        SQLTemplate insertE3 = new SQLTemplate(E3.class,
				"INSERT INTO utest.e3 (id, name, e5_id) values (33, 'www', 5)");
		newContext().performGenericQuery(insertE2);
        newContext().performGenericQuery(insertE5);
        newContext().performGenericQuery(insertE3);

        E2 instanceE2 = newContext().selectOne(SelectQuery.query(E2.class, E2.NAME.eq("yyy")));
        assertNotNull(instanceE2);

        LrEntity<E2> e2 = metadataService.getLrEntity(E2.class);

        String path = Pointers.buildPath(
                Pointers.buildRelationship(E2.E3S.getName(), 3),
                E3.E5.getName()
        );
        LrPointer pointer = new PointerParser(pointerService).getPointer(e2, path);
        PointerContext context = new CayennePointerContext(cayenneService, Collections.singletonList(pointer));

        pointer.resolve(context, instanceE2);
    }

    @Override
    protected void doAddResources(FeatureContext context) {
        for (Object object : context.getConfiguration().getInstances()) {
            if (object instanceof LinkRestRuntime) {
                LinkRestRuntime runtime = (LinkRestRuntime) object;
                metadataService = runtime.service(IMetadataService.class);
                cayenneService = runtime.service(ICayennePersister.class);
                break;
            }
        }
        pointerService = new LrPointerService(metadataService);
    }
}
