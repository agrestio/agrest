package io.agrest.runtime.processor.select;

import io.agrest.processor.Processor;
import io.agrest.processor.ProcessorOutcome;

/**
 * @since 3.4
 */
public class AssembleQueryStage implements Processor<SelectContext<?>> {

    @Override
    public ProcessorOutcome execute(SelectContext<?> context) {
        doExecute(context);
        return ProcessorOutcome.CONTINUE;
    }

    protected <T> void doExecute(SelectContext<T> context) {
        // note that depending on the resolver child queries may not be assembled until later when the parent
        // data is fetched.. So this stage listeners may not get the state they want
        // TODO: deprecate SelectStage.ASSEMBLE_QUERY stage?
        context.getEntity().getResolver().assembleQuery(context);
    }
}
