package com.nhl.link.rest.runtime.adapter.sencha;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.nhl.link.rest.DataResponse;
import com.nhl.link.rest.provider.DataResponseWriter;

/**
 * @since 1.19
 */
public class SenchaDataResponseWriter extends DataResponseWriter {

	@Override
	protected void writeData(DataResponse<?> t, JsonGenerator out) throws IOException {

		// add "success:true" to every data response. "false" should never bet
		// combined with DataResponse, so we can hardcode the value.
		out.writeBooleanField("success", true);
		super.writeData(t, out);
	}
}
