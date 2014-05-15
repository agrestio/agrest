package com.nhl.link.rest.runtime;

import static com.nhl.link.rest.property.PropertyBuilder.property;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.apache.cayenne.exp.Property;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nhl.link.rest.ClientEntity;
import com.nhl.link.rest.ClientProperty;
import com.nhl.link.rest.DataResponse;
import com.nhl.link.rest.LinkRestException;
import com.nhl.link.rest.SelectBuilder;
import com.nhl.link.rest.encoder.Encoder;
import com.nhl.link.rest.runtime.encoder.IEncoderService;
import com.nhl.link.rest.runtime.parser.IRequestParser;

public abstract class BaseSelectBuilder<T> implements SelectBuilder<T> {

	private static final Logger logger = LoggerFactory.getLogger(BaseSelectBuilder.class);

	protected Class<T> type;
	private UriInfo uriInfo;
	protected Object id;
	private String autocompleteProperty;
	private IEncoderService encoderService;
	private IRequestParser requestParser;
	private Map<String, ClientProperty> extraProperties;
	private Encoder dataEncoder;

	public BaseSelectBuilder(Class<T> type, IEncoderService encoderService, IRequestParser requestParser) {
		this.type = type;
		this.encoderService = encoderService;
		this.requestParser = requestParser;
	}

	@Override
	public SelectBuilder<T> with(UriInfo uriInfo) {
		this.uriInfo = uriInfo;
		return this;
	}

	/**
	 * @since 6.5
	 */
	@Override
	public SelectBuilder<T> withDataEncoder(Encoder dataEncoder) {
		this.dataEncoder = dataEncoder;
		return this;
	}

	@Override
	public SelectBuilder<T> withAutocompleteOn(Property<?> autocompleteProperty) {
		this.autocompleteProperty = autocompleteProperty != null ? autocompleteProperty.getName() : null;
		return this;
	}

	@Override
	public SelectBuilder<T> withProperty(String name) {
		return withProperty(name, property());
	}

	@Override
	public SelectBuilder<T> withProperty(String name, ClientProperty clientProperty) {
		if (extraProperties == null) {
			extraProperties = new HashMap<>();
		}

		ClientProperty oldProperty = extraProperties.put(name, clientProperty);
		if (oldProperty != null) {
			logger.info("Overriding existing custom property '" + name + "', ignoring...");
		}

		return this;
	}

	@Override
	public SelectBuilder<T> byId(Object id) {
		// TODO: return a special builder that will preserve 'byId' strategy on
		// select

		if (id == null) {
			throw new LinkRestException(Status.NOT_FOUND, "Null 'id'");
		}

		this.id = id;
		return this;
	}

	@Override
	public DataResponse<T> select() {

		DataResponse<T> responseBuilder = DataResponse.forType(getType());

		// parse request
		requestParser.parseSelect(responseBuilder, uriInfo, autocompleteProperty);

		if (extraProperties != null) {
			responseBuilder.getEntity().getExtraProperties().putAll(extraProperties);
		}

		// get data from DB
		fetchObjects(responseBuilder);

		List<T> objects = responseBuilder.getObjects();
		ClientEntity<T> rootEntity = responseBuilder.getEntity();

		if (isById() && objects.size() != 1) {

			if (objects.isEmpty()) {
				throw new LinkRestException(Status.NOT_FOUND, "No object for ID '" + id + "' and entity '"
						+ rootEntity.getEntity().getName() + "'");
			} else {
				throw new LinkRestException(Status.INTERNAL_SERVER_ERROR, "Found more than one object for ID '" + id
						+ "' and entity '" + rootEntity.getEntity().getName() + "'");
			}
		}

		// build response encoder

		if (dataEncoder != null) {
			responseBuilder.withEncoder(dataEncoder);
		} else {
			// make sure we create encode, even for the empty list, as we need
			// to encode the totals...
			encoderService.makeEncoder(responseBuilder);
		}

		return responseBuilder;
	}

	protected boolean isById() {
		return id != null;
	}

	protected abstract void fetchObjects(DataResponse<T> responseBuilder);

	public Class<T> getType() {
		if (type != null) {
			return type;
		}

		throw new IllegalStateException("No type can be determined from the builder root type.");
	}

}
