package io.agrest.j17;

import io.agrest.annotation.AgAttribute;
import io.agrest.annotation.AgId;
import io.agrest.annotation.AgRelationship;
import io.agrest.j17.junit.AgPojoTester;
import io.agrest.meta.AgEntity;
import io.agrest.meta.AgIdPart;
import io.agrest.meta.AgSchema;
import io.bootique.junit5.BQTest;
import io.bootique.junit5.BQTestTool;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@BQTest
public class RecordCompilerTest {

    @BQTestTool
    static final AgPojoTester tester = AgPojoTester.builder()
            .build();

    @Test
    public void testRecordPropertiesRecognized() {
        AgSchema schema = tester.runtime().service(AgSchema.class);
        AgEntity<R1> r1 = schema.getEntity(R1.class);

        String ids = r1.getIdParts().stream().map(AgIdPart::getName).sorted().collect(Collectors.joining(","));
        assertEquals("agId", ids);

        String attributes = r1.getAttributes().stream().map(io.agrest.meta.AgAttribute::getName).sorted().collect(Collectors.joining(","));
        assertEquals("agAt1,agAt2", attributes);
        assertTrue(r1.getAttribute("agAt1").isReadable());
        assertFalse(r1.getAttribute("agAt1").isWritable());
        assertFalse(r1.getAttribute("agAt2").isReadable());
        assertTrue(r1.getAttribute("agAt2").isWritable());

        String relationships = r1.getRelationships().stream().map(io.agrest.meta.AgRelationship::getName).sorted().collect(Collectors.joining(","));
        assertEquals("agRel1,agRel2", relationships);
        assertFalse(r1.getRelationship("agRel1").isToMany());
        assertTrue(r1.getRelationship("agRel2").isToMany());
    }

    record R1(
            @AgId long agId,
            @AgAttribute(writable = false) String agAt1,
            @AgAttribute(readable = false) LocalDate agAt2,
            @AgRelationship R2 agRel1,
            @AgRelationship List<R3> agRel2,
            Object nonAg) {
    }

    record R2(Object nonAg, @AgAttribute int agAt) {
    }

    record R3(Object nonAg, @AgAttribute int agAt) {
    }
}
