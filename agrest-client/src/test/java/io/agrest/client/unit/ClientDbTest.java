package io.agrest.client.unit;

import io.agrest.cayenne.unit.AgCayenneTester;
import io.agrest.cayenne.unit.DbTest;
import io.agrest.client.AgClient;

public class ClientDbTest extends DbTest {

    protected AgClient client(AgCayenneTester tester) {
        return AgClient.client(tester.internalTarget());
    }

    protected AgClient client(AgCayenneTester tester, String path) {
        return AgClient.client(tester.internalTarget().path(path));
    }
}
