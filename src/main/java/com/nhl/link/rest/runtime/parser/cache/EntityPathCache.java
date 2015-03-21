package com.nhl.link.rest.runtime.parser.cache;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.ws.rs.core.Response.Status;

import org.apache.cayenne.exp.parser.ASTObjPath;
import org.apache.cayenne.exp.parser.ASTPath;

import com.nhl.link.rest.LinkRestException;
import com.nhl.link.rest.meta.LrAttribute;
import com.nhl.link.rest.meta.LrEntity;
import com.nhl.link.rest.meta.LrRelationship;
import com.nhl.link.rest.runtime.meta.IMetadataService;
import com.nhl.link.rest.runtime.parser.PathConstants;

class EntityPathCache {

	private LrEntity<?> entity;
	private Map<String, PathDescriptor> pathCache;
	private IMetadataService metadataService;

	EntityPathCache(final LrEntity<?> entity, IMetadataService metadataService) {
		this.entity = entity;
		this.metadataService = metadataService;
		this.pathCache = new ConcurrentHashMap<>();

		// immediately cache a special entry matching "id" constant
		pathCache.put(PathConstants.ID_PK_ATTRIBUTE, new PathDescriptor() {

			LrAttribute id = entity.getSingleId();

			@Override
			public boolean isAttribute() {
				return true;
			}

			@Override
			public String getType() {
				return id.getJavaType();
			}

			@Override
			public ASTPath getPathExp() {
				return id.getPathExp();
			}
		});
	}

	PathDescriptor getPathDescriptor(final ASTObjPath path) {

		PathDescriptor entry = pathCache.get(path.getPath());
		if (entry == null) {

			String stringPath = (String) path.getOperand(0);
			final Object last = lastPathComponent(entity, stringPath);

			if (last instanceof LrAttribute) {
				entry = new PathDescriptor() {

					LrAttribute attribute = (LrAttribute) last;

					@Override
					public boolean isAttribute() {
						return true;
					}

					@Override
					public String getType() {
						return attribute.getJavaType();
					}

					@Override
					public ASTPath getPathExp() {
						return path;
					}
				};
			} else {
				entry = new PathDescriptor() {

					LrRelationship relationship = (LrRelationship) last;

					@Override
					public boolean isAttribute() {
						return false;
					}

					@Override
					public String getType() {
						return relationship.getTargetEntityType().getName();
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

	Object lastPathComponent(LrEntity<?> entity, String path) {

		int dot = path.indexOf(PathConstants.DOT);

		if (dot == 0 || dot == path.length() - 1) {
			throw new LinkRestException(Status.BAD_REQUEST, "Invalid path '" + path + "' for '" + entity.getName()
					+ "'");
		}

		if (dot > 0) {
			String segment = path.substring(0, dot);

			String pathSegment = segment.endsWith("+") ? segment.substring(0, segment.length() - 1) : segment;

			// must be a relationship ..
			LrRelationship relationship = entity.getRelationship(pathSegment);
			if (relationship == null) {
				throw new LinkRestException(Status.BAD_REQUEST, "Invalid path '" + path + "' for '" + entity.getName()
						+ "'. Not a relationship");
			}

			LrEntity<?> targetEntity = metadataService.getLrEntity(relationship.getTargetEntityType());
			return lastPathComponent(targetEntity, path.substring(dot + 1));
		}

		// can be a relationship or an attribute
		LrAttribute attribute = entity.getAttribute(path);
		if (attribute != null) {
			return attribute;
		}

		// if not an attribute, take into account a possibility of outer join
		String pathSegment = path.endsWith("+") ? path.substring(0, path.length() - 1) : path;
		LrRelationship relationship = entity.getRelationship(pathSegment);
		if (relationship != null) {
			return relationship;
		}

		throw new LinkRestException(Status.BAD_REQUEST, "Invalid path '" + path + "' for '" + entity.getName() + "'");
	}
}
