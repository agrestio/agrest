package io.agrest.runtime.protocol;

import com.fasterxml.jackson.databind.JsonNode;
import io.agrest.EntityUpdate;
import io.agrest.annotation.AgAttribute;
import io.agrest.annotation.AgId;
import io.agrest.annotation.AgRelationship;
import io.agrest.junit.AgPojoTester;
import io.agrest.meta.AgEntity;
import io.agrest.meta.AgSchema;
import io.agrest.runtime.jackson.IJacksonService;
import io.bootique.junit5.BQTest;
import io.bootique.junit5.BQTestTool;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@BQTest
public class UpdateRequestParserTest {

    @BQTestTool
    static final AgPojoTester tester = AgPojoTester
            .builder()
            .build();

    final AgEntity<O1> o1Entity = tester.runtime().service(AgSchema.class).getEntity(O1.class);
    final AgEntity<O2> o2Entity = tester.runtime().service(AgSchema.class).getEntity(O2.class);

    final IJacksonService jackson = tester.runtime().service(IJacksonService.class);
    final UpdateRequestParser parser = (UpdateRequestParser) tester.runtime().service(IUpdateRequestParser.class);

    @Test
    public void parse_Depth2_ToMany() {
        List<EntityUpdate<O1>> updates = parser.parse(o1Entity,
                "{\"name\":\"a\",\"o2s\":[{\"id\":2,\"name\":\"n2\"},{\"id\":1,\"name\":\"n1\"}]}");

        assertEquals(1, updates.size());

        EntityUpdate<O1> o1 = updates.get(0);
        assertEquals(0, o1.getIdParts().size());
        assertEquals(Map.of("name", "a"), o1.getAttributes());
        assertEquals(Map.of(), o1.getToManyIds());
        assertEquals(Map.of(), o1.getToOneIds());
        assertEquals(0, o1.getToOnes().size());
        assertEquals(1, o1.getToManys().size());
        assertEquals(2, o1.getToMany("o2s").size());

        List<EntityUpdate<O2>> o2s = o1.getToMany("o2s");
        EntityUpdate<O2> o21 = o2s.get(0);
        assertEquals(Map.of("id", 2), o21.getIdParts());
        assertEquals(Map.of("name", "n2"), o21.getAttributes());

        EntityUpdate<O2> o22 = o2s.get(1);
        assertEquals(Map.of("id", 1), o22.getIdParts());
        assertEquals(Map.of("name", "n1"), o22.getAttributes());
    }

    @Test
    public void parse_Depth2_ToManyIdsEmpty() {
        List<EntityUpdate<O1>> updates = parser.parse(o1Entity, "{\"o2s\":[]}");
        assertEquals(1, updates.size());

        EntityUpdate<O1> o1 = updates.get(0);
        assertEquals(1, o1.getToManyIds().size());
        assertEquals(Set.of(), o1.getToManyIds("o2s"));
    }

    @Test
    public void parse_Depth2_ToManyIdAbsent() {
        List<EntityUpdate<O1>> updates = parser.parse(o1Entity, "{}");
        assertEquals(1, updates.size());

        EntityUpdate<O1> o1 = updates.get(0);
        assertEquals(Map.of(), o1.getToManyIds());
        assertNull(o1.getToManyIds("o2s"));
    }

    @Test
    public void parse_Depth2_ToManyEmpty() {
        List<EntityUpdate<O1>> updates = parser.parse(o1Entity, "{\"o2s\":[]}");
        assertEquals(1, updates.size());

        EntityUpdate<O1> o1 = updates.get(0);
        assertEquals(1, o1.getToManys().size());
        assertEquals(List.of(), o1.getToMany("o2s"));
    }

    @Test
    public void parse_Depth2_ToManyAbsent() {
        List<EntityUpdate<O1>> updates = parser.parse(o1Entity, "{}");
        assertEquals(1, updates.size());

        EntityUpdate<O1> o1 = updates.get(0);
        assertEquals(Map.of(), o1.getToManys());
        assertNull(o1.getToMany("o2s"));
    }

