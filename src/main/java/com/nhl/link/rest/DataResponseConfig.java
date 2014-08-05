package com.nhl.link.rest;

import org.apache.cayenne.map.ObjEntity;

import com.nhl.link.rest.runtime.ILinkRestService;

/**
 * Stores parameters pertaining to a DataResponse. DataResponseConfig objects
 * are normally created via {@link ILinkRestService#newConfig(Class)} and then
 * customized in the code to describe a response structure desired by the server
 * application. During each request LinkRest would merge DataResponseConfig with
 * client configs, normally not allowing the client to request more data than
 * the config specifies (a client can request *less* data of course).
 * 
 * @since 1.1
 */
public class DataResponseConfig {

	private int fetchOffset;
	private int fetchLimit;
	private EntityConfig entity;

	public DataResponseConfig(ObjEntity rootEntity) {
		this(new EntityConfig(rootEntity));
	}

	/**
	 * @since 1.2
	 */
	public DataResponseConfig(EntityConfig entity) {
		this.entity = entity;
	}

	public DataResponseConfig fetchOffset(int fetchOffset) {
		this.fetchOffset = fetchOffset;
		return this;
	}

	public DataResponseConfig fetchLimit(int fetchLimit) {
		this.fetchLimit = fetchLimit;
		return this;
	}

	public EntityConfig getEntity() {
		return entity;
	}

	public int getFetchOffset() {
		return fetchOffset;
	}

	public int getFetchLimit() {
		return fetchLimit;
	}

	/**
	 * @since 1.2
	 */
	public DataResponseConfig deepCopy() {

		EntityConfig entityCopy = entity.deepCopy();
		DataResponseConfig copy = new DataResponseConfig(entityCopy);

		copy.fetchLimit(fetchLimit);
		copy.fetchOffset(fetchOffset);
		return copy;
	}
}
