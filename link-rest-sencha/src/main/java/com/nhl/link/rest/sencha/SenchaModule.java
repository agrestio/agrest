package com.nhl.link.rest.sencha;

import com.nhl.link.rest.runtime.encoder.IEncoderService;
import com.nhl.link.rest.runtime.parser.IRequestParser;
import com.nhl.link.rest.runtime.parser.IUpdateParser;
import com.nhl.link.rest.runtime.semantics.IRelationshipMapper;
import org.apache.cayenne.di.Binder;
import org.apache.cayenne.di.Module;

/**
 * @since 2.10
 */
public class SenchaModule implements Module {

    @Override
    public void configure(Binder binder) {
        binder.bind(IRequestParser.class).to(SenchaRequestParser.class);
        binder.bind(IEncoderService.class).to(SenchaEncoderService.class);
        binder.bind(IRelationshipMapper.class).to(SenchaRelationshipMapper.class);
        binder.bind(ISenchaFilterProcessor.class).to(SenchaFilterProcessor.class);
        binder.bind(IUpdateParser.class).to(SenchaUpdateParser.class);
    }
}
