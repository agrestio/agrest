package io.agrest.client;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.agrest.client.protocol.Expression;
import io.agrest.client.protocol.Include;
import io.agrest.client.protocol.AgcRequest;
import io.agrest.client.protocol.Sort;
import io.agrest.client.runtime.response.DataResponseHandler;
import io.agrest.client.runtime.response.SimpleResponseHandler;
import io.agrest.client.runtime.run.InvocationBuilder;
import io.agrest.base.jsonvalueconverter.JsonValueConverter;
import io.agrest.base.jsonvalueconverter.DefaultJsonValueConverterFactoryProvider;
import io.agrest.base.jsonvalueconverter.IJsonValueConverterFactory;

import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import java.util.Collections;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @since 2.0
 */
public class AgClient {

	private static JsonFactory jsonFactory;
	private static IJsonValueConverterFactory jsonEntityReaderFactory;

	static {
		jsonFactory = new ObjectMapper().getFactory();
		jsonEntityReaderFactory = new DefaultJsonValueConverterFactoryProvider(Collections.emptyMap()).get();
	}

	public static AgClient client(WebTarget target) {
		return new AgClient(target);
	}

	private WebTarget target;
	private AgcRequest.AgRequestBuilder request;
	private Consumer<Invocation.Builder> config;

	private AgClient(WebTarget target) {
		this.target = target;
		request = AgcRequest.builder();
	}

	public AgClient exclude(String... excludePaths) {
		request.exclude(excludePaths);
		return this;
	}

	public AgClient include(String... includePaths) {
		request.include(includePaths);
		return this;
	}

	public AgClient include(Include include) {
		request.include(include);
		return this;
	}

	public AgClient include(Include.IncludeBuilder include) {
		request.include(include.build());
		return this;
	}

	public AgClient cayenneExp(Expression.ExpressionBuilder cayenneExp) {
		request.cayenneExp(cayenneExp.build());
		return this;
	}

	public AgClient sort(String... properties) {
		request.sort(properties);
		return this;
	}

	public AgClient sort(Sort ordering) {
		request.sort(ordering);
		return this;
	}

	public AgClient start(long startIndex) {
		request.start(startIndex);
		return this;
	}

	public AgClient limit(long limit) {
		request.limit(limit);
		return this;
	}

	/**
	 * @since 3.0
	 *
	 * @param config
	 * @return AgClient object
	 */
	public AgClient configure(Consumer<Invocation.Builder> config) {
		this.config = config;
		return this;
	}

	public <T> ClientDataResponse<T> get(Class<T> targetType) {
		return invoke(targetType, InvocationBuilder.target(target).config(config).request(request.build()).buildGet());
	}

	/**
	 * @since 2.1
     */
	public <T> ClientDataResponse<T> post(Class<T> targetType, String data) {
		return invoke(targetType, InvocationBuilder.target(target).config(config).request(request.build()).buildPost(data));
	}

	/**
	 * @since 2.1
     */
	public <T> ClientDataResponse<T> put(Class<T> targetType, String data) {
		return invoke(targetType, InvocationBuilder.target(target).config(config).request(request.build()).buildPut(data));
	}

	/**
	 * @since 2.1
     */
	public ClientSimpleResponse delete() {
		Supplier<Response> invocation = InvocationBuilder.target(target).config(config).request(request.build()).buildDelete();
		return new SimpleResponseHandler(jsonFactory).handleResponse(invocation.get());
	}

	private <T> ClientDataResponse<T> invoke(Class<T> targetType, Supplier<Response> invocation) {
		JsonValueConverter<T> entityReader = getEntityReader(targetType);
		Response response = invocation.get();
		DataResponseHandler<T> responseHandler = new DataResponseHandler<>(jsonFactory, entityReader);
		return responseHandler.handleResponse(response);
	}

	private <T> JsonValueConverter<T> getEntityReader(Class<T> entityType) {
		Objects.requireNonNull(entityType, "Missing target type");
		return jsonEntityReaderFactory.typedConverter(entityType)
				.orElseThrow(() -> new AgClientException("Can't build converter for type: " + entityType.getName()));
	}
}
