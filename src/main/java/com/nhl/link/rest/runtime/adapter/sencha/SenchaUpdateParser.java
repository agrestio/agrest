package com.nhl.link.rest.runtime.adapter.sencha;

import java.util.regex.Pattern;

import org.apache.cayenne.di.Inject;

import com.fasterxml.jackson.databind.JsonNode;
import com.nhl.link.rest.EntityUpdate;
import com.nhl.link.rest.parser.converter.GenericConverter;
import com.nhl.link.rest.runtime.jackson.IJacksonService;
import com.nhl.link.rest.runtime.parser.UpdateParser;
import com.nhl.link.rest.runtime.parser.converter.IJsonValueConverterFactory;
import com.nhl.link.rest.runtime.semantics.IRelationshipMapper;

/**
 * 
 * Strips off Sencha-generated temporary IDs from the update data structures.
 * 
 * @see http ://docs.sencha.com/extjs/5.0/apidocs/#!/api/Ext.data.identifier.
 *      Generator
 * 
 * @since 1.20
 */
public class SenchaUpdateParser extends UpdateParser {

	private static final Pattern DASH_ID_PATTERN = Pattern.compile(".-[\\d]+$");

	private Pattern tempIdPattern;

	public SenchaUpdateParser(@Inject IRelationshipMapper relationshipMapper,
			@Inject IJsonValueConverterFactory converterFactory, @Inject IJacksonService jacksonService) {
		super(relationshipMapper, converterFactory, jacksonService);

		// do we need to make it injectable?
		this.tempIdPattern = DASH_ID_PATTERN;
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
