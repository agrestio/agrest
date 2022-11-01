package io.agrest.meta;

import io.agrest.AgRequestBuilder;
import io.agrest.EntityUpdate;
import io.agrest.RootResourceEntity;
import io.agrest.access.CreateAuthorizer;
import io.agrest.access.DeleteAuthorizer;
import io.agrest.access.ReadFilter;
import io.agrest.access.UpdateAuthorizer;
import io.agrest.pojo.model.P1;
import io.agrest.reader.DataReader;
import io.agrest.resolver.RootDataResolver;
import io.agrest.runtime.processor.select.SelectContext;
import org.apache.cayenne.di.Injector;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

public class AgEntityOverlayTest {

    @Test
    public void testResolve_attribute() {
        RootDataResolver<P1> r1 = mock(RootDataResolver.class);

        AgEntity<P1> e = new DefaultEntity<>(
                "p1", P1.class,
                List.of(), Map.of(), Map.of(), Map.of(),
                r1,
                ReadFilter.allowsAllFilter(),
                CreateAuthorizer.allowsAllFilter(),
                UpdateAuthorizer.allowsAllFilter(),
                DeleteAuthorizer.allowsAllFilter()
        );

        AgEntityOverlay<P1> attributeOverlay = AgEntity.overlay(P1.class)
                .attribute("a1", String.class, P1::getName);

        AgEntity<P1> eo = attributeOverlay.resolve(mock(AgSchema.class), e);
        assertEquals("p1", eo.getName());
        assertEquals(P1.class, eo.getType());
        assertEquals(r1, eo.getDataResolver());
        assertEquals(1, eo.getAttributes().size());
    }

    @Test
    public void testResolve_attribute_throwingFunction() {
        RootDataResolver<P1> r1 = mock(RootDataResolver.class);

        AgEntity<P1> e = new DefaultEntity<>(
                "p1", P1.class,
                List.of(), Map.of(), Map.of(), Map.of(),
                r1,
                ReadFilter.allowsAllFilter(),
                CreateAuthorizer.allowsAllFilter(),
                UpdateAuthorizer.allowsAllFilter(),
                DeleteAuthorizer.allowsAllFilter()
        );

        Function<P1, String> throwing = p1 -> {
            throw new RuntimeException("testing this exception");
        };

        AgEntityOverlay<P1> attributeOverlay = AgEntity
                .overlay(P1.class)
                .attribute("a1", String.class, throwing);

        AgEntity<P1> eo = attributeOverlay.resolve(mock(AgSchema.class), e);
        DataReader reader = eo.getAttribute("a1").getDataReader();
        assertThrows(RuntimeException.class, () -> reader.read(new P1()));
    }

    @Test
    public void testResolve_RootResolver() {
        RootDataResolver<P1> r1 = mock(RootDataResolver.class);
        RootDataResolver<P1> r2 = mock(RootDataResolver.class);

        AgEntity<P1> e = new DefaultEntity<>(
                "p1", P1.class,
                List.of(), Map.of(), Map.of(), Map.of(),
                r1,
                ReadFilter.allowsAllFilter(),
                CreateAuthorizer.allowsAllFilter(),
                UpdateAuthorizer.allowsAllFilter(),
                DeleteAuthorizer.allowsAllFilter()
        );

        AgEntityOverlay<P1> resolverOnly = AgEntity.overlay(P1.class)
                .dataResolver(r2);

        AgEntity<P1> eo = resolverOnly.resolve(mock(AgSchema.class), e);
        assertEquals("p1", eo.getName());
        assertEquals(P1.class, eo.getType());
        assertEquals(r2, eo.getDataResolver());
    }

