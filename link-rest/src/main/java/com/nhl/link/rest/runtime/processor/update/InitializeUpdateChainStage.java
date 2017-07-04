package com.nhl.link.rest.runtime.processor.update;

import com.nhl.link.rest.annotation.listener.UpdateChainInitialized;
import com.nhl.link.rest.processor.BaseLinearProcessingStage;
import com.nhl.link.rest.processor.ProcessingStage;

import java.lang.annotation.Annotation;

/**
 * @since 1.16
 * @deprecated since 2.7 we have a new design of stages based on {@link com.nhl.link.rest.processor2.Processor} interface.
 */
public class InitializeUpdateChainStage<T> extends BaseLinearProcessingStage<UpdateContext<T>, T> {

	public InitializeUpdateChainStage(ProcessingStage<UpdateContext<T>, ? super T> next) {
		super(next);
	}

	@Override
	protected void doExecute(UpdateContext<T> context) {
		// do nothing stage... I guess still useful as a marker for where the
		// processing starts...
	}

	@Override
	public Class<? extends Annotation> afterStageListener() {
		return UpdateChainInitialized.class;
	}

}
