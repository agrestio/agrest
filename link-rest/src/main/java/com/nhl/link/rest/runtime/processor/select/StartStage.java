package com.nhl.link.rest.runtime.processor.select;

import com.nhl.link.rest.processor2.Processor;
import com.nhl.link.rest.processor2.ProcessorOutcome;
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
