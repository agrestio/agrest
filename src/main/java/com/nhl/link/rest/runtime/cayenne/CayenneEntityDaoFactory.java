package com.nhl.link.rest.runtime.cayenne;

import org.apache.cayenne.di.Inject;

import com.nhl.link.rest.meta.LrEntity;
import com.nhl.link.rest.runtime.constraints.IConstraintsHandler;
import com.nhl.link.rest.runtime.dao.EntityDao;
import com.nhl.link.rest.runtime.dao.IEntityDaoFactory;
import com.nhl.link.rest.runtime.encoder.IEncoderService;
import com.nhl.link.rest.runtime.meta.IMetadataService;
import com.nhl.link.rest.runtime.parser.IRequestParser;

/**
 * @since 1.15
 */
public class CayenneEntityDaoFactory implements IEntityDaoFactory {

	private IRequestParser requestParser;
	private IEncoderService encoderService;
	private ICayennePersister cayennePersister;
	private IConstraintsHandler constraintsHandler;
	private IMetadataService metadataService;

	public CayenneEntityDaoFactory(@Inject IRequestParser requestParser, @Inject IEncoderService encoderService,
			@Inject ICayennePersister cayennePersister, @Inject IConstraintsHandler constraintsHandler,
			@Inject IMetadataService metadataService) {
		this.requestParser = requestParser;
		this.encoderService = encoderService;
		this.cayennePersister = cayennePersister;
		this.constraintsHandler = constraintsHandler;
		this.metadataService = metadataService;
	}

	@Override
	public <T> EntityDao<T> dao(LrEntity<T> entity) {
		return new CayenneDao<>(entity.getType(), requestParser, encoderService, cayennePersister, constraintsHandler,
				metadataService);
	}
}
