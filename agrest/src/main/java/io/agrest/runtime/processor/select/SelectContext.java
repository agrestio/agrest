package io.agrest.runtime.processor.select;

import io.agrest.AgRequest;
import io.agrest.CompoundObjectId;
import io.agrest.DataResponse;
import io.agrest.EntityParent;
import io.agrest.EntityProperty;
import io.agrest.AgObjectId;
import io.agrest.ResourceEntity;
import io.agrest.SimpleObjectId;
import io.agrest.SizeConstraints;
import io.agrest.constraints.Constraint;
import io.agrest.encoder.Encoder;
import io.agrest.processor.BaseProcessingContext;
import org.apache.cayenne.query.SelectQuery;

import javax.ws.rs.core.UriInfo;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Maintains state of the request processing chain for select requests.
 * 
 * @since 1.16
 */
public class SelectContext<T> extends BaseProcessingContext<T> {

	private AgObjectId id;
	private EntityParent<?> parent;
	private ResourceEntity<T> entity;
	private UriInfo uriInfo;
	private Map<String, EntityProperty> extraProperties;
	private SizeConstraints sizeConstraints;
	private Constraint<T> constraint;
	private boolean atMostOneObject;
	private Encoder encoder;
	private int prefetchSemantics;
	private List objects;
	private AgRequest rawRequest;
	private AgRequest request;

	// TODO: deprecate dependency on Cayenne in generic code
	private SelectQuery<T> select;

	public SelectContext(Class<T> type) {
		super(type);
	}

	/**
	 * Returns a new response object reflecting the context state.
	 * 
	 * @since 1.24
	 * @return a new response object reflecting the context state.
	 */
	public DataResponse<T> createDataResponse() {
		List<? extends T> objects = this.objects != null ? this.objects : Collections.<T> emptyList();
		DataResponse<T> response = DataResponse.forType(getType());
		response.setObjects(objects);
		response.setEncoder(encoder);
		response.setStatus(getStatus());
		return response;
	}

	public boolean isById() {
		return id != null;
	}

	public AgObjectId getId() {
		return id;
	}

	public void setId(Object id) {
		this.id = new SimpleObjectId(id);
	}

	public void setCompoundId(Map<String, Object> ids) {
		this.id = new CompoundObjectId(ids);
	}

	public EntityParent<?> getParent() {
		return parent;
	}

	public void setParent(EntityParent<?> parent) {
		this.parent = parent;
	}

	public UriInfo getUriInfo() {
		return uriInfo;
	}

	/**
	 * @since 2.5
	 */
	public Map<String, List<String>> getProtocolParameters() {
		return uriInfo != null ? uriInfo.getQueryParameters() : Collections.emptyMap();
	}

	public void setUriInfo(UriInfo uriInfo) {
		this.uriInfo = uriInfo;
	}

	public Map<String, EntityProperty> getExtraProperties() {
		return extraProperties;
	}

	public void setExtraProperties(Map<String, EntityProperty> extraProperties) {
		this.extraProperties = extraProperties;
	}

	public SizeConstraints getSizeConstraints() {
		return sizeConstraints;
	}

	public void setSizeConstraints(SizeConstraints sizeConstraints) {
		this.sizeConstraints = sizeConstraints;
	}


	/**
	 * @since 2.4
	 * @return this context's constraint function.
	 */
	public Constraint<T> getConstraint() {
		return constraint;
	}

	/**
	 * @since 2.4
	 * @param constraint constraint function.
	 */
	public void setConstraint(Constraint<T> constraint) {
		this.constraint = constraint;
	}

	// TODO: deprecate dependency on Cayenne in generic code
	public SelectQuery<T> getSelect() {
		return select;
	}

	// TODO: deprecate dependency on Cayenne in generic code
	public void setSelect(SelectQuery<T> select) {
		this.select = select;
	}

	public boolean isAtMostOneObject() {
		return atMostOneObject;
	}

	public void setAtMostOneObject(boolean expectingOne) {
		this.atMostOneObject = expectingOne;
	}

	public Encoder getEncoder() {
		return encoder;
	}

	public void setEncoder(Encoder encoder) {
		this.encoder = encoder;
	}

	/**
	 * @since 1.20
	 */
	public ResourceEntity<T> getEntity() {
		return entity;
	}

	/**
	 * @since 1.20
	 */
	public void setEntity(ResourceEntity<T> entity) {
		this.entity = entity;
	}

	/**
	 * @since 1.24
	 */
	public int getPrefetchSemantics() {
		return prefetchSemantics;
	}

	/**
	 * @since 1.24
	 */
	public void setPrefetchSemantics(int prefetchSemantics) {
		this.prefetchSemantics = prefetchSemantics;
	}

	/**
	 * @since 1.24
	 */
	public List<T> getObjects() {
		return objects;
	}

	/**
	 * @since 1.24
	 */
	public void setObjects(List<? extends T> objects) {
		this.objects = objects;
	}

	/**
	 * Returns AgRequest that contains query parameters.
	 * This parameters are used on the CreateEntityStage to create an entity.
	 *
	 * @since 2.13
	 */
	public AgRequest getRawRequest() {
		return rawRequest;
	}

	/**
	 * Saves AgRequest that contains query parameters.
	 *
	 * This AgRequest object is build from two sources.
	 * 1. Parse UriInfo and create query parameters objects.
	 * 2. If some of query parameters are passed explicitly they will be used instead of parsing from UriInfo.
	 * These explicit query parameters are saved in rawRequest object during ParseRequestStage.
	 *
	 * @since 2.13
	 */
	public void setRawRequest(AgRequest request) {
		this.rawRequest = request;
	}

	/**
	 * Returns AgRequest object that contains query parameters explicitly passed through API method call
	 *
	 * @since 2.13
	 */
	public AgRequest getRequest() {
		return request;
	}

	/**
	 * Saves AgRequest object that contains query parameters explicitly passed through API method call
	 * These parameters are created during ConvertQueryParamsStage
	 *
	 * <pre>{@code
	 *
	 * 		public DataResponse<E2> getE2(@Context UriInfo uriInfo, @QueryParam CayenneExp cayenneExp) {
	 * 			// Explicit query parameter
	 * 			AgRequest agRequest = AgRequest.builder().cayenneExp(cayenneExp).build();
	 *
	 * 			return LinkRest.service(config).select(E2.class)
	 * 							.uri(uriInfo)
	 * 							.request(agRequest) // overrides parameters from uriInfo
	 * 							.get();
	 * 		}
	 *
	 * }</pre>
	 *
	 * @since 2.13
	 */
	public void setRequest(AgRequest request) {
		this.request = request;
	}
}
