package com.nhl.link.rest.it;

import com.nhl.link.rest.DataResponse;
import com.nhl.link.rest.LinkRest;
import com.nhl.link.rest.it.fixture.JerseyTestOnDerby;
import com.nhl.link.rest.it.fixture.cayenne.E2;
import com.nhl.link.rest.it.fixture.cayenne.E20;
import com.nhl.link.rest.it.fixture.cayenne.E21;
import org.junit.Test;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

public class GET_AggregateIT extends JerseyTestOnDerby {

    @Override
    protected void doAddResources(FeatureContext context) {
        context.register(Resource.class);
    }

    /**
        # Aggregation on the root entity (employee)
        # ?include=count()&include=lastName

        data:
          - count() : 10
            lastName: Smith
          - count() : 1
            lastName: Adamchik
        total: 2
     */
    @Test
    public void test_Select_AggregationOnRootEntity() {

        insert("e2", "id, name", "1, 'xxx'");
        insert("e2", "id, name", "2, 'yyy'");
        insert("e2", "id, name", "3, 'yyy'");

        Response response = target("/e2")
                .queryParam("include", "count()")
                .queryParam("include", "name")
                .queryParam("sort", "name")
                .request()
                .get();

        onSuccess(response).bodyEquals(2, "{\"count()\":1,\"name\":\"xxx\"},{\"count()\":2,\"name\":\"yyy\"}");
    }

    /**
        # Aggregation on the root entity (employee) , including related property
        # ?include=avg(salary)&include=department.name

        data:
          - avg(salary) : 10000
            department:
               name: accounting
          - avg(salary) : 20000
            department:
               name: it
        total: 2
     */
    @Test
    public void test_Select_AggregationOnRootEntity_GroupByRelated() {

        insert("e21", "id, name", "1, 'xxx'");
        insert("e21", "id, name", "2, 'yyy'");
        insert("e20", "id, e21_id, age, name", "1, 1, 10, 'aaa'");
        insert("e20", "id, e21_id, age, name", "2, 1, 20, 'bbb'");
        insert("e20", "id, e21_id, age, name", "3, 2,  5, 'ccc'");

        Response response = target("/e20")
                .queryParam("include", "avg(age)")
                .queryParam("include", "e21.name")
                .queryParam("sort", "e21.name")
                .request()
                .get();

        onSuccess(response).bodyEquals(2,
                "{\"avg(age)\":15,\"e21\":{\"name\":\"xxx\"}}," +
                "{\"avg(age)\":5,\"e21\":{\"name\":\"yyy\"}}");
    }

    /**
        # Aggregation on a related entity (root is department)
        # ?include=employees.avg(salary)&include=name

        data:
          - name: accounting
            "@aggregated:employees":
                avg(salary) : 10000
          - name: it
            "@aggregated:employees":
                employees.avg(salary) : 20000
        total: 2
     */
    @Test
    public void test_Select_AggregationOnRelatedEntity_GroupByRoot() {

        insert("e21", "id, name", "1, 'xxx'");
        insert("e21", "id, name", "2, 'yyy'");
        insert("e20", "id, e21_id, age, name", "1, 1, 10, 'aaa'");
        insert("e20", "id, e21_id, age, name", "2, 1, 20, 'bbb'");
        insert("e20", "id, e21_id, age, name", "3, 2,  5, 'ccc'");

        Response response = target("/e21")
                .queryParam("include", "e20s.sum(age)")
                .queryParam("include", "name")
                .queryParam("sort", "name")
                .request()
                .get();

        onSuccess(response).bodyEquals(2,
                "{\"@aggregated:e20s\":{\"sum(age)\":30},\"name\":\"xxx\"}," +
                "{\"@aggregated:e20s\":{\"sum(age)\":5},\"name\":\"yyy\"}");
    }

