package io.agrest.cayenne.encoder;

import com.fasterxml.jackson.core.JsonGenerator;
import io.agrest.DataResponse;
import io.agrest.ResourceEntity;
import io.agrest.cayenne.unit.CayenneAgTester;
import io.agrest.cayenne.unit.DbTest;
import io.agrest.encoder.Encoder;
import io.agrest.encoder.EntityEncoderFilter;
import io.agrest.cayenne.cayenne.main.E2;
import io.agrest.cayenne.cayenne.main.E3;
import io.bootique.junit5.BQTestTool;
import org.apache.cayenne.Cayenne;
import org.apache.cayenne.Persistent;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Collection;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class Encoder_VisitFilteredIT extends DbTest {

    @BQTestTool
    static final CayenneAgTester tester = tester()

            .entities(E2.class, E3.class)
            .agCustomizer(ab -> ab.entityEncoderFilter(new TestFilter(1, 3)))
            .build();

    @Test
    public void testEncoderFilter() {

        tester.e2().insertColumns("id_", "name")
                .values(1, "xxx")
                .values(2, "yyy")
                .values(3, "zzz")
                .values(4, "zzz").exec();

        DataResponse<E2> response = tester.ag().select(E2.class).get();
        assertEquals("2;E2:1;E2:3", Encoder_VisitIT.responseContents(response));
    }

    static class TestFilter implements EntityEncoderFilter {

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

