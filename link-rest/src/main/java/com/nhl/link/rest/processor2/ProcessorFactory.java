package com.nhl.link.rest.processor2;

import com.nhl.link.rest.processor.ProcessingContext;

import java.util.EnumMap;
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
     * @return a processor that is a combination of default stages intermixed with provided listeners.
     */
    public Processor<C> createProcessor(EnumMap<E, Processor<C>> processors) {

        if (processors.isEmpty()) {
            return defaultProcessor;
        }

        Processor<C> p = null;

        // note that EnumMap iterates in the ordinal order of the underlying enum.
        // This is important for ordering stages...
        for (Map.Entry<E, Processor<C>> e : defaultStages.entrySet()) {

            p = p == null ? e.getValue() : p.andThen(e.getValue());


            Processor<C> customProcessor = processors.get(e.getKey());
            if (customProcessor != null) {
                p = p.andThen(customProcessor);
            }
        }

        return p;
    }
}