    /**
        # Aggregation on a related entity, grouping by property from that entity (root is department)
        # ?include=employees.avg(salary)&include=employees.lastName

        data:
          - name: accounting
            "@aggregated:employees":
              -  avg(salary) : 10000
                 lastName: Smith
              -  avg(salary) : 20000
                 lastName: Doe
          - name: it
          ...
        total: 2
     */
    @Test
    public void test_Select_AggregationOnRelatedEntity_GroupByBoth() {

        insert("e21", "id, name", "1, 'xxx'");
        insert("e21", "id, name", "2, 'yyy'");
        insert("e20", "id, e21_id, age, name", "1, 1, 10, 'aaa'");
        insert("e20", "id, e21_id, age, name", "2, 1, 20, 'bbb'");
        insert("e20", "id, e21_id, age, name", "3, 2,  5, 'ccc'");

        Response response = target("/e21")
                .queryParam("include", "e20s.sum(age)")
                .queryParam("include", "e20s.name")
                .queryParam("include", "name")
                .queryParam("sort", "name")
                .request()
                .get();

        onSuccess(response).bodyEquals(3,
                "{\"@aggregated:e20s\":{\"name\":\"bbb\",\"sum(age)\":20},\"name\":\"xxx\"}," +
                "{\"@aggregated:e20s\":{\"name\":\"aaa\",\"sum(age)\":10},\"name\":\"xxx\"}," +
                "{\"@aggregated:e20s\":{\"name\":\"ccc\",\"sum(age)\":5},\"name\":\"yyy\"}");
    }

    /**
        # Aggregation on a related entity, grouping by property from that entity (root is department)
        # ?include=employees.avg(salary)&include=employees.lastName&include=name

        data:
          - id: ...
            name: accounting
            ...
            "@aggregated:employees":
              -  avg(salary) : 10000
                 lastName: Smith
              -  avg(salary) : 20000
                 lastName: Doe
          - id: ...
            name: it
            ...
        total: 2
     */
//    @Test
    public void test_Select_AggregationOnRelatedEntity_GroupRelated_IncludeRoot() {

        insert("e21", "id, name, age, description", "1, 'xxx', 99, 'xxx_desc'");
        insert("e21", "id, name, age, description", "2, 'yyy', 77, 'yyy_desc'");
        insert("e20", "id, e21_id, age, name", "1, 1, 10, 'aaa'");
        insert("e20", "id, e21_id, age, name", "2, 1, 20, 'aaa'");
        insert("e20", "id, e21_id, age, name", "3, 2,  5, 'bbb'");
        insert("e20", "id, e21_id, age, name", "4, 2, 15, 'ccc'");

        Response response = target("/e21")
                .queryParam("include", "e20s.sum(age)")
                .queryParam("include", "e20s.name")
                .queryParam("sort", "name")
                .queryParam("sort", "e20s.name")
                .request()
                .get();

        // need to map by self (and recursively by related to-one entity in general case)
        onSuccess(response).bodyEquals(3,
                "{\"@aggregated:e20s\":{\"name\":\"aaa\",\"sum(age)\":30},\"age\":99,\"description\":\"xxx_desc\",\"name\":\"xxx\"}," +
                "{\"@aggregated:e20s\":{\"name\":\"ccc\",\"sum(age)\":15},\"age\":77,\"description\":\"yyy_desc\",\"name\":\"yyy\"}," +
                "{\"@aggregated:e20s\":{\"name\":\"bbb\",\"sum(age)\":5},\"age\":77,\"description\":\"yyy_desc\",\"name\":\"yyy\"}");
    }

    @Path("")
    @Produces(MediaType.APPLICATION_JSON)
    public static class Resource {

        @Context
        private Configuration config;

        @GET
        @Path("e2")
        public DataResponse<E2> getE2(@Context UriInfo uriInfo) {
            return LinkRest.service(config).select(E2.class).uri(uriInfo).get();
        }

        @GET
        @Path("e20")
        public DataResponse<E20> getE20(@Context UriInfo uriInfo) {
            return LinkRest.service(config).select(E20.class).uri(uriInfo).get();
        }

        @GET
        @Path("e21")
        public DataResponse<E21> getE21(@Context UriInfo uriInfo) {
            return LinkRest.service(config).select(E21.class).uri(uriInfo).get();
        }
    }
}
