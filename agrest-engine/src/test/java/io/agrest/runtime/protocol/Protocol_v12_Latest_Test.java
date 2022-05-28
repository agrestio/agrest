package io.agrest.runtime.protocol;

import io.agrest.annotation.AgAttribute;
import io.agrest.annotation.AgId;
import io.agrest.annotation.AgRelationship;
import io.agrest.junit.AgPojoTester;
import io.agrest.protocol.Direction;
import io.agrest.protocol.Sort;
import io.agrest.runtime.protocol.junit.ProtocolTester;
import io.bootique.junit5.BQTest;
import io.bootique.junit5.BQTestTool;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

/**
 * Tests the latest version of the protocol
 */
@BQTest
public class Protocol_v12_Latest_Test {

    // TODO: start, limit, mapBy

    @BQTestTool
    static final AgPojoTester tester = AgPojoTester
            .builder()
            .build();

    @Test
    public void testExp() {
        ProtocolTester.test(Pojo.class, tester.runtime())
                .param("exp", "name = 'Joe'")
                .parseRequest()
                .assertExp("name = 'Joe'");
    }

    @Test
    public void testSort_None() {
        ProtocolTester.test(Protocol_v11_Test.Pojo.class, tester.runtime())
                .parseRequest()
                .assertSort();
    }

    @Test
    public void testSort_Path_ImpliedAsc() {
        ProtocolTester.test(Protocol_v11_Test.Pojo.class, tester.runtime())
                .param("sort", "name")
                .parseRequest()
                .assertSort(new Sort("name", Direction.asc));
    }

    @Test
    public void testSort_PathAndAsc() {
        ProtocolTester.test(Protocol_v11_Test.Pojo.class, tester.runtime())
                .param("sort", "name")
                .param("direction", "asc")
                .parseRequest()
                .assertSort(new Sort("name", Direction.asc));
    }

    @Test
    public void testSort_PathAndAscCi() {
        ProtocolTester.test(Protocol_v11_Test.Pojo.class, tester.runtime())
                .param("sort", "name")
                .param("direction", "asc_ci")
                .parseRequest()
                .assertSort(new Sort("name", Direction.asc_ci));
    }

    @Test
    public void testSort_PathAndDesc() {
        ProtocolTester.test(Protocol_v11_Test.Pojo.class, tester.runtime())
                .param("sort", "name")
                .param("direction", "desc")
                .parseRequest()
                .assertSort(new Sort("name", Direction.desc));
    }

    @Test
    public void testSort_PathAndDescCi() {
        ProtocolTester.test(Protocol_v11_Test.Pojo.class, tester.runtime())
                .param("sort", "name")
                .param("direction", "desc_ci")
                .parseRequest()
                .assertSort(new Sort("name", Direction.desc_ci));
    }

    @Test
    public void testSort_PathAndAsc_UC() {
        ProtocolTester.test(Protocol_v11_Test.Pojo.class, tester.runtime())
                .param("sort", "name")
                .param("direction", "ASC")
                .parseRequest()
                .assertSort(new Sort("name", Direction.asc));
    }

    @Test
    public void testSort_PathAndAscCi_UC() {
        ProtocolTester.test(Protocol_v11_Test.Pojo.class, tester.runtime())
                .param("sort", "name")
                .param("direction", "ASC_CI")
                .parseRequest()
                .assertSort(new Sort("name", Direction.asc_ci));
    }

    @Test
    public void testSort_PathAndDesc_UC() {
        ProtocolTester.test(Protocol_v11_Test.Pojo.class, tester.runtime())
                .param("sort", "name")
                .param("direction", "DESC")
                .parseRequest()
                .assertSort(new Sort("name", Direction.desc));
    }

    @Test
    public void testSort_PathAndDescCi_UC() {
        ProtocolTester.test(Protocol_v11_Test.Pojo.class, tester.runtime())
                .param("sort", "name")
                .param("direction", "DESC_CI")
                .parseRequest()
                .assertSort(new Sort("name", Direction.desc_ci));
    }

    @Test
    public void testSort_Object() {
        ProtocolTester.test(Protocol_v11_Test.Pojo.class, tester.runtime())
                .param("sort", "{\"path\":\"name\",\"direction\":\"asc_ci\"}")
                .parseRequest()
                .assertSort(new Sort("name", Direction.asc_ci));
    }

    @Test
    public void testSort_ObjectArray() {
        ProtocolTester.test(Protocol_v11_Test.Pojo.class, tester.runtime())
                .param("sort", "[{\"path\":\"name\",\"direction\":\"asc_ci\"},{\"path\":\"dateOfBirth\",\"direction\":\"desc_ci\"}]")
                .parseRequest()
                .assertSort(new Sort("name", Direction.asc_ci), new Sort("dateOfBirth", Direction.desc_ci));
    }

    @Test
    public void testDefaultInclude() {
        ProtocolTester.test(Pojo.class, tester.runtime())
                .parseRequest()
                .assertIdIncluded()
                .assertAttributes("dateOfBirth", "name")
                .assertRelationships();
    }

    @Test
    public void testInclude() {
        ProtocolTester.test(Pojo.class, tester.runtime())
                .param("include", "id", "name")
                .parseRequest()
                .assertIdIncluded()
                .assertAttributes("name")
                .assertRelationships();
    }

    @Test
    public void testInclude_Related() {
        ProtocolTester.test(Pojo.class, tester.runtime())
                .param("include", "id", "name", "details")
                .parseRequest()
                .assertIdIncluded()
                .assertAttributes("name")
                .assertRelationships("details");
    }

    @Test
    public void testExclude() {
        ProtocolTester.test(Pojo.class, tester.runtime())
                .param("exclude", "id", "name")
                .parseRequest()
                .assertIdExcluded()
                .assertAttributes("dateOfBirth")
                .assertRelationships();
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

        @AgRelationship
        public List<PojoDetail> getDetails() {
            return Collections.emptyList();
        }
    }

    public static class PojoDetail {

        @AgId
        public int getId() {
            return -1;
        }

        @AgAttribute
        public String getName() {
            return "";
        }

        @AgRelationship
        public Pojo getParent() {
            return null;
        }
    }
}
