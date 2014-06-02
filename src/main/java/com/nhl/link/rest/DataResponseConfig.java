package com.nhl.link.rest;

import org.apache.cayenne.map.ObjEntity;

/**
 * Stores parameters pertaining to a DataResponse. Configs can be built from
 * client parameters or created manually via API on the server. Multiple configs
 * can be merged in one and applied to a single DataResponse.
 * 
 * @since 1.1
 */
public class DataResponseConfig {

	private int fetchOffset;
	private int fetchLimit;
	private EntityConfig entity;

	public DataResponseConfig(ObjEntity rootEntity) {
		this.entity = new EntityConfig(rootEntity);
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
}
