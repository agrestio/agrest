package com.nhl.link.rest.runtime.processor.select;

import com.nhl.link.rest.annotation.listener.SelectChainInitialized;
import com.nhl.link.rest.processor.BaseLinearProcessingStage;
import com.nhl.link.rest.processor.ProcessingStage;
import org.apache.cayenne.query.PrefetchTreeNode;

import java.lang.annotation.Annotation;

/**
 * @since 1.19
 * @deprecated since 2.7 in favor of {@link com.nhl.link.rest.processor2.Processor} based stages.
 */
public class InitializeSelectChainStage<T> extends BaseLinearProcessingStage<SelectContext<T>, T> {

    public InitializeSelectChainStage(ProcessingStage<SelectContext<T>, ? super T> next) {
        super(next);
    }

    @Override
    public Class<? extends Annotation> afterStageListener() {
        return SelectChainInitialized.class;
    }

    @Override
    protected void doExecute(SelectContext<T> context) {
        context.setPrefetchSemantics(PrefetchTreeNode.DISJOINT_PREFETCH_SEMANTICS);
    }
}
