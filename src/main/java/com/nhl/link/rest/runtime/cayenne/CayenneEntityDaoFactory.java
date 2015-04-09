package com.nhl.link.rest.runtime.cayenne;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.Response.Status;

import org.apache.cayenne.di.Inject;

import com.nhl.link.rest.LinkRestException;
import com.nhl.link.rest.meta.LrEntity;
import com.nhl.link.rest.processor.ProcessingStage;
import com.nhl.link.rest.processor.Processor;
import com.nhl.link.rest.runtime.cayenne.processor.CayenneContextInitStage;
import com.nhl.link.rest.runtime.cayenne.processor.CayenneDeleteStage;
import com.nhl.link.rest.runtime.cayenne.processor.CayenneFetchStage;
import com.nhl.link.rest.runtime.cayenne.processor.CayenneUnrelateStage;
import com.nhl.link.rest.runtime.cayenne.processor.CayenneCreateStage;
import com.nhl.link.rest.runtime.cayenne.processor.CayenneCreateOrUpdateStage;
import com.nhl.link.rest.runtime.cayenne.processor.CayenneFullSyncStage;
import com.nhl.link.rest.runtime.cayenne.processor.CayenneUpdateStage;
import com.nhl.link.rest.runtime.constraints.IConstraintsHandler;
import com.nhl.link.rest.runtime.dao.EntityDao;
import com.nhl.link.rest.runtime.dao.IEntityDaoFactory;
import com.nhl.link.rest.runtime.encoder.IEncoderService;
import com.nhl.link.rest.runtime.meta.IMetadataService;
import com.nhl.link.rest.runtime.parser.IRequestParser;
import com.nhl.link.rest.runtime.processor.delete.DeleteContext;
import com.nhl.link.rest.runtime.processor.delete.DeleteInitStage;
import com.nhl.link.rest.runtime.processor.select.ApplyRequestStage;
import com.nhl.link.rest.runtime.processor.select.ApplyServerParamsStage;
import com.nhl.link.rest.runtime.processor.select.SelectInitStage;
import com.nhl.link.rest.runtime.processor.select.SelectContext;
import com.nhl.link.rest.runtime.processor.unrelate.UnrelateContext;
import com.nhl.link.rest.runtime.processor.unrelate.UnrelateInitStage;
import com.nhl.link.rest.runtime.processor.update.UpdateApplyRequestStage;
import com.nhl.link.rest.runtime.processor.update.UpdateApplyServerParamsStage;
import com.nhl.link.rest.runtime.processor.update.UpdateContext;
import com.nhl.link.rest.runtime.processor.update.UpdateInitStage;
import com.nhl.link.rest.runtime.processor.update.UpdatePostProcessStage;

/**
 * @since 1.15
 */
public class CayenneEntityDaoFactory implements IEntityDaoFactory {

	private IRequestParser requestParser;
	private IEncoderService encoderService;
	private ICayennePersister persister;
	private IConstraintsHandler constraintsHandler;
	private IMetadataService metadataService;

	private Processor<SelectContext<?>> selectProcessor;
	private Map<UpdateOperation, Processor<UpdateContext<?>>> updateProcessors;
	private Processor<DeleteContext<?>> deleteProcessor;
	private Processor<UnrelateContext<?>> unrelateProcessor;

	public CayenneEntityDaoFactory(@Inject IRequestParser requestParser, @Inject IEncoderService encoderService,
			@Inject ICayennePersister persister, @Inject IConstraintsHandler constraintsHandler,
			@Inject IMetadataService metadataService) {

		this.requestParser = requestParser;
		this.encoderService = encoderService;
		this.persister = persister;
		this.constraintsHandler = constraintsHandler;
		this.metadataService = metadataService;

		this.selectProcessor = createSelectProcessor();

		this.updateProcessors = new HashMap<>();
		updateProcessors.put(UpdateOperation.create, createCreateProcessor());
		updateProcessors.put(UpdateOperation.update, createUpdateProcessor());
		updateProcessors.put(UpdateOperation.createOrUpdate, createOrUpdateProcessor(false));
		updateProcessors.put(UpdateOperation.idempotentCreateOrUpdate, createOrUpdateProcessor(true));
		updateProcessors.put(UpdateOperation.idempotentFullSync, createFullSyncProcessor(true));

		this.deleteProcessor = createDeleteProcessor();
		this.unrelateProcessor = createUnrelateProcessor();
	}

	private Processor<UnrelateContext<?>> createUnrelateProcessor() {
		ProcessingStage<UnrelateContext<?>> stage2 = new CayenneUnrelateStage(null, metadataService);
		ProcessingStage<UnrelateContext<?>> stage1 = new UnrelateInitStage(stage2);
		ProcessingStage<UnrelateContext<?>> stage0 = new CayenneContextInitStage<>(stage1, persister);

		return stage0;
	}

