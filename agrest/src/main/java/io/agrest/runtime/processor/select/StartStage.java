package io.agrest.runtime.processor.select;

import io.agrest.processor.Processor;
import io.agrest.processor.ProcessorOutcome;
import org.apache.cayenne.query.PrefetchTreeNode;

/**
 * @since 2.7
 */
public class StartStage implements Processor<SelectContext<?>> {

    @Override
    public ProcessorOutcome execute(SelectContext<?> context) {
        context.setPrefetchSemantics(PrefetchTreeNode.DISJOINT_PREFETCH_SEMANTICS);
        return ProcessorOutcome.CONTINUE;
    }
}