    @Test
    public void testResolve_RootResolverFunction() {

        AgEntity<P1> e = new DefaultEntity<>(
                "p1",
                P1.class,
                List.of(), Map.of(), Map.of(), Map.of(),
                mock(RootDataResolver.class),
                ReadFilter.allowsAllFilter(),
                CreateAuthorizer.allowsAllFilter(),
                UpdateAuthorizer.allowsAllFilter(),
                DeleteAuthorizer.allowsAllFilter()
        );

        List<P1> p1s = asList(new P1(), new P1());

        AgEntityOverlay<P1> resolverOnly = AgEntity.overlay(P1.class).dataResolver(c -> p1s);
        AgEntity<P1> eo = resolverOnly.resolve(mock(AgSchema.class), e);

        SelectContext<P1> context = new SelectContext<>(
                P1.class,
                mock(AgRequestBuilder.class),
                mock(Injector.class));
        context.setEntity(new RootResourceEntity<>(mock(AgEntity.class)));
        eo.getDataResolver().fetchData(context);

        assertSame(p1s, context.getEntity().getData());
    }

    @Test
    public void testResolve_readFilter() {

        AgEntity<P1> e = new DefaultEntity<>(
                "p1",
                P1.class,
                List.of(), Map.of(), Map.of(), Map.of(),
                mock(RootDataResolver.class),
                ReadFilter.allowsAllFilter(),
                CreateAuthorizer.allowsAllFilter(),
                UpdateAuthorizer.allowsAllFilter(),
                DeleteAuthorizer.allowsAllFilter()
        );

        AgEntityOverlay<P1> o = AgEntity.overlay(P1.class).readFilter(p1 -> p1.getName().startsWith("a"));

        ReadFilter<P1> f = o.resolve(mock(AgSchema.class), e).getReadFilter();
        P1 p11 = new P1();
        p11.setName("x");
        P1 p12 = new P1();
        p12.setName("a");

        assertFalse(f.isAllowed(p11));
        assertTrue(f.isAllowed(p12));
    }

    @Test
    public void testResolve_readFilter_Additivity() {

        AgEntity<P1> e = new DefaultEntity<>(
                "p1",
                P1.class,
                List.of(), Map.of(), Map.of(), Map.of(),
                mock(RootDataResolver.class),
                p1 -> p1.getName().startsWith("a"),
                CreateAuthorizer.allowsAllFilter(),
                UpdateAuthorizer.allowsAllFilter(),
                DeleteAuthorizer.allowsAllFilter()
        );

        AgEntityOverlay<P1> o = AgEntity.overlay(P1.class).readFilter(p1 -> p1.getName().endsWith("a"));

        ReadFilter<P1> f = o.resolve(mock(AgSchema.class), e).getReadFilter();
        P1 p11 = new P1();
        p11.setName("ax");

        P1 p12 = new P1();
        p12.setName("xa");

        P1 p13 = new P1();
        p13.setName("axa");

        assertFalse(f.isAllowed(p11));
        assertFalse(f.isAllowed(p12));
        assertTrue(f.isAllowed(p13));
    }

    @Test
    public void testResolve_readFilter_IgnoreOverlaid() {

        AgEntity<P1> e = new DefaultEntity<>(
                "p1",
                P1.class,
                List.of(), Map.of(), Map.of(), Map.of(),
                mock(RootDataResolver.class),
                p1 -> p1.getName().startsWith("a"),
                CreateAuthorizer.allowsAllFilter(),
                UpdateAuthorizer.allowsAllFilter(),
                DeleteAuthorizer.allowsAllFilter()
        );

        AgEntityOverlay<P1> o = AgEntity.overlay(P1.class)
                .ignoreOverlaidReadFilter()
                .readFilter(p1 -> p1.getName().endsWith("a"));

        ReadFilter<P1> f = o.resolve(mock(AgSchema.class), e).getReadFilter();
        P1 p11 = new P1();
        p11.setName("ax");

        P1 p12 = new P1();
        p12.setName("xa");

        P1 p13 = new P1();
        p13.setName("axa");

        assertFalse(f.isAllowed(p11));
        assertTrue(f.isAllowed(p12));
        assertTrue(f.isAllowed(p13));
    }

