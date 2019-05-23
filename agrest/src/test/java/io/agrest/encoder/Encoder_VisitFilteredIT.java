package io.agrest.encoder;

import com.fasterxml.jackson.core.JsonGenerator;
import io.agrest.DataResponse;
import io.agrest.ResourceEntity;
import io.agrest.it.fixture.JerseyAndDerbyCase;
import io.agrest.it.fixture.cayenne.E2;
import io.agrest.it.fixture.cayenne.E3;
import io.agrest.runtime.AgBuilder;
import org.apache.cayenne.Cayenne;
import org.apache.cayenne.Persistent;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.util.Collection;
import java.util.function.UnaryOperator;

import static java.util.Arrays.asList;
import static org.junit.Assert.*;

public class Encoder_VisitFilteredIT extends JerseyAndDerbyCase {

    @BeforeClass
    public static void startTestRuntime() {
        UnaryOperator<AgBuilder> customizer = ab -> ab.encoderFilter(new TestFilter(1, 3));
        JerseyAndDerbyCase.startTestRuntime(customizer);
    }

    @Override
    protected Class<?>[] testEntities() {
        return new Class[]{E2.class, E3.class};
    }

    @Test
    public void testEncoderFilter() {

        e2().insertColumns("id_", "name")
                .values(1, "xxx")
                .values(2, "yyy")
                .values(3, "zzz")
                .values(4, "zzz").exec();

        DataResponse<E2> response = ag().select(E2.class).get();
        assertEquals("2;E2:1;E2:3", Encoder_VisitIT.responseContents(response));
    }

    static class TestFilter implements EncoderFilter {

        private Collection<Integer> ids;

        TestFilter(Integer... idsToEncode) {
            this.ids = asList(idsToEncode);
        }

        @Override
        public boolean matches(ResourceEntity<?> entity) {
            return true;
        }

        @Override
        public boolean encode(String propertyName, Object object, JsonGenerator out, Encoder delegate)
                throws IOException {

            if (willEncode(propertyName, object, delegate)) {
                return delegate.encode(propertyName, object, out);
            }

            return false;
        }

        @Override
        public boolean willEncode(String propertyName, Object object, Encoder delegate) {
            return ids.contains(Cayenne.intPKForObject((Persistent) object));
        }
    }
}

