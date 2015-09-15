package com.nhl.link.rest.runtime.processor.select;

import java.util.Map;

import javax.ws.rs.core.UriInfo;

import org.apache.cayenne.query.SelectQuery;

import com.nhl.link.rest.DataResponse;
import com.nhl.link.rest.EntityParent;
import com.nhl.link.rest.EntityProperty;
import com.nhl.link.rest.ResourceEntity;
import com.nhl.link.rest.SizeConstraints;
import com.nhl.link.rest.constraints.ConstraintsBuilder;
import com.nhl.link.rest.encoder.Encoder;
import com.nhl.link.rest.processor.BaseProcessingContext;

/**
 * Maintains state of the request processing chain for select requests.
 * 
 * @since 1.16
 */
public class SelectContext<T> extends BaseProcessingContext<T> {

	private Object id;
	private EntityParent<?> parent;
	private ResourceEntity<T> entity;
	private DataResponse<T> response;
	private UriInfo uriInfo;
	private String autocompleteProperty;
	private Map<String, EntityProperty> extraProperties;
	private SizeConstraints sizeConstraints;
	private ConstraintsBuilder<T> treeConstraints;
	private boolean atMostOneObject;
	private Encoder encoder;

	// TODO: deprecate dependency on Cayenne in generic code
	private SelectQuery<T> select;

	public SelectContext(Class<T> type) {
		super(type);
	}

	public boolean isById() {
		return id != null;
	}

	public Object getId() {
		return id;
	}

	public void setId(Object id) {
		this.id = id;
	}

	public EntityParent<?> getParent() {
		return parent;
	}

	public void setParent(EntityParent<?> parent) {
		this.parent = parent;
	}

	public DataResponse<T> getResponse() {
		return response;
	}

	public void setResponse(DataResponse<T> response) {
		this.response = response;
	}

	public UriInfo getUriInfo() {
		return uriInfo;
	}

	public void setUriInfo(UriInfo uriInfo) {
		this.uriInfo = uriInfo;
	}

	public String getAutocompleteProperty() {
		return autocompleteProperty;
	}

	public void setAutocompleteProperty(String autocompleteProperty) {
		this.autocompleteProperty = autocompleteProperty;
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

	public ConstraintsBuilder<T> getTreeConstraints() {
		return treeConstraints;
	}

	public void setTreeConstraints(ConstraintsBuilder<T> treeConstraints) {
		this.treeConstraints = treeConstraints;
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
}
