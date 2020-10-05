package io.agrest.runtime.entity;

import io.agrest.NestedResourceEntity;
import io.agrest.ResourceEntity;
import io.agrest.RootResourceEntity;
import io.agrest.annotation.AgAttribute;
import io.agrest.annotation.AgId;
import io.agrest.annotation.AgRelationship;
import io.agrest.base.protocol.Include;
import io.agrest.meta.AgEntity;
import io.agrest.meta.AgEntityOverlay;
import io.agrest.meta.compiler.AgEntityCompiler;
import io.agrest.meta.compiler.PojoEntityCompiler;
import io.agrest.resolver.ThrowingNestedDataResolver;
import io.agrest.runtime.meta.IMetadataService;
import io.agrest.runtime.meta.MetadataService;
import io.agrest.runtime.path.IPathDescriptorManager;
import io.agrest.runtime.path.PathDescriptorManager;
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

    private IMetadataService metadataService;
    private IncludeMerger includeMerger;

    @BeforeEach
    public void setUp() {

        AgEntityCompiler compiler = new PojoEntityCompiler(Collections.emptyMap());
        this.metadataService = new MetadataService(Collections.singletonList(compiler));

        IPathDescriptorManager pathCache = new PathDescriptorManager();
        ICayenneExpMerger expMerger = new CayenneExpMerger(new ExpressionParser(), new ExpressionPostProcessor(pathCache));
        ISortMerger sortMerger = new SortMerger(pathCache);
        IMapByMerger mapByMerger = new MapByMerger(metadataService);
        ISizeMerger sizeMerger = new SizeMerger();
        this.includeMerger = new IncludeMerger(metadataService, expMerger, sortMerger, mapByMerger, sizeMerger);
    }

    @Test
    public void testMergeNothing() {

        AgEntity<X> entity = metadataService.getAgEntity(X.class);
        ResourceEntity<X> root = new RootResourceEntity<>(entity, null);
        includeMerger.merge(root, asList(), Collections.emptyMap());

        assertEquals(entity.getAttributes().size(), root.getAttributes().size());
        assertTrue(root.isIdIncluded());
        assertEquals(0, root.getChildren().size());
    }

    @Test
    public void testMergeAttributes() {

        AgEntity<X> entity = metadataService.getAgEntity(X.class);
        ResourceEntity<X> root = new RootResourceEntity<>(entity, null);
        includeMerger.merge(root, asList(new Include("name")), Collections.emptyMap());

        assertEquals(1, root.getAttributes().size());
        assertFalse(root.isIdIncluded());
        assertEquals(0, root.getChildren().size());
    }

    @Test
    public void testMergeAttributesAndRelationships() {

        AgEntity<X> entity = metadataService.getAgEntity(X.class);
        ResourceEntity<X> root = new RootResourceEntity<>(entity, null);
        includeMerger.merge(root, asList(new Include("name"), new Include("ys")), Collections.emptyMap());

        assertEquals(1, root.getAttributes().size());
        assertFalse(root.isIdIncluded());
        assertEquals(1, root.getChildren().size());
    }

    @Test
    public void testMerge_AttributesAndRelationships_OverlappedPath() {

        AgEntity<X> entity = metadataService.getAgEntity(X.class);
        ResourceEntity<X> root = new RootResourceEntity<>(entity, null);
        includeMerger.merge(root, asList(
                new Include("name"),
                new Include("ys.name"),
                new Include("ys.z")
        ), Collections.emptyMap());

        assertEquals(1, root.getAttributes().size());
        assertFalse(root.isIdIncluded());
        assertEquals(1, root.getChildren().size());

        NestedResourceEntity<?> yChild = root.getChild("ys");
        assertNotNull(yChild);
        assertEquals(1, yChild.getAttributes().size());
        assertFalse(yChild.isIdIncluded());
        assertEquals(1, yChild.getChildren().size());

        NestedResourceEntity<?> zChild = yChild.getChild("z");
        assertNotNull(zChild);
        assertEquals(metadataService.getAgEntity(Z.class).getAttributes().size(), zChild.getAttributes().size());
        assertTrue(zChild.isIdIncluded());
        assertEquals(0, zChild.getChildren().size());
    }

    @Test
    public void testMerge_AttributesAndRelationships_OverlappedPath_Overlays() {

        Map<Class<?>, AgEntityOverlay<?>> overlays = new HashMap<>();
        overlays.put(X.class, AgEntity
                .overlay(X.class)
                .redefineRelationshipResolver("ys", (t, r) -> ThrowingNestedDataResolver.getInstance()));

        overlays.put(Y.class, AgEntity
                .overlay(Y.class)
                .redefineRelationshipResolver("z", (t, r) -> ThrowingNestedDataResolver.getInstance()));

        AgEntity<X> entity = metadataService.getAgEntity(X.class);
        ResourceEntity<X> root = new RootResourceEntity<>(entity, (AgEntityOverlay<X>) overlays.get(X.class));

        includeMerger.merge(root, asList(
                new Include("name"),
                new Include("ys.name"),
                new Include("ys.z")
        ), overlays);

        assertEquals(1, root.getAttributes().size());
        assertFalse(root.isIdIncluded());
        assertEquals(1, root.getChildren().size());

        NestedResourceEntity<?> yChild = root.getChild("ys");
        assertNotNull(yChild);
        assertEquals(1, yChild.getAttributes().size());
        assertFalse(yChild.isIdIncluded());
        assertEquals(1, yChild.getChildren().size());

        NestedResourceEntity<?> zChild = yChild.getChild("z");
        assertNotNull(zChild);
        assertEquals(metadataService.getAgEntity(Z.class).getAttributes().size(), zChild.getAttributes().size());
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
