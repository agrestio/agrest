package io.agrest.runtime.entity;

import io.agrest.RelatedResourceEntity;
import io.agrest.ResourceEntity;
import io.agrest.RootResourceEntity;
import io.agrest.annotation.AgAttribute;
import io.agrest.annotation.AgId;
import io.agrest.annotation.AgRelationship;
import io.agrest.compiler.AgEntityCompiler;
import io.agrest.compiler.AnnotationsAgEntityCompiler;
import io.agrest.meta.AgEntity;
import io.agrest.meta.AgEntityOverlay;
import io.agrest.meta.AgSchema;
import io.agrest.meta.LazySchema;
import io.agrest.protocol.Include;
import io.agrest.resolver.ThrowingRelatedDataResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.*;

public class IncludeMergerTest {

    private AgSchema schema;
    private IncludeMerger includeMerger;

    @BeforeEach
    public void setUp() {

        AgEntityCompiler compiler = new AnnotationsAgEntityCompiler(Collections.emptyMap());
        this.schema = new LazySchema(List.of(compiler));

        IExpMerger expMerger = new ExpMerger();
        ISortMerger sortMerger = new SortMerger();
        IMapByMerger mapByMerger = new MapByMerger(schema);
        ISizeMerger sizeMerger = new SizeMerger();
        this.includeMerger = new IncludeMerger(schema, expMerger, sortMerger, mapByMerger, sizeMerger);
    }

    @Test
    public void testMergeNothing() {

        AgEntity<X> entity = schema.getEntity(X.class);
        ResourceEntity<X> root = new RootResourceEntity<>(entity);
        includeMerger.merge(root, asList(), Collections.emptyMap(), 100);

        assertEquals(entity.getAttributes().size(), root.getBaseProjection().getAttributes().size());
        assertTrue(root.isIdIncluded());
        assertEquals(0, root.getChildren().size());
    }

    @Test
    public void testMergeAttributes() {

        AgEntity<X> entity = schema.getEntity(X.class);
        ResourceEntity<X> root = new RootResourceEntity<>(entity);
        includeMerger.merge(root, asList(new Include("name")), Collections.emptyMap(), 100);

        assertEquals(1, root.getBaseProjection().getAttributes().size());
        assertFalse(root.isIdIncluded());
        assertEquals(0, root.getChildren().size());
    }

    @Test
    public void testMergeAttributesAndRelationships() {

        AgEntity<X> entity = schema.getEntity(X.class);
        ResourceEntity<X> root = new RootResourceEntity<>(entity);
        includeMerger.merge(root, asList(new Include("name"), new Include("ys")), Collections.emptyMap(), 100);

        assertEquals(1, root.getBaseProjection().getAttributes().size());
        assertFalse(root.isIdIncluded());
        assertEquals(1, root.getChildren().size());
    }

    @Test
    public void testMerge_AttributesAndRelationships_OverlappedPath() {

        AgEntity<X> entity = schema.getEntity(X.class);
        ResourceEntity<X> root = new RootResourceEntity<>(entity);
        includeMerger.merge(root, asList(
                new Include("name"),
                new Include("ys.name"),
                new Include("ys.z")
        ), Collections.emptyMap(), 100);

        assertEquals(1, root.getBaseProjection().getAttributes().size());
        assertFalse(root.isIdIncluded());
        assertEquals(1, root.getChildren().size());

        RelatedResourceEntity<?> yChild = root.getChild("ys");
        assertNotNull(yChild);
        assertEquals(1, yChild.getBaseProjection().getAttributes().size());
        assertFalse(yChild.isIdIncluded());
        assertEquals(1, yChild.getChildren().size());

        RelatedResourceEntity<?> zChild = yChild.getChild("z");
        assertNotNull(zChild);
        assertEquals(schema.getEntity(Z.class).getAttributes().size(), zChild.getBaseProjection().getAttributes().size());
        assertTrue(zChild.isIdIncluded());
        assertEquals(0, zChild.getChildren().size());
    }

    @Test
    public void testMerge_AttributesAndRelationships_OverlappedPath_Overlays() {

        Map<Class<?>, AgEntityOverlay<?>> overlays = new HashMap<>();
        overlays.put(X.class, AgEntity
                .overlay(X.class)
                .relatedDataResolver("ys", (t, r) -> ThrowingRelatedDataResolver.getInstance()));

        overlays.put(Y.class, AgEntity
                .overlay(Y.class)
                .relatedDataResolver("z", (t, r) -> ThrowingRelatedDataResolver.getInstance()));

        AgEntity<X> entity = schema.getEntity(X.class);
        AgEntity<X> entityOverlaid = entity.resolveOverlay(schema, (AgEntityOverlay<X>) overlays.get(X.class));
        ResourceEntity<X> root = new RootResourceEntity<>(entityOverlaid);

        includeMerger.merge(root, asList(
                new Include("name"),
                new Include("ys.name"),
                new Include("ys.z")
        ), overlays, 100);

        assertEquals(1, root.getBaseProjection().getAttributes().size());
        assertFalse(root.isIdIncluded());
        assertEquals(1, root.getChildren().size());

        RelatedResourceEntity<?> yChild = root.getChild("ys");
        assertNotNull(yChild);
        assertEquals(1, yChild.getBaseProjection().getAttributes().size());
        assertFalse(yChild.isIdIncluded());
        assertEquals(1, yChild.getChildren().size());

        RelatedResourceEntity<?> zChild = yChild.getChild("z");
        assertNotNull(zChild);
        assertEquals(schema.getEntity(Z.class).getAttributes().size(), zChild.getBaseProjection().getAttributes().size());
        assertTrue(zChild.isIdIncluded());
        assertEquals(0, zChild.getChildren().size());
    }

    public static class X {

        private int id;
        private LocalDate date;
        private String name;
        private List<Y> ys;

        @AgId
        public int getId() {
            return id;
        }

        @AgAttribute
        public LocalDate getDate() {
            return date;
        }

        @AgAttribute
        public String getName() {
            return name;
        }

        @AgRelationship
        public List<Y> getYs() {
            return ys;
        }
    }

    public static class Y {

        private String name;
        private String phoneNumber;
        private Z z;

        @AgAttribute
        public String getName() {
            return name;
        }

        @AgAttribute
        public String getPhoneNumber() {
            return phoneNumber;
        }

        @AgRelationship
        public Z getZ() {
            return z;
        }
    }

    public static class Z {

        private int id;
        private String name;
        private A a;

        @AgId
        public int getId() {
            return id;
        }

        @AgAttribute
        public String getName() {
            return name;
        }

        @AgRelationship
        public A getA() {
            return a;
        }
    }

    public static class A {
        private int id;

        @AgId
        public int getId() {
            return id;
        }
    }
}
