package com.nhl.link.rest.runtime.adapter.sencha;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.apache.cayenne.di.Binder;
import org.apache.cayenne.di.DIBootstrap;
import org.apache.cayenne.di.Injector;
import org.apache.cayenne.di.Key;
import org.apache.cayenne.di.Module;
import org.junit.Test;

import com.nhl.link.rest.runtime.parser.RequestParser;
import com.nhl.link.rest.update.UpdateFilter;

public class SenchaAdapterTest {

	@Test
	public void testContributeToRuntime() {
		final SenchaAdapter adapter = new SenchaAdapter();

		Module m = new Module() {
			@Override
			public void configure(Binder binder) {
				adapter.contributeToRuntime(binder);
			}
		};

		Injector i = DIBootstrap.createInjector(m);

		Class<?> listClass = List.class;
		@SuppressWarnings("unchecked")
		List<UpdateFilter> filters = (List<UpdateFilter>) i.getInstance(Key.get((Class<List<?>>) listClass,
				RequestParser.UPDATE_FILTER_LIST));

		assertEquals(1, filters.size());
		assertTrue(filters.get(0) instanceof SenchaTempIdCleaner);
	}
}
