package io.agrest.client.unit;

import io.agrest.client.AgClient;
import io.agrest.unit.AgPojoTester;
import io.agrest.unit.PojoTest;

public class ClientPojoTest extends PojoTest {

    protected AgClient client(AgPojoTester tester) {
        return AgClient.client(tester.internalTarget());
    }

    protected AgClient client(AgPojoTester tester, String path) {
        return AgClient.client(tester.internalTarget().path(path));
    }
}
