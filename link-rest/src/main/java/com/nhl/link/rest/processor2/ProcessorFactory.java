package com.nhl.link.rest.processor2;

import com.nhl.link.rest.processor.ChainProcessor;
import com.nhl.link.rest.processor.ProcessingContext;
import com.nhl.link.rest.processor.ProcessingStage;
import com.nhl.link.rest.runtime.listener.ListenerInvocation;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @param <E>
 * @param <C>
 * @since 2.7
 */
public class ProcessorFactory<E extends Enum<E>, C extends ProcessingContext<?>> {

    private EnumMap<E, Processor<C>> defaultStages;
    private Processor<C> defaultProcessor;

    public ProcessorFactory(EnumMap<E, Processor<C>> defaultStages) {
        this.defaultStages = Objects.requireNonNull(defaultStages);
        this.defaultProcessor = composeStages(defaultStages);
    }

    protected Processor<C> composeStages(EnumMap<E, Processor<C>> stages) {

        if (stages.isEmpty()) {
            return c -> ProcessorOutcome.CONTINUE;
        }

        Processor<C> p = null;

        // note that EnumMap iterates in the ordinal order of the underlying enum.
        // This is important for ordering stages...
        for (Processor<C> s : stages.values()) {
            p = p == null ? s : p.andThen(s);
        }

        return p;
    }

    /**
     * Creates a processor that is a combination of default stages intermixed with provided listeners.
     *
     * @param listeners a map of listeners by stage id.
     * @return a processor that is a combination of default stages intermixed with provided listeners.
     */
    // TODO: convert listeners to processors on the fly when they are registered in the builder... so this method
    // should not be aware of listeners
    public Processor<C> createProcessor(EnumMap<E, Processor<C>> processors,
                                        EnumMap<E, List<ListenerInvocation>> listeners) {

        if (listeners.isEmpty() && processors.isEmpty()) {
            return defaultProcessor;
        }

        Processor<C> p = null;

        // note that EnumMap iterates in the ordinal order of the underlying enum.
        // This is important for ordering stages...
        for (Map.Entry<E, Processor<C>> e : defaultStages.entrySet()) {

            p = p == null ? e.getValue() : p.andThen(e.getValue());

            List<ListenerInvocation> invocations = listeners.get(e.getKey());
            if (invocations != null && !invocations.isEmpty()) {
                p = p.andThen(toListenersProcessor(invocations));
            }

            Processor<C> customProcessor = processors.get(e.getKey());
            if(customProcessor != null) {
                p = p.andThen(customProcessor);
            }
        }

        return p;
    }

    protected Processor<C> toListenersProcessor(List<ListenerInvocation> invocations) {

        if (invocations.isEmpty()) {
            return c -> ProcessorOutcome.CONTINUE;
        }

        return c -> invokeListeners_Erased(c, invocations);
    }

    // fun Java generic hacks...

    static ProcessorOutcome invokeListeners_Erased(
            ProcessingContext context,
            List<ListenerInvocation> invocations) {
        return invokeListeners(context, invocations);
    }

    static <C extends ProcessingContext<T>, T> ProcessorOutcome invokeListeners(
            C context,
            List<ListenerInvocation> invocations) {

        ProcessingStage<C, ? super T> marker = c -> null;
        ProcessingStage<C, ? super T> next = marker;

        for (ListenerInvocation i : invocations) {
            next = i.invoke(context, next);
        }

        if (next == null) {
            return ProcessorOutcome.STOP;
        } else if (next == marker) {
            return ProcessorOutcome.CONTINUE;
        } else {
            // custom stage ... execute and do not proceed to the normal pipeline
            ChainProcessor.execute(next, context);
            return ProcessorOutcome.STOP;
        }
    }
}
