package com.nhl.link.rest.runtime;

import static com.nhl.link.rest.property.PropertyBuilder.property;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.apache.cayenne.exp.Property;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nhl.link.rest.DataResponse;
import com.nhl.link.rest.ResourceEntity;
import com.nhl.link.rest.EntityParent;
import com.nhl.link.rest.EntityProperty;
import com.nhl.link.rest.LinkRestException;
import com.nhl.link.rest.SelectBuilder;
import com.nhl.link.rest.SizeConstraints;
import com.nhl.link.rest.constraints.ConstraintsBuilder;
import com.nhl.link.rest.encoder.Encoder;
import com.nhl.link.rest.runtime.constraints.IConstraintsHandler;
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
	private IConstraintsHandler constraintsHandler;
	private Map<String, EntityProperty> extraProperties;
	private Encoder dataEncoder;
	private SizeConstraints sizeConstraints;
	private ConstraintsBuilder<T> treeConstraints;
	protected EntityParent<?> parent;

	public BaseSelectBuilder(Class<T> type, IEncoderService encoderService, IRequestParser requestParser,
			IConstraintsHandler constraintsHandler) {
		this.type = type;
		this.encoderService = encoderService;
		this.requestParser = requestParser;
		this.constraintsHandler = constraintsHandler;
	}

	/**
	 * @since 1.4
	 */
	@Override
	public SelectBuilder<T> parent(Class<?> parentType, Object parentId, Property<T> relationshipFromParent) {
		this.parent = new EntityParent<>(parentType, parentId, relationshipFromParent.getName());
		return this;
	}

	/**
	 * @since 1.4
	 */
	@Override
	public SelectBuilder<T> parent(Class<?> parentType, Object parentId, String relationshipFromParent) {
		this.parent = new EntityParent<>(parentType, parentId, relationshipFromParent);
		return this;
	}

	/**
	 * @since 1.7
	 */
	@Override
	public SelectBuilder<T> parent(EntityParent<?> parent) {
		this.parent = parent;
		return this;
	}

	/**
	 * @since 1.7
	 */
	@Override
	public SelectBuilder<T> toManyParent(Class<?> parentType, Object parentId,
			Property<? extends Collection<T>> relationshipFromParent) {
		return parent(parentType, parentId, relationshipFromParent.getName());
	}

	@Override
	public SelectBuilder<T> constraints(ConstraintsBuilder<T> constraints) {
		this.treeConstraints = constraints;
		return this;
	}

	@Override
	public SelectBuilder<T> fetchLimit(int limit) {
		getOrCreateSizeConstraints().fetchLimit(limit);
		return this;
	}

	@Override
	public SelectBuilder<T> fetchOffset(int offset) {
		getOrCreateSizeConstraints().fetchOffset(offset);
		return this;
	}

	private SizeConstraints getOrCreateSizeConstraints() {
		if (sizeConstraints == null) {
			sizeConstraints = new SizeConstraints();
		}

		return sizeConstraints;
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

		// apply constraints
		constraintsHandler.constrainResponse(response, sizeConstraints, treeConstraints);

		if (extraProperties != null) {
			response.getEntity().getExtraProperties().putAll(extraProperties);
		}

		// get data from DB
		fetchObjects(response);

		List<T> objects = response.getObjects();
		ResourceEntity<T> rootEntity = response.getEntity();

		if (oneObject && objects.size() != 1) {

			if (objects.isEmpty()) {
				throw new LinkRestException(Status.NOT_FOUND, "No object for ID '" + id + "' and entity '"
						+ rootEntity.getLrEntity().getName() + "'");
			} else {
				throw new LinkRestException(Status.INTERNAL_SERVER_ERROR, "Found more than one object for ID '" + id
						+ "' and entity '" + rootEntity.getLrEntity().getName() + "'");
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
