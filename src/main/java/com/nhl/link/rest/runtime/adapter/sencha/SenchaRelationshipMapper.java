package com.nhl.link.rest.runtime.adapter.sencha;

import com.nhl.link.rest.meta.LrEntity;
import com.nhl.link.rest.meta.LrRelationship;
import com.nhl.link.rest.runtime.semantics.IRelationshipMapper;

/**
 * @since 1.8
 */
public class SenchaRelationshipMapper implements IRelationshipMapper {

	private static final String SUFFIX = "_id";

	@Override
	public String toRelatedIdName(LrRelationship relationship) {
		return relationship.getName() + SUFFIX;
	}

	@Override
	public LrRelationship toRelationship(LrEntity<?> root, String relatedIdName) {

		if (relatedIdName.length() > SUFFIX.length() && relatedIdName.endsWith(SUFFIX)) {
			String baseName = relatedIdName.substring(0, relatedIdName.length() - SUFFIX.length());
			return root.getRelationship(baseName);
		}

		return null;
	}

}
