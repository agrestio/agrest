package com.nhl.link.rest.runtime.adapter.sencha;

import java.util.Collection;

import javax.ws.rs.core.Feature;

import org.apache.cayenne.di.Binder;

import com.nhl.link.rest.DataResponse;
import com.nhl.link.rest.runtime.LinkRestRuntime;
import com.nhl.link.rest.runtime.adapter.LinkRestAdapter;
import com.nhl.link.rest.runtime.encoder.IEncoderService;
import com.nhl.link.rest.runtime.parser.IRequestParser;
import com.nhl.link.rest.runtime.parser.sort.ISortProcessor;
import com.nhl.link.rest.runtime.semantics.IRelationshipMapper;

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

		binder.<Class<?>> bindMap(LinkRestRuntime.BODY_WRITERS_MAP).put(DataResponse.class.getName(),
				SenchaDataResponseWriter.class);
	}

	@Override
	public void contributeToJaxRs(Collection<Feature> features) {
		// nothing is contributed specifically for Sencha here..
	}

}
