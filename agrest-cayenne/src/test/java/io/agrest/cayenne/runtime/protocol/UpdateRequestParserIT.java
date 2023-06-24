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
import static org.junit.jupiter.api.Assertions.assertNull;


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
        assertNull(e2.getId());
        assertEquals(Map.of("name", "a"), e2.getValues());
        assertEquals(Map.of(), e2.getRelatedIds());
        assertEquals(1, e2.getRelatedUpdates().size());
        assertEquals(2, e2.getRelatedUpdates().get("e3s").size());

        EntityUpdate<E3> e31 = (EntityUpdate<E3>) e2.getRelatedUpdates().get("e3s").get(0);
        assertEquals(Map.of("db:id_", 2), e31.getId());
        assertEquals(Map.of("name", "n2"), e31.getValues());

        EntityUpdate<E3> e32 = (EntityUpdate<E3>) e2.getRelatedUpdates().get("e3s").get(1);
        assertEquals(Map.of("db:id_", 1), e32.getId());
        assertEquals(Map.of("name", "n1"), e32.getValues());
    }
}
