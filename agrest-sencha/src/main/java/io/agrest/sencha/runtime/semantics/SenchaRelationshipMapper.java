package io.agrest.sencha.runtime.semantics;

import io.agrest.meta.AgEntity;
import io.agrest.meta.AgRelationship;
import io.agrest.runtime.semantics.IRelationshipMapper;

/**
 * @since 1.8
 */
public class SenchaRelationshipMapper implements IRelationshipMapper {

	private static final String SUFFIX = "_id";

	@Override
	public String toRelatedIdName(AgRelationship relationship) {
		return relationship.getName() + SUFFIX;
	}

	@Override
	public AgRelationship toRelationship(AgEntity<?> root, String relatedIdName) {

		// allow both relname_id and relname forms ...

		if (relatedIdName.length() > SUFFIX.length() && relatedIdName.endsWith(SUFFIX)) {
			String baseName = relatedIdName.substring(0, relatedIdName.length() - SUFFIX.length());
			return root.getRelationship(baseName);
		} else {
			return root.getRelationship(relatedIdName);
		}
	}

}
