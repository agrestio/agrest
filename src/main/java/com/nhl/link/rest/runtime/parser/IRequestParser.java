package com.nhl.link.rest.runtime.parser;

import com.nhl.link.rest.runtime.processor.select.SelectContext;
import com.nhl.link.rest.runtime.processor.update.UpdateContext;

/**
 * Defines protocol adapter between the REST interface and LinkRest backend.
 */
public interface IRequestParser {

	void parseSelect(SelectContext<?> context);

	/**
	 * Parses an update that may contain zero or more objects of a single kind
	 * with or without IDs.
	 */
	void parseUpdate(UpdateContext<?> context);

}