	private Processor<DeleteContext<?>> createDeleteProcessor() {

		ProcessingStage<DeleteContext<?>> stage2 = new CayenneDeleteStage(null);
		ProcessingStage<DeleteContext<?>> stage1 = new DeleteInitStage(stage2);
		ProcessingStage<DeleteContext<?>> stage0 = new CayenneContextInitStage<>(stage1, persister);

		return stage0;
	}

	private Processor<SelectContext<?>> createSelectProcessor() {

		ProcessingStage<SelectContext<?>> stage3 = new CayenneFetchStage(null, persister);
		ProcessingStage<SelectContext<?>> stage2 = new ApplyServerParamsStage(stage3, encoderService, constraintsHandler);
		ProcessingStage<SelectContext<?>> stage1 = new ApplyRequestStage(stage2, requestParser);
		ProcessingStage<SelectContext<?>> stage0 = new SelectInitStage(stage1);

		return stage0;
	}

	private Processor<UpdateContext<?>> createCreateProcessor() {

		ProcessingStage<UpdateContext<?>> stage5 = new UpdatePostProcessStage(null, Status.CREATED);
		ProcessingStage<UpdateContext<?>> stage4 = new CayenneCreateStage(stage5);
		ProcessingStage<UpdateContext<?>> stage3 = new UpdateApplyServerParamsStage(stage4, encoderService,
				constraintsHandler, metadataService);
		ProcessingStage<UpdateContext<?>> stage2 = new UpdateApplyRequestStage(stage3, requestParser);
		ProcessingStage<UpdateContext<?>> stage1 = new UpdateInitStage(stage2);
		ProcessingStage<UpdateContext<?>> stage0 = new CayenneContextInitStage<>(stage1, persister);

		return stage0;
	}

	private Processor<UpdateContext<?>> createUpdateProcessor() {

		ProcessingStage<UpdateContext<?>> stage5 = new UpdatePostProcessStage(null, Status.OK);
		ProcessingStage<UpdateContext<?>> stage4 = new CayenneUpdateStage(stage5);
		ProcessingStage<UpdateContext<?>> stage3 = new UpdateApplyServerParamsStage(stage4, encoderService,
				constraintsHandler, metadataService);
		ProcessingStage<UpdateContext<?>> stage2 = new UpdateApplyRequestStage(stage3, requestParser);
		ProcessingStage<UpdateContext<?>> stage1 = new UpdateInitStage(stage2);
		ProcessingStage<UpdateContext<?>> stage0 = new CayenneContextInitStage<>(stage1, persister);

		return stage0;
	}

	private Processor<UpdateContext<?>> createOrUpdateProcessor(boolean idempotnent) {

		ProcessingStage<UpdateContext<?>> stage5 = new UpdatePostProcessStage(null, Status.OK);
		ProcessingStage<UpdateContext<?>> stage4 = new CayenneCreateOrUpdateStage(stage5, idempotnent);
		ProcessingStage<UpdateContext<?>> stage3 = new UpdateApplyServerParamsStage(stage4, encoderService,
				constraintsHandler, metadataService);
		ProcessingStage<UpdateContext<?>> stage2 = new UpdateApplyRequestStage(stage3, requestParser);
		ProcessingStage<UpdateContext<?>> stage1 = new UpdateInitStage(stage2);
		ProcessingStage<UpdateContext<?>> stage0 = new CayenneContextInitStage<>(stage1, persister);

		return stage0;
	}

	private Processor<UpdateContext<?>> createFullSyncProcessor(boolean idempotnent) {

		ProcessingStage<UpdateContext<?>> stage5 = new UpdatePostProcessStage(null, Status.OK);
		ProcessingStage<UpdateContext<?>> stage4 = new CayenneFullSyncStage(stage5, idempotnent);
		ProcessingStage<UpdateContext<?>> stage3 = new UpdateApplyServerParamsStage(stage4, encoderService,
				constraintsHandler, metadataService);
		ProcessingStage<UpdateContext<?>> stage2 = new UpdateApplyRequestStage(stage3, requestParser);
		ProcessingStage<UpdateContext<?>> stage1 = new UpdateInitStage(stage2);
		ProcessingStage<UpdateContext<?>> stage0 = new CayenneContextInitStage<>(stage1, persister);

		return stage0;
	}

	@Override
	public <T> EntityDao<T> dao(LrEntity<T> entity) {

		// sanity check
		if (persister.entityResolver().getObjEntity(entity.getType()) == null) {
			throw new LinkRestException(Status.BAD_REQUEST, "Not a Cayenne entity: " + entity.getName());
		}

		return new CayenneDao<>(entity.getType(), selectProcessor, updateProcessors, deleteProcessor, unrelateProcessor);
	}
}
