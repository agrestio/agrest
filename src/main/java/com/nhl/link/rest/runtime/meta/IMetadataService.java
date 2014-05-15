package com.nhl.link.rest.runtime.meta;

import org.apache.cayenne.map.ObjEntity;
import org.apache.cayenne.query.Select;

/**
 * Provides entity metadata in Cayenne format. Although the actual ObjEntities
 * may describe Cayenne entities, as well as POJOs or any other objects.
 * 
 * @since 6.8
 */
public interface IMetadataService {

	ObjEntity getObjEntity(Class<?> type);
	
	ObjEntity getObjEntity(Select<?> select);
}
