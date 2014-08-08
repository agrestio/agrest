package com.nhl.link.rest.runtime.meta;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.ws.rs.core.Response.Status;

import org.apache.cayenne.di.Inject;
import org.apache.cayenne.map.DataMap;
import org.apache.cayenne.map.EntityResolver;
import org.apache.cayenne.map.ObjEntity;
import org.apache.cayenne.query.Select;

import com.nhl.link.rest.LinkRestException;
import com.nhl.link.rest.runtime.cayenne.ICayennePersister;

public class MetadataService implements IMetadataService {

	public static final String NON_PERSISTENT_ENTITIES_LIST = "linkrest.meta.nonpersistent.list";

	private EntityResolver entityResolver;

	public MetadataService(@Inject(NON_PERSISTENT_ENTITIES_LIST) List<DataMap> nonPersistentEntities,
			@Inject ICayennePersister cayenneService) {

		EntityResolver cayenneResolver = cayenneService.entityResolver();
		if (nonPersistentEntities.isEmpty()) {
			this.entityResolver = cayenneResolver;
		} else {

			// clone Cayenne resolver to avoid polluting Cayenne stack with
			// POJOs

			Collection<DataMap> dataMaps = new ArrayList<>(cayenneResolver.getDataMaps());
			dataMaps.addAll(nonPersistentEntities);
			this.entityResolver = new EntityResolver(dataMaps);
		}
	}

	@Override
	public ObjEntity getObjEntity(Class<?> type) {

		if (type == null) {
			throw new NullPointerException("Null type");
		}

		ObjEntity e = entityResolver.getObjEntity(type);

		if (e == null) {
			throw new LinkRestException(Status.BAD_REQUEST, "Invalid entity: " + type.getName());
		}

		return e;
	}

	@Override
	public ObjEntity getObjEntity(Select<?> select) {
		if (select == null) {
			throw new NullPointerException("Null type");
		}

		ObjEntity e = select.getMetaData(entityResolver).getObjEntity();

		if (e == null) {
			throw new LinkRestException(Status.BAD_REQUEST, "No entity for select");
		}

		return e;
	}

}
