package com.nhl.link.rest.runtime.adapter.sencha;

import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.JsonNode;
import com.nhl.link.rest.EntityUpdate;
import com.nhl.link.rest.parser.converter.GenericConverter;
import com.nhl.link.rest.runtime.jackson.IJacksonService;
import com.nhl.link.rest.runtime.parser.DataObjectProcessor;
import com.nhl.link.rest.runtime.parser.converter.IJsonValueConverterFactory;
import com.nhl.link.rest.runtime.semantics.IRelationshipMapper;

/**
 * Strips off Sencha-generated temporary IDs from the update data structures.
 * 
 * @see http 
 *      ://docs.sencha.com/extjs/5.0/apidocs/#!/api/Ext.data.identifier.Generator
 * @since 1.11
 */
public class SenchaDataObjectProcessor extends DataObjectProcessor {

	private Pattern tempIdPattern;

	public SenchaDataObjectProcessor(Pattern tempIdPattern, IJacksonService jsonParser,
			IRelationshipMapper relationshipMapper, IJsonValueConverterFactory converterFactory) {
		super(jsonParser, relationshipMapper, converterFactory);

		this.tempIdPattern = tempIdPattern;
	}

	@Override
	protected void extractPK(EntityUpdate<?> update, JsonNode valueNode) {
		Object value = GenericConverter.converter().value(valueNode);

		// if PK is a Sencha temporary value, completely ignore it...
		if (!isTempId(value)) {
			super.extractPK(update, valueNode);
		}
	}

	protected boolean isTempId(Object value) {
		if (value instanceof String) {
			String idString = (String) value;
			if (tempIdPattern.matcher(idString).find()) {
				return true;
			}
		}

		return false;
	}
}
