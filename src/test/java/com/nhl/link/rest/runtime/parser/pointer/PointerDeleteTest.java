package com.nhl.link.rest.runtime.parser.pointer;

import com.nhl.link.rest.it.fixture.JerseyTestOnDerby;
import com.nhl.link.rest.it.fixture.cayenne.E19;
import com.nhl.link.rest.it.fixture.cayenne.E2;
import com.nhl.link.rest.it.fixture.cayenne.E3;
import com.nhl.link.rest.it.fixture.cayenne.E4;
import com.nhl.link.rest.meta.LrEntity;
import com.nhl.link.rest.runtime.LinkRestRuntime;
import com.nhl.link.rest.runtime.cayenne.ICayennePersister;
import com.nhl.link.rest.runtime.meta.IMetadataService;
import org.apache.cayenne.query.ObjectSelect;
import org.apache.cayenne.query.SQLTemplate;
import org.apache.cayenne.query.SelectById;
import org.apache.cayenne.query.SelectQuery;
import org.junit.Test;

import javax.ws.rs.core.FeatureContext;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class PointerDeleteTest extends JerseyTestOnDerby {

    private IMetadataService metadataService;
    private ICayennePersister cayenneService;
    private LrPointerService pointerService;

    @Test
    public void testDelete_InstancePointer() throws Exception {

        Integer deletedId = 1, persistentId = 2;

        SQLTemplate insertE4_1 = new SQLTemplate(E4.class,
				"INSERT INTO utest.e4 (id, c_varchar, c_int) values (" + deletedId + ", 'xxx', 5)");
        SQLTemplate insertE4_2 = new SQLTemplate(E4.class,
				"INSERT INTO utest.e4 (id, c_varchar, c_int) values (" + persistentId + ", 'yyy', 7)");
		newContext().performGenericQuery(insertE4_1);
        newContext().performGenericQuery(insertE4_2);

        LrEntity<E4> e4 = metadataService.getLrEntity(E4.class);

        LrPointer pointer = new PointerParser(pointerService).getPointer(e4, String.valueOf(deletedId));

        PointerContext context = new CayennePointerContext(cayenneService, Collections.singletonList(pointer));
        pointer.delete(context);
        context.commitChanges();

        E4 deleted = SelectById.query(E4.class, deletedId).selectOne(cayenneService.newContext());
        assertNull(deleted);

        E4 persistent = SelectById.query(E4.class, persistentId).selectOne(cayenneService.newContext());
        assertNotNull(persistent);
    }

    @Test
    public void testDelete_EntityCollectionPointer() throws Exception {

        SQLTemplate insertE4_1 = new SQLTemplate(E4.class,
				"INSERT INTO utest.e4 (id, c_varchar, c_int) values (1, 'xxx', 5)");
        SQLTemplate insertE4_2 = new SQLTemplate(E4.class,
				"INSERT INTO utest.e4 (id, c_varchar, c_int) values (2, 'yyy', 7)");
		newContext().performGenericQuery(insertE4_1);
        newContext().performGenericQuery(insertE4_2);

        LrEntity<E4> e4 = metadataService.getLrEntity(E4.class);

        LrPointer pointer = new PointerParser(pointerService).getPointer(e4, Pointers.PATH_SEPARATOR);

        PointerContext context = new CayennePointerContext(cayenneService, Collections.singletonList(pointer));
        pointer.delete(context);
        context.commitChanges();

        List<E4> list = ObjectSelect.query(E4.class).select(cayenneService.newContext());
        assertTrue(list.isEmpty());
    }

    @Test
    public void testDelete_AttributePointer_ObjectValue() throws Exception {

        Integer updatedId = 1, unmodifiedId = 2;

        SQLTemplate insertE4_1 = new SQLTemplate(E4.class,
				"INSERT INTO utest.e4 (id, c_varchar, c_int) values (" + updatedId + ", 'xxx', 5)");
        SQLTemplate insertE4_2 = new SQLTemplate(E4.class,
				"INSERT INTO utest.e4 (id, c_varchar, c_int) values (" + unmodifiedId + ", 'yyy', 7)");
		newContext().performGenericQuery(insertE4_1);
        newContext().performGenericQuery(insertE4_2);

        LrEntity<E4> e4 = metadataService.getLrEntity(E4.class);

        LrPointer pointer = new PointerParser(pointerService).getPointer(e4,
                Pointers.buildPath(String.valueOf(updatedId), E4.C_VARCHAR.getName()));

        PointerContext context = new CayennePointerContext(cayenneService, Collections.singletonList(pointer));
        pointer.delete(context);
        context.commitChanges();

        E4 updated = SelectById.query(E4.class, updatedId).selectOne(cayenneService.newContext());
        assertNull(updated.getCVarchar());

        E4 unmodified = SelectById.query(E4.class, unmodifiedId).selectOne(cayenneService.newContext());
        assertEquals("yyy", unmodified.getCVarchar());
    }

    @Test
    public void testDelete_AttributePointer_PrimitiveValue() throws Exception {

        Integer updatedId = 1;

        SQLTemplate insertE19 = new SQLTemplate(E19.class,
				"INSERT INTO utest.e19 (id, int_primitive) values (" + updatedId + ", 1)");
		newContext().performGenericQuery(insertE19);

        LrEntity<E19> e19 = metadataService.getLrEntity(E19.class);

        LrPointer pointer = new PointerParser(pointerService).getPointer(e19,
                Pointers.buildPath(String.valueOf(updatedId), E19.INT_PRIMITIVE.getName()));

        PointerContext context = new CayennePointerContext(cayenneService, Collections.singletonList(pointer));
        pointer.delete(context);
        context.commitChanges();

        E19 updated = SelectById.query(E19.class, updatedId).selectOne(cayenneService.newContext());
        assertEquals(0, updated.getIntPrimitive());
    }

    @Test
    public void testDelete_ToOneRelationshipPointer_Implicit() throws Exception {

        SQLTemplate insertE2 = new SQLTemplate(E2.class,
				"INSERT INTO utest.e2 (id, name) values (2, 'yyy')");
        SQLTemplate insertE3_1 = new SQLTemplate(E3.class,
				"INSERT INTO utest.e3 (id, name, e2_id) values (1, 'xxx', 2)");
        SQLTemplate insertE3_2 = new SQLTemplate(E3.class,
				"INSERT INTO utest.e3 (id, name, e2_id) values (3, 'zzz', 2)");
		newContext().performGenericQuery(insertE2);
        newContext().performGenericQuery(insertE3_1);
        newContext().performGenericQuery(insertE3_2);

        E3 baseObject = newContext().selectOne(SelectQuery.query(E3.class, E3.NAME.eq("xxx")));

        LrEntity<E3> e3 = metadataService.getLrEntity(E3.class);

        LrPointer pointer = new PointerParser(pointerService).getPointer(e3, E3.E2.getName());

        PointerContext context = new CayennePointerContext(cayenneService, Collections.singletonList(pointer));
        pointer.delete(context, baseObject);
        context.commitChanges();

        E3 updatedE3 = newContext().selectOne(SelectQuery.query(E3.class, E3.NAME.eq("xxx")));
        E3 unmodifiedE3 = newContext().selectOne(SelectQuery.query(E3.class, E3.NAME.eq("zzz")));
        E2 instanceE2 = newContext().selectOne(SelectQuery.query(E2.class, E2.NAME.eq("yyy")));

        assertNull(updatedE3.getE2());
        assertNotNull(unmodifiedE3.getE2());
        assertEquals(1, instanceE2.getE3s().size());
    }

    @Test
    public void testDelete_ToOneRelationshipPointer_Explicit() throws Exception {

        SQLTemplate insertE2 = new SQLTemplate(E2.class,
				"INSERT INTO utest.e2 (id, name) values (2, 'yyy')");
        SQLTemplate insertE3_1 = new SQLTemplate(E3.class,
				"INSERT INTO utest.e3 (id, name, e2_id) values (1, 'xxx', 2)");
        SQLTemplate insertE3_2 = new SQLTemplate(E3.class,
				"INSERT INTO utest.e3 (id, name, e2_id) values (3, 'zzz', 2)");
		newContext().performGenericQuery(insertE2);
        newContext().performGenericQuery(insertE3_1);
        newContext().performGenericQuery(insertE3_2);

        E3 baseObject = newContext().selectOne(SelectQuery.query(E3.class, E3.NAME.eq("xxx")));

        LrEntity<E3> e3 = metadataService.getLrEntity(E3.class);

        LrPointer pointer = new PointerParser(pointerService).getPointer(e3,
                Pointers.buildRelationship(E3.E2.getName(), 2));

        PointerContext context = new CayennePointerContext(cayenneService, Collections.singletonList(pointer));
        pointer.delete(context, baseObject);
        context.commitChanges();

        E3 updatedE3 = newContext().selectOne(SelectQuery.query(E3.class, E3.NAME.eq("xxx")));
        E3 unmodifiedE3 = newContext().selectOne(SelectQuery.query(E3.class, E3.NAME.eq("zzz")));
        E2 instanceE2 = newContext().selectOne(SelectQuery.query(E2.class, E2.NAME.eq("yyy")));

        assertNull(updatedE3.getE2());
        assertNotNull(unmodifiedE3.getE2());
        assertEquals(1, instanceE2.getE3s().size());
    }

    @Test
    public void testDelete_ToManyRelationship_CollectionPointer() throws Exception {

        SQLTemplate insertE2 = new SQLTemplate(E2.class,
				"INSERT INTO utest.e2 (id, name) values (2, 'yyy')");
        SQLTemplate insertE3_1 = new SQLTemplate(E3.class,
				"INSERT INTO utest.e3 (id, name, e2_id) values (1, 'xxx', 2)");
        SQLTemplate insertE3_2 = new SQLTemplate(E3.class,
				"INSERT INTO utest.e3 (id, name, e2_id) values (3, 'zzz', 2)");
		newContext().performGenericQuery(insertE2);
        newContext().performGenericQuery(insertE3_1);
        newContext().performGenericQuery(insertE3_2);

        E2 baseObject = newContext().selectOne(SelectQuery.query(E2.class, E2.NAME.eq("yyy")));

        LrEntity<E2> e2 = metadataService.getLrEntity(E2.class);

        LrPointer pointer = new PointerParser(pointerService).getPointer(e2, E2.E3S.getName());

        PointerContext context = new CayennePointerContext(cayenneService, Collections.singletonList(pointer));
        pointer.delete(context, baseObject);
        context.commitChanges();

        E2 updatedE2 = newContext().selectOne(SelectQuery.query(E2.class, E2.NAME.eq("yyy")));
        E3 instanceE3_1 = newContext().selectOne(SelectQuery.query(E3.class, E3.NAME.eq("xxx")));
        E3 instanceE3_2 = newContext().selectOne(SelectQuery.query(E3.class, E3.NAME.eq("zzz")));

        assertTrue(updatedE2.getE3s().isEmpty());
        assertNull(instanceE3_1.getE2());
        assertNull(instanceE3_2.getE2());
    }

    @Test
    public void testDelete_ToManyRelationship_InstancePointer() throws Exception {

        Integer relatedId = 1;

        SQLTemplate insertE2 = new SQLTemplate(E2.class,
				"INSERT INTO utest.e2 (id, name) values (2, 'yyy')");
        SQLTemplate insertE3_1 = new SQLTemplate(E3.class,
				"INSERT INTO utest.e3 (id, name, e2_id) values (" + relatedId + ", 'xxx', 2)");
        SQLTemplate insertE3_2 = new SQLTemplate(E3.class,
				"INSERT INTO utest.e3 (id, name, e2_id) values (3, 'zzz', 2)");
		newContext().performGenericQuery(insertE2);
        newContext().performGenericQuery(insertE3_1);
        newContext().performGenericQuery(insertE3_2);

        E2 baseObject = newContext().selectOne(SelectQuery.query(E2.class, E2.NAME.eq("yyy")));

        LrEntity<E2> e2 = metadataService.getLrEntity(E2.class);

        LrPointer pointer = new PointerParser(pointerService).getPointer(e2,
                Pointers.buildRelationship(E2.E3S.getName(), relatedId));

        PointerContext context = new CayennePointerContext(cayenneService, Collections.singletonList(pointer));
        pointer.delete(context, baseObject);
        context.commitChanges();

        E2 updatedE2 = newContext().selectOne(SelectQuery.query(E2.class, E2.NAME.eq("yyy")));
        E3 updatedE3 = newContext().selectOne(SelectQuery.query(E3.class, E3.NAME.eq("xxx")));
        E3 unmodifiedE3 = newContext().selectOne(SelectQuery.query(E3.class, E3.NAME.eq("zzz")));

        assertEquals(1, updatedE2.getE3s().size());
        assertNull(updatedE3.getE2());
        assertNotNull(unmodifiedE3.getE2());
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
