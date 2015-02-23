package com.nhl.link.rest.it.fixture;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.sql.SQLException;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.core.MediaType;

import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.configuration.server.ServerRuntime;
import org.apache.cayenne.query.EJBQLQuery;
import org.apache.cayenne.query.QueryChain;
import org.apache.cayenne.query.RefreshQuery;
import org.apache.cayenne.query.SQLSelect;
import org.apache.cayenne.query.SQLTemplate;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.inmemory.InMemoryTestContainerFactory;
import org.glassfish.jersey.test.spi.TestContainerException;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

import com.nhl.link.rest.it.fixture.cayenne.E1;
import com.nhl.link.rest.runtime.LinkRestBuilder;

/**
 * A main superclass of LinkRest unit tests that require full stack Jersey
 * container.
 */
public abstract class JerseyTestOnDerby extends JerseyTest {

	protected static ServerRuntime runtime;
	protected static DerbyManager derbyAssembly;

	@BeforeClass
	public static void setUpClass() throws IOException, SQLException {
		derbyAssembly = new DerbyManager("target/derby");
		runtime = new ServerRuntime("cayenne-linkrest-tests.xml");
	}

	@AfterClass
	public static void tearDownClass() throws IOException, SQLException {
		runtime.shutdown();
		runtime = null;

		derbyAssembly.shutdown();
		derbyAssembly = null;
	}

	protected ObjectContext context;

	@Before
	public void before() {
		this.context = runtime.newContext();

		// this is to prevent shared caches from returning bogus data between
		// test runs
		context.performQuery(new RefreshQuery());

		QueryChain chain = new QueryChain();

		// ordering is important to avoid FK constraint failures on delete
		chain.addQuery(new EJBQLQuery("delete from E4"));
		chain.addQuery(new EJBQLQuery("delete from E3"));
		chain.addQuery(new EJBQLQuery("delete from E2"));
		chain.addQuery(new EJBQLQuery("delete from E5"));
		chain.addQuery(new EJBQLQuery("delete from E6"));
		chain.addQuery(new EJBQLQuery("delete from E7"));
		chain.addQuery(new EJBQLQuery("delete from E9"));
		chain.addQuery(new EJBQLQuery("delete from E8"));
		chain.addQuery(new EJBQLQuery("delete from E11"));
		chain.addQuery(new EJBQLQuery("delete from E10"));
		chain.addQuery(new EJBQLQuery("delete from E12E13"));
		chain.addQuery(new EJBQLQuery("delete from E12"));
		chain.addQuery(new EJBQLQuery("delete from E13"));

		chain.addQuery(new EJBQLQuery("delete from E14"));
		chain.addQuery(new EJBQLQuery("delete from E15"));

		context.performGenericQuery(chain);
	}

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
		return new LinkRestBuilder().cayenneRuntime(runtime);
	}

	protected abstract void doAddResources(FeatureContext context);

	protected int intForQuery(String querySql) {
		return SQLSelect.scalarQuery(Integer.class, querySql).selectOne(context).intValue();
	}

	protected void insert(String table, String columns, String values) {
		String insertSql = "INSERT INTO utest." + table + " (" + columns + ") VALUES (" + values + ")";
		context.performGenericQuery(new SQLTemplate(E1.class, insertSql));
	}

	protected Entity<String> jsonEntity(String data) {
		return Entity.entity(data, MediaType.APPLICATION_JSON);
	}
}
