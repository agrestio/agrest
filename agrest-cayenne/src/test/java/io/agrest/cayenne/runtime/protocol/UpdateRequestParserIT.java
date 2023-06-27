package io.agrest.cayenne.runtime.protocol;

import io.agrest.EntityUpdate;
import io.agrest.cayenne.cayenne.main.E2;
import io.agrest.cayenne.cayenne.main.E3;
import io.agrest.cayenne.unit.main.MainDbTest;
import io.agrest.cayenne.unit.main.MainModelTester;
import io.agrest.meta.AgEntity;
import io.agrest.meta.AgSchema;
import io.agrest.runtime.protocol.IUpdateRequestParser;
import io.agrest.runtime.protocol.UpdateRequestParser;
import io.bootique.junit5.BQTestTool;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class UpdateRequestParserIT extends MainDbTest {

    @BQTestTool
    static final MainModelTester tester = tester().build();

    final AgEntity<E2> e2Entity = tester.runtime().service(AgSchema.class).getEntity(E2.class);
    final UpdateRequestParser parse = (UpdateRequestParser) tester.runtime().service(IUpdateRequestParser.class);

    @Test
    public void parse_Nested_ToMany() {
        List<EntityUpdate<E2>> updates = parse.parse(e2Entity,
                "{\"name\":\"a\",\"e3s\":[{\"id\":2,\"name\":\"n2\"},{\"id\":1,\"name\":\"n1\"}]}");

        assertEquals(1, updates.size());

        EntityUpdate<E2> e2 = updates.get(0);
        assertEquals(0, e2.getIdParts().size());
        assertEquals(Map.of("name", "a"), e2.getAttributes());
        assertEquals(Map.of(), e2.getToManyIds());
        assertEquals(Map.of(), e2.getToOneIds());
        assertEquals(0, e2.getToOnes().size());
        assertEquals(1, e2.getToManys().size());
        assertEquals(2, e2.getToMany("e3s").size());

        List<EntityUpdate<E3>> e3s1 = e2.getToMany("e3s");
        EntityUpdate<E3> e31 = e3s1.get(0);
        assertEquals(Map.of("db:id_", 2), e31.getIdParts());
        assertEquals(Map.of("name", "n2"), e31.getAttributes());

        EntityUpdate<E3> e32 = e3s1.get(1);
        assertEquals(Map.of("db:id_", 1), e32.getIdParts());
        assertEquals(Map.of("name", "n1"), e32.getAttributes());
    }
}
