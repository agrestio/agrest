package io.agrest.runtime.processor.meta;

import io.agrest.MetadataStage;
import io.agrest.processor.Processor;
import org.apache.cayenne.di.DIRuntimeException;
import org.apache.cayenne.di.Inject;
import org.apache.cayenne.di.Provider;

import java.util.EnumMap;

/**
 * @since 2.7
 */
public class MetadataProcessorFactoryProvider implements Provider<MetadataProcessorFactory> {

    private EnumMap<MetadataStage, Processor<MetadataContext<?>>> stages;

    public MetadataProcessorFactoryProvider(@Inject CollectMetadataStage collectMetadataStage) {
        stages = new EnumMap<>(MetadataStage.class);
        stages.put(MetadataStage.COLLECT_METADATA, collectMetadataStage);
    }

    @Override
    public MetadataProcessorFactory get() throws DIRuntimeException {
        return new MetadataProcessorFactory(stages);
    }
}
