package com.nhl.link.rest.runtime.cayenne;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Response.Status;

import org.apache.cayenne.DataObject;
import org.apache.cayenne.di.Inject;

import com.nhl.link.rest.MetadataResponse;
import com.nhl.link.rest.ResourceEntity;
import com.nhl.link.rest.encoder.EncoderFilter;
import com.nhl.link.rest.meta.LrEntity;
import com.nhl.link.rest.meta.LrResource;
import com.nhl.link.rest.processor.BaseLinearProcessingStage;
import com.nhl.link.rest.processor.ProcessingStage;
import com.nhl.link.rest.runtime.UpdateOperation;
import com.nhl.link.rest.runtime.cayenne.processor.CayenneContextInitStage;
import com.nhl.link.rest.runtime.cayenne.processor.CayenneCreateOrUpdateStage;
import com.nhl.link.rest.runtime.cayenne.processor.CayenneCreateStage;
import com.nhl.link.rest.runtime.cayenne.processor.CayenneDeleteStage;
import com.nhl.link.rest.runtime.cayenne.processor.CayenneFetchStage;
import com.nhl.link.rest.runtime.cayenne.processor.CayenneFullSyncStage;
import com.nhl.link.rest.runtime.cayenne.processor.CayenneQueryAssembleStage;
import com.nhl.link.rest.runtime.cayenne.processor.CayenneUnrelateStage;
import com.nhl.link.rest.runtime.cayenne.processor.CayenneUpdatePostProcessStage;
import com.nhl.link.rest.runtime.cayenne.processor.CayenneUpdateStage;
import com.nhl.link.rest.runtime.constraints.IConstraintsHandler;
import com.nhl.link.rest.runtime.encoder.EncoderService;
import com.nhl.link.rest.runtime.encoder.IEncoderService;
import com.nhl.link.rest.runtime.meta.IMetadataService;
import com.nhl.link.rest.runtime.meta.IResourceMetadataService;
import com.nhl.link.rest.runtime.parser.IRequestParser;
import com.nhl.link.rest.runtime.parser.IUpdateParser;
import com.nhl.link.rest.runtime.processor.IProcessorFactory;
import com.nhl.link.rest.runtime.processor.delete.DeleteContext;
import com.nhl.link.rest.runtime.processor.meta.MetadataContext;
import com.nhl.link.rest.runtime.processor.select.ApplySelectServerParamsStage;
import com.nhl.link.rest.runtime.processor.select.InitializeSelectChainStage;
import com.nhl.link.rest.runtime.processor.select.ParseSelectRequestStage;
import com.nhl.link.rest.runtime.processor.select.SelectContext;
import com.nhl.link.rest.runtime.processor.unrelate.UnrelateContext;
import com.nhl.link.rest.runtime.processor.update.ApplyUpdateServerParamsStage;
import com.nhl.link.rest.runtime.processor.update.InitializeUpdateChainStage;
import com.nhl.link.rest.runtime.processor.update.ParseUpdateRequestStage;
import com.nhl.link.rest.runtime.processor.update.UpdateContext;

/**
 * @since 1.16
 */
public class CayenneProcessorFactory implements IProcessorFactory {

	private IUpdateParser updateParser;
	private IRequestParser requestParser;
	private IEncoderService encoderService;
	private ICayennePersister persister;
	private IConstraintsHandler constraintsHandler;
	private IMetadataService metadataService;
	private IResourceMetadataService resourceMetadataService;
	private List<EncoderFilter> filters;

	public CayenneProcessorFactory(@Inject IRequestParser requestParser, @Inject IUpdateParser updateParser,
			@Inject IEncoderService encoderService, @Inject ICayennePersister persister,
			@Inject IConstraintsHandler constraintsHandler, @Inject IMetadataService metadataService,
			@Inject IResourceMetadataService resourceMetadataService,
			@Inject(EncoderService.ENCODER_FILTER_LIST) List<EncoderFilter> filters) {
		this.requestParser = requestParser;
		this.encoderService = encoderService;
		this.persister = persister;
		this.constraintsHandler = constraintsHandler;
		this.metadataService = metadataService;
		this.resourceMetadataService = resourceMetadataService;
		this.updateParser = updateParser;
		this.filters = filters;
	}

