package com.nhl.link.rest.runtime;

import static com.nhl.link.rest.property.PropertyBuilder.property;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.apache.cayenne.exp.Property;
import org.apache.cayenne.map.ObjAttribute;
import org.apache.cayenne.map.ObjEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nhl.link.rest.DataResponse;
import com.nhl.link.rest.DataResponseConstraints;
import com.nhl.link.rest.Entity;
import com.nhl.link.rest.EntityConstraintsBuilder;
import com.nhl.link.rest.EntityProperty;
import com.nhl.link.rest.LinkRestException;
import com.nhl.link.rest.SelectBuilder;
import com.nhl.link.rest.encoder.Encoder;
import com.nhl.link.rest.runtime.constraints.IConstraintsHandler;
import com.nhl.link.rest.runtime.encoder.IEncoderService;
import com.nhl.link.rest.runtime.meta.IMetadataService;
import com.nhl.link.rest.runtime.parser.IRequestParser;

public abstract class BaseSelectBuilder<T> implements SelectBuilder<T> {

	private static final Logger logger = LoggerFactory.getLogger(BaseSelectBuilder.class);

	protected Class<T> type;
	private UriInfo uriInfo;
	protected Object id;
	private String autocompleteProperty;
	private IEncoderService encoderService;
	private IRequestParser requestParser;
	private IConstraintsHandler configMerger;
	private IMetadataService metadataService;
	private Map<String, EntityProperty> extraProperties;
	private Encoder dataEncoder;
	private DataResponseConstraints config;

	public BaseSelectBuilder(Class<T> type, IEncoderService encoderService, IRequestParser requestParser,
			IConstraintsHandler configMerger, IMetadataService metadataService) {
		this.type = type;
		this.encoderService = encoderService;
		this.requestParser = requestParser;
		this.configMerger = configMerger;
		this.metadataService = metadataService;
	}

	/**
	 * @since 1.2
	 */
	protected DataResponseConstraints getConfig() {
		if (config == null) {
			config = createConfig();
		}

		return config;
	}

	@Override
	public SelectBuilder<T> constraints(EntityConstraintsBuilder constraints) {
		getConfig().getEntityConstraints().append(constraints);
		return this;
	}

	@Override
	public SelectBuilder<T> withEntity(EntityConstraintsBuilder builder) {
		return constraints(builder);
	}

	@Override
	public SelectBuilder<T> fetchLimit(int limit) {
		getConfig().fetchLimit(limit);
		return this;
	}

	@Override
	public SelectBuilder<T> fetchOffset(int offset) {
		getConfig().fetchOffset(offset);
		return this;
	}

	/**
	 * @since 1.2
	 */
	protected DataResponseConstraints createConfig() {

		ObjEntity entity = metadataService.getObjEntity(type);

		EntityConstraintsBuilder constraints = EntityConstraintsBuilder.constraints();

		// apply defaults:
		constraints.includeId();
		for (ObjAttribute a : entity.getAttributes()) {
			constraints.attribute(a.getName());
		}

		return new DataResponseConstraints(constraints);
	}

	@Override
	public SelectBuilder<T> with(UriInfo uriInfo) {
		this.uriInfo = uriInfo;
		return this;
	}

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
	public SelectBuilder<T> withProperty(String name, EntityProperty clientProperty) {
		if (extraProperties == null) {
			extraProperties = new HashMap<>();
		}

		EntityProperty oldProperty = extraProperties.put(name, clientProperty);
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
		// 'byId' behaving as "selectOne" is really legacy behavior of 1.1...
		// should deprecate eventually
		return doSelect(isById());
	}

	@Override
	public DataResponse<T> selectOne() {
		return doSelect(true);
	}

	protected DataResponse<T> doSelect(boolean oneObject) {

		DataResponse<T> response = DataResponse.forType(getType());

		// parse request
		requestParser.parseSelect(response, uriInfo, autocompleteProperty);

		// apply server-side config *after* all the client settings were loaded.
		// Those client settings that are not allowed will be blocked and
		// reported at this step
		if (config != null) {
			configMerger.apply(config, response);
		}

		if (extraProperties != null) {
			response.getEntity().getExtraProperties().putAll(extraProperties);
		}

		// get data from DB
		fetchObjects(response);

		List<T> objects = response.getObjects();
		Entity<T> rootEntity = response.getEntity();

		if (oneObject && objects.size() != 1) {

			if (objects.isEmpty()) {
				throw new LinkRestException(Status.NOT_FOUND, "No object for ID '" + id + "' and entity '"
						+ rootEntity.getCayenneEntity().getName() + "'");
			} else {
				throw new LinkRestException(Status.INTERNAL_SERVER_ERROR, "Found more than one object for ID '" + id
						+ "' and entity '" + rootEntity.getCayenneEntity().getName() + "'");
			}
		}

		// make sure we create encode, even for the empty list, as we need
		// to encode the totals...
		return response.withEncoder(dataEncoder != null ? dataEncoder : encoderService.makeEncoder(response));
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
