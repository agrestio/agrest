package io.agrest.it.fixture;

import org.apache.cayenne.DataChannel;
import org.apache.cayenne.DataChannelFilter;
import org.apache.cayenne.DataChannelFilterChain;
import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.QueryResponse;
import org.apache.cayenne.graph.GraphDiff;
import org.apache.cayenne.query.Query;
import org.junit.rules.ExternalResource;

/**
 * Collects Cayenne query statistics within JUnit run.
 */
public class CayenneOpCounter extends ExternalResource implements DataChannelFilter {

    private int commitCounter;
    private int queryCounter;

    public int getCommitCount() {
        return commitCounter;
    }

    public int getQueryCounter() {
        return queryCounter;
    }

    @Override
    protected void before() {
        commitCounter = 0;
        queryCounter = 0;
    }

    @Override
    public void init(DataChannel channel) {
        // do nothing...
    }

    @Override
    public QueryResponse onQuery(ObjectContext originatingContext, Query query, DataChannelFilterChain filterChain) {
        queryCounter++;
        return filterChain.onQuery(originatingContext, query);
    }

    @Override
    public GraphDiff onSync(ObjectContext originatingContext, GraphDiff changes, int syncType, DataChannelFilterChain filterChain) {

        if (syncType == DataChannel.FLUSH_CASCADE_SYNC) {
            commitCounter++;
        }

        return filterChain.onSync(originatingContext, changes, syncType);
    }
}
