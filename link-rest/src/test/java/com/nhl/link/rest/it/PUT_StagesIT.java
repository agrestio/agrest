package com.nhl.link.rest.it;

import com.nhl.link.rest.it.fixture.JerseyTestOnDerby;
import com.nhl.link.rest.it.fixture.listener.UpdateCallbackListener;
import com.nhl.link.rest.it.fixture.resource.E3Resource;
import org.junit.Test;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.core.Response;
import java.io.IOException;

import static com.nhl.link.rest.unit.matcher.LRMatchers.okAndHasData;
import static org.junit.Assert.*;

public class PUT_StagesIT extends JerseyTestOnDerby {

    @Override
    protected void doAddResources(FeatureContext context) {
        context.register(E3Resource.class);
    }

    @Test
    @Deprecated
    public void testPut_ToOne() throws WebApplicationException, IOException {
        insert("e3", "id, name", "3, 'z'");
        insert("e3", "id, name", "4, 'a'");


        UpdateCallbackListener.BEFORE_UPDATE_CALLED = false;

        Response response = target("/e3/callbackstage").request()
                .put(jsonEntity("[{\"id\":3,\"name\":\"x\"}]"));

        assertThat(response, okAndHasData(1, "[{\"id\":3,\"name\":\"x\",\"phoneNumber\":null}]"));
        assertEquals(1, intForQuery("SELECT COUNT(1) FROM utest.e3 WHERE id = 3 AND name = 'x'"));
        assertEquals(0, intForQuery("SELECT COUNT(1) FROM utest.e3 WHERE id = 4"));

        assertTrue(UpdateCallbackListener.BEFORE_UPDATE_CALLED);
    }
}
