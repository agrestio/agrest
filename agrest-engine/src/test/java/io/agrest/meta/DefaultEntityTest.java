package io.agrest.meta;

import io.agrest.access.CreateAuthorizer;
import io.agrest.access.DeleteAuthorizer;
import io.agrest.access.ReadFilter;
import io.agrest.access.UpdateAuthorizer;
import io.agrest.resolver.RootDataResolver;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

public class DefaultEntityTest {

    final AgSchema mockSchema = mock(AgSchema.class);

    final AgEntity<O111> e111 = new DefaultEntity<>(
            "o111", O111.class, false,
            List.of(), Map.of(), Map.of(), Map.of(),
            mock(RootDataResolver.class),
            ReadFilter.allowsAllFilter(),
            CreateAuthorizer.allowsAllFilter(),
            UpdateAuthorizer.allowsAllFilter(),
            DeleteAuthorizer.allowsAllFilter()
    );

    final AgEntity<O11> e11 = new DefaultEntity<>(
            "o11", O11.class, false,
            List.of(e111), Map.of(), Map.of(), Map.of(),
            mock(RootDataResolver.class),
            ReadFilter.allowsAllFilter(),
            CreateAuthorizer.allowsAllFilter(),
            UpdateAuthorizer.allowsAllFilter(),
            DeleteAuthorizer.allowsAllFilter()
    );

    final AgEntity<O11> e12 = new DefaultEntity<>(
            "o12", O12.class, false,
            List.of(), Map.of(), Map.of(), Map.of(),
            mock(RootDataResolver.class),
            ReadFilter.allowsAllFilter(),
            CreateAuthorizer.allowsAllFilter(),
            UpdateAuthorizer.allowsAllFilter(),
            DeleteAuthorizer.allowsAllFilter()
    );


    final AgEntity<O1> e1 = new DefaultEntity<>(
            "o1", O1.class, false,
            List.of(e11, e12),
            Map.of(), Map.of(), Map.of(),
            mock(RootDataResolver.class),
            ReadFilter.allowsAllFilter(),
            CreateAuthorizer.allowsAllFilter(),
            UpdateAuthorizer.allowsAllFilter(),
            DeleteAuthorizer.allowsAllFilter()
    );

    @Test
    public void testResolveOverlayHierarchy_NoSubclasses() {
        assertSame(e111, e111.resolveOverlayHierarchy(mockSchema, Map.of()));
        assertSame(e111, e111.resolveOverlayHierarchy(mockSchema, Map.of(O111.class, AgEntity.overlay(O111.class))));

        AgEntityOverlay<O111> o1 = AgEntity.overlay(O111.class).attribute("oa1", String.class, o -> "oa1");
        AgEntity<O111> oe1 = e111.resolveOverlayHierarchy(mockSchema, Map.of(O111.class, o1));
        assertNotSame(e111, oe1);
        assertEquals("oa1", oe1.getAttributes().stream().map(AgAttribute::getName).collect(Collectors.joining(",")));
    }

    @Test
    public void testResolveOverlayHierarchy_RootChanges() {

        assertSame(e1, e1.resolveOverlayHierarchy(mockSchema, Map.of()));
        assertSame(e1, e1.resolveOverlayHierarchy(mockSchema, Map.of(O1.class, AgEntity.overlay(O1.class))));

        AgEntityOverlay<O1> o1 = AgEntity.overlay(O1.class).attribute("oa1", String.class, o -> "oa1");
        AgEntity<O1> oe1 = e1.resolveOverlayHierarchy(mockSchema, Map.of(O1.class, o1));
        assertNotSame(e1, oe1);
        assertEquals("oa1", oe1.getAttributes().stream().map(AgAttribute::getName).collect(Collectors.joining(",")));

        assertEquals(2, oe1.getSubEntities().size());
        assertEquals("o11,o12", oe1.getSubEntities().stream().map(AgEntity::getName).sorted().collect(Collectors.joining(",")));
        assertFalse(oe1.getSubEntities().contains(e11), "Child entity must have been overlaid with parent changes");
        assertFalse(oe1.getSubEntities().contains(e12), "Child entity must have been overlaid with parent changes");
    }

    @Test
    public void testResolveOverlayHierarchy_ChildChanges() {

        AgEntityOverlay<O111> o111 = AgEntity.overlay(O111.class).attribute("a111", String.class, o -> "a111");

        AgEntity<O1> oe1 = e1.resolveOverlayHierarchy(mockSchema, Map.of(O111.class, o111));
        assertNotSame(e1, oe1);

        assertEquals(2, oe1.getSubEntities().size());
        assertFalse(oe1.getSubEntities().contains(e11));
        assertTrue(oe1.getSubEntities().contains(e12));

        AgEntity<O11> oe11 = (AgEntity<O11>) oe1.getSubEntities().stream().filter(e -> "o11".equals(e.getName())).findFirst().get();
        assertNotSame(e11, oe11);
        assertEquals(1, oe11.getSubEntities().size());

        AgEntity<O11> oe111 = (AgEntity<O11>) oe11.getSubEntities().stream().filter(e -> "o111".equals(e.getName())).findFirst().get();
        assertNotSame(e111, oe111);

        assertEquals("a111", oe111.getAttributes().stream().map(AgAttribute::getName).collect(Collectors.joining(",")));
    }

    public static class O1 {
    }

    public static class O11 extends O1 {
    }

    public static class O12 extends O1 {
    }

    public static class O111 extends O11 {
    }
}
