package com.nhl.link.rest.runtime.config;

import java.util.Iterator;
import java.util.Map.Entry;

import javax.ws.rs.core.Response.Status;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nhl.link.rest.Entity;
import com.nhl.link.rest.DataResponse;
import com.nhl.link.rest.DataResponseConfig;
import com.nhl.link.rest.EntityConfig;
import com.nhl.link.rest.LinkRestException;
import com.nhl.link.rest.runtime.parser.PathConstants;

/**
 * An {@link IConfigMerger} that ensures that no target attributes exceed the
 * bounds defined in the source config.
 * 
 * @since 1.1
 */
public class IntersectConfigMerger implements IConfigMerger {

	private static final Logger LOGGER = LoggerFactory.getLogger(IntersectConfigMerger.class);

	@Override
	public void merge(DataResponseConfig source, DataResponse<?> target) {

		// fetchOffset - do not exceed source offset
		int upperOffset = source.getFetchOffset();
		if (upperOffset > 0 && target.getFetchOffset() > upperOffset) {
			LOGGER.info("Reducing fetch offset from " + target.getFetchOffset() + " to max allowed value of "
					+ upperOffset);
			target.withFetchOffset(upperOffset);
		}

		// fetchLimit - do not exceed source limit
		int upperLimit = source.getFetchLimit();
		if (upperLimit > 0 && target.getFetchLimit() > upperLimit) {
			LOGGER.info("Reducing fetch limit from " + target.getFetchLimit() + " to max allowed value of "
					+ upperLimit);
			target.withFetchLimit(upperLimit);
		}

		// entity - ensure attribute/relationship tree span of source is not
		// exceeded in target. Null source means we don't need to worry about
		// unauthorized attributes and relationships
		if (target.getEntity() != null) {
			mergeEntity(source.getEntity(), target.getEntity());
		}
	}

	protected void mergeEntity(EntityConfig source, Entity<?> target) {

		if (!source.isIdIncluded()) {
			target.setIdIncluded(false);
		}

		Iterator<String> ait = target.getAttributes().iterator();
		while (ait.hasNext()) {

			String a = ait.next();
			if (!source.hasAttribute(a)) {
				LOGGER.info("Attribute not allowed, removing: " + a);
				ait.remove();
			}
		}

		Iterator<Entry<String, Entity<?>>> rit = target.getRelationships().entrySet().iterator();
		while (rit.hasNext()) {

			Entry<String, Entity<?>> e = rit.next();
			EntityConfig sourceChild = source.getChild(e.getKey());
			if (sourceChild != null) {

				// removing recursively ... the depth or recursion depends on
				// the depth of target, which is server-controlled. So it should
				// be a reasonably safe operation in regard to stack overflow
				mergeEntity(sourceChild, e.getValue());
			} else {
				LOGGER.info("Relationship not allowed, removing: " + e.getKey());
				rit.remove();
			}
		}

		if (source.getQualifier() != null) {
			target.andQualifier(source.getQualifier());
		}

		// process 'mapByPath' ... treat it as a regular relationship/attribute
		// path.. Ignoring 'mapBy', presuming it matches the path. This way we
		// can simply check for one single path, not for all attributes in the
		// entities involved.

		if (target.getMapByPath() != null && !allowedMapBy(source, target.getMapByPath())) {
			LOGGER.info("'mapBy' not allowed, removing: " + target.getMapByPath());
			target.setMapByPath(null);
			target.setMapBy(null);
		}
	}

	protected boolean allowedMapBy(EntityConfig source, String path) {

		int dot = path.indexOf(PathConstants.DOT);

		if (dot == 0) {
			throw new LinkRestException(Status.BAD_REQUEST, "Path starts with dot: " + path);
		}

		if (dot == path.length() - 1) {
			throw new LinkRestException(Status.BAD_REQUEST, "Path ends with dot: " + path);
		}

		if (dot > 0) {
			// process intermediate component
			String property = path.substring(0, dot);
			EntityConfig child = source.getChild(property);
			return child != null && allowedMapBy(child, path.substring(dot + 1));

		} else {
			return allowedMapBy_LastComponent(source, path);
		}
	}

	protected boolean allowedMapBy_LastComponent(EntityConfig source, String path) {

		// process last component
		String property = path;

		if (property == null || property.length() == 0 || property.equals(PathConstants.ID_PK_ATTRIBUTE)) {
			return source.isIdIncluded();
		}

		if (source.hasAttribute(property)) {
			return true;
		}

		EntityConfig child = source.getChild(property);
		return child != null && allowedMapBy_LastComponent(child, null);
	}
}