    @Test
    public void testResolve_readFilter_IgnoreOverlaid_NoOverlayFilter() {

        AgEntity<P1> e = new DefaultEntity<>(
                "p1",
                P1.class,
                List.of(), Map.of(), Map.of(), Map.of(),
                mock(RootDataResolver.class),
                p1 -> p1.getName().startsWith("a"),
                CreateAuthorizer.allowsAllFilter(),
                UpdateAuthorizer.allowsAllFilter(),
                DeleteAuthorizer.allowsAllFilter()
        );

        AgEntityOverlay<P1> o = AgEntity.overlay(P1.class).ignoreOverlaidReadFilter();

        ReadFilter<P1> f = o.resolve(mock(AgSchema.class), e).getReadFilter();
        P1 p11 = new P1();
        p11.setName("ax");

        P1 p12 = new P1();
        p12.setName("xa");

        P1 p13 = new P1();
        p13.setName("axa");

        assertTrue(f.isAllowed(p11));
        assertTrue(f.isAllowed(p12));
        assertTrue(f.isAllowed(p13));
    }

    @Test
    public void testResolve_createAuthorizer() {

        AgEntity<P1> e = new DefaultEntity<>(
                "p1",
                P1.class,
                List.of(), Map.of(), Map.of(), Map.of(),
                mock(RootDataResolver.class),
                ReadFilter.allowsAllFilter(),
                CreateAuthorizer.allowsAllFilter(),
                UpdateAuthorizer.allowsAllFilter(),
                DeleteAuthorizer.allowsAllFilter()
        );

        AgEntityOverlay<P1> o = AgEntity.overlay(P1.class).createAuthorizer(u -> ((String) u.getValues().get("name")).startsWith("a"));
        CreateAuthorizer<P1> f = o.resolve(mock(AgSchema.class), e).getCreateAuthorizer();

        EntityUpdate<P1> p11 = new EntityUpdate<>(mock(AgEntity.class));
        p11.getValues().put("name", "x");

        EntityUpdate<P1> p12 = new EntityUpdate<>(mock(AgEntity.class));
        p12.getValues().put("name", "a");

        assertFalse(f.isAllowed(p11));
        assertTrue(f.isAllowed(p12));
    }

    @Test
    public void testResolve_createAuthorizer_Additivity() {

        AgEntity<P1> e = new DefaultEntity<>(
                "p1",
                P1.class,
                List.of(), Map.of(), Map.of(), Map.of(),
                mock(RootDataResolver.class),
                ReadFilter.allowsAllFilter(),
                u -> ((String) u.getValues().get("name")).startsWith("a"),
                UpdateAuthorizer.allowsAllFilter(),
                DeleteAuthorizer.allowsAllFilter()
        );

        AgEntityOverlay<P1> o = AgEntity.overlay(P1.class).createAuthorizer(u -> ((String) u.getValues().get("name")).endsWith("a"));
        CreateAuthorizer<P1> f = o.resolve(mock(AgSchema.class), e).getCreateAuthorizer();

        EntityUpdate<P1> p11 = new EntityUpdate<>(mock(AgEntity.class));
        p11.getValues().put("name", "ax");

        EntityUpdate<P1> p12 = new EntityUpdate<>(mock(AgEntity.class));
        p12.getValues().put("name", "xa");

        EntityUpdate<P1> p13 = new EntityUpdate<>(mock(AgEntity.class));
        p13.getValues().put("name", "axa");

        assertFalse(f.isAllowed(p11));
        assertFalse(f.isAllowed(p12));
        assertTrue(f.isAllowed(p13));
    }

    @Test
    public void testResolve_createAuthorizer_IgnoreOverlaid() {

        AgEntity<P1> e = new DefaultEntity<>(
                "p1",
                P1.class,
                List.of(), Map.of(), Map.of(), Map.of(),
                mock(RootDataResolver.class),
                ReadFilter.allowsAllFilter(),
                u -> ((String) u.getValues().get("name")).startsWith("a"),
                UpdateAuthorizer.allowsAllFilter(),
                DeleteAuthorizer.allowsAllFilter()
        );

        AgEntityOverlay<P1> o = AgEntity.overlay(P1.class)
                .createAuthorizer(u -> ((String) u.getValues().get("name")).endsWith("a"))
                .ignoreOverlaidCreateAuthorizer();
        CreateAuthorizer<P1> f = o.resolve(mock(AgSchema.class), e).getCreateAuthorizer();

        EntityUpdate<P1> p11 = new EntityUpdate<>(mock(AgEntity.class));
        p11.getValues().put("name", "ax");

        EntityUpdate<P1> p12 = new EntityUpdate<>(mock(AgEntity.class));
        p12.getValues().put("name", "xa");

        EntityUpdate<P1> p13 = new EntityUpdate<>(mock(AgEntity.class));
        p13.getValues().put("name", "axa");

        assertFalse(f.isAllowed(p11));
        assertTrue(f.isAllowed(p12));
        assertTrue(f.isAllowed(p13));
    }

