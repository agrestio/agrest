package io.agrest.cayenne.spi;


import io.agrest.DataResponse;
import io.agrest.SelectStage;
import io.agrest.cayenne.cayenne.main.E2;
import io.agrest.cayenne.unit.main.MainDbTest;
import io.agrest.cayenne.unit.main.MainModelTester;
import io.agrest.jaxrs3.AgJaxrs;
import io.bootique.junit5.BQTestTool;
import org.apache.cayenne.validation.SimpleValidationFailure;
import org.apache.cayenne.validation.ValidationException;
import org.apache.cayenne.validation.ValidationResult;
import org.junit.jupiter.api.Test;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Configuration;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriInfo;

public class ValidationExceptionMapperIT extends MainDbTest {

    @BQTestTool
    static final MainModelTester tester = tester(Resource.class).build();

    @Test
    public void exception() {
        tester.target("/g1/1").get()
                .wasBadRequest()
                .bodyEquals("{\"message\":\"Object validation failed\"}");

        tester.target("/g1/2").get()
                .wasBadRequest()
                .bodyEquals("{\"message\":\"Object validation failed with 2 errors\"}");
    }

    @Path("")
    @Produces(MediaType.APPLICATION_JSON)
    public static class Resource {

        @Context
        private Configuration config;

        @GET
        @Path("g1/{failure_count}")
        public DataResponse<E2> getE2(
                @PathParam("failure_count") int failureCount,
                @Context UriInfo uriInfo) {

            // must be thrown within Ag chain
            return AgJaxrs.select(E2.class, config)
                    .stage(SelectStage.APPLY_SERVER_PARAMS, c -> {
                        ValidationResult result = new ValidationResult();

                        for (int i = 0; i < failureCount; i++) {
                            result.addFailure(new SimpleValidationFailure(new Object(), "_error_"));
                        }
                        throw new ValidationException("_cayenne_validation_", result);
                    })
                    .get();
        }
    }
}
