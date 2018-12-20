package io.agrest.encoder;

import io.agrest.it.fixture.CayenneDerbyStack;
import io.agrest.it.fixture.DbCleaner;
import io.agrest.runtime.cayenne.AgCayenneBuilder;
import io.agrest.runtime.IAgService;
import org.junit.ClassRule;
import org.junit.Rule;

public abstract class EncoderITBase {

	@ClassRule
	public static CayenneDerbyStack DB = new CayenneDerbyStack("derby-for-encoder");

	@Rule
	public DbCleaner dbCleaner = new DbCleaner(DB.newContext());

	protected IAgService createAgService(EncoderFilter... filters) {
		io.agrest.runtime.AgBuilder builder = AgCayenneBuilder.builder(DB.getCayenneStack());
		for (EncoderFilter filter : filters) {
			builder.encoderFilter(filter);
		}

		return builder.build().service(IAgService.class);
	}

}
