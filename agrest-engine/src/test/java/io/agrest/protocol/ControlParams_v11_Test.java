package io.agrest.protocol;

import io.agrest.annotation.AgAttribute;
import io.agrest.annotation.AgId;
import io.agrest.junit.AgPojoTester;
import io.agrest.protocol.junit.ControlParamsTester;
import io.bootique.junit5.BQTest;
import io.bootique.junit5.BQTestTool;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

/**
 * Tests features of the protocol v1.1 that are kept for backwards compatibility.
 */
@BQTest
public class ControlParams_v11_Test {

    @BQTestTool
    static final AgPojoTester tester = AgPojoTester
            .builder()
            .build();

    @Test
    public void sort_SortPathAndDir() {
        ControlParamsTester.test(Pojo.class, tester.runtime())
                .param("sort", "name")
                .param("dir", "DESC")
                .parseRequest()
                .assertSort(new Sort("name", Direction.desc));
    }

    @Test
    public void sort_Object() {
        ControlParamsTester.test(Pojo.class, tester.runtime())
                .param("sort", "{\"property\":\"name\",\"direction\":\"ASC_CI\"}")
                .parseRequest()
                .assertSort(new Sort("name", Direction.asc_ci));
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
