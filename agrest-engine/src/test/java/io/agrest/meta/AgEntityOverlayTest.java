package io.agrest.meta;

import io.agrest.access.CreateAuthorizer;
import io.agrest.access.DeleteAuthorizer;
import io.agrest.access.ReadFilter;
import io.agrest.access.UpdateAuthorizer;
import io.agrest.pojo.model.P1;
import io.agrest.resolver.RootDataResolver;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

public class AgEntityOverlayTest {

    @Test
    public void testResolve_AddAttribute() {
        RootDataResolver<P1> r1 = mock(RootDataResolver.class);

        AgEntity<P1> e = new DefaultAgEntity(
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

        AgEntity<P1> eo = attributeOverlay.resolve(mock(AgDataMap.class), e);
        assertEquals("p1", eo.getName());
        assertEquals(P1.class, eo.getType());
        assertEquals(r1, eo.getDataResolver());
        assertEquals(1, eo.getAttributes().size());
    }

    @Test
    public void testResolve_RootResolver() {
        RootDataResolver<P1> r1 = mock(RootDataResolver.class);
        RootDataResolver<P1> r2 = mock(RootDataResolver.class);

        AgEntity<P1> e = new DefaultAgEntity(
                "p1", P1.class,
                Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap(),
                r1,
                ReadFilter.allowsAllFilter(),
                CreateAuthorizer.allowsAllFilter(),
                UpdateAuthorizer.allowsAllFilter(),
                DeleteAuthorizer.allowsAllFilter()
        );

        AgEntityOverlay<P1> resolverOnly = AgEntity.overlay(P1.class)
                .redefineRootDataResolver(r2);

        AgEntity<P1> eo = resolverOnly.resolve(mock(AgDataMap.class), e);
        assertEquals("p1", eo.getName());
        assertEquals(P1.class, eo.getType());
        assertEquals(r2, eo.getDataResolver());
    }
}
