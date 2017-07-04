package com.nhl.link.rest.runtime.cayenne;

import com.nhl.link.rest.ResourceEntity;
import com.nhl.link.rest.encoder.EncoderFilter;
import com.nhl.link.rest.meta.LrEntity;
import com.nhl.link.rest.meta.LrResource;
import com.nhl.link.rest.processor.BaseLinearProcessingStage;
import com.nhl.link.rest.processor.ProcessingStage;
import com.nhl.link.rest.runtime.cayenne.processor.CayenneContextInitStage;
import com.nhl.link.rest.runtime.cayenne.processor.CayenneUnrelateStage;
import com.nhl.link.rest.runtime.constraints.IConstraintsHandler;
import com.nhl.link.rest.runtime.encoder.IEncoderService;
import com.nhl.link.rest.runtime.meta.IMetadataService;
import com.nhl.link.rest.runtime.meta.IResourceMetadataService;
import com.nhl.link.rest.runtime.parser.IRequestParser;
import com.nhl.link.rest.runtime.parser.IUpdateParser;
import com.nhl.link.rest.runtime.processor.IProcessorFactory;
import com.nhl.link.rest.runtime.processor.meta.MetadataContext;
import com.nhl.link.rest.runtime.processor.unrelate.UnrelateContext;
import org.apache.cayenne.di.Inject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @since 1.16
 * @deprecated since 2.7 not used for select processors, and the rest will be factored out soon.
 */
public class CayenneProcessorFactory implements IProcessorFactory {

	private IEncoderService encoderService;
	private ICayennePersister persister;
	private IMetadataService metadataService;
	private IResourceMetadataService resourceMetadataService;

	public CayenneProcessorFactory(@Inject IRequestParser requestParser, @Inject IUpdateParser updateParser,
			@Inject IEncoderService encoderService, @Inject ICayennePersister persister,
			@Inject IConstraintsHandler constraintsHandler, @Inject IMetadataService metadataService,
			@Inject IResourceMetadataService resourceMetadataService,
			@Inject List<EncoderFilter> filters) {
		this.encoderService = encoderService;
		this.persister = persister;
		this.metadataService = metadataService;
		this.resourceMetadataService = resourceMetadataService;
	}

	@Override
	public Map<Class<?>, Map<String, ProcessingStage<?, ?>>> processors() {
		Map<Class<?>, Map<String, ProcessingStage<?, ?>>> map = new HashMap<>();
		map.put(UnrelateContext.class,
				Collections.<String, ProcessingStage<?, ?>> singletonMap(null, createUnrelateProcessor()));
		map.put(MetadataContext.class, Collections.<String, ProcessingStage<?, ?>> singletonMap(null, createMetadataProcessor()));
		return map;
	}

	private ProcessingStage<UnrelateContext<Object>, Object> createUnrelateProcessor() {
		BaseLinearProcessingStage<UnrelateContext<Object>, Object> stage1 = new CayenneUnrelateStage<>(null,
				metadataService);
		BaseLinearProcessingStage<UnrelateContext<Object>, Object> stage0 = new CayenneContextInitStage<>(stage1,
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

				context.setResources(filteredResources);
				context.setEncoder(encoderService.metadataEncoder(resourceEntity));
			}
		};

		return stage0;
	}
}