    @Test
    public void testResolve_updateAuthorizer() {

        AgEntity<P1> e = new DefaultEntity<>(
                "p1",
                P1.class,
                List.of(), Map.of(), Map.of(), Map.of(),
                mock(RootDataResolver.class),
                ReadFilter.allowsAllFilter(),
                CreateAuthorizer.allowsAllFilter(),
                UpdateAuthorizer.allowsAllFilter(),
                DeleteAuthorizer.allowsAllFilter()
        );

        AgEntityOverlay<P1> o = AgEntity.overlay(P1.class).updateAuthorizer((p1, u) -> ((String) u.getValues().get("name")).startsWith("a"));
        UpdateAuthorizer<P1> f = o.resolve(mock(AgSchema.class), e).getUpdateAuthorizer();

        EntityUpdate<P1> p11 = new EntityUpdate<>(mock(AgEntity.class));
        p11.getValues().put("name", "x");

        EntityUpdate<P1> p12 = new EntityUpdate<>(mock(AgEntity.class));
        p12.getValues().put("name", "a");

        assertFalse(f.isAllowed(mock(P1.class), p11));
        assertTrue(f.isAllowed(mock(P1.class), p12));
    }

    @Test
    public void testResolve_updateAuthorizer_Additivity() {

        AgEntity<P1> e = new DefaultEntity<>(
                "p1",
                P1.class,
                List.of(), Map.of(), Map.of(), Map.of(),
                mock(RootDataResolver.class),
                ReadFilter.allowsAllFilter(),
                CreateAuthorizer.allowsAllFilter(),
                (p1, u) -> ((String) u.getValues().get("name")).startsWith("a"),
                DeleteAuthorizer.allowsAllFilter()
        );

        AgEntityOverlay<P1> o = AgEntity.overlay(P1.class).updateAuthorizer((p1, u) -> ((String) u.getValues().get("name")).endsWith("a"));
        UpdateAuthorizer<P1> f = o.resolve(mock(AgSchema.class), e).getUpdateAuthorizer();

        EntityUpdate<P1> p11 = new EntityUpdate<>(mock(AgEntity.class));
        p11.getValues().put("name", "ax");

        EntityUpdate<P1> p12 = new EntityUpdate<>(mock(AgEntity.class));
        p12.getValues().put("name", "xa");

        EntityUpdate<P1> p13 = new EntityUpdate<>(mock(AgEntity.class));
        p13.getValues().put("name", "axa");

        assertFalse(f.isAllowed(mock(P1.class), p11));
        assertFalse(f.isAllowed(mock(P1.class), p12));
        assertTrue(f.isAllowed(mock(P1.class), p13));
    }

    @Test
    public void testResolve_updateAuthorizer_IgnoreOverlaid() {

        AgEntity<P1> e = new DefaultEntity<>(
                "p1",
                P1.class,
                List.of(), Map.of(), Map.of(), Map.of(),
                mock(RootDataResolver.class),
                ReadFilter.allowsAllFilter(),
                CreateAuthorizer.allowsAllFilter(),
                (p1, u) -> ((String) u.getValues().get("name")).startsWith("a"),
                DeleteAuthorizer.allowsAllFilter()
        );

        AgEntityOverlay<P1> o = AgEntity.overlay(P1.class)
                .ignoreOverlaidUpdateAuthorizer()
                .updateAuthorizer((p1, u) -> ((String) u.getValues().get("name")).endsWith("a"));
        UpdateAuthorizer<P1> f = o.resolve(mock(AgSchema.class), e).getUpdateAuthorizer();

        EntityUpdate<P1> p11 = new EntityUpdate<>(mock(AgEntity.class));
        p11.getValues().put("name", "ax");

        EntityUpdate<P1> p12 = new EntityUpdate<>(mock(AgEntity.class));
        p12.getValues().put("name", "xa");

        EntityUpdate<P1> p13 = new EntityUpdate<>(mock(AgEntity.class));
        p13.getValues().put("name", "axa");

        assertFalse(f.isAllowed(mock(P1.class), p11));
        assertTrue(f.isAllowed(mock(P1.class), p12));
        assertTrue(f.isAllowed(mock(P1.class), p13));
    }

