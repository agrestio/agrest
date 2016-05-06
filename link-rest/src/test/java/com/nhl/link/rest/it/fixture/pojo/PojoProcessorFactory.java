package com.nhl.link.rest.it.fixture.pojo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.cayenne.di.Inject;
import org.apache.cayenne.exp.Expression;
import org.apache.cayenne.query.Ordering;

import com.nhl.link.rest.encoder.EncoderFilter;
import com.nhl.link.rest.processor.BaseLinearProcessingStage;
import com.nhl.link.rest.processor.ProcessingStage;
import com.nhl.link.rest.runtime.constraints.IConstraintsHandler;
import com.nhl.link.rest.runtime.encoder.IEncoderService;
import com.nhl.link.rest.runtime.meta.IMetadataService;
import com.nhl.link.rest.runtime.parser.IRequestParser;
import com.nhl.link.rest.runtime.processor.IProcessorFactory;
import com.nhl.link.rest.runtime.processor.select.ApplySelectServerParamsStage;
import com.nhl.link.rest.runtime.processor.select.InitializeSelectChainStage;
import com.nhl.link.rest.runtime.processor.select.ParseSelectRequestStage;
import com.nhl.link.rest.runtime.processor.select.SelectContext;

public class PojoProcessorFactory implements IProcessorFactory {

	private IEncoderService encoderService;
	private IRequestParser requestParser;
	private IConstraintsHandler constraintsHandler;
	private IMetadataService metadataService;
	private PojoDB db;

	public PojoProcessorFactory(@Inject IEncoderService encoderService, @Inject IRequestParser requestParser,
			@Inject IConstraintsHandler constraintsHandler, @Inject IMetadataService metadataService) {

		this.encoderService = encoderService;
		this.requestParser = requestParser;
		this.constraintsHandler = constraintsHandler;
		this.metadataService = metadataService;

		this.db = JerseyTestOnPojo.pojoDB;
	}

	@Override
	public Map<Class<?>, Map<String, ProcessingStage<?, ?>>> processors() {
		Map<Class<?>, Map<String, ProcessingStage<?, ?>>> map = new HashMap<>();
		map.put(SelectContext.class,
				Collections.<String, ProcessingStage<?, ?>> singletonMap(null, createSelectProcessor()));
		return map;
	}

	protected ProcessingStage<SelectContext<Object>, Object> createSelectProcessor() {

		BaseLinearProcessingStage<SelectContext<Object>, Object> stage4 = new PojoFetchStage<>(null);
		BaseLinearProcessingStage<SelectContext<Object>, Object> stage3 = new ApplySelectServerParamsStage<>(stage4,
				encoderService, constraintsHandler, Collections.<EncoderFilter> emptyList());
		BaseLinearProcessingStage<SelectContext<Object>, Object> stage2 = new ParseSelectRequestStage<>(stage3,
				requestParser, metadataService);
		BaseLinearProcessingStage<SelectContext<Object>, Object> stage1 = new InitializeSelectChainStage<>(stage2);

		return stage1;
	}

	class PojoFetchStage<T> extends BaseLinearProcessingStage<SelectContext<T>, T> {

		public PojoFetchStage(ProcessingStage<SelectContext<T>, ? super T> next) {
			super(next);
		}

		@Override
		protected void doExecute(SelectContext<T> context) {
			findObjects(context);
		}

		protected void findObjects(SelectContext<T> context) {

			Map<Object, T> typeBucket = db.bucketForType(context.getType());
			if (context.isById()) {
				T object = typeBucket.get(context.getId().get());
				context.setObjects(object != null ? Collections.singletonList(object) : Collections.<T> emptyList());
				return;
			}

			// clone the list and then filter/sort it as needed
			List<T> list = new ArrayList<>(typeBucket.values());

			Expression filter = context.getEntity().getQualifier();
			if (filter != null) {

				Iterator<T> it = list.iterator();
				while (it.hasNext()) {
					T t = it.next();
					if (!filter.match(t)) {
						it.remove();
					}
				}
			}

			for (Ordering o : context.getEntity().getOrderings()) {
				o.orderList(list);
			}

			context.setObjects(list);
		}

	}
}
