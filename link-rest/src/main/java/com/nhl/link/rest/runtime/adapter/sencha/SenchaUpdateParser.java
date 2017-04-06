package com.nhl.link.rest.runtime.adapter.sencha;

import com.fasterxml.jackson.databind.JsonNode;
import com.nhl.link.rest.meta.LrEntity;
import com.nhl.link.rest.parser.converter.GenericConverter;
import com.nhl.link.rest.runtime.jackson.IJacksonService;
import com.nhl.link.rest.runtime.parser.EntityJsonTraverser;
import com.nhl.link.rest.runtime.parser.EntityJsonVisitor;
import com.nhl.link.rest.runtime.parser.UpdateParser;
import com.nhl.link.rest.runtime.semantics.IRelationshipMapper;
import org.apache.cayenne.di.Inject;

import java.util.regex.Pattern;

/**
 * Strips off Sencha-generated temporary IDs from the update data structures.
 * 
 * @since 1.20
 */
public class SenchaUpdateParser extends UpdateParser {

	private static final Pattern DASH_ID_PATTERN = Pattern.compile(".-[\\d]+$");

	private EntityJsonTraverser senchaEntityJsonTraverser;

	public SenchaUpdateParser(@Inject IRelationshipMapper relationshipMapper, @Inject IJacksonService jacksonService) {
		super(relationshipMapper, jacksonService);
		senchaEntityJsonTraverser = new SenchaEntityJsonTraverser(relationshipMapper);
	}

	protected boolean isTempId(Object value) {
		if (value instanceof String) {
			String idString = (String) value;
			if (DASH_ID_PATTERN.matcher(idString).find()) {
				return true;
			}
		}

		return false;
	}

	@Override
	protected EntityJsonTraverser entityJsonTraverser() {
		return senchaEntityJsonTraverser;
	}

	private class SenchaEntityJsonTraverser extends EntityJsonTraverser {

		public SenchaEntityJsonTraverser(IRelationshipMapper relationshipMapper) {
			super(relationshipMapper);
		}

		@Override
		protected void extractPK(LrEntity<?> entity, EntityJsonVisitor visitor, JsonNode valueNode) {
			Object value = GenericConverter.converter().value(valueNode);

			// if PK is a Sencha temporary value, completely ignore it...
			if (!isTempId(value)) {
				super.extractPK(entity, visitor, valueNode);
			}
		}
	}
}