    @Test
    public void testMergeResolve_updateAuthorizer_IgnoreOverlaid() {

        AgEntity<P1> e = new DefaultEntity<>(
                "p1",
                P1.class,
                List.of(), Map.of(), Map.of(), Map.of(),
                mock(RootDataResolver.class),
                ReadFilter.allowsAllFilter(),
                CreateAuthorizer.allowsAllFilter(),
                (p1, u) -> ((String) u.getValues().get("name")).startsWith("a"),
                DeleteAuthorizer.allowsAllFilter()
        );

        AgEntityOverlay<P1> o = AgEntity.overlay(P1.class)
                .ignoreOverlaidUpdateAuthorizer()
                .updateAuthorizer((p1, u) -> ((String) u.getValues().get("name")).endsWith("a"));

        AgEntityOverlay<P1> oMerged = AgEntity.overlay(P1.class).merge(o);
        UpdateAuthorizer<P1> f = oMerged.resolve(mock(AgSchema.class), e).getUpdateAuthorizer();

        EntityUpdate<P1> p11 = new EntityUpdate<>(mock(AgEntity.class));
        p11.getValues().put("name", "ax");

        EntityUpdate<P1> p12 = new EntityUpdate<>(mock(AgEntity.class));
        p12.getValues().put("name", "xa");

        EntityUpdate<P1> p13 = new EntityUpdate<>(mock(AgEntity.class));
        p13.getValues().put("name", "axa");

        assertFalse(f.isAllowed(mock(P1.class), p11));
        assertTrue(f.isAllowed(mock(P1.class), p12));
        assertTrue(f.isAllowed(mock(P1.class), p13));
    }

    @Test
    public void testResolve_deleteAuthorizer() {

        AgEntity<P1> e = new DefaultEntity<>(
                "p1",
                P1.class,
                List.of(), Map.of(), Map.of(), Map.of(),
                mock(RootDataResolver.class),
                ReadFilter.allowsAllFilter(),
                CreateAuthorizer.allowsAllFilter(),
                UpdateAuthorizer.allowsAllFilter(),
                DeleteAuthorizer.allowsAllFilter()
        );

        AgEntityOverlay<P1> o = AgEntity.overlay(P1.class).deleteAuthorizer(p1 -> p1.getName().startsWith("a"));
        DeleteAuthorizer<P1> f = o.resolve(mock(AgSchema.class), e).getDeleteAuthorizer();

        P1 p11 = new P1();
        p11.setName("x");
        P1 p12 = new P1();
        p12.setName("a");

        assertFalse(f.isAllowed(p11));
        assertTrue(f.isAllowed(p12));
    }

    @Test
    public void testResolve_deleteAuthorizer_Additivity() {

        AgEntity<P1> e = new DefaultEntity<>(
                "p1",
                P1.class,
                List.of(), Map.of(), Map.of(), Map.of(),
                mock(RootDataResolver.class),
                ReadFilter.allowsAllFilter(),
                CreateAuthorizer.allowsAllFilter(),
                UpdateAuthorizer.allowsAllFilter(),
                p1 -> p1.getName().startsWith("a")
        );

        AgEntityOverlay<P1> o = AgEntity.overlay(P1.class).deleteAuthorizer(p1 -> p1.getName().endsWith("a"));
        DeleteAuthorizer<P1> f = o.resolve(mock(AgSchema.class), e).getDeleteAuthorizer();

        P1 p11 = new P1();
        p11.setName("ax");

        P1 p12 = new P1();
        p12.setName("xa");

        P1 p13 = new P1();
        p13.setName("axa");

        assertFalse(f.isAllowed(p11));
        assertFalse(f.isAllowed(p12));
        assertTrue(f.isAllowed(p13));
    }

