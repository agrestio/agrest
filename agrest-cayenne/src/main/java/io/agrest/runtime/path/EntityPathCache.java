package io.agrest.runtime.path;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.ws.rs.core.Response.Status;

import io.agrest.AgException;
import io.agrest.PathConstants;
import io.agrest.meta.AgAttribute;
import io.agrest.meta.AgEntity;
import io.agrest.meta.AgRelationship;
import org.apache.cayenne.exp.parser.ASTObjPath;
import org.apache.cayenne.exp.parser.ASTPath;

class EntityPathCache {

	private AgEntity<?> entity;
	private Map<String, PathDescriptor> pathCache;

	EntityPathCache(final AgEntity<?> entity) {
		this.entity = entity;
		this.pathCache = new ConcurrentHashMap<>();

		// immediately cache a special entry matching "id" constant ... if there
		// is a single ID

		// TODO: single ID check allows us to support id-less entities (quite
		// common, e.g. various aggregated data reports). However it does not
		// solve an issue of more common case of entities with multi-column ID.
		// Will need a concept of "virtual" ID built from ObjectId (or POJO id
		// properties) via cayenne-lifecycle.

		if (entity.getIds().size() == 1) {

			pathCache.put(PathConstants.ID_PK_ATTRIBUTE, new PathDescriptor() {

				AgAttribute id = entity.getIds().iterator().next();

				@Override
				public boolean isAttribute() {
					return true;
				}

				@Override
				public Class<?> getType() {
					return id.getType();
				}

				@Override
				public ASTPath getPathExp() {
					return id.getPathExp();
				}
			});
		}
	}

	PathDescriptor getPathDescriptor(final ASTObjPath path) {

		PathDescriptor entry = pathCache.get(path.getPath());
		if (entry == null) {

			String stringPath = (String) path.getOperand(0);
			final Object last = lastPathComponent(entity, stringPath);

			if (last instanceof AgAttribute) {
				entry = new PathDescriptor() {

					AgAttribute attribute = (AgAttribute) last;

					@Override
					public boolean isAttribute() {
						return true;
					}

					@Override
					public Class<?> getType() {
						return attribute.getType();
					}

					@Override
					public ASTPath getPathExp() {
						return path;
					}
				};
			} else {
				entry = new PathDescriptor() {

					AgRelationship relationship = (AgRelationship) last;
					Class<?> type = relationship.getTargetEntity().getType();

					@Override
					public boolean isAttribute() {
						return false;
					}

					@Override
					public Class<?> getType() {
						return type;
					}

					@Override
					public ASTPath getPathExp() {
						return path;
					}
				};
			}

			pathCache.put(path.getPath(), entry);
		}

		return entry;
	}

	Object lastPathComponent(AgEntity<?> entity, String path) {

		int dot = path.indexOf(PathConstants.DOT);

		if (dot == 0 || dot == path.length() - 1) {
			throw new AgException(Status.BAD_REQUEST, "Invalid path '" + path + "' for '" + entity.getName()
					+ "'");
		}

		if (dot > 0) {
			String segment = toRelationshipName(path.substring(0, dot));

			// must be a relationship ..
			AgRelationship relationship = entity.getRelationship(segment);
			if (relationship == null) {
				throw new AgException(Status.BAD_REQUEST, "Invalid path '" + path + "' for '" + entity.getName()
						+ "'. Not a relationship");
			}

			AgEntity<?> targetEntity = relationship.getTargetEntity();
			return lastPathComponent(targetEntity, path.substring(dot + 1));
		}

		// can be a relationship or an attribute
		AgAttribute attribute = entity.getAttribute(path);
		if (attribute != null) {
			return attribute;
		}

		AgRelationship relationship = entity.getRelationship(toRelationshipName(path));
		if (relationship != null) {
			return relationship;
		}

		throw new AgException(Status.BAD_REQUEST, "Invalid path '" + path + "' for '" + entity.getName() + "'");
	}

	private String toRelationshipName(String pathSegment) {
		return pathSegment.endsWith("+") ? pathSegment.substring(0, pathSegment.length() - 1) : pathSegment;
	}
}
