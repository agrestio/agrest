package io.agrest.it.fixture;

import io.agrest.it.fixture.cayenne.E1;
import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.access.dbsync.CreateIfNoSchemaStrategy;
import org.apache.cayenne.access.dbsync.SchemaUpdateStrategyFactory;
import org.apache.cayenne.configuration.server.ServerModule;
import org.apache.cayenne.configuration.server.ServerRuntime;
import org.apache.cayenne.query.SQLSelect;
import org.apache.cayenne.query.SQLTemplate;
import org.junit.rules.ExternalResource;

// TODO: switch to Bootique test framework...
public class CayenneDerbyStack extends ExternalResource {

    private ServerRuntime cayenne;
    private DerbyManager derby;
    private String dbPath;

    public CayenneDerbyStack(String dbName) {
        this.dbPath = "target/" + dbName;
    }

    @Override
    public void before() {
        derby = new DerbyManager(dbPath);

        cayenne = ServerRuntime.builder().addConfig("cayenne-linkrest-tests.xml")
                .addModule(binder ->
                        binder.bind(SchemaUpdateStrategyFactory.class).toInstance(descriptor -> new CreateIfNoSchemaStrategy()))
                .addModule(binder -> ServerModule.contributeDomainFilters(binder).add(new DynamicModelLoader()))
                .jdbcDriver("org.apache.derby.jdbc.EmbeddedDriver").url(String.format("jdbc:derby:%s;create=true", dbPath))
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
        Number result = SQLSelect.scalarQuery(Number.class, querySql).selectOne(newContext());
        return result != null ? result.intValue() : -1;
    }

    public long longForQuery(String querySql) {
        Number result = SQLSelect.scalarQuery(Number.class, querySql).selectOne(newContext());
        return result != null ? result.longValue() : -1;
    }

    public String stringForQuery(String querySql) {
        return SQLSelect.scalarQuery(String.class, querySql).selectOne(newContext());
    }

    public void insert(String table, String columns, String values) {
        insert("utest", table, columns, values);
    }

    public void insert(String schema, String table, String columns, String values) {
        String insertSql = "INSERT INTO " + schema + "." + table + " (" + columns + ") VALUES (" + values + ")";
        newContext().performGenericQuery(new SQLTemplate(E1.class, insertSql));
    }
}
