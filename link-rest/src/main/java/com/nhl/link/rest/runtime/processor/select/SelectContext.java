package com.nhl.link.rest.runtime.processor.select;

import com.nhl.link.rest.CompoundObjectId;
import com.nhl.link.rest.DataResponse;
import com.nhl.link.rest.EntityParent;
import com.nhl.link.rest.EntityProperty;
import com.nhl.link.rest.LrObjectId;
import com.nhl.link.rest.ResourceEntity;
import com.nhl.link.rest.SimpleObjectId;
import com.nhl.link.rest.SizeConstraints;
import com.nhl.link.rest.constraints.Constraint;
import com.nhl.link.rest.encoder.Encoder;
import com.nhl.link.rest.processor.BaseProcessingContext;
import com.nhl.link.rest.runtime.query.Query;
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

	private LrObjectId id;
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
	private Map<String, List<String>> queryParams;
	private Query query;

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

	public LrObjectId getId() {
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
		return queryParams != null ? queryParams
				: uriInfo != null ? uriInfo.getQueryParameters() : Collections.emptyMap();
	}

	/**
	 * @since 2.13
	 */
	public Map<String, List<String>> getQueryParams() {
		return queryParams;
	}

	/**
	 * @since 2.13
	 */
	public void setQueryParams(Map<String, List<String>> parameters) {
		queryParams = parameters;
	}

	/**
	 * @since 2.13
	 */
	public Query getQuery() {
		return query;
	}

	/**
	 * @since 2.13
	 */
	public void setQuery(Query query) {
		this.query = query;
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
}
