package com.nhl.link.rest.runtime.parser.cache;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.ws.rs.core.Response.Status;

import org.apache.cayenne.exp.ExpressionException;
import org.apache.cayenne.exp.parser.ASTDbPath;
import org.apache.cayenne.exp.parser.ASTObjPath;
import org.apache.cayenne.exp.parser.ASTPath;
import org.apache.cayenne.map.ObjAttribute;
import org.apache.cayenne.map.ObjEntity;
import org.apache.cayenne.map.ObjRelationship;
import org.apache.cayenne.map.PathComponent;

import com.nhl.link.rest.LinkRestException;
import com.nhl.link.rest.runtime.parser.PathConstants;

class EntityPathCache {

	private ObjEntity entity;
	private Map<String, PathDescriptor> pathCache;

	EntityPathCache(ObjEntity entity) {
		this.entity = entity;
		this.pathCache = new ConcurrentHashMap<>();

		// immediately cache a special entry matching "id" constant

		final ObjAttribute pk = entity.getPrimaryKeys().iterator().next();
		final ASTPath dbPath = new ASTDbPath(pk.getDbAttributeName());

		pathCache.put(PathConstants.ID_PK_ATTRIBUTE, new PathDescriptor() {

			@Override
			public boolean isAttribute() {
				return true;
			}

			@Override
			public String getType() {
				return pk.getType();
			}

			@Override
			public ASTPath getPathExp() {
				return dbPath;
			}
		});
	}

	PathDescriptor getPathDescriptor(final ASTObjPath path) {

		PathDescriptor entry = pathCache.get(path.getPath());
		if (entry == null) {

			final PathComponent<ObjAttribute, ObjRelationship> pc;

			try {
				pc = entity.lastPathComponent(path, Collections.emptyMap());
			} catch (ExpressionException e) {
				// bad path
				throw new LinkRestException(Status.BAD_REQUEST, "Invalid path '" + path.getPath() + "' for '"
						+ entity.getName() + "'");
			}

			entry = new PathDescriptor() {

				@Override
				public boolean isAttribute() {
					return pc.getAttribute() != null;
				}

				@Override
				public String getType() {
					return isAttribute() ? pc.getAttribute().getType() : pc.getRelationship().getTargetEntity()
							.getClassName();
				}

				@Override
				public ASTPath getPathExp() {
					return path;
				}
			};

			pathCache.put(path.getPath(), entry);
		}

		return entry;
	}
}
