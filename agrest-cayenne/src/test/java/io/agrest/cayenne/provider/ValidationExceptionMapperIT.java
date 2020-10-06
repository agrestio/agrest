package io.agrest.cayenne.provider;

import io.agrest.DataResponse;
import io.agrest.cayenne.unit.CayenneAgTester;
import io.agrest.cayenne.unit.DbTest;
import io.agrest.cayenne.cayenne.main.E2;
import io.bootique.junit5.BQTestTool;
import org.apache.cayenne.validation.SimpleValidationFailure;
import org.apache.cayenne.validation.ValidationException;
import org.apache.cayenne.validation.ValidationResult;
import org.junit.jupiter.api.Test;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

public class ValidationExceptionMapperIT extends DbTest {

    @BQTestTool
    static final CayenneAgTester tester = tester(Resource.class)

            .build();

    @Test
    public void testException() {
        tester.target("/g1").get()
                .wasBadRequest()
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
