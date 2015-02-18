package com.nhl.link.rest.runtime.cayenne;

import javax.ws.rs.core.Response.Status;

import com.nhl.link.rest.ResourceEntity;
import org.apache.cayenne.Cayenne;
import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.ObjectId;
import org.apache.cayenne.map.ObjEntity;
import org.apache.cayenne.query.ObjectIdQuery;

import com.nhl.link.rest.LinkRestException;
import org.apache.cayenne.query.PrefetchTreeNode;

import java.util.Map;

/**
 * @since 1.7
 */
class Util {

	private Util() {
	}

	/**
	 * @since 1.7
	 */
	@SuppressWarnings("unchecked")
	static <A> A findById(ObjectContext context, Class<A> type, Object id) {
		ObjEntity entity = context.getEntityResolver().getObjEntity(type);

		// sanity checking...
		if (entity == null) {
			throw new LinkRestException(Status.INTERNAL_SERVER_ERROR, "Unknown entity class: " + type);
		}

		if (id == null) {
			throw new LinkRestException(Status.BAD_REQUEST, "No id specified");
		}

		String idName = entity.getPrimaryKeyNames().iterator().next();
		ObjectIdQuery select = new ObjectIdQuery(new ObjectId(entity.getName(), idName, id));
		return (A) Cayenne.objectForQuery(context, select);
	}

	/**
	 * @since 1.14
	 */
	static PrefetchTreeNode createPrefetch(ResourceEntity<?> entity, int prefetchSemantics, String path) {
        return createPrefetches(PrefetchTreeNode.withPath(path, prefetchSemantics).getNode(path), entity, prefetchSemantics);
    }

	/**
	 * @since 1.14
	 */
	static PrefetchTreeNode createPrefetch(ResourceEntity<?> entity, int prefetchSemantics) {
        return createPrefetches(new PrefetchTreeNode(), entity, prefetchSemantics);
    }

    private static PrefetchTreeNode createPrefetches(PrefetchTreeNode root, ResourceEntity<?> entity, int prefetchSemantics) {
		for (Map.Entry<String, ResourceEntity<?>> e : entity.getChildren().entrySet()) {
			PrefetchTreeNode child = root.addPath(e.getKey());

			// always full prefetch related entities... we can't use phantom as
			// this will hit object cache and hence won't be cache controlled
			// via query cache anymore...
			child.setPhantom(false);
			child.setSemantics(prefetchSemantics);

			createPrefetches(child, e.getValue(), prefetchSemantics);
		}

		if (entity.getMapBy() != null) {
			root = createPrefetches(root, entity.getMapBy(), prefetchSemantics);
		}

		return root;
	}
}
