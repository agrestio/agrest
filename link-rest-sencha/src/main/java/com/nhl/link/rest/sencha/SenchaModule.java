package com.nhl.link.rest.sencha;

import com.nhl.link.rest.runtime.encoder.IEncoderService;
import com.nhl.link.rest.runtime.parser.IUpdateParser;
import com.nhl.link.rest.runtime.processor.select.ConstructResourceEntityStage;
import com.nhl.link.rest.runtime.processor.select.ParseRequestStage;
import com.nhl.link.rest.runtime.semantics.IRelationshipMapper;
import org.apache.cayenne.di.Binder;
import org.apache.cayenne.di.Module;

/**
 * @since 2.10
 */
public class SenchaModule implements Module {

    @Override
    public void configure(Binder binder) {
        binder.bind(ParseRequestStage.class).to(SenchaParseRequestStage.class);
        binder.bind(ConstructResourceEntityStage.class).to(SenchaConstructResourceEntityStage.class);
        binder.bind(IEncoderService.class).to(SenchaEncoderService.class);
        binder.bind(IRelationshipMapper.class).to(SenchaRelationshipMapper.class);
        binder.bind(ISenchaFilterProcessor.class).to(SenchaFilterProcessor.class);
        binder.bind(IUpdateParser.class).to(SenchaUpdateParser.class);
    }
}
