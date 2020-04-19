package io.agrest.client.runtime.response;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.agrest.client.ClientDataResponse;
import io.agrest.client.AgClientException;
import io.agrest.base.jsonvalueconverter.JsonValueConverter;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @since 2.0
 */
public class DataResponseHandler<T> extends BaseResponseHandler<ClientDataResponse<T>> {

	private static final String DATA_NODE = "data";
	private static final String TOTAL_NODE = "total";

	private JsonValueConverter<T> jsonEntityReader;

	public DataResponseHandler(JsonFactory jsonFactory, JsonValueConverter<T> jsonEntityReader) {
		super(jsonFactory);
		this.jsonEntityReader = jsonEntityReader;
	}

	@Override
	protected ClientDataResponse<T> doHandleResponse(Status status, Response response) {

		List<T> items;
		long total;

		String entity = response.readEntity(String.class);
		JsonNode responseNode;
		try {
			responseNode = new ObjectMapper().readTree(jsonFactory.createParser(entity));

			JsonNode dataNode = responseNode.get(DATA_NODE);
			if (dataNode == null || !dataNode.isArray()) {
				throw new AgClientException(
						"Failed to parse response -- '" + DATA_NODE + "' is missing or has a wrong type");
			}

			items = new ArrayList<>(dataNode.size() + 1);
			for (JsonNode itemNode : dataNode) {
				items.add(jsonEntityReader.value(itemNode));
			}

			JsonNode totalNode = responseNode.get(TOTAL_NODE);
			if (totalNode == null || !totalNode.isNumber()) {
				throw new AgClientException(
						"Failed to parse response -- '" + TOTAL_NODE + "' is missing or has a wrong type");
			}

			total = totalNode.asLong();

		} catch (IOException e) {
			throw new AgClientException("Failed to parse response", e);
		}

		return new ClientDataResponse<>(status, items, total);
	}
}
