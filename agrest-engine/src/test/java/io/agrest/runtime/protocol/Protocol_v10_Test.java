package io.agrest.runtime.protocol;

import io.agrest.annotation.AgAttribute;
import io.agrest.annotation.AgId;
import io.agrest.junit.AgPojoTester;
import io.agrest.runtime.protocol.junit.ProtocolTester;
import io.bootique.junit5.BQTest;
import io.bootique.junit5.BQTestTool;
import org.junit.jupiter.api.Test;

/**
 * Tests the features of the protocol v.1.0 that are kept for backwards compatibility.
 */
@BQTest
public class Protocol_v10_Test {

    @BQTestTool
    static final AgPojoTester tester = AgPojoTester
            .builder()
            .build();

    @Test
    public void testCayenneExp() {
        ProtocolTester.test(Pojo.class, tester.runtime())
                .param("cayenneExp", "name = 'Joe'")
                .parseRequest()
                .assertExp("name = 'Joe'");
    }

    public static class Pojo {

        @AgId
        public int getId() {
            return -1;
        }

        @AgAttribute
        public String getName() {
            return "";
        }
    }
}
