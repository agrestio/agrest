package io.agrest.encoder;

import io.agrest.it.fixture.CayenneDerbyStack;
import io.agrest.it.fixture.DbCleaner;
import io.agrest.runtime.AgRESTBuilder;
import io.agrest.runtime.IAgRESTService;
import org.junit.ClassRule;
import org.junit.Rule;

public abstract class EncoderITBase {

	@ClassRule
	public static CayenneDerbyStack DB = new CayenneDerbyStack("derby-for-encoder");

	@Rule
	public DbCleaner dbCleaner = new DbCleaner(DB.newContext());

	protected IAgRESTService createLRService(EncoderFilter... filters) {
		AgRESTBuilder builder = AgRESTBuilder.builder(DB.getCayenneStack());
		for (EncoderFilter filter : filters) {
			builder.encoderFilter(filter);
		}

		return builder.build().service(IAgRESTService.class);
	}

}
