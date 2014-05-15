package com.nhl.link.rest.runtime.jackson;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;

/**
 * An interface used with {@link IJacksonService} that allows object to stream
 * its contents as JSON.
 */
public interface JsonConvertable {

	void generateJSON(JsonGenerator out) throws IOException;
}