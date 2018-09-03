package io.agrest.encoder;

import io.agrest.it.fixture.CayenneDerbyStack;
import io.agrest.it.fixture.DbCleaner;
import io.agrest.runtime.ILinkRestService;
import io.agrest.runtime.LinkRestBuilder;
import org.junit.ClassRule;
import org.junit.Rule;

public abstract class EncoderITBase {

	@ClassRule
	public static CayenneDerbyStack DB = new CayenneDerbyStack("derby-for-encoder");

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
