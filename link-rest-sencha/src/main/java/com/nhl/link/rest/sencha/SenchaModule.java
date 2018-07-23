package com.nhl.link.rest.sencha;

import com.nhl.link.rest.runtime.encoder.IEncoderService;
import com.nhl.link.rest.runtime.protocol.IEntityUpdateParser;
import com.nhl.link.rest.runtime.processor.select.CreateResourceEntityStage;
import com.nhl.link.rest.runtime.processor.select.ParseRequestStage;
import com.nhl.link.rest.runtime.semantics.IRelationshipMapper;
import com.nhl.link.rest.sencha.runtime.encoder.SenchaEncoderService;
import com.nhl.link.rest.sencha.runtime.entity.ISenchaFilterExpressionCompiler;
import com.nhl.link.rest.sencha.runtime.entity.SenchaFilterExpressionCompiler;
import com.nhl.link.rest.sencha.runtime.processor.select.SenchaCreateResourceEntityStage;
import com.nhl.link.rest.sencha.runtime.processor.select.SenchaParseRequestStage;
import com.nhl.link.rest.sencha.runtime.protocol.ISenchaFilterParser;
import com.nhl.link.rest.sencha.runtime.protocol.SenchaFilterParser;
import com.nhl.link.rest.sencha.runtime.protocol.SenchaUpdateParser;
import com.nhl.link.rest.sencha.runtime.semantics.SenchaRelationshipMapper;
import org.apache.cayenne.di.Binder;
import org.apache.cayenne.di.Module;

/**
 * @since 2.10
 */
public class SenchaModule implements Module {

    @Override
    public void configure(Binder binder) {
        binder.bind(ParseRequestStage.class).to(SenchaParseRequestStage.class);
        binder.bind(CreateResourceEntityStage.class).to(SenchaCreateResourceEntityStage.class);
        binder.bind(IEncoderService.class).to(SenchaEncoderService.class);
        binder.bind(IRelationshipMapper.class).to(SenchaRelationshipMapper.class);
        binder.bind(ISenchaFilterExpressionCompiler.class).to(SenchaFilterExpressionCompiler.class);
        binder.bind(ISenchaFilterParser.class).to(SenchaFilterParser.class);
        binder.bind(IEntityUpdateParser.class).to(SenchaUpdateParser.class);
    }
}
