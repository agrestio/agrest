package com.nhl.link.rest.encoder;

import org.junit.ClassRule;
import org.junit.Rule;

import com.nhl.link.rest.it.fixture.CayenneDerbyStack;
import com.nhl.link.rest.it.fixture.DbCleaner;
import com.nhl.link.rest.runtime.ILinkRestService;
import com.nhl.link.rest.runtime.LinkRestBuilder;

public abstract class EncoderITBase {
	
	@ClassRule
	public static CayenneDerbyStack DB = new CayenneDerbyStack();

	@Rule
	public DbCleaner dbCleaner = new DbCleaner(DB.newContext());

	protected ILinkRestService createLRService(EncoderFilter... filters) {
		LinkRestBuilder builder = LinkRestBuilder.builder(DB.getCayenneStack());
		for (EncoderFilter filter : filters) {
			builder.encoderFilter(filter);
		}

		return builder.build().service(ILinkRestService.class);
	}

}
