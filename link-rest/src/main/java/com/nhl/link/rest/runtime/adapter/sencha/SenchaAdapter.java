package com.nhl.link.rest.runtime.adapter.sencha;

import com.nhl.link.rest.runtime.adapter.LinkRestAdapter;
import com.nhl.link.rest.runtime.encoder.IEncoderService;
import com.nhl.link.rest.runtime.parser.IRequestParser;
import com.nhl.link.rest.runtime.parser.IUpdateParser;
import com.nhl.link.rest.runtime.parser.sort.ISortProcessor;
import com.nhl.link.rest.runtime.semantics.IRelationshipMapper;
import org.apache.cayenne.di.Binder;

import javax.ws.rs.core.Feature;
import java.util.Collection;

/**
 * A collection of Sencha-specific extensions to LinkRest.
 * 
 * @since 1.3
 */
public class SenchaAdapter implements LinkRestAdapter {

	@Override
	public void contributeToRuntime(Binder binder) {

		binder.bind(IRequestParser.class).to(SenchaRequestParser.class);
		binder.bind(ISortProcessor.class).to(SenchaSortProcessor.class);
		binder.bind(IEncoderService.class).to(SenchaEncoderService.class);
		binder.bind(IRelationshipMapper.class).to(SenchaRelationshipMapper.class);
		binder.bind(ISenchaFilterProcessor.class).to(SenchaFilterProcessor.class);
		binder.bind(IUpdateParser.class).to(SenchaUpdateParser.class);
	}

	@Override
	public void contributeToJaxRs(Collection<Feature> features) {
		features.add(context -> {
			context.register(SenchaDeletePayloadParser.class);
			return true;
		});
	}

}
