package io.agrest;

import io.agrest.meta.AgEntity;
import io.agrest.pojo.model.P1;
import io.agrest.protocol.Exp;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

public class ResourceEntityTest {

    @Test
    public void testQualifier() {
        @SuppressWarnings("unchecked")
        ResourceEntity<P1> e = new RootResourceEntity<>(mock(AgEntity.class));
        assertNull(e.getQualifier());

        e.andQualifier(Exp.simple("a = 1"));
        assertEquals(Exp.simple("a = 1"), e.getQualifier());

        e.andQualifier(Exp.simple("b = 2"));
        assertEquals(Exp.simple("a = 1").and(Exp.simple("b = 2")), e.getQualifier());
    }


    @Test
    public void testGetDataWindow_NoOffsetLimit() {
        List<P1> data = asList(new P1(), new P1(), new P1());

        ResourceEntity<P1> e = new RootResourceEntity<>(mock(AgEntity.class));
        assertSame(data, e.getDataWindow(data));
    }

    @Test
    public void testGetDataWindow_Offset() {
        List<P1> data = asList(new P1(), new P1(), new P1());

        ResourceEntity<P1> e = new RootResourceEntity<>(mock(AgEntity.class));
        e.setFetchOffset(1);
        assertEquals(asList(data.get(1), data.get(2)), e.getDataWindow(data));
    }

    @Test
    public void testGetDataWindow_OffsetPastEnd() {
        List<P1> data = asList(new P1(), new P1(), new P1());

        ResourceEntity<P1> e = new RootResourceEntity<>(mock(AgEntity.class));
        e.setFetchOffset(4);
        assertEquals(Collections.emptyList(), e.getDataWindow(data));
    }

    @Test
    public void testGetDataWindow_Limit() {
        List<P1> data = asList(new P1(), new P1(), new P1());

        ResourceEntity<P1> e = new RootResourceEntity<>(mock(AgEntity.class));
        e.setFetchLimit(2);
        assertEquals(asList(data.get(0), data.get(1)), e.getDataWindow(data));
    }

    @Test
    public void testGetDataWindow_LimitPastEnd() {
        List<P1> data = asList(new P1(), new P1(), new P1());

        ResourceEntity<P1> e = new RootResourceEntity<>(mock(AgEntity.class));
        e.setFetchLimit(4);
        assertEquals(asList(data.get(0), data.get(1), data.get(2)), e.getDataWindow(data));
    }

    @Test
    public void testGetDataWindow_OffsetLimit() {
        List<P1> data = asList(new P1(), new P1(), new P1());

        ResourceEntity<P1> e = new RootResourceEntity<>(mock(AgEntity.class));
        e.setFetchOffset(1);
        e.setFetchLimit(1);
        assertEquals(asList(data.get(1)), e.getDataWindow(data));
    }

    @Test
    public void testGetDataWindow_OffsetNegativeLimit() {
        List<P1> data = asList(new P1(), new P1(), new P1());

        ResourceEntity<P1> e = new RootResourceEntity<>(mock(AgEntity.class));
        e.setFetchOffset(1);
        e.setFetchLimit(-5);
        assertEquals(asList(data.get(1), data.get(2)), e.getDataWindow(data));
    }

    @Test
    public void testGetDataWindow_LimitNegativeOffset() {
        List<P1> data = asList(new P1(), new P1(), new P1());

        ResourceEntity<P1> e = new RootResourceEntity<>(mock(AgEntity.class));
        e.setFetchLimit(2);
        e.setFetchOffset(-2);
        assertEquals(asList(data.get(0), data.get(1)), e.getDataWindow(data));
    }

}
