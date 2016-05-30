package com.nhl.link.rest.it.fixture;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.core.MediaType;

import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.query.Query;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.inmemory.InMemoryTestContainerFactory;
import org.glassfish.jersey.test.spi.TestContainerException;
import org.junit.ClassRule;
import org.junit.Rule;

import com.nhl.link.rest.runtime.LinkRestBuilder;

/**
 * A main superclass of LinkRest unit tests that require full stack Jersey
 * container.
 */
public abstract class JerseyTestOnDerby extends JerseyTest {

	@ClassRule
	public static CayenneDerbyStack DB_STACK = new CayenneDerbyStack();

	@Rule
	public DbCleaner dbCleaner = new DbCleaner(DB_STACK.newContext());

	public JerseyTestOnDerby() throws TestContainerException {
		super(new InMemoryTestContainerFactory());
	}

	protected String urlEnc(String queryParam) {
		try {
			// URLEncoder replaces spaces with "+"... Those are not decoded
			// properly by Jersey in 'uriInfo.getQueryParameters()' (TODO: why?)
			return URLEncoder.encode(queryParam, "UTF-8").replace("+", "%20");
		} catch (UnsupportedEncodingException e) {

			// unexpected... we know that UTF-8 is present
			throw new RuntimeException(e);
		}
	}

	@Override
	public Application configure() {

		Feature lrFeature = doConfigure().build();

		Feature unitFeature = new Feature() {

			@Override
			public boolean configure(FeatureContext context) {
				doAddResources(context);
				return true;
			}
		};

		return new ResourceConfig().register(unitFeature).register(lrFeature);
	}

	protected LinkRestBuilder doConfigure() {
		return LinkRestBuilder.builder(DB_STACK.getCayenneStack());
	}

	protected abstract void doAddResources(FeatureContext context);

	/**
	 * @deprecated Kept around as we have lots of tests that need it. Consider
	 *             changing those gradually to
	 *             {@link #insert(String, String, String)}, etc.
	 */
	@Deprecated
	protected void performQuery(Query query) {
		newContext().performGenericQuery(query);
	}

	protected String stringForQuery(String querySql) {
		return DB_STACK.stringForQuery(querySql);
	}

	protected int intForQuery(String querySql) {
		return DB_STACK.intForQuery(querySql);
	}

	protected ObjectContext newContext() {
		return DB_STACK.newContext();
	}

	protected void insert(String table, String columns, String values) {
		DB_STACK.insert(table, columns, values);
	}

	protected Entity<String> jsonEntity(String data) {
		return Entity.entity(data, MediaType.APPLICATION_JSON);
	}
}
