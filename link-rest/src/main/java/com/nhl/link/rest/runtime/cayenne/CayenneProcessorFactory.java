package com.nhl.link.rest.runtime.cayenne;

import com.nhl.link.rest.encoder.EncoderFilter;
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
import com.nhl.link.rest.runtime.processor.unrelate.UnrelateContext;
import org.apache.cayenne.di.Inject;

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
		return map;
	}

	private ProcessingStage<UnrelateContext<Object>, Object> createUnrelateProcessor() {
		BaseLinearProcessingStage<UnrelateContext<Object>, Object> stage1 = new CayenneUnrelateStage<>(null,
				metadataService);
		BaseLinearProcessingStage<UnrelateContext<Object>, Object> stage0 = new CayenneContextInitStage<>(stage1,
				persister);

		return stage0;
	}
}
