package io.agrest.runtime;

import io.agrest.NestedResourceEntity;
import io.agrest.it.fixture.cayenne.E1;
import io.agrest.it.fixture.cayenne.E2;
import io.agrest.it.fixture.cayenne.E3;
import io.agrest.it.fixture.cayenne.E5;
import io.agrest.it.fixture.pojo.model.P1;
import io.agrest.meta.AgAttribute;
import io.agrest.meta.AgEntity;
import io.agrest.meta.AgRelationship;
import io.agrest.property.PropertyReader;
import io.agrest.resolver.NestedDataResolver;
import io.agrest.runtime.cayenne.processor.select.ViaQueryWithParentExpResolver;
import io.agrest.runtime.meta.IMetadataService;
import io.agrest.runtime.processor.select.SelectContext;
import io.agrest.unit.TestWithCayenneMapping;
import org.junit.Test;

import static org.junit.Assert.*;

public class AgBuilder_OverlayTest extends TestWithCayenneMapping {

    @Test
    public void testOverlay_RedefineAttribute_New() {
        AgRuntime runtime = new AgBuilder()
                .cayenneService(mockCayennePersister)
                .entityOverlay(AgEntity.overlay(E3.class).redefineAttribute("adHoc", Integer.class, e3 -> 2))
                .build();

        E3 e3 = new E3();
        e3.setName("aname");

        AgEntity<E3> e3Entity = runtime.service(IMetadataService.class).getAgEntity(E3.class);
        assertNotNull(e3Entity);

        AgAttribute e3_AdHoc = e3Entity.getAttribute("adHoc");
        assertNotNull(e3_AdHoc);
        assertEquals(Integer.class, e3_AdHoc.getType());
        assertEquals(2, e3_AdHoc.getPropertyReader().value(e3, "adHoc"));

        AgAttribute e3_Unchanged = e3Entity.getAttribute("name");
        assertNotNull(e3_Unchanged);
        assertEquals(String.class, e3_Unchanged.getType());
        assertEquals("aname", e3_Unchanged.getPropertyReader().value(e3, "name"));
    }

    @Test
    public void testOverlay_RedefineAttribute_Replace() {
        AgRuntime runtime = new AgBuilder()
                .cayenneService(mockCayennePersister)
                .entityOverlay(AgEntity.overlay(E3.class).redefineAttribute("phoneNumber", Long.class, e3 -> Long.valueOf(e3.getPhoneNumber())))
                .build();

        E3 e3 = new E3();
        e3.setName("aname");
        e3.setPhoneNumber("3333333");

        AgEntity<E3> e3Entity = runtime.service(IMetadataService.class).getAgEntity(E3.class);
        assertNotNull(e3Entity);

        AgAttribute e3_Replaced = e3Entity.getAttribute("phoneNumber");
        assertNotNull(e3_Replaced);
        assertEquals(Long.class, e3_Replaced.getType());
        assertEquals(3_333_333L, e3_Replaced.getPropertyReader().value(e3, "phoneNumber"));

        AgAttribute e3_Unchanged = e3Entity.getAttribute("name");
        assertNotNull(e3_Unchanged);
        assertEquals(String.class, e3_Unchanged.getType());
        assertEquals("aname", e3_Unchanged.getPropertyReader().value(e3, "name"));
    }

    @Test
    public void testOverlay_RedefineRelationshipResolver_Replace() {

        NestedDataResolver<?> resolver = new TestNestedDataResolver();

        AgRuntime runtime = new AgBuilder()
                .cayenneRuntime(TestWithCayenneMapping.runtime)
                .entityOverlay(AgEntity.overlay(E3.class).redefineRelationshipResolver("e2", (t, n) -> resolver))
                .build();

        IMetadataService metadata = runtime.service(IMetadataService.class);

        AgEntity<E3> e3Entity = metadata.getAgEntity(E3.class);
        assertNotNull(e3Entity);

        AgRelationship replaced = e3Entity.getRelationship("e2");
        assertNotNull(replaced);
        assertSame(metadata.getAgEntity(E2.class), replaced.getTargetEntity());
        assertSame(resolver, replaced.getResolver());
        assertFalse(replaced.isToMany());

        AgRelationship unchanged = e3Entity.getRelationship("e5");
        assertNotNull(unchanged);
        assertSame(metadata.getAgEntity(E5.class), unchanged.getTargetEntity());
        assertNotSame(resolver, unchanged.getResolver());
        assertTrue(unchanged.getResolver() instanceof ViaQueryWithParentExpResolver);

        assertFalse(unchanged.isToMany());
    }

