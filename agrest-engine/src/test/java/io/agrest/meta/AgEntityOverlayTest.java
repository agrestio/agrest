package io.agrest.meta;

import io.agrest.AgRequestBuilder;
import io.agrest.RootResourceEntity;
import io.agrest.access.CreateAuthorizer;
import io.agrest.access.DeleteAuthorizer;
import io.agrest.access.ReadFilter;
import io.agrest.access.UpdateAuthorizer;
import io.agrest.pojo.model.P1;
import io.agrest.resolver.RootDataResolver;
import io.agrest.runtime.processor.select.SelectContext;
import org.apache.cayenne.di.Injector;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;

public class AgEntityOverlayTest {

    @Test
    public void testResolve_AddAttribute() {
        RootDataResolver<P1> r1 = mock(RootDataResolver.class);

        AgEntity<P1> e = new DefaultEntity(
                "p1", P1.class,
                Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap(),
                r1,
                ReadFilter.allowsAllFilter(),
                CreateAuthorizer.allowsAllFilter(),
                UpdateAuthorizer.allowsAllFilter(),
                DeleteAuthorizer.allowsAllFilter()
        );

        AgEntityOverlay<P1> attributeOverlay = AgEntity.overlay(P1.class)
                .redefineAttribute("a1", String.class, P1::getName);

        AgEntity<P1> eo = attributeOverlay.resolve(mock(AgSchema.class), e);
        assertEquals("p1", eo.getName());
        assertEquals(P1.class, eo.getType());
        assertEquals(r1, eo.getDataResolver());
        assertEquals(1, eo.getAttributes().size());
    }

    @Test
    public void testResolve_RootResolver() {
        RootDataResolver<P1> r1 = mock(RootDataResolver.class);
        RootDataResolver<P1> r2 = mock(RootDataResolver.class);

        AgEntity<P1> e = new DefaultEntity(
                "p1", P1.class,
                Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap(),
                r1,
                ReadFilter.allowsAllFilter(),
                CreateAuthorizer.allowsAllFilter(),
                UpdateAuthorizer.allowsAllFilter(),
                DeleteAuthorizer.allowsAllFilter()
        );

        AgEntityOverlay<P1> resolverOnly = AgEntity.overlay(P1.class)
                .redefineDataResolver(r2);

        AgEntity<P1> eo = resolverOnly.resolve(mock(AgSchema.class), e);
        assertEquals("p1", eo.getName());
        assertEquals(P1.class, eo.getType());
        assertEquals(r2, eo.getDataResolver());
    }

    @Test
    public void testResolve_RootResolverFunction() {

        AgEntity<P1> e = new DefaultEntity(
                "p1", P1.class,
                Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap(),
                mock(RootDataResolver.class),
                ReadFilter.allowsAllFilter(),
                CreateAuthorizer.allowsAllFilter(),
                UpdateAuthorizer.allowsAllFilter(),
                DeleteAuthorizer.allowsAllFilter()
        );

        List<P1> p1s = asList(new P1(), new P1());

        AgEntityOverlay<P1> resolverOnly = AgEntity.overlay(P1.class).redefineDataResolver(c -> p1s);
        AgEntity<P1> eo = resolverOnly.resolve(mock(AgSchema.class), e);

        SelectContext<P1> context = new SelectContext<>(
                P1.class,
                mock(AgRequestBuilder.class),
                mock(Injector.class));
        context.setEntity(new RootResourceEntity<>(mock(AgEntity.class)));
        eo.getDataResolver().fetchData(context);

        assertSame(p1s, context.getEntity().getData());
    }
}
