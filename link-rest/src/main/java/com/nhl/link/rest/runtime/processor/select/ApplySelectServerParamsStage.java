package com.nhl.link.rest.runtime.processor.select;

import java.lang.annotation.Annotation;

import com.nhl.link.rest.DataResponse;
import com.nhl.link.rest.ResourceEntity;
import com.nhl.link.rest.annotation.listener.SelectServerParamsApplied;
import com.nhl.link.rest.encoder.Encoder;
import com.nhl.link.rest.processor.ProcessingStage;
import com.nhl.link.rest.processor.BaseLinearProcessingStage;
import com.nhl.link.rest.runtime.constraints.IConstraintsHandler;
import com.nhl.link.rest.runtime.encoder.IEncoderService;

/**
 * @since 1.19
 */
public class ApplySelectServerParamsStage<T> extends BaseLinearProcessingStage<SelectContext<T>, T> {

	private IConstraintsHandler constraintsHandler;
	private IEncoderService encoderService;

	public ApplySelectServerParamsStage(ProcessingStage<SelectContext<T>, ? super T> next,
			IEncoderService encoderService, IConstraintsHandler constraintsHandler) {

		super(next);

		this.encoderService = encoderService;
		this.constraintsHandler = constraintsHandler;
	}

	@Override
	public Class<? extends Annotation> afterStageListener() {
		return SelectServerParamsApplied.class;
	}

	@SuppressWarnings({ "rawtypes" })
	@Override
	protected void doExecute(SelectContext<T> context) {

		DataResponse response = context.getResponse();
		ResourceEntity<T> entity = context.getEntity();

		constraintsHandler.constrainResponse(entity, context.getSizeConstraints(), context.getTreeConstraints());

		if (context.getExtraProperties() != null) {
			entity.getExtraProperties().putAll(context.getExtraProperties());
		}

		// make sure we create the encoder, even if we end up with an empty
		// list, as we need to encode the totals

		Encoder encoder = context.getEncoder() != null ? context.getEncoder() : encoderService.dataEncoder(entity);
		response.withEncoder(encoder);
	}
}
