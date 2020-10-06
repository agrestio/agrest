package io.agrest.sencha.ops.unit;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class SenchaBodyAssertions {

    private static final String RESPONSE_START = "{\"success\":true,";

    public static String checkAndNormalizeBody(String agBody) {
        assertTrue(agBody.startsWith(RESPONSE_START), () -> "Not a valid Ag Sencha response: " + agBody);
        return "{" + agBody.substring(RESPONSE_START.length());
    }
}
