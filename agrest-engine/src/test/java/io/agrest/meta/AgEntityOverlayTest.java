package io.agrest.meta;

import io.agrest.AgRequestBuilder;
import io.agrest.EntityUpdate;
import io.agrest.RootResourceEntity;
import io.agrest.access.CreateAuthorizer;
import io.agrest.access.DeleteAuthorizer;
import io.agrest.access.PathChecker;
import io.agrest.access.ReadFilter;
import io.agrest.access.UpdateAuthorizer;
import io.agrest.junit.pojo.P1;
import io.agrest.reader.DataReader;
import io.agrest.resolver.RootDataResolver;
import io.agrest.runtime.meta.RequestSchema;
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
    public void resolve_attribute() {
        RootDataResolver<P1> r1 = mock(RootDataResolver.class);

        AgEntity<P1> e = new DefaultEntity<>(
                "p1", P1.class, false,
                List.of(), Map.of(), Map.of(), Map.of(),
                r1,
                ReadFilter.allowsAllFilter(),
                CreateAuthorizer.allowsAllFilter(),
                UpdateAuthorizer.allowsAllFilter(),
                DeleteAuthorizer.allowsAllFilter()
        );

        AgEntityOverlay<P1> attributeOverlay = AgEntity.overlay(P1.class)
                .attribute("a1", String.class, P1::getName);

        AgEntity<P1> eo = attributeOverlay.resolve(mock(AgSchema.class), e, e.getSubEntities());
        assertEquals("p1", eo.getName());
        assertEquals(P1.class, eo.getType());
        assertEquals(r1, eo.getDataResolver());
        assertEquals(1, eo.getAttributes().size());
    }

    @Test
    public void resolve_attribute_throwingFunction() {
        RootDataResolver<P1> r1 = mock(RootDataResolver.class);

        AgEntity<P1> e = new DefaultEntity<>(
                "p1", P1.class, false,
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

        AgEntity<P1> eo = attributeOverlay.resolve(mock(AgSchema.class), e, e.getSubEntities());
        DataReader reader = eo.getAttribute("a1").getDataReader();
        assertThrows(RuntimeException.class, () -> reader.read(new P1()));
    }

    @Test
    public void resolve_RootResolver() {
        RootDataResolver<P1> r1 = mock(RootDataResolver.class);
        RootDataResolver<P1> r2 = mock(RootDataResolver.class);

        AgEntity<P1> e = new DefaultEntity<>(
                "p1", P1.class, false,
                List.of(), Map.of(), Map.of(), Map.of(),
                r1,
                ReadFilter.allowsAllFilter(),
                CreateAuthorizer.allowsAllFilter(),
                UpdateAuthorizer.allowsAllFilter(),
                DeleteAuthorizer.allowsAllFilter()
        );

        AgEntityOverlay<P1> resolverOnly = AgEntity.overlay(P1.class)
                .dataResolver(r2);

        AgEntity<P1> eo = resolverOnly.resolve(mock(AgSchema.class), e, e.getSubEntities());
        assertEquals("p1", eo.getName());
        assertEquals(P1.class, eo.getType());
        assertEquals(r2, eo.getDataResolver());
    }

    @Test
    public void resolve_RootResolverFunction() {

        AgEntity<P1> e = new DefaultEntity<>(
                "p1",
                P1.class, false,
                List.of(), Map.of(), Map.of(), Map.of(),
                mock(RootDataResolver.class),
                ReadFilter.allowsAllFilter(),
                CreateAuthorizer.allowsAllFilter(),
                UpdateAuthorizer.allowsAllFilter(),
                DeleteAuthorizer.allowsAllFilter()
        );

        List<P1> p1s = asList(new P1(), new P1());

        AgEntityOverlay<P1> resolverOnly = AgEntity.overlay(P1.class).dataResolver(c -> p1s);
        AgEntity<P1> eo = resolverOnly.resolve(mock(AgSchema.class), e, e.getSubEntities());

        SelectContext<P1> context = new SelectContext<>(
                P1.class,
                new RequestSchema(mock(AgSchema.class)),
                mock(AgRequestBuilder.class),
                PathChecker.ofDefault(),
                mock(Injector.class));

        context.setEntity(new RootResourceEntity<>(eo));
        eo.getDataResolver().fetchData(context);

        assertSame(p1s, context.getEntity().getData());
    }

    @Test
    public void resolve_readFilter() {

        AgEntity<P1> e = new DefaultEntity<>(
                "p1",
                P1.class, false,
                List.of(), Map.of(), Map.of(), Map.of(),
                mock(RootDataResolver.class),
                ReadFilter.allowsAllFilter(),
                CreateAuthorizer.allowsAllFilter(),
                UpdateAuthorizer.allowsAllFilter(),
                DeleteAuthorizer.allowsAllFilter()
        );

        AgEntityOverlay<P1> o = AgEntity.overlay(P1.class).readFilter(p1 -> p1.getName().startsWith("a"));

        ReadFilter<P1> f = o.resolve(mock(AgSchema.class), e, e.getSubEntities()).getReadFilter();
        P1 p11 = new P1();
        p11.setName("x");
        P1 p12 = new P1();
        p12.setName("a");

        assertFalse(f.isAllowed(p11));
        assertTrue(f.isAllowed(p12));
    }

    @Test
    public void resolve_readFilter_Additivity() {

        AgEntity<P1> e = new DefaultEntity<>(
                "p1",
                P1.class, false,
                List.of(), Map.of(), Map.of(), Map.of(),
                mock(RootDataResolver.class),
                p1 -> p1.getName().startsWith("a"),
                CreateAuthorizer.allowsAllFilter(),
                UpdateAuthorizer.allowsAllFilter(),
                DeleteAuthorizer.allowsAllFilter()
        );

        AgEntityOverlay<P1> o = AgEntity.overlay(P1.class).readFilter(p1 -> p1.getName().endsWith("a"));

        ReadFilter<P1> f = o.resolve(mock(AgSchema.class), e, e.getSubEntities()).getReadFilter();
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
    public void resolve_readFilter_IgnoreOverlaid() {

        AgEntity<P1> e = new DefaultEntity<>(
                "p1",
                P1.class, false,
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

        ReadFilter<P1> f = o.resolve(mock(AgSchema.class), e, e.getSubEntities()).getReadFilter();
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
    public void resolve_readFilter_IgnoreOverlaid_NoOverlayFilter() {

        AgEntity<P1> e = new DefaultEntity<>(
                "p1",
                P1.class, false,
                List.of(), Map.of(), Map.of(), Map.of(),
                mock(RootDataResolver.class),
                p1 -> p1.getName().startsWith("a"),
                CreateAuthorizer.allowsAllFilter(),
                UpdateAuthorizer.allowsAllFilter(),
                DeleteAuthorizer.allowsAllFilter()
        );

        AgEntityOverlay<P1> o = AgEntity.overlay(P1.class).ignoreOverlaidReadFilter();

        ReadFilter<P1> f = o.resolve(mock(AgSchema.class), e, e.getSubEntities()).getReadFilter();
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
    public void resolve_createAuthorizer() {

        AgEntity<P1> e = new DefaultEntity<>(
                "p1",
                P1.class, false,
                List.of(), Map.of(), Map.of(), Map.of(),
                mock(RootDataResolver.class),
                ReadFilter.allowsAllFilter(),
                CreateAuthorizer.allowsAllFilter(),
                UpdateAuthorizer.allowsAllFilter(),
                DeleteAuthorizer.allowsAllFilter()
        );

        AgEntityOverlay<P1> o = AgEntity.overlay(P1.class).createAuthorizer(u -> ((String) u.getAttributes().get("name")).startsWith("a"));
        CreateAuthorizer<P1> f = o.resolve(mock(AgSchema.class), e, e.getSubEntities()).getCreateAuthorizer();

        EntityUpdate<P1> p11 = new EntityUpdate<>(mock(AgEntity.class));
        p11.setAttribute("name", "x");

        EntityUpdate<P1> p12 = new EntityUpdate<>(mock(AgEntity.class));
        p12.setAttribute("name", "a");

        assertFalse(f.isAllowed(p11));
        assertTrue(f.isAllowed(p12));
    }

    @Test
    public void resolve_createAuthorizer_Additivity() {

        AgEntity<P1> e = new DefaultEntity<>(
                "p1",
                P1.class, false,
                List.of(), Map.of(), Map.of(), Map.of(),
                mock(RootDataResolver.class),
                ReadFilter.allowsAllFilter(),
                u -> ((String) u.getAttributes().get("name")).startsWith("a"),
                UpdateAuthorizer.allowsAllFilter(),
                DeleteAuthorizer.allowsAllFilter()
        );

        AgEntityOverlay<P1> o = AgEntity.overlay(P1.class).createAuthorizer(u -> ((String) u.getAttributes().get("name")).endsWith("a"));
        CreateAuthorizer<P1> f = o.resolve(mock(AgSchema.class), e, e.getSubEntities()).getCreateAuthorizer();

        EntityUpdate<P1> p11 = new EntityUpdate<>(mock(AgEntity.class));
        p11.setAttribute("name", "ax");

        EntityUpdate<P1> p12 = new EntityUpdate<>(mock(AgEntity.class));
        p12.setAttribute("name", "xa");

        EntityUpdate<P1> p13 = new EntityUpdate<>(mock(AgEntity.class));
        p13.setAttribute("name", "axa");

        assertFalse(f.isAllowed(p11));
        assertFalse(f.isAllowed(p12));
        assertTrue(f.isAllowed(p13));
    }

    @Test
    public void resolve_createAuthorizer_IgnoreOverlaid() {

        AgEntity<P1> e = new DefaultEntity<>(
                "p1",
                P1.class, false,
                List.of(), Map.of(), Map.of(), Map.of(),
                mock(RootDataResolver.class),
                ReadFilter.allowsAllFilter(),
                u -> ((String) u.getAttributes().get("name")).startsWith("a"),
                UpdateAuthorizer.allowsAllFilter(),
                DeleteAuthorizer.allowsAllFilter()
        );

        AgEntityOverlay<P1> o = AgEntity.overlay(P1.class)
                .createAuthorizer(u -> ((String) u.getAttributes().get("name")).endsWith("a"))
                .ignoreOverlaidCreateAuthorizer();
        CreateAuthorizer<P1> f = o.resolve(mock(AgSchema.class), e, e.getSubEntities()).getCreateAuthorizer();

        EntityUpdate<P1> p11 = new EntityUpdate<>(mock(AgEntity.class));
        p11.setAttribute("name", "ax");

        EntityUpdate<P1> p12 = new EntityUpdate<>(mock(AgEntity.class));
        p12.setAttribute("name", "xa");

        EntityUpdate<P1> p13 = new EntityUpdate<>(mock(AgEntity.class));
        p13.setAttribute("name", "axa");

        assertFalse(f.isAllowed(p11));
        assertTrue(f.isAllowed(p12));
        assertTrue(f.isAllowed(p13));
    }

    @Test
    public void resolve_updateAuthorizer() {

        AgEntity<P1> e = new DefaultEntity<>(
                "p1",
                P1.class, false,
                List.of(), Map.of(), Map.of(), Map.of(),
                mock(RootDataResolver.class),
                ReadFilter.allowsAllFilter(),
                CreateAuthorizer.allowsAllFilter(),
                UpdateAuthorizer.allowsAllFilter(),
                DeleteAuthorizer.allowsAllFilter()
        );

        AgEntityOverlay<P1> o = AgEntity.overlay(P1.class).updateAuthorizer((p1, u) -> ((String) u.getAttributes().get("name")).startsWith("a"));
        UpdateAuthorizer<P1> f = o.resolve(mock(AgSchema.class), e, e.getSubEntities()).getUpdateAuthorizer();

        EntityUpdate<P1> p11 = new EntityUpdate<>(mock(AgEntity.class));
        p11.setAttribute("name", "x");

        EntityUpdate<P1> p12 = new EntityUpdate<>(mock(AgEntity.class));
        p12.setAttribute("name", "a");

        assertFalse(f.isAllowed(mock(P1.class), p11));
        assertTrue(f.isAllowed(mock(P1.class), p12));
    }

    @Test
    public void resolve_updateAuthorizer_Additivity() {

        AgEntity<P1> e = new DefaultEntity<>(
                "p1",
                P1.class, false,
                List.of(), Map.of(), Map.of(), Map.of(),
                mock(RootDataResolver.class),
                ReadFilter.allowsAllFilter(),
                CreateAuthorizer.allowsAllFilter(),
                (p1, u) -> ((String) u.getAttributes().get("name")).startsWith("a"),
                DeleteAuthorizer.allowsAllFilter()
        );

        AgEntityOverlay<P1> o = AgEntity.overlay(P1.class).updateAuthorizer((p1, u) -> ((String) u.getAttributes().get("name")).endsWith("a"));
        UpdateAuthorizer<P1> f = o.resolve(mock(AgSchema.class), e, e.getSubEntities()).getUpdateAuthorizer();

        EntityUpdate<P1> p11 = new EntityUpdate<>(mock(AgEntity.class));
        p11.setAttribute("name", "ax");

        EntityUpdate<P1> p12 = new EntityUpdate<>(mock(AgEntity.class));
        p12.setAttribute("name", "xa");

        EntityUpdate<P1> p13 = new EntityUpdate<>(mock(AgEntity.class));
        p13.setAttribute("name", "axa");

        assertFalse(f.isAllowed(mock(P1.class), p11));
        assertFalse(f.isAllowed(mock(P1.class), p12));
        assertTrue(f.isAllowed(mock(P1.class), p13));
    }

    @Test
    public void resolve_updateAuthorizer_IgnoreOverlaid() {

        AgEntity<P1> e = new DefaultEntity<>(
                "p1",
                P1.class, false,
                List.of(), Map.of(), Map.of(), Map.of(),
                mock(RootDataResolver.class),
                ReadFilter.allowsAllFilter(),
                CreateAuthorizer.allowsAllFilter(),
                (p1, u) -> ((String) u.getAttributes().get("name")).startsWith("a"),
                DeleteAuthorizer.allowsAllFilter()
        );

        AgEntityOverlay<P1> o = AgEntity.overlay(P1.class)
                .ignoreOverlaidUpdateAuthorizer()
                .updateAuthorizer((p1, u) -> ((String) u.getAttributes().get("name")).endsWith("a"));
        UpdateAuthorizer<P1> f = o.resolve(mock(AgSchema.class), e, e.getSubEntities()).getUpdateAuthorizer();

        EntityUpdate<P1> p11 = new EntityUpdate<>(mock(AgEntity.class));
        p11.setAttribute("name", "ax");

        EntityUpdate<P1> p12 = new EntityUpdate<>(mock(AgEntity.class));
        p12.setAttribute("name", "xa");

        EntityUpdate<P1> p13 = new EntityUpdate<>(mock(AgEntity.class));
        p13.setAttribute("name", "axa");

        assertFalse(f.isAllowed(mock(P1.class), p11));
        assertTrue(f.isAllowed(mock(P1.class), p12));
        assertTrue(f.isAllowed(mock(P1.class), p13));
    }

    @Test
    public void mergeResolve_updateAuthorizer_IgnoreOverlaid() {

        AgEntity<P1> e = new DefaultEntity<>(
                "p1",
                P1.class, false,
                List.of(), Map.of(), Map.of(), Map.of(),
                mock(RootDataResolver.class),
                ReadFilter.allowsAllFilter(),
                CreateAuthorizer.allowsAllFilter(),
                (p1, u) -> ((String) u.getAttributes().get("name")).startsWith("a"),
                DeleteAuthorizer.allowsAllFilter()
        );

        AgEntityOverlay<P1> o = AgEntity.overlay(P1.class)
                .ignoreOverlaidUpdateAuthorizer()
                .updateAuthorizer((p1, u) -> ((String) u.getAttributes().get("name")).endsWith("a"));

        AgEntityOverlay<P1> oMerged = AgEntity.overlay(P1.class).merge(o);
        UpdateAuthorizer<P1> f = oMerged.resolve(mock(AgSchema.class), e, e.getSubEntities()).getUpdateAuthorizer();

        EntityUpdate<P1> p11 = new EntityUpdate<>(mock(AgEntity.class));
        p11.setAttribute("name", "ax");

        EntityUpdate<P1> p12 = new EntityUpdate<>(mock(AgEntity.class));
        p12.setAttribute("name", "xa");

        EntityUpdate<P1> p13 = new EntityUpdate<>(mock(AgEntity.class));
        p13.setAttribute("name", "axa");

        assertFalse(f.isAllowed(mock(P1.class), p11));
        assertTrue(f.isAllowed(mock(P1.class), p12));
        assertTrue(f.isAllowed(mock(P1.class), p13));
    }

    @Test
    public void resolve_deleteAuthorizer() {

        AgEntity<P1> e = new DefaultEntity<>(
                "p1",
                P1.class, false,
                List.of(), Map.of(), Map.of(), Map.of(),
                mock(RootDataResolver.class),
                ReadFilter.allowsAllFilter(),
                CreateAuthorizer.allowsAllFilter(),
                UpdateAuthorizer.allowsAllFilter(),
                DeleteAuthorizer.allowsAllFilter()
        );

        AgEntityOverlay<P1> o = AgEntity.overlay(P1.class).deleteAuthorizer(p1 -> p1.getName().startsWith("a"));
        DeleteAuthorizer<P1> f = o.resolve(mock(AgSchema.class), e, e.getSubEntities()).getDeleteAuthorizer();

        P1 p11 = new P1();
        p11.setName("x");
        P1 p12 = new P1();
        p12.setName("a");

        assertFalse(f.isAllowed(p11));
        assertTrue(f.isAllowed(p12));
    }

    @Test
    public void resolve_deleteAuthorizer_Additivity() {

        AgEntity<P1> e = new DefaultEntity<>(
                "p1",
                P1.class, false,
                List.of(), Map.of(), Map.of(), Map.of(),
                mock(RootDataResolver.class),
                ReadFilter.allowsAllFilter(),
                CreateAuthorizer.allowsAllFilter(),
                UpdateAuthorizer.allowsAllFilter(),
                p1 -> p1.getName().startsWith("a")
        );

        AgEntityOverlay<P1> o = AgEntity.overlay(P1.class).deleteAuthorizer(p1 -> p1.getName().endsWith("a"));
        DeleteAuthorizer<P1> f = o.resolve(mock(AgSchema.class), e, e.getSubEntities()).getDeleteAuthorizer();

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
    public void resolve_deleteAuthorizer_IgnoreOverlaid() {

        AgEntity<P1> e = new DefaultEntity<>(
                "p1",
                P1.class, false,
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
        DeleteAuthorizer<P1> f = o.resolve(mock(AgSchema.class), e, e.getSubEntities()).getDeleteAuthorizer();

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
    public void resolve_propFilter_MakeInaccessible() {

        AgEntity<P1> e = new DefaultEntity<>(
                "p1",
                P1.class, false,
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
        AgAttribute a = o.resolve(mock(AgSchema.class), e, e.getSubEntities()).getAttribute("name");
        assertFalse(a.isReadable());
        assertFalse(a.isWritable());
    }

    @Test
    public void resolve_propFilter_MakeAccessible() {

        AgEntity<P1> e = new DefaultEntity<>(
                "p1",
                P1.class, false,
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
        AgAttribute a = o.resolve(mock(AgSchema.class), e, e.getSubEntities()).getAttribute("name");
        assertTrue(a.isReadable());
        assertTrue(a.isWritable());
    }

    @Test
    public void resolve_propFilter_MakeIdInAccessible() {

        AgEntity<P1> e = new DefaultEntity<>(
                "p1",
                P1.class, false,
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
        Collection<AgIdPart> parts = o.resolve(mock(AgSchema.class), e, e.getSubEntities()).getIdParts();
        assertEquals(1, parts.size());
        AgIdPart part = parts.iterator().next();
        assertEquals("id1", part.getName());
        assertFalse(part.isReadable());
        assertFalse(part.isWritable());
    }
}
