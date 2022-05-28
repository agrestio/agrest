package io.agrest.runtime.protocol;

import io.agrest.annotation.AgAttribute;
import io.agrest.annotation.AgId;
import io.agrest.junit.AgPojoTester;
import io.agrest.protocol.Dir;
import io.agrest.protocol.Sort;
import io.agrest.runtime.protocol.junit.ProtocolTester;
import io.bootique.junit5.BQTest;
import io.bootique.junit5.BQTestTool;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

/**
 * Tests features of the protocol v1.1 that are kept for backwards compatibility.
 */
@BQTest
public class Protocol_v11_Test {

    @BQTestTool
    static final AgPojoTester tester = AgPojoTester
            .builder()
            .build();

    @Test
    public void testSort_SortPathAndDir() {
        ProtocolTester.test(Pojo.class, tester.runtime())
                .param("sort", "name")
                .param("dir", "DESC")
                .parseRequest()
                .assertSort(new Sort("name", Dir.DESC));
    }

    @Test
    public void testSort_Object() {
        ProtocolTester.test(Pojo.class, tester.runtime())
                .param("sort", "{\"property\":\"name\",\"direction\":\"ASC_CI\"}")
                .parseRequest()
                .assertSort(new Sort("name", Dir.ASC_CI));
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

        @AgAttribute
        public LocalDate getDateOfBirth() {
            return LocalDate.now();
        }
    }
}
