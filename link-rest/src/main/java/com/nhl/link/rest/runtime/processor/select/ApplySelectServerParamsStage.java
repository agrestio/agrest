package com.nhl.link.rest.runtime.processor.select;

import com.nhl.link.rest.ResourceEntity;
import com.nhl.link.rest.annotation.listener.SelectServerParamsApplied;
import com.nhl.link.rest.encoder.EncoderFilter;
import com.nhl.link.rest.processor.BaseLinearProcessingStage;
import com.nhl.link.rest.processor.ProcessingStage;
import com.nhl.link.rest.runtime.constraints.IConstraintsHandler;
import com.nhl.link.rest.runtime.encoder.IEncoderService;

import java.lang.annotation.Annotation;
import java.util.List;

/**
 * @since 1.19
 * @deprecated since 2.7 in favor of {@link com.nhl.link.rest.processor2.Processor} based stages.
 */
public class ApplySelectServerParamsStage<T> extends BaseLinearProcessingStage<SelectContext<T>, T> {

	private IConstraintsHandler constraintsHandler;
	private IEncoderService encoderService;
	private List<EncoderFilter> filters;

	public ApplySelectServerParamsStage(ProcessingStage<SelectContext<T>, ? super T> next,
			IEncoderService encoderService, IConstraintsHandler constraintsHandler, List<EncoderFilter> filters) {

		super(next);

		this.encoderService = encoderService;
		this.constraintsHandler = constraintsHandler;
		this.filters = filters;
	}

	@Override
	public Class<? extends Annotation> afterStageListener() {
		return SelectServerParamsApplied.class;
	}

	@Override
	protected void doExecute(SelectContext<T> context) {

		ResourceEntity<T> entity = context.getEntity();

		constraintsHandler.constrainResponse(entity, context.getSizeConstraints(), context.getConstraint());

		if (context.getExtraProperties() != null) {
			entity.getExtraProperties().putAll(context.getExtraProperties());
		}

		for (EncoderFilter filter : filters) {
			if (filter.matches(entity)) {
				entity.setFiltered(true);
				break;
			}
		}

		// make sure we create the encoder, even if we end up with an empty
		// list, as we need to encode the totals

		if (context.getEncoder() == null) {
			context.setEncoder(encoderService.dataEncoder(entity));
		}
	}
}
