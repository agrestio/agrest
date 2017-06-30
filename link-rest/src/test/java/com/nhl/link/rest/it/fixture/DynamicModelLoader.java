package com.nhl.link.rest.it.fixture;

import com.nhl.link.rest.it.fixture.cayenne.E22;
import com.nhl.link.rest.it.fixture.cayenne.E25;
import org.apache.cayenne.DataChannel;
import org.apache.cayenne.DataChannelFilter;
import org.apache.cayenne.DataChannelFilterChain;
import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.QueryResponse;
import org.apache.cayenne.graph.GraphDiff;
import org.apache.cayenne.map.ObjAttribute;
import org.apache.cayenne.map.ObjEntity;
import org.apache.cayenne.map.ObjRelationship;
import org.apache.cayenne.query.Query;

public class DynamicModelLoader implements DataChannelFilter {

    @Override
    public void init(DataChannel channel) {

        // load dynamic attributes and relationships... Will be testing whether LR can handle generic Cayenne properties
        // with no getters/setters.

        ObjEntity e25 = channel.getEntityResolver().getObjEntity(E25.class);
        ObjAttribute dynamicAttribute = new ObjAttribute("name", "java.lang.String", e25);
        dynamicAttribute.setDbAttributePath("name");
        e25.addAttribute(dynamicAttribute);

        ObjEntity e22 = channel.getEntityResolver().getObjEntity(E22.class);
        ObjRelationship e25_e22 = new ObjRelationship("e22");
        e25_e22.setTargetEntityName(e22.getName());
        e25_e22.setSourceEntity(e25);
        e25_e22.setDbRelationshipPath("e22");
        e25.addRelationship(e25_e22);
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