    @Test
    public void parse_Depth2_ToOne() {
        List<EntityUpdate<O2>> updates = parser.parse(o2Entity, "{\"id\":2,\"o1\":{\"name\":\"n2\"}}");

        assertEquals(1, updates.size());

        EntityUpdate<O2> o2 = updates.get(0);
        assertEquals(Map.of("id", 2), o2.getIdParts());

        EntityUpdate<O1> o1 = o2.getToOne("o1");
        assertEquals(0, o1.getIdParts().size());
        assertEquals(Map.of("name", "n2"), o1.getAttributes());
    }

    @Test
    public void parse_Depth3() {
        List<EntityUpdate<O1>> updates = parser.parse(o1Entity,
                "{\"name\":\"a\",\"o2s\":[{\"id\":2,\"name\":\"n2\",\"o3\":{\"name\":\"nn1\"}},{\"id\":1,\"name\":\"n1\"}]}");

        assertEquals(1, updates.size());

        EntityUpdate<O1> o1 = updates.get(0);
        assertEquals(0, o1.getIdParts().size());
        assertEquals(Map.of("name", "a"), o1.getAttributes());
        assertEquals(Map.of(), o1.getToManyIds());
        assertEquals(Map.of(), o1.getToOneIds());
        assertEquals(0, o1.getToOnes().size());
        assertEquals(1, o1.getToManys().size());
        assertEquals(2, o1.getToMany("o2s").size());

        List<EntityUpdate<O2>> o2s = o1.getToMany("o2s");
        EntityUpdate<O2> o21 = o2s.get(0);
        assertEquals(Map.of("id", 2), o21.getIdParts());
        assertEquals(Map.of("name", "n2"), o21.getAttributes());

        EntityUpdate<O3> o31 = o21.getToOne("o3");
        assertNotNull(o31);
        assertEquals(Map.of("name", "nn1"), o31.getAttributes());

        EntityUpdate<O2> o22 = o2s.get(1);
        assertEquals(Map.of("id", 1), o22.getIdParts());
        assertEquals(Map.of("name", "n1"), o22.getAttributes());
        assertNull(o22.getToOne("o3"));
    }

    @Test
    public void parse_Depth3_MaxDepth2() {
        JsonNode json = jackson.parseJson("{\"name\":\"a\",\"o2s\":[{\"id\":2,\"name\":\"n2\",\"o3\":{\"name\":\"nn1\"}}]}");

        List<EntityUpdate<O1>> updates = parser.getParser(o1Entity).parse(json, 2);

        EntityUpdate<O1> o1 = updates.get(0);
        assertEquals(1, o1.getToMany("o2s").size());

        List<EntityUpdate<O2>> o2s = o1.getToMany("o2s");
        EntityUpdate<O2> o21 = o2s.get(0);
        assertEquals(Map.of("id", 2), o21.getIdParts());
        assertEquals(Map.of("name", "n2"), o21.getAttributes());

        assertNull(o21.getToOne("o3"), "Must have been cut");
    }


    public static class O1 {

        @AgAttribute
        public String getName() {
            throw new UnsupportedOperationException("not expected to be called");
        }

        @AgRelationship
        public List<O2> getO2s() {
            throw new UnsupportedOperationException("not expected to be called");
        }
    }

    public static class O2 {

        @AgId
        public int getId() {
            throw new UnsupportedOperationException("not expected to be called");
        }

        @AgAttribute
        public String getName() {
            throw new UnsupportedOperationException("not expected to be called");
        }

        @AgRelationship
        public O1 getO1() {
            throw new UnsupportedOperationException("not expected to be called");
        }

        @AgRelationship
        public O3 getO3() {
            throw new UnsupportedOperationException("not expected to be called");
        }
    }

    public static class O3 {

        @AgAttribute
        public String getName() {
            throw new UnsupportedOperationException("not expected to be called");
        }
    }
}
