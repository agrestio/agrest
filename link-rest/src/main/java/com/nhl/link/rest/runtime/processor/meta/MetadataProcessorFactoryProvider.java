package com.nhl.link.rest.runtime.processor.meta;

import com.nhl.link.rest.MetadataStage;
import com.nhl.link.rest.processor.Processor;
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
