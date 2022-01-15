package io.agrest.runtime;

import io.agrest.NestedResourceEntity;
import io.agrest.meta.AgAttribute;
import io.agrest.meta.AgDataMap;
import io.agrest.meta.AgEntity;
import io.agrest.meta.AgRelationship;
import io.agrest.pojo.model.P1;
import io.agrest.processor.ProcessingContext;
import io.agrest.property.PropertyReader;
import io.agrest.resolver.NestedDataResolver;
import io.agrest.resolver.ReaderBasedResolver;
import io.agrest.runtime.processor.select.SelectContext;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class AgBuilder_OverlayTest {

    @Test
    public void testOverlay_RedefineAttribute_New() {
        AgRuntime runtime = new AgBuilder()
                .entityOverlay(AgEntity.overlay(X.class).redefineAttribute("adHoc", Integer.class, true, true, e -> 2))
                .build();

        X x = new X();
        x.setName("aname");

        AgEntity<X> entity = runtime.service(AgDataMap.class).getEntity(X.class);
        assertNotNull(entity);

        AgAttribute x_adHoc = entity.getAttribute("adHoc");
        assertNotNull(x_adHoc);
        assertEquals(Integer.class, x_adHoc.getType());
        assertEquals(2, x_adHoc.getPropertyReader().value(x));

        AgAttribute x_Unchanged = entity.getAttribute("name");
        assertNotNull(x_Unchanged);
        assertEquals(String.class, x_Unchanged.getType());
        assertEquals("aname", x_Unchanged.getPropertyReader().value(x));
    }

    @Test
    public void testOverlay_RedefineAttribute_Replace() {
        AgRuntime runtime = new AgBuilder()
                .entityOverlay(AgEntity.overlay(X.class).redefineAttribute("phoneNumber", Long.class, x -> Long.valueOf(x.getPhoneNumber())))
                .build();

        X x = new X();
        x.setName("aname");
        x.setPhoneNumber("3333333");

        AgEntity<X> entity = runtime.service(AgDataMap.class).getEntity(X.class);
        assertNotNull(entity);

        AgAttribute replaced = entity.getAttribute("phoneNumber");
        assertNotNull(replaced);
        assertEquals(Long.class, replaced.getType());
        assertEquals(3_333_333L, replaced.getPropertyReader().value(x));

        AgAttribute unchanged = entity.getAttribute("name");
        assertNotNull(unchanged);
        assertEquals(String.class, unchanged.getType());
        assertEquals("aname", unchanged.getPropertyReader().value(x));
    }

    @Test
    public void testOverlay_RedefineRelationshipResolver_Replace() {

        NestedDataResolver<?> resolver = new TestNestedDataResolver<>();

        AgRuntime runtime = new AgBuilder()
                .entityOverlay(AgEntity.overlay(X.class).redefineRelationshipResolver("y", (t, n) -> resolver))
                .build();

        AgDataMap metadata = runtime.service(AgDataMap.class);

        AgEntity<X> entity = metadata.getEntity(X.class);
        assertNotNull(entity);

        AgRelationship replaced = entity.getRelationship("y");
        assertNotNull(replaced);
        assertSame(metadata.getEntity(Y.class), replaced.getTargetEntity());
        assertSame(resolver, replaced.getResolver());
        assertFalse(replaced.isToMany());

        AgRelationship unchanged = entity.getRelationship("z");
        assertNotNull(unchanged);
        assertSame(metadata.getEntity(Z.class), unchanged.getTargetEntity());
        assertNotSame(resolver, unchanged.getResolver());
        assertTrue(unchanged.getResolver() instanceof ReaderBasedResolver);
        assertFalse(unchanged.isToMany());
    }

    @Test
    public void testOverlay_RedefineRelationshipResolver_New() {

        NestedDataResolver<?> resolver = new TestNestedDataResolver<>();

        AgRuntime runtime = new AgBuilder()
                // this overlay is partial, as it is missing "targetType" property
                .entityOverlay(AgEntity.overlay(X.class).redefineRelationshipResolver("adHoc", (t, n) -> resolver))
                .build();

        AgDataMap metadata = runtime.service(AgDataMap.class);
        AgEntity<X> entity = metadata.getEntity(X.class);
        assertThrows(IllegalStateException.class, () -> entity.getRelationship("adHoc"));
    }

    @Test
    public void testOverlay_RedefineToMany_Replace() {

        NestedDataResolver<Object> resolver = new TestNestedDataResolver<>();

        AgRuntime runtime = new AgBuilder()
                // just for kicks redefine to-one as to-many, and change its target
                .entityOverlay(AgEntity.overlay(X.class).redefineToMany("y", A.class, (t, n) -> resolver))
                .build();

        AgDataMap metadata = runtime.service(AgDataMap.class);

        AgEntity<X> entity = metadata.getEntity(X.class);
        assertNotNull(entity);

        AgRelationship replaced = entity.getRelationship("y");
        assertNotNull(replaced);
        assertSame(metadata.getEntity(A.class), replaced.getTargetEntity());
        assertSame(resolver, replaced.getResolver());
        assertTrue(replaced.isToMany());

        AgRelationship unchanged = entity.getRelationship("z");
        assertNotNull(unchanged);
        assertSame(metadata.getEntity(Z.class), unchanged.getTargetEntity());
        assertNotSame(resolver, unchanged.getResolver());
        assertTrue(unchanged.getResolver() instanceof ReaderBasedResolver);
        assertFalse(unchanged.isToMany());
    }

    @Test
    public void testOverlay_RedefineToOne_New() {

        NestedDataResolver<P1> resolver = new TestNestedDataResolver<>();

        AgRuntime runtime = new AgBuilder()
                .entityOverlay(AgEntity.overlay(X.class).redefineToOne("adHoc", A.class, true, true, (t, n) -> resolver))
                .build();

        AgDataMap metadata = runtime.service(AgDataMap.class);

        AgEntity<X> entity = metadata.getEntity(X.class);
        assertNotNull(entity);

        AgRelationship created = entity.getRelationship("adHoc");
        assertNotNull(created);
        assertSame(metadata.getEntity(A.class), created.getTargetEntity());
        assertSame(resolver, created.getResolver());
        assertFalse(created.isToMany());

        AgRelationship unchanged = entity.getRelationship("z");
        assertNotNull(unchanged);
        assertSame(metadata.getEntity(Z.class), unchanged.getTargetEntity());
        assertNotSame(resolver, unchanged.getResolver());
        assertTrue(unchanged.getResolver() instanceof ReaderBasedResolver);
        assertFalse(unchanged.isToMany());
    }

    @Test
    public void testOverlay_Exclude() {
        AgRuntime runtime = new AgBuilder()
                .entityOverlay(AgEntity.overlay(X.class).readablePropFilter(b -> b.property("phoneNumber", false)))
                .entityOverlay(AgEntity.overlay(X.class).writablePropFilter(b -> b.property("phoneNumber", false)))
                .build();

        AgEntity<X> entity = runtime.service(AgDataMap.class).getEntity(X.class);
        AgAttribute phone = entity.getAttribute("phoneNumber");
        assertNotNull(phone);
        assertFalse(phone.isReadable());
        assertFalse(phone.isWritable());
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
        public PropertyReader reader(NestedResourceEntity<T> entity, ProcessingContext<?> context) {
            throw new UnsupportedOperationException();
        }
    }

    public static class X {

        private String name;
        private String phoneNumber;
        private Y y;
        private Z z;

        @io.agrest.annotation.AgAttribute
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        @io.agrest.annotation.AgAttribute
        public String getPhoneNumber() {
            return phoneNumber;
        }

        public void setPhoneNumber(String phoneNumber) {
            this.phoneNumber = phoneNumber;
        }

        @io.agrest.annotation.AgRelationship
        public Y getY() {
            return y;
        }

        public void setY(Y y) {
            this.y = y;
        }

        @io.agrest.annotation.AgRelationship
        public Z getZ() {
            return z;
        }

        public void setZ(Z z) {
            this.z = z;
        }
    }

    public static class Y {

    }

    public static class Z {

    }

    public static class A {

    }
}
