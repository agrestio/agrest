package com.nhl.link.rest.runtime.parser.pointer;

import com.nhl.link.rest.it.fixture.JerseyTestOnDerby;
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
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class PointerUpdateTest extends JerseyTestOnDerby {

    private IMetadataService metadataService;
    private ICayennePersister cayenneService;
    private LrPointerService pointerService;

    @Test
    public void testUpdate_InstancePointer() throws Exception {

        Integer updatedId = 1;

        SQLTemplate insertE4 = new SQLTemplate(E4.class,
				"INSERT INTO utest.e4 (id, c_varchar, c_int) values (" + updatedId + ", 'xxx', 5)");
		newContext().performGenericQuery(insertE4);

        E4 newObject = new E4();
        newObject.setCDecimal(BigDecimal.ONE);
        newObject.setCVarchar("abc");

        LrEntity<E4> e4 = metadataService.getLrEntity(E4.class);

        LrPointer pointer = new PointerParser(pointerService).getPointer(e4, String.valueOf(updatedId));

        PointerContext context = new CayennePointerContext(cayenneService, Collections.singletonList(pointer));
        pointer.update(context, newObject);
        context.commitChanges();

        E4 updated = SelectById.query(E4.class, updatedId).selectOne(cayenneService.newContext());
        assertEquals("abc", updated.getCVarchar());
        assertEquals(1, updated.getCDecimal().intValueExact());
        assertNull(updated.getCInt());
    }

    @Test
    public void testUpdate_EntityCollectionPointer() throws Exception {

        SQLTemplate insertE4_1 = new SQLTemplate(E4.class,
				"INSERT INTO utest.e4 (c_varchar, c_int) values ('xxx', 5)");
        SQLTemplate insertE4_2 = new SQLTemplate(E4.class,
				"INSERT INTO utest.e4 (c_varchar, c_int) values ('yyy', 7)");
		newContext().performGenericQuery(insertE4_1);
        newContext().performGenericQuery(insertE4_2);

        E4 newObject = new E4();
        newObject.setCVarchar("abc");
        newObject.setCInt(9);

        LrEntity<E4> e4 = metadataService.getLrEntity(E4.class);

        LrPointer pointer = new PointerParser(pointerService).getPointer(e4, Pointers.PATH_SEPARATOR);

        PointerContext context = new CayennePointerContext(cayenneService, Collections.singletonList(pointer));
        pointer.update(context, newObject);
        context.commitChanges();

        List<E4> list = ObjectSelect.query(E4.class).select(cayenneService.newContext());
        assertEquals(3, list.size());
    }

    @Test
    public void testUpdate_EntityCollectionPointer_EmptyArray() throws Exception {

        SQLTemplate insertE4_1 = new SQLTemplate(E4.class,
				"INSERT INTO utest.e4 (id, c_varchar, c_int) values (1, 'xxx', 5)");
        SQLTemplate insertE4_2 = new SQLTemplate(E4.class,
				"INSERT INTO utest.e4 (id, c_varchar, c_int) values (2, 'yyy', 7)");
		newContext().performGenericQuery(insertE4_1);
        newContext().performGenericQuery(insertE4_2);

        LrEntity<E4> e4 = metadataService.getLrEntity(E4.class);

        LrPointer pointer = new PointerParser(pointerService).getPointer(e4, Pointers.PATH_SEPARATOR);

        PointerContext context = new CayennePointerContext(cayenneService, Collections.singletonList(pointer));
        pointer.update(context, new Object[]{});
        context.commitChanges();

        List<E4> list = ObjectSelect.query(E4.class).select(cayenneService.newContext());
        assertEquals(0, list.size());
    }

    @Test
    public void testUpdate_AttributePointer() throws Exception {

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
        pointer.update(context, "abc");
        context.commitChanges();

        E4 updated = SelectById.query(E4.class, updatedId).selectOne(cayenneService.newContext());
        assertEquals("abc", updated.getCVarchar());

        E4 unmodified = SelectById.query(E4.class, unmodifiedId).selectOne(cayenneService.newContext());
        assertEquals("yyy", unmodified.getCVarchar());
    }

    @Test
    public void testUpdate_ToOneRelationshipPointer_Implicit() throws Exception {

        SQLTemplate insertE2_1 = new SQLTemplate(E2.class,
				"INSERT INTO utest.e2 (id, name) values (2, 'yyy')");
        SQLTemplate insertE2_2 = new SQLTemplate(E2.class,
				"INSERT INTO utest.e2 (id, name) values (4, 'www')");
        SQLTemplate insertE3 = new SQLTemplate(E3.class,
				"INSERT INTO utest.e3 (id, name, e2_id) values (1, 'xxx', 2)");
		newContext().performGenericQuery(insertE2_1);
        newContext().performGenericQuery(insertE2_2);
        newContext().performGenericQuery(insertE3);

        E3 baseObject = newContext().selectOne(SelectQuery.query(E3.class, E3.NAME.eq("xxx")));
        E2 newRelatedObject = newContext().selectOne(SelectQuery.query(E2.class, E2.NAME.eq("www")));

        LrEntity<E3> e3 = metadataService.getLrEntity(E3.class);

        LrPointer pointer = new PointerParser(pointerService).getPointer(e3, E3.E2.getName());

        PointerContext context = new CayennePointerContext(cayenneService, Collections.singletonList(pointer));
        pointer.update(context, baseObject, newRelatedObject);
        context.commitChanges();

        E3 updatedE3 = newContext().selectOne(SelectQuery.query(E3.class, E3.NAME.eq("xxx")));
        E2 oldE2 = newContext().selectOne(SelectQuery.query(E2.class, E2.NAME.eq("yyy")));
        E2 newE2 = newContext().selectOne(SelectQuery.query(E2.class, E2.NAME.eq("www")));

        assertEquals(newE2.getName(), updatedE3.getE2().getName());
        assertTrue(oldE2.getE3s().isEmpty());
    }

    @Test
    public void testUpdate_ToOneRelationshipPointer_Explicit() throws Exception {

        Integer removedId = 2;

        SQLTemplate insertE2_1 = new SQLTemplate(E2.class,
				"INSERT INTO utest.e2 (id, name) values (" + removedId + ", 'yyy')");
        SQLTemplate insertE2_2 = new SQLTemplate(E2.class,
				"INSERT INTO utest.e2 (id, name) values (4, 'www')");
        SQLTemplate insertE3 = new SQLTemplate(E3.class,
				"INSERT INTO utest.e3 (id, name, e2_id) values (1, 'xxx', 2)");
		newContext().performGenericQuery(insertE2_1);
        newContext().performGenericQuery(insertE2_2);
        newContext().performGenericQuery(insertE3);

        E3 baseObject = newContext().selectOne(SelectQuery.query(E3.class, E3.NAME.eq("xxx")));
        E2 newRelatedObject = newContext().selectOne(SelectQuery.query(E2.class, E2.NAME.eq("www")));

        LrEntity<E3> e3 = metadataService.getLrEntity(E3.class);

        LrPointer pointer = new PointerParser(pointerService).getPointer(e3,
                Pointers.buildRelationship(E3.E2.getName(), removedId));

        PointerContext context = new CayennePointerContext(cayenneService, Collections.singletonList(pointer));
        pointer.update(context, baseObject, newRelatedObject);
        context.commitChanges();

        E3 updatedE3 = newContext().selectOne(SelectQuery.query(E3.class, E3.NAME.eq("xxx")));
        E2 oldE2 = newContext().selectOne(SelectQuery.query(E2.class, E2.NAME.eq("yyy")));
        E2 newE2 = newContext().selectOne(SelectQuery.query(E2.class, E2.NAME.eq("www")));

        assertEquals(newE2.getName(), updatedE3.getE2().getName());
        assertTrue(oldE2.getE3s().isEmpty());
    }

    @Test
    public void testUpdate_ToManyRelationship_CollectionPointer() throws Exception {

        SQLTemplate insertE2 = new SQLTemplate(E2.class,
				"INSERT INTO utest.e2 (id, name) values (2, 'yyy')");
        SQLTemplate insertE3_1 = new SQLTemplate(E3.class,
				"INSERT INTO utest.e3 (id, name, e2_id) values (1, 'xxx', 2)");
        SQLTemplate insertE3_2 = new SQLTemplate(E3.class,
				"INSERT INTO utest.e3 (id, name) values (3, 'zzz')");
		newContext().performGenericQuery(insertE2);
        newContext().performGenericQuery(insertE3_1);
        newContext().performGenericQuery(insertE3_2);

        E2 baseObject = newContext().selectOne(SelectQuery.query(E2.class, E2.NAME.eq("yyy")));
        E3 newRelatedObject = newContext().selectOne(SelectQuery.query(E3.class, E3.NAME.eq("zzz")));

        LrEntity<E2> e2 = metadataService.getLrEntity(E2.class);

        LrPointer pointer = new PointerParser(pointerService).getPointer(e2, E2.E3S.getName());

        PointerContext context = new CayennePointerContext(cayenneService, Collections.singletonList(pointer));
        pointer.update(context, baseObject, newRelatedObject);
        context.commitChanges();

        E2 updatedE2 = newContext().selectOne(SelectQuery.query(E2.class, E2.NAME.eq("yyy")));

        assertEquals(2, updatedE2.getE3s().size());
    }

    @Test(expected = Exception.class)
    public void testUpdate_ToManyRelationship_CollectionPointer_AddTransientObject() throws Exception {

        SQLTemplate insertE2 = new SQLTemplate(E2.class,
				"INSERT INTO utest.e2 (id, name) values (2, 'yyy')");
		newContext().performGenericQuery(insertE2);

        E2 baseObject = newContext().selectOne(SelectQuery.query(E2.class, E2.NAME.eq("yyy")));
        E3 newRelatedObject = new E3();
        newRelatedObject.setName("abc");

        LrEntity<E2> e2 = metadataService.getLrEntity(E2.class);

        LrPointer pointer = new PointerParser(pointerService).getPointer(e2, E2.E3S.getName());

        PointerContext context = new CayennePointerContext(cayenneService, Collections.singletonList(pointer));
        pointer.update(context, baseObject, newRelatedObject);
    }

    @Test
    public void testUpdate_ToManyRelationship_CollectionPointer_EmptyList() throws Exception {

        SQLTemplate insertE2 = new SQLTemplate(E2.class,
				"INSERT INTO utest.e2 (id, name) values (2, 'yyy')");
        SQLTemplate insertE3_1 = new SQLTemplate(E3.class,
				"INSERT INTO utest.e3 (id, name, e2_id) values (1, 'xxx', 2)");
        SQLTemplate insertE3_2 = new SQLTemplate(E3.class,
				"INSERT INTO utest.e3 (id, name) values (3, 'zzz')");
		newContext().performGenericQuery(insertE2);
        newContext().performGenericQuery(insertE3_1);
        newContext().performGenericQuery(insertE3_2);

        E2 baseObject = newContext().selectOne(SelectQuery.query(E2.class, E2.NAME.eq("yyy")));

        LrEntity<E2> e2 = metadataService.getLrEntity(E2.class);

        LrPointer pointer = new PointerParser(pointerService).getPointer(e2, E2.E3S.getName());

        PointerContext context = new CayennePointerContext(cayenneService, Collections.singletonList(pointer));
        pointer.update(context, baseObject, new ArrayList());
        context.commitChanges();

        E2 updatedE2 = newContext().selectOne(SelectQuery.query(E2.class, E2.NAME.eq("yyy")));

        assertEquals(0, updatedE2.getE3s().size());
    }

    @Test
    public void testUpdate_ToManyRelationship_InstancePointer() throws Exception {

        Integer replacedId = 1;

        SQLTemplate insertE2 = new SQLTemplate(E2.class,
				"INSERT INTO utest.e2 (id, name) values (2, 'yyy')");
        SQLTemplate insertE3_1 = new SQLTemplate(E3.class,
				"INSERT INTO utest.e3 (id, name, e2_id) values (" + replacedId + ", 'xxx', 2)");
        SQLTemplate insertE3_2 = new SQLTemplate(E3.class,
				"INSERT INTO utest.e3 (id, name) values (3, 'zzz')");
		newContext().performGenericQuery(insertE2);
        newContext().performGenericQuery(insertE3_1);
        newContext().performGenericQuery(insertE3_2);

        E2 baseObject = newContext().selectOne(SelectQuery.query(E2.class, E2.NAME.eq("yyy")));
        E3 newRelatedObject = newContext().selectOne(SelectQuery.query(E3.class, E3.NAME.eq("zzz")));

        LrEntity<E2> e2 = metadataService.getLrEntity(E2.class);

        LrPointer pointer = new PointerParser(pointerService).getPointer(e2,
                Pointers.buildRelationship(E2.E3S.getName(), replacedId));

        PointerContext context = new CayennePointerContext(cayenneService, Collections.singletonList(pointer));
        pointer.update(context, baseObject, newRelatedObject);
        context.commitChanges();

        E2 updatedE2 = newContext().selectOne(SelectQuery.query(E2.class, E2.NAME.eq("yyy")));

        assertEquals(1, updatedE2.getE3s().size());
        assertEquals("zzz", updatedE2.getE3s().get(0).getName());
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
