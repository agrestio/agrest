package com.nhl.link.rest.runtime.jackson;

import java.io.IOException;
import java.io.OutputStream;

import com.fasterxml.jackson.core.JsonFactory;

public interface IJacksonService {

	JsonFactory getJsonFactory();

	void outputJson(JsonConvertable object, OutputStream out) throws IOException;
}
