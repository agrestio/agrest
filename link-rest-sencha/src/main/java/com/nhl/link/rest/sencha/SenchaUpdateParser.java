package com.nhl.link.rest.sencha;

import com.fasterxml.jackson.databind.JsonNode;
import com.nhl.link.rest.meta.LrEntity;
import com.nhl.link.rest.runtime.jackson.IJacksonService;
import com.nhl.link.rest.runtime.parser.EntityJsonTraverser;
import com.nhl.link.rest.runtime.parser.EntityJsonVisitor;
import com.nhl.link.rest.runtime.parser.UpdateParser;
import com.nhl.link.rest.runtime.parser.converter.IJsonValueConverterFactory;
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

	public SenchaUpdateParser(@Inject IRelationshipMapper relationshipMapper,
							  @Inject IJacksonService jacksonService,
							  @Inject IJsonValueConverterFactory converterFactory) {
		super(relationshipMapper, jacksonService, converterFactory);
		senchaEntityJsonTraverser = new SenchaEntityJsonTraverser(relationshipMapper, converterFactory);
	}

	protected boolean isTempId(JsonNode value) {
		if (value != null && value.isTextual()) {
			String idString = value.textValue();
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

		public SenchaEntityJsonTraverser(IRelationshipMapper relationshipMapper, IJsonValueConverterFactory converterFactory) {
			super(relationshipMapper, converterFactory);
		}

		@Override
		protected void extractPK(LrEntity<?> entity, EntityJsonVisitor visitor, JsonNode valueNode) {
			// if PK is a Sencha temporary value, completely ignore it...
			if (!isTempId(valueNode)) {
				super.extractPK(entity, visitor, valueNode);
			}
		}
	}
}
