package com.nhl.link.rest.runtime.adapter.sencha;

import java.util.regex.Pattern;

import com.nhl.link.rest.EntityUpdate;
import com.nhl.link.rest.UpdateResponse;
import com.nhl.link.rest.update.UpdateFilter;

/**
 * Strips off Sencha-generated temporary IDs from the update data structures.
 * 
 * @see http 
 *      ://docs.sencha.com/extjs/5.0/apidocs/#!/api/Ext.data.identifier.Generator
 * @since 1.3
 */
public class SenchaTempIdCleaner implements UpdateFilter {

	private static final Pattern DASH_ID_PATTERN = Pattern.compile(".-[\\d]+$");

	private Pattern tempIdPattern;

	public static SenchaTempIdCleaner dashId() {
		return new SenchaTempIdCleaner(DASH_ID_PATTERN);
	}

	public SenchaTempIdCleaner(Pattern tempIdPattern) {
		this.tempIdPattern = tempIdPattern;
	}

	@Override
	public <T> UpdateResponse<T> afterParse(UpdateResponse<T> response) {

		for (EntityUpdate u : response.getUpdates()) {

			Object id = u.getId();

			if (id instanceof String) {

				String idString = (String) id;
				if (tempIdPattern.matcher(idString).find()) {
					u.setId(null);
				}
			}
		}

		return response;
	}

}
