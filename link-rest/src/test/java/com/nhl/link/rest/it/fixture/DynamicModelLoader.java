package com.nhl.link.rest.it.fixture;

import org.apache.cayenne.DataChannel;
import org.apache.cayenne.DataChannelFilter;
import org.apache.cayenne.DataChannelFilterChain;
import org.apache.cayenne.E25;
import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.QueryResponse;
import org.apache.cayenne.graph.GraphDiff;
import org.apache.cayenne.map.ObjAttribute;
import org.apache.cayenne.map.ObjEntity;
import org.apache.cayenne.query.Query;

public class DynamicModelLoader implements DataChannelFilter {

    @Override
    public void init(DataChannel channel) {

        // load dynamic attributes and relationships...

        ObjEntity e25 = channel.getEntityResolver().getObjEntity(E25.class);
        ObjAttribute dynamicAttribute = new ObjAttribute("name", "java.lang.String", e25);
        dynamicAttribute.setDbAttributePath("name");
        e25.addAttribute(dynamicAttribute);
    }

    @Override
    public QueryResponse onQuery(ObjectContext originatingContext, Query query, DataChannelFilterChain filterChain) {
        return filterChain.onQuery(originatingContext, query);
    }

    @Override
    public GraphDiff onSync(ObjectContext originatingContext,
                            GraphDiff changes,
                            int syncType,
                            DataChannelFilterChain filterChain) {
        return filterChain.onSync(originatingContext, changes, syncType);
    }
}
