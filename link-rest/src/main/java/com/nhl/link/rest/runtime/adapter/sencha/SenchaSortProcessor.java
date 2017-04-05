package com.nhl.link.rest.runtime.adapter.sencha;

import com.nhl.link.rest.ResourceEntity;
import com.nhl.link.rest.runtime.jackson.IJacksonService;
import com.nhl.link.rest.runtime.parser.cache.IPathCache;
import com.nhl.link.rest.runtime.parser.sort.SortProcessor;
import org.apache.cayenne.di.Inject;

import java.util.List;
import java.util.Map;

/**
 * @since 1.5
 */
public class SenchaSortProcessor extends SortProcessor {

    static final String GROUP = "group";
    static final String GROUP_DIR = "groupDir";

    public SenchaSortProcessor(@Inject IJacksonService jacksonService, @Inject IPathCache pathCache) {
        super(jacksonService, pathCache);
    }

    @Override
    public void process(ResourceEntity<?> entity, Map<String, List<String>> protocolParameters) {
        // sencha groupers go before sorters (sorters are processed by super).
        process(entity, string(protocolParameters, GROUP), string(protocolParameters, GROUP_DIR));
        super.process(entity, protocolParameters);
    }
}
