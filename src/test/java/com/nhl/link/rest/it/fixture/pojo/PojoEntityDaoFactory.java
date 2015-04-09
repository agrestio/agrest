package com.nhl.link.rest.it.fixture.pojo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.cayenne.di.Inject;
import org.apache.cayenne.exp.Expression;
import org.apache.cayenne.query.Ordering;

import com.nhl.link.rest.meta.LrEntity;
import com.nhl.link.rest.processor.Processor;
import com.nhl.link.rest.processor.ProcessingStage;
import com.nhl.link.rest.runtime.constraints.IConstraintsHandler;
import com.nhl.link.rest.runtime.dao.EntityDao;
import com.nhl.link.rest.runtime.dao.IEntityDaoFactory;
import com.nhl.link.rest.runtime.encoder.IEncoderService;
import com.nhl.link.rest.runtime.parser.IRequestParser;
import com.nhl.link.rest.runtime.processor.select.ApplyRequestStage;
import com.nhl.link.rest.runtime.processor.select.ApplyServerParamsStage;
import com.nhl.link.rest.runtime.processor.select.SelectInitStage;
import com.nhl.link.rest.runtime.processor.select.SelectContext;

public class PojoEntityDaoFactory implements IEntityDaoFactory {

	private IEncoderService encoderService;
	private IRequestParser requestParser;
	private IConstraintsHandler constraintsHandler;
	private Processor<SelectContext<?>> selectProcessor;
	private PojoDB db;

	public PojoEntityDaoFactory(@Inject IEncoderService encoderService, @Inject IRequestParser requestParser,
			@Inject IConstraintsHandler constraintsHandler) {

		this.encoderService = encoderService;
		this.requestParser = requestParser;
		this.constraintsHandler = constraintsHandler;

		this.db = JerseyTestOnPojo.pojoDB;
		this.selectProcessor = createSelectProcessor();
	}

	protected Processor<SelectContext<?>> createSelectProcessor() {

		ProcessingStage<SelectContext<?>> stage4 = new PojoFetchStage(null);
		ProcessingStage<SelectContext<?>> stage3 = new ApplyServerParamsStage(stage4, encoderService, constraintsHandler);
		ProcessingStage<SelectContext<?>> stage2 = new ApplyRequestStage(stage3, requestParser);
		ProcessingStage<SelectContext<?>> stage1 = new SelectInitStage(stage2);

		return stage1;
	}

	@Override
	public <T> EntityDao<T> dao(LrEntity<T> entity) {
		return new PojoDao<T>(entity.getType(), selectProcessor);
	}

	class PojoFetchStage extends ProcessingStage<SelectContext<?>> {

		public PojoFetchStage(Processor<SelectContext<?>> next) {
			super(next);
		}

		@Override
		protected void doExecute(SelectContext<?> context) {
			findObjects(context);
		}

		protected <T> void findObjects(SelectContext<T> context) {

			Map<Object, T> typeBucket = db.bucketForType(context.getType());
			if (context.isById()) {
				T object = typeBucket.get(context.getId());
				context.getResponse().withObjects(
						object != null ? Collections.<T> singletonList(object) : Collections.<T> emptyList());
				return;
			}

			// clone the list and then filter/sort it as needed
			List<T> list = new ArrayList<>(typeBucket.values());

			Expression filter = context.getResponse().getEntity().getQualifier();
			if (filter != null) {

				Iterator<T> it = list.iterator();
				while (it.hasNext()) {
					T t = it.next();
					if (!filter.match(t)) {
						it.remove();
					}
				}
			}

			for (Ordering o : context.getResponse().getEntity().getOrderings()) {
				o.orderList(list);
			}

			context.getResponse().withObjects(list);
		}

	}
}