    @Test
    public void testResolve_deleteAuthorizer_IgnoreOverlaid() {

        AgEntity<P1> e = new DefaultEntity<>(
                "p1",
                P1.class,
                List.of(), Map.of(), Map.of(), Map.of(),
                mock(RootDataResolver.class),
                ReadFilter.allowsAllFilter(),
                CreateAuthorizer.allowsAllFilter(),
                UpdateAuthorizer.allowsAllFilter(),
                p1 -> p1.getName().startsWith("a")
        );

        AgEntityOverlay<P1> o = AgEntity.overlay(P1.class)
                .ignoreOverlaidDeleteAuthorizer()
                .deleteAuthorizer(p1 -> p1.getName().endsWith("a"));
        DeleteAuthorizer<P1> f = o.resolve(mock(AgSchema.class), e).getDeleteAuthorizer();

        P1 p11 = new P1();
        p11.setName("ax");

        P1 p12 = new P1();
        p12.setName("xa");

        P1 p13 = new P1();
        p13.setName("axa");

        assertFalse(f.isAllowed(p11));
        assertTrue(f.isAllowed(p12));
        assertTrue(f.isAllowed(p13));
    }

    @Test
    public void testResolve_propFilter_MakeInaccessible() {

        AgEntity<P1> e = new DefaultEntity<>(
                "p1",
                P1.class,
                List.of(), Map.of(),
                Map.of("name", new DefaultAttribute("name", String.class, true, true, o -> null)),
                Collections.emptyMap(),
                mock(RootDataResolver.class),
                ReadFilter.allowsAllFilter(),
                CreateAuthorizer.allowsAllFilter(),
                UpdateAuthorizer.allowsAllFilter(),
                DeleteAuthorizer.allowsAllFilter()
        );

        AgEntityOverlay<P1> o = AgEntity.overlay(P1.class)
                .readablePropFilter(b -> b.property("name", false))
                .writablePropFilter(b -> b.property("name", false));
        AgAttribute a = o.resolve(mock(AgSchema.class), e).getAttribute("name");
        assertFalse(a.isReadable());
        assertFalse(a.isWritable());
    }

    @Test
    public void testResolve_propFilter_MakeAccessible() {

        AgEntity<P1> e = new DefaultEntity<>(
                "p1",
                P1.class,
                List.of(), Map.of(),
                Map.of("name", new DefaultAttribute("name", String.class, false, false, o -> null)),
                Collections.emptyMap(),
                mock(RootDataResolver.class),
                ReadFilter.allowsAllFilter(),
                CreateAuthorizer.allowsAllFilter(),
                UpdateAuthorizer.allowsAllFilter(),
                DeleteAuthorizer.allowsAllFilter()
        );

        AgEntityOverlay<P1> o = AgEntity.overlay(P1.class)
                .readablePropFilter(b -> b.property("name", true))
                .writablePropFilter(b -> b.property("name", true));
        AgAttribute a = o.resolve(mock(AgSchema.class), e).getAttribute("name");
        assertTrue(a.isReadable());
        assertTrue(a.isWritable());
    }

    @Test
    public void testResolve_propFilter_MakeIdInAccessible() {

        AgEntity<P1> e = new DefaultEntity<>(
                "p1",
                P1.class,
                List.of(),
                Map.of("id1", new DefaultIdPart("id1", Integer.class, true, true, o -> "")),
                Collections.emptyMap(),
                Collections.emptyMap(),
                mock(RootDataResolver.class),
                ReadFilter.allowsAllFilter(),
                CreateAuthorizer.allowsAllFilter(),
                UpdateAuthorizer.allowsAllFilter(),
                DeleteAuthorizer.allowsAllFilter()
        );

        AgEntityOverlay<P1> o = AgEntity.overlay(P1.class)
                .readablePropFilter(b -> b.id(false))
                .writablePropFilter(b -> b.id(false));
        Collection<AgIdPart> parts = o.resolve(mock(AgSchema.class), e).getIdParts();
        assertEquals(1, parts.size());
        AgIdPart part = parts.iterator().next();
        assertEquals("id1", part.getName());
        assertFalse(part.isReadable());
        assertFalse(part.isWritable());
    }

}
