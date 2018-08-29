package com.nhl.link.rest.provider;

import com.nhl.link.rest.DataResponse;
import com.nhl.link.rest.it.fixture.JerseyTestOnDerby;
import com.nhl.link.rest.it.fixture.cayenne.E2;
import org.apache.cayenne.validation.SimpleValidationFailure;
import org.apache.cayenne.validation.ValidationException;
import org.apache.cayenne.validation.ValidationResult;
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

public class ValidationExceptionMapperIT extends JerseyTestOnDerby {

    @Override
    protected void doAddResources(FeatureContext context) {
        context.register(Resource.class);
    }

    @Test
    public void testException() {
        Response response = target("/g1").request().get();
        onResponse(response)
                .statusEquals(Response.Status.BAD_REQUEST)
                .bodyEquals("{\"success\":false,\"message\":\"Object validation failed. There were 1 failure(s).\"}");
    }

    @Path("")
    @Produces(MediaType.APPLICATION_JSON)
    public static class Resource {

        @Context
        private Configuration config;

        @GET
        @Path("g1")
        public DataResponse<E2> getE2(@Context UriInfo uriInfo) {
            ValidationResult result = new ValidationResult();
            result.addFailure(new SimpleValidationFailure(new Object(), "_error_"));
            throw new ValidationException("_cayenne_validation_", result);
        }
    }
}
