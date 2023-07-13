package io.agrest.protocol;

import io.agrest.annotation.AgAttribute;
import io.agrest.annotation.AgId;
import io.agrest.annotation.AgRelationship;
import io.agrest.junit.AgPojoTester;
import io.agrest.protocol.junit.ControlParamsTester;
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
public class ControlParams_v12_Latest_Test {

    // TODO: start, limit, mapBy

    @BQTestTool
    static final AgPojoTester tester = AgPojoTester
            .builder()
            .build();

    @Test
    public void exp() {
        ControlParamsTester.test(Pojo.class, tester.runtime())
                .param("exp", "name = 'Joe'")
                .parseRequest()
                .assertExp(Exp.from("name = 'Joe'"));
    }

    @Test
    public void sort_None() {
        ControlParamsTester.test(ControlParams_v11_Test.Pojo.class, tester.runtime())
                .parseRequest()
                .assertSort();
    }

    @Test
    public void sort_Path_ImpliedAsc() {
        ControlParamsTester.test(ControlParams_v11_Test.Pojo.class, tester.runtime())
                .param("sort", "name")
                .parseRequest()
                .assertSort(new Sort("name", Direction.asc));
    }

    @Test
    public void sort_PathAndAsc() {
        ControlParamsTester.test(ControlParams_v11_Test.Pojo.class, tester.runtime())
                .param("sort", "name")
                .param("direction", "asc")
                .parseRequest()
                .assertSort(new Sort("name", Direction.asc));
    }

    @Test
    public void sort_PathAndAscCi() {
        ControlParamsTester.test(ControlParams_v11_Test.Pojo.class, tester.runtime())
                .param("sort", "name")
                .param("direction", "asc_ci")
                .parseRequest()
                .assertSort(new Sort("name", Direction.asc_ci));
    }

    @Test
    public void sort_PathAndDesc() {
        ControlParamsTester.test(ControlParams_v11_Test.Pojo.class, tester.runtime())
                .param("sort", "name")
                .param("direction", "desc")
                .parseRequest()
                .assertSort(new Sort("name", Direction.desc));
    }

    @Test
    public void sort_PathAndDescCi() {
        ControlParamsTester.test(ControlParams_v11_Test.Pojo.class, tester.runtime())
                .param("sort", "name")
                .param("direction", "desc_ci")
                .parseRequest()
                .assertSort(new Sort("name", Direction.desc_ci));
    }

    @Test
    public void sort_PathAndAsc_UC() {
        ControlParamsTester.test(ControlParams_v11_Test.Pojo.class, tester.runtime())
                .param("sort", "name")
                .param("direction", "ASC")
                .parseRequest()
                .assertSort(new Sort("name", Direction.asc));
    }

    @Test
    public void sort_PathAndAscCi_UC() {
        ControlParamsTester.test(ControlParams_v11_Test.Pojo.class, tester.runtime())
                .param("sort", "name")
                .param("direction", "ASC_CI")
                .parseRequest()
                .assertSort(new Sort("name", Direction.asc_ci));
    }

    @Test
    public void sort_PathAndDesc_UC() {
        ControlParamsTester.test(ControlParams_v11_Test.Pojo.class, tester.runtime())
                .param("sort", "name")
                .param("direction", "DESC")
                .parseRequest()
                .assertSort(new Sort("name", Direction.desc));
    }

    @Test
    public void sort_PathAndDescCi_UC() {
        ControlParamsTester.test(ControlParams_v11_Test.Pojo.class, tester.runtime())
                .param("sort", "name")
                .param("direction", "DESC_CI")
                .parseRequest()
                .assertSort(new Sort("name", Direction.desc_ci));
    }

    @Test
    public void sort_Object() {
        ControlParamsTester.test(ControlParams_v11_Test.Pojo.class, tester.runtime())
                .param("sort", "{\"path\":\"name\",\"direction\":\"asc_ci\"}")
                .parseRequest()
                .assertSort(new Sort("name", Direction.asc_ci));
    }

    @Test
    public void sort_ObjectArray() {
        ControlParamsTester.test(ControlParams_v11_Test.Pojo.class, tester.runtime())
                .param("sort", "[{\"path\":\"name\",\"direction\":\"asc_ci\"},{\"path\":\"dateOfBirth\",\"direction\":\"desc_ci\"}]")
                .parseRequest()
                .assertSort(new Sort("name", Direction.asc_ci), new Sort("dateOfBirth", Direction.desc_ci));
    }

    @Test
    public void defaultInclude() {
        ControlParamsTester.test(Pojo.class, tester.runtime())
                .parseRequest()
                .assertIdIncluded()
                .assertAttributes("dateOfBirth", "name")
                .assertRelationships();
    }

    @Test
    public void include() {
        ControlParamsTester.test(Pojo.class, tester.runtime())
                .param("include", "id", "name")
                .parseRequest()
                .assertIdIncluded()
                .assertAttributes("name")
                .assertRelationships();
    }

    @Test
    public void include_Related() {
        ControlParamsTester.test(Pojo.class, tester.runtime())
                .param("include", "id", "name", "details")
                .parseRequest()
                .assertIdIncluded()
                .assertAttributes("name")
                .assertRelationships("details");
    }

    @Test
    public void exclude() {
        ControlParamsTester.test(Pojo.class, tester.runtime())
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
