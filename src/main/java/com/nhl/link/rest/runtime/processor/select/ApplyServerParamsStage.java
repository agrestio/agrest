package com.nhl.link.rest.runtime.processor.select;

import com.nhl.link.rest.DataResponse;
import com.nhl.link.rest.encoder.Encoder;
import com.nhl.link.rest.processor.Processor;
import com.nhl.link.rest.processor.ProcessingStage;
import com.nhl.link.rest.runtime.constraints.IConstraintsHandler;
import com.nhl.link.rest.runtime.encoder.IEncoderService;

/**
 * @since 1.16
 */
public class ApplyServerParamsStage extends ProcessingStage<SelectContext<?>> {

	private IConstraintsHandler constraintsHandler;
	private IEncoderService encoderService;

	public ApplyServerParamsStage(Processor<SelectContext<?>> next, IEncoderService encoderService,
			IConstraintsHandler constraintsHandler) {

		super(next);

		this.encoderService = encoderService;
		this.constraintsHandler = constraintsHandler;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	protected void doExecute(SelectContext<?> context) {

		DataResponse response = context.getResponse();

		constraintsHandler.constrainResponse(response, context.getSizeConstraints(), context.getTreeConstraints());

		if (context.getExtraProperties() != null) {
			response.getEntity().getExtraProperties().putAll(context.getExtraProperties());
		}

		// make sure we create the encoder, even if we end up with an empty
		// list, as we need to encode the totals

		Encoder encoder = context.getEncoder() != null ? context.getEncoder() : encoderService.makeEncoder(response);
		response.withEncoder(encoder);
	}
}
