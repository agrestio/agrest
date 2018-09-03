package io.agrest.sencha;

import io.agrest.runtime.encoder.IEncoderService;
import io.agrest.runtime.processor.select.CreateResourceEntityStage;
import io.agrest.runtime.processor.select.ParseRequestStage;
import io.agrest.runtime.protocol.IEntityUpdateParser;
import io.agrest.runtime.semantics.IRelationshipMapper;
import io.agrest.sencha.runtime.encoder.SenchaEncoderService;
import io.agrest.sencha.runtime.entity.ISenchaFilterExpressionCompiler;
import io.agrest.sencha.runtime.entity.SenchaFilterExpressionCompiler;
import io.agrest.sencha.runtime.processor.select.SenchaCreateResourceEntityStage;
import io.agrest.sencha.runtime.processor.select.SenchaParseRequestStage;
import io.agrest.sencha.runtime.protocol.ISenchaFilterParser;
import io.agrest.sencha.runtime.protocol.SenchaFilterParser;
import io.agrest.sencha.runtime.protocol.SenchaUpdateParser;
import io.agrest.sencha.runtime.semantics.SenchaRelationshipMapper;
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
