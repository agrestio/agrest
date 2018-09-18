package io.agrest.it.fixture;

import io.agrest.runtime.AgBuilder;
import io.agrest.runtime.AgRuntime;
import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.exp.Expression;
import org.apache.cayenne.exp.Property;
import org.apache.cayenne.query.ObjectSelect;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.inmemory.InMemoryTestContainerFactory;
import org.glassfish.jersey.test.spi.TestContainerException;
import org.junit.ClassRule;
import org.junit.Rule;

import javax.ws.rs.core.Application;
import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.core.Response;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * A main superclass of AgREST unit tests that require full stack Jersey container.
 */
public abstract class JerseyTestOnDerby extends JerseyTest {

    // TODO: switch to Bootique test framework...

    @ClassRule
    public static CayenneDerbyStack DB_STACK = new CayenneDerbyStack("derby-for-jersey");

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

        AgRuntime agFeature = doConfigure().build();

        Feature unitFeature = context -> {
            doAddResources(context);
            return true;
        };

        return new ResourceConfig()
                .register(unitFeature)
                .register(agFeature);
    }

    protected AgBuilder doConfigure() {
        return AgBuilder.builder(DB_STACK.getCayenneStack());
    }

    protected abstract void doAddResources(FeatureContext context);

    protected String stringForQuery(String querySql) {
        return DB_STACK.stringForQuery(querySql);
    }

    protected int intForQuery(String querySql) {
        return DB_STACK.intForQuery(querySql);
    }

    protected long countRows(String table) {
        return DB_STACK.longForQuery("SELECT count(1) FROM utest." + table);
    }

    protected long countRows(String table, String where) {
        return DB_STACK.longForQuery("SELECT count(1) FROM utest." + table + " " + where);
    }

    protected long countRows(Class<?> entity) {
        return ObjectSelect.columnQuery(entity, Property.COUNT).selectOne(newContext());
    }

    protected long countRows(Class<?> entity, Expression filter) {
        return ObjectSelect.columnQuery(entity, Property.COUNT).
                where(filter)
                .selectOne(newContext());
    }

    protected ObjectContext newContext() {
        return DB_STACK.newContext();
    }

    protected void insert(String table, String columns, String values) {
        DB_STACK.insert(table, columns, values);
    }

    protected void insert(String schema, String table, String columns, String values) {
        DB_STACK.insert(schema, table, columns, values);
    }

    protected ResponseAssertions onSuccess(Response response) {
        return new ResponseAssertions(response).wasSuccess();
    }

    protected ResponseAssertions onResponse(Response response) {
        return new ResponseAssertions(response);
    }
}
