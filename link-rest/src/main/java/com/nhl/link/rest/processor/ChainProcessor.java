package com.nhl.link.rest.processor;

/**
 * An executor of a processing chain that runs stages one-by-one, calling
 * listeners in between.
 *
 * @since 1.19
 * @deprecated since 2.7 we are using a different processor model - {@link com.nhl.link.rest.processor2.Processor}. So
 * after refactoring is complete, ChainProcessor becomes irrelevant.
 */
public class ChainProcessor {

    public static <C extends ProcessingContext<T>, T> void execute(ProcessingStage<C, ? super T> chainHead, C context) {

        ProcessingStage<C, ? super T> next = chainHead.execute(context);
        if (next != null) {
            execute(next, context);
        }
    }
}
