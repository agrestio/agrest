package com.nhl.link.rest.runtime.adapter.sencha;

import java.util.Collection;

import javax.ws.rs.core.Feature;

import org.apache.cayenne.di.Binder;

import com.nhl.link.rest.runtime.adapter.LinkRestAdapter;
import com.nhl.link.rest.runtime.parser.RequestParser;
import com.nhl.link.rest.runtime.parser.filter.IFilterProcessor;
import com.nhl.link.rest.runtime.parser.sort.ISortProcessor;
import com.nhl.link.rest.update.UpdateFilter;

/**
 * A collection of Sencha-specific extensions to LinkRest.
 * 
 * @since 1.3
 */
public class SenchaAdapter implements LinkRestAdapter {

	@Override
	public void contributeToRuntime(Binder binder) {

		// "dashId" filter is hardcoded... should that be configurable?
		binder.<UpdateFilter> bindList(RequestParser.UPDATE_FILTER_LIST).add(SenchaTempIdCleaner.dashId());

		binder.bind(ISortProcessor.class).to(SenchaSortProcessor.class);
		binder.bind(IFilterProcessor.class).to(SenchaFilterProcessor.class);
	}

	@Override
	public void contributeToJaxRs(Collection<Feature> features) {
		// nothing is contributed specifically for Sencha here..
	}

}
