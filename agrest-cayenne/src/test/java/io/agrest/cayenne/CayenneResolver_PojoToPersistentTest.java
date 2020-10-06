package io.agrest.cayenne;

import io.agrest.cayenne.unit.CayenneNoDbTest;
import io.agrest.cayenne.cayenne.main.E25;
import io.agrest.pojo.model.PX1;
import io.agrest.pojo.runtime.PX1RootResolver;
import io.agrest.meta.AgEntity;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class CayenneResolver_PojoToPersistentTest extends CayenneNoDbTest {

    @Test
    public void testFail_nestedViaQueryWithParentIds() {
        try {
            AgEntity
                    .overlay(PX1.class)
                    .redefineRootDataResolver(new PX1RootResolver())
                    // Cayenne resolver will fail as the parent is not a Cayenne object
                    .redefineToOne("overlayToOne", E25.class, CayenneResolvers.nested(mockCayennePersister).viaQueryWithParentIds());

            fail("Must have failed");
        } catch (IllegalStateException e) {
            assertEquals("Entity 'PX1' is not mapped in Cayenne, so its relationship 'overlayToOne' can't be resolved with a Cayenne resolver",
                    e.getMessage());
        }
    }

    @Test
    public void testFail_nestedViaQueryWithParentExp() {

        try {
            AgEntity
                    .overlay(PX1.class)
                    .redefineRootDataResolver(new PX1RootResolver())
                    // Cayenne resolver will fail as the parent is not a Cayenne object
                    .redefineToOne("overlayToOne", E25.class, CayenneResolvers.nested(mockCayennePersister).viaQueryWithParentExp());

            fail("Must have failed");
        } catch (IllegalStateException e) {
            assertEquals("Entity 'PX1' is not mapped in Cayenne, so its relationship 'overlayToOne' can't be resolved with a Cayenne resolver",
                    e.getMessage());
        }
    }

    @Test
    public void testFailWithCayenneResolver_nestedViaJointParentPrefetch() {

        try {
            AgEntity
                    .overlay(PX1.class)
                    .redefineRootDataResolver(new PX1RootResolver())
                    // Cayenne resolver will fail as the parent is not a Cayenne object
                    .redefineToOne("overlayToOne", E25.class, CayenneResolvers.nested(mockCayennePersister).viaParentPrefetch());

            fail("Must have failed");
        } catch (IllegalStateException e) {
            assertEquals("Entity 'PX1' is not mapped in Cayenne, so its relationship 'overlayToOne' can't be resolved with a Cayenne resolver",
                    e.getMessage());
        }
    }
}
