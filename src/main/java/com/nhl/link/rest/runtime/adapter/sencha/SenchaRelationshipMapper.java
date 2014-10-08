package com.nhl.link.rest.runtime.adapter.sencha;

import org.apache.cayenne.map.ObjEntity;
import org.apache.cayenne.map.ObjRelationship;

import com.nhl.link.rest.runtime.semantics.IRelationshipMapper;

/**
 * @since 1.8
 */
public class SenchaRelationshipMapper implements IRelationshipMapper {

	private static final String SUFFIX = "_id";

	@Override
	public String toRelatedIdName(ObjRelationship relationship) {
		return relationship.getName() + SUFFIX;
	}

	@Override
	public ObjRelationship toRelationship(ObjEntity root, String relatedIdName) {

		if (relatedIdName.length() > SUFFIX.length() && relatedIdName.endsWith(SUFFIX)) {
			String baseName = relatedIdName.substring(0, relatedIdName.length() - SUFFIX.length());
			return (ObjRelationship) root.getRelationship(baseName);
		}

		return null;
	}

}