    @Test
    public void testOverlay_RedefineRelationshipResolver_New() {

        NestedDataResolver<?> resolver = new TestNestedDataResolver();

        AgRuntime runtime = new AgBuilder()
                .cayenneRuntime(TestWithCayenneMapping.runtime)
                .entityOverlay(AgEntity.overlay(E3.class).redefineRelationshipResolver("adHoc", (t, n) -> resolver))
                .build();

        IMetadataService metadata = runtime.service(IMetadataService.class);

        AgEntity<E3> e3Entity = metadata.getAgEntity(E3.class);
        assertNotNull(e3Entity);

        AgRelationship adHoc = e3Entity.getRelationship("adHoc");
        assertNull(adHoc);

        AgRelationship unchanged = e3Entity.getRelationship("e5");
        assertNotNull(unchanged);
        assertSame(metadata.getAgEntity(E5.class), unchanged.getTargetEntity());
        assertNotSame(resolver, unchanged.getResolver());
        assertTrue(unchanged.getResolver() instanceof ViaQueryWithParentExpResolver);
        assertFalse(unchanged.isToMany());
    }

    @Test
    public void testOverlay_RedefineToMany_Replace() {

        NestedDataResolver<Object> resolver = new TestNestedDataResolver<>();

        AgRuntime runtime = new AgBuilder()
                .cayenneRuntime(TestWithCayenneMapping.runtime)
                // just for kicks redefine to-one as to-many, and change its target
                .entityOverlay(AgEntity.overlay(E3.class).redefineToMany("e2", E1.class, (t, n) -> resolver))
                .build();

        IMetadataService metadata = runtime.service(IMetadataService.class);

        AgEntity<E3> e3Entity = metadata.getAgEntity(E3.class);
        assertNotNull(e3Entity);

        AgRelationship replaced = e3Entity.getRelationship("e2");
        assertNotNull(replaced);
        assertSame(metadata.getAgEntity(E1.class), replaced.getTargetEntity());
        assertSame(resolver, replaced.getResolver());
        assertTrue(replaced.isToMany());

        AgRelationship unchanged = e3Entity.getRelationship("e5");
        assertNotNull(unchanged);
        assertSame(metadata.getAgEntity(E5.class), unchanged.getTargetEntity());
        assertNotSame(resolver, unchanged.getResolver());
        assertTrue(unchanged.getResolver() instanceof ViaQueryWithParentExpResolver);
        assertFalse(unchanged.isToMany());
    }

    @Test
    public void testOverlay_RedefineToOne_New() {

        NestedDataResolver<P1> resolver = new TestNestedDataResolver<>();

        AgRuntime runtime = new AgBuilder()
                .cayenneRuntime(TestWithCayenneMapping.runtime)
                .entityOverlay(AgEntity.overlay(E3.class).redefineToOne("adHoc", P1.class, (t, n) -> resolver))
                .build();

        IMetadataService metadata = runtime.service(IMetadataService.class);

        AgEntity<E3> e3Entity = metadata.getAgEntity(E3.class);
        assertNotNull(e3Entity);

        AgRelationship created = e3Entity.getRelationship("adHoc");
        assertNotNull(created);
        assertSame(metadata.getAgEntity(P1.class), created.getTargetEntity());
        assertSame(resolver, created.getResolver());
        assertFalse(created.isToMany());

        AgRelationship unchanged = e3Entity.getRelationship("e5");
        assertNotNull(unchanged);
        assertSame(metadata.getAgEntity(E5.class), unchanged.getTargetEntity());
        assertNotSame(resolver, unchanged.getResolver());
        assertTrue(unchanged.getResolver() instanceof ViaQueryWithParentExpResolver);
        assertFalse(unchanged.isToMany());
    }

    static class TestNestedDataResolver<T> implements NestedDataResolver<T> {

        @Override
        public void onParentQueryAssembled(NestedResourceEntity<T> entity, SelectContext<?> context) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void onParentDataResolved(NestedResourceEntity<T> entity, Iterable<?> parentData, SelectContext<?> context) {
            throw new UnsupportedOperationException();
        }

        @Override
        public PropertyReader reader(NestedResourceEntity<T> entity) {
            throw new UnsupportedOperationException();
        }
    }
}
