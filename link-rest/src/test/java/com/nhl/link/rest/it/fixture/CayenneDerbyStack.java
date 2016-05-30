package com.nhl.link.rest.it.fixture;

import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.access.dbsync.CreateIfNoSchemaStrategy;
import org.apache.cayenne.access.dbsync.SchemaUpdateStrategy;
import org.apache.cayenne.configuration.server.ServerRuntime;
import org.apache.cayenne.configuration.server.ServerRuntimeBuilder;
import org.apache.cayenne.di.Binder;
import org.apache.cayenne.di.Module;
import org.apache.cayenne.query.SQLSelect;
import org.apache.cayenne.query.SQLTemplate;
import org.junit.rules.ExternalResource;

import com.nhl.link.rest.it.fixture.cayenne.E1;

public class CayenneDerbyStack extends ExternalResource {

	private ServerRuntime cayenne;
	private DerbyManager derby;

	@Override
	public void before() {
		derby = new DerbyManager("target/test-on-derby");

		cayenne = new ServerRuntimeBuilder().addConfig("cayenne-linkrest-tests.xml").addModule(new Module() {
			@Override
			public void configure(Binder binder) {
				binder.bind(SchemaUpdateStrategy.class).to(CreateIfNoSchemaStrategy.class);
			}
		}).jdbcDriver("org.apache.derby.jdbc.EmbeddedDriver").url("jdbc:derby:target/test-on-derby;create=true")
				.build();
	}

	@Override
	protected void after() {
		cayenne.shutdown();
		cayenne = null;

		derby.shutdown();
		derby = null;
	}

	public ServerRuntime getCayenneStack() {
		return cayenne;
	}

	public ObjectContext newContext() {
		return cayenne.newContext();
	}

	public int intForQuery(String querySql) {
		Integer result = SQLSelect.scalarQuery(Integer.class, querySql).selectOne(newContext());
		return result != null ? result : -1;
	}
	
	public String stringForQuery(String querySql) {
		return SQLSelect.scalarQuery(String.class, querySql).selectOne(newContext());
	}

	public void insert(String table, String columns, String values) {
		String insertSql = "INSERT INTO utest." + table + " (" + columns + ") VALUES (" + values + ")";
		newContext().performGenericQuery(new SQLTemplate(E1.class, insertSql));
	}
}
