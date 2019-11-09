package io.agrest.runtime.entity;

import io.agrest.NestedResourceEntity;
import io.agrest.ResourceEntity;
import io.agrest.RootResourceEntity;
import io.agrest.it.fixture.cayenne.E2;
import io.agrest.it.fixture.cayenne.E3;
import io.agrest.it.fixture.cayenne.E5;
import io.agrest.meta.AgEntity;
import io.agrest.meta.AgEntityOverlay;
import io.agrest.protocol.Include;
import io.agrest.runtime.cayenne.CayenneResolvers;
import io.agrest.runtime.path.IPathDescriptorManager;
import io.agrest.runtime.path.PathDescriptorManager;
import io.agrest.unit.TestWithCayenneMapping;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.junit.Assert.*;

public class IncludeMergerTest extends TestWithCayenneMapping {

    private IncludeMerger includeMerger;

    @Before
    public void setUp() {

        IPathDescriptorManager pathCache = new PathDescriptorManager();
        ICayenneExpMerger expMerger = new CayenneExpMerger(new ExpressionParser(), new ExpressionPostProcessor(pathCache));
        ISortMerger sortMerger = new SortMerger(pathCache);
        IMapByMerger mapByMerger = new MapByMerger(metadataService);
        ISizeMerger sizeMerger = new SizeMerger();
        this.includeMerger = new IncludeMerger(metadataService, expMerger, sortMerger, mapByMerger, sizeMerger);
    }

    @Test
    public void testMergeNothing() {

        AgEntity<E5> e5 = getAgEntity(E5.class);
        ResourceEntity<E5> e5Root = new RootResourceEntity<>(e5, null);
        includeMerger.merge(e5Root, asList(), Collections.emptyMap());

        assertEquals(e5.getAttributes().size(), e5Root.getAttributes().size());
        assertTrue(e5Root.isIdIncluded());
        assertEquals(0, e5Root.getChildren().size());
    }

    @Test
    public void testMergeAttributes() {

        AgEntity<E5> e5 = getAgEntity(E5.class);
        ResourceEntity<E5> e5Root = new RootResourceEntity<>(e5, null);
        includeMerger.merge(e5Root, asList(new Include(E5.NAME.getName())), Collections.emptyMap());

        assertEquals(1, e5Root.getAttributes().size());
        assertFalse(e5Root.isIdIncluded());
        assertEquals(0, e5Root.getChildren().size());
    }

    @Test
    public void testMergeAttributesAndRelationships() {

        AgEntity<E5> e5 = getAgEntity(E5.class);
        ResourceEntity<E5> e5Root = new RootResourceEntity<>(e5, null);
        includeMerger.merge(e5Root, asList(
                new Include(E5.NAME.getName()),
                new Include(E5.E3S.getName())
        ), Collections.emptyMap());

        assertEquals(1, e5Root.getAttributes().size());
        assertFalse(e5Root.isIdIncluded());
        assertEquals(1, e5Root.getChildren().size());
    }

    @Test
    public void testMerge_AttributesAndRelationships_OverlappedPath() {

        AgEntity<E5> e5 = getAgEntity(E5.class);
        ResourceEntity<E5> e5Root = new RootResourceEntity<>(e5, null);
        includeMerger.merge(e5Root, asList(
                new Include(E5.NAME.getName()),
                new Include(E5.E3S.dot(E3.NAME).getName()),
                new Include(E5.E3S.dot(E3.E2).getName())
        ), Collections.emptyMap());

        assertEquals(1, e5Root.getAttributes().size());
        assertFalse(e5Root.isIdIncluded());
        assertEquals(1, e5Root.getChildren().size());

        NestedResourceEntity<?> e3Child = e5Root.getChild(E5.E3S.getName());
        assertNotNull(e3Child);
        assertEquals(1, e3Child.getAttributes().size());
        assertFalse(e3Child.isIdIncluded());
        assertEquals(1, e3Child.getChildren().size());

        NestedResourceEntity<?> e2Child = e3Child.getChild(E3.E2.getName());
        assertNotNull(e2Child);
        assertEquals(getAgEntity(E2.class).getAttributes().size(), e2Child.getAttributes().size());
        assertTrue(e2Child.isIdIncluded());
        assertEquals(0, e2Child.getChildren().size());
    }

    @Test
    public void testMerge_AttributesAndRelationships_OverlappedPath_Overlays() {

        Map<Class<?>, AgEntityOverlay<?>> overlays = new HashMap<>();
        overlays.put(E5.class, AgEntity
                .overlay(E5.class)
                .redefineRelationshipResolver(E5.E3S.getName(), CayenneResolvers.nestedViaQueryWithParentIds(mockCayennePersister)));

        overlays.put(E3.class, AgEntity
                .overlay(E3.class)
                .redefineRelationshipResolver(E3.E2.getName(), CayenneResolvers.nestedViaJointParentPrefetch()));

        AgEntity<E5> e5 = getAgEntity(E5.class);
        ResourceEntity<E5> e5Root = new RootResourceEntity<>(e5, (AgEntityOverlay<E5>) overlays.get(E5.class));
        includeMerger.merge(e5Root, asList(
                new Include(E5.NAME.getName()),
                new Include(E5.E3S.dot(E3.NAME).getName()),
                new Include(E5.E3S.dot(E3.E2).getName())
        ), overlays);

        assertEquals(1, e5Root.getAttributes().size());
        assertFalse(e5Root.isIdIncluded());
        assertEquals(1, e5Root.getChildren().size());

        NestedResourceEntity<?> e3Child = e5Root.getChild(E5.E3S.getName());
        assertNotNull(e3Child);
        assertEquals(1, e3Child.getAttributes().size());
        assertFalse(e3Child.isIdIncluded());
        assertEquals(1, e3Child.getChildren().size());

        NestedResourceEntity<?> e2Child = e3Child.getChild(E3.E2.getName());
        assertNotNull(e2Child);
        assertEquals(getAgEntity(E2.class).getAttributes().size(), e2Child.getAttributes().size());
        assertTrue(e2Child.isIdIncluded());
        assertEquals(0, e2Child.getChildren().size());
    }
}
