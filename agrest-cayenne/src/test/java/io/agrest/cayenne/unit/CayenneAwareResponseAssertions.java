package io.agrest.cayenne.unit;

import io.agrest.it.fixture.ResponseAssertions;

import javax.ws.rs.core.Response;

import static org.junit.jupiter.api.Assertions.*;

public class CayenneAwareResponseAssertions extends ResponseAssertions<CayenneAwareResponseAssertions> {

    private CayenneOpCounter opCounter;

    public CayenneAwareResponseAssertions(Response response, CayenneOpCounter opCounter) {
        super(response);
        this.opCounter = opCounter;
    }

    public CayenneAwareResponseAssertions ranQueries(int expectedQueries) {
        assertEquals("Unexpected number of queries was run", expectedQueries, opCounter.getQueryCounter());
        return this;
    }

    public CayenneAwareResponseAssertions ranCommits(int expectedCommits) {
        assertEquals("Unexpected number of commits was executed", expectedCommits, opCounter.getCommitCount());
        return this;
    }
}