	@Override
	public Map<Class<?>, Map<String, ProcessingStage<?, ?>>> processors() {
		Map<Class<?>, Map<String, ProcessingStage<?, ?>>> map = new HashMap<>();
		map.put(SelectContext.class,
				Collections.<String, ProcessingStage<?, ?>> singletonMap(null, createSelectProcessor()));
		map.put(DeleteContext.class,
				Collections.<String, ProcessingStage<?, ?>> singletonMap(null, createDeleteProcessor()));
		map.put(UnrelateContext.class,
				Collections.<String, ProcessingStage<?, ?>> singletonMap(null, createUnrelateProcessor()));
		map.put(UpdateContext.class, createUpdateProcessors());
		map.put(MetadataContext.class,
				Collections.<String, ProcessingStage<?, ?>> singletonMap(null, createMetadataProcessor()));
		return map;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private Map<String, ProcessingStage<?, ?>> createUpdateProcessors() {

		Map map = new HashMap<>();

		map.put(UpdateOperation.create.name(), createCreateProcessor());
		map.put(UpdateOperation.createOrUpdate.name(), createOrUpdateProcessor(false));
		map.put(UpdateOperation.update.name(), createUpdateProcessor());
		map.put(UpdateOperation.idempotentCreateOrUpdate.name(), createOrUpdateProcessor(true));
		map.put(UpdateOperation.idempotentFullSync.name(), createFullSyncProcessor(true));

		return map;
	}

	private ProcessingStage<UnrelateContext<Object>, Object> createUnrelateProcessor() {
		BaseLinearProcessingStage<UnrelateContext<Object>, Object> stage1 = new CayenneUnrelateStage<>(null,
				metadataService);
		BaseLinearProcessingStage<UnrelateContext<Object>, Object> stage0 = new CayenneContextInitStage<>(stage1,
				persister);

		return stage0;
	}

	private ProcessingStage<DeleteContext<Object>, Object> createDeleteProcessor() {
		BaseLinearProcessingStage<DeleteContext<Object>, Object> stage1 = new CayenneDeleteStage<>(null);
		BaseLinearProcessingStage<DeleteContext<Object>, Object> stage0 = new CayenneContextInitStage<>(stage1,
				persister);

		return stage0;
	}

	private ProcessingStage<SelectContext<Object>, Object> createSelectProcessor() {

		BaseLinearProcessingStage<SelectContext<Object>, Object> stage4 = new CayenneFetchStage<>(null, persister);
		BaseLinearProcessingStage<SelectContext<Object>, Object> stage3 = new CayenneQueryAssembleStage<>(stage4,
				persister);
		BaseLinearProcessingStage<SelectContext<Object>, Object> stage2 = new ApplySelectServerParamsStage<>(stage3,
				encoderService, constraintsHandler, filters);
		BaseLinearProcessingStage<SelectContext<Object>, Object> stage1 = new ParseSelectRequestStage<>(stage2,
				requestParser, metadataService);
		BaseLinearProcessingStage<SelectContext<Object>, Object> stage0 = new InitializeSelectChainStage<>(stage1);

		return stage0;
	}

	private ProcessingStage<UpdateContext<DataObject>, DataObject> createCreateProcessor() {

		BaseLinearProcessingStage<UpdateContext<DataObject>, DataObject> stage5 = new CayenneUpdatePostProcessStage<>(
				null, Status.CREATED);
		BaseLinearProcessingStage<UpdateContext<DataObject>, DataObject> stage4 = new CayenneCreateStage<>(stage5);
		BaseLinearProcessingStage<UpdateContext<DataObject>, DataObject> stage3 = new ApplyUpdateServerParamsStage<>(
				stage4, encoderService, constraintsHandler, metadataService);
		BaseLinearProcessingStage<UpdateContext<DataObject>, DataObject> stage2 = new ParseUpdateRequestStage<>(stage3,
				requestParser, updateParser, metadataService);
		BaseLinearProcessingStage<UpdateContext<DataObject>, DataObject> stage1 = new InitializeUpdateChainStage<>(
				stage2);
		BaseLinearProcessingStage<UpdateContext<DataObject>, DataObject> stage0 = new CayenneContextInitStage<>(stage1,
				persister);

		return stage0;
	}

	private ProcessingStage<UpdateContext<DataObject>, DataObject> createUpdateProcessor() {

		BaseLinearProcessingStage<UpdateContext<DataObject>, DataObject> stage5 = new CayenneUpdatePostProcessStage<>(
				null, Status.OK);
		BaseLinearProcessingStage<UpdateContext<DataObject>, DataObject> stage4 = new CayenneUpdateStage<>(stage5);
		BaseLinearProcessingStage<UpdateContext<DataObject>, DataObject> stage3 = new ApplyUpdateServerParamsStage<>(
				stage4, encoderService, constraintsHandler, metadataService);
		BaseLinearProcessingStage<UpdateContext<DataObject>, DataObject> stage2 = new ParseUpdateRequestStage<>(stage3,
				requestParser, updateParser, metadataService);
		BaseLinearProcessingStage<UpdateContext<DataObject>, DataObject> stage1 = new InitializeUpdateChainStage<>(
				stage2);
		BaseLinearProcessingStage<UpdateContext<DataObject>, DataObject> stage0 = new CayenneContextInitStage<>(stage1,
				persister);

		return stage0;
	}

	private ProcessingStage<UpdateContext<DataObject>, DataObject> createOrUpdateProcessor(boolean idempotnent) {

		BaseLinearProcessingStage<UpdateContext<DataObject>, DataObject> stage5 = new CayenneUpdatePostProcessStage<>(
				null, Status.OK);
		BaseLinearProcessingStage<UpdateContext<DataObject>, DataObject> stage4 = new CayenneCreateOrUpdateStage<>(
				stage5, idempotnent);
		BaseLinearProcessingStage<UpdateContext<DataObject>, DataObject> stage3 = new ApplyUpdateServerParamsStage<>(
				stage4, encoderService, constraintsHandler, metadataService);
		BaseLinearProcessingStage<UpdateContext<DataObject>, DataObject> stage2 = new ParseUpdateRequestStage<>(stage3,
				requestParser, updateParser, metadataService);
		BaseLinearProcessingStage<UpdateContext<DataObject>, DataObject> stage1 = new InitializeUpdateChainStage<>(
				stage2);
		BaseLinearProcessingStage<UpdateContext<DataObject>, DataObject> stage0 = new CayenneContextInitStage<>(stage1,
				persister);

		return stage0;
	}

	private ProcessingStage<UpdateContext<DataObject>, DataObject> createFullSyncProcessor(boolean idempotnent) {

		BaseLinearProcessingStage<UpdateContext<DataObject>, DataObject> stage5 = new CayenneUpdatePostProcessStage<>(
				null, Status.OK);
		BaseLinearProcessingStage<UpdateContext<DataObject>, DataObject> stage4 = new CayenneFullSyncStage<>(stage5,
				idempotnent);
		BaseLinearProcessingStage<UpdateContext<DataObject>, DataObject> stage3 = new ApplyUpdateServerParamsStage<>(
				stage4, encoderService, constraintsHandler, metadataService);
		BaseLinearProcessingStage<UpdateContext<DataObject>, DataObject> stage2 = new ParseUpdateRequestStage<>(stage3,
				requestParser, updateParser, metadataService);
		BaseLinearProcessingStage<UpdateContext<DataObject>, DataObject> stage1 = new InitializeUpdateChainStage<>(
				stage2);
		BaseLinearProcessingStage<UpdateContext<DataObject>, DataObject> stage0 = new CayenneContextInitStage<>(stage1,
				persister);

		return stage0;
	}

	private <T> ProcessingStage<MetadataContext<T>, T> createMetadataProcessor() {

		BaseLinearProcessingStage<MetadataContext<T>, T> stage0 = new BaseLinearProcessingStage<MetadataContext<T>, T>(
				null) {

			@SuppressWarnings("unchecked")
			@Override
			protected void doExecute(MetadataContext<T> context) {
				LrEntity<T> entity = context.getEntity();
				Collection<LrResource<?>> resources = resourceMetadataService.getLrResources(context.getResource());
				Collection<LrResource<T>> filteredResources = new ArrayList<>(resources.size());
				for (LrResource<?> resource : resources) {
					LrEntity<?> resourceEntity = resource.getEntity();
					if (resourceEntity != null && resourceEntity.getName().equals(entity.getName())) {
						filteredResources.add((LrResource<T>) resource);
					}
				}

				ResourceEntity<T> resourceEntity = new ResourceEntity<>(entity);
				resourceEntity.setApplicationBase(context.getApplicationBase());

				MetadataResponse<T> response = new MetadataResponse<>(context.getType()).resourceEntity(resourceEntity)
						.withResources(filteredResources);

				context.setResponse(response.withEncoder(encoderService.metadataEncoder(resourceEntity)));
			}
		};

		return stage0;
	}
}
