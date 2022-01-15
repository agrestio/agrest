package io.agrest.provider;

import io.agrest.AgException;
import io.agrest.SimpleResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class JaxrsAgExceptionMapper implements ExceptionMapper<AgException> {

    private static final Logger LOGGER = LoggerFactory.getLogger(JaxrsAgExceptionMapper.class);

    @Override
    public Response toResponse(AgException exception) {

        String message = exception.getMessage();
        Throwable cause = exception.getCause() != null && exception.getCause() != exception ? exception.getCause() : null;
        String causeMessage = cause != null ? cause.getMessage() : null;
        int status = exception.getStatus();

        if (LOGGER.isInfoEnabled()) {
            StringBuilder log = new StringBuilder();

            Status jaxRSStatus = Status.fromStatusCode(status);

            log.append(status).append(" ").append(jaxRSStatus.getReasonPhrase());

            if (message != null) {
                log.append(" (").append(message).append(")");
            }

            if (causeMessage != null) {
                log.append(" [cause: ").append(causeMessage).append("]");
            } else if (cause != null) {
                log.append(" [cause: ").append(cause.getClass().getName()).append("]");
            }

            // include stack trace in debug mode...
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(log.toString(), exception);
            } else {
                LOGGER.info(log.toString());
            }
        }

        return Response
                .status(status)
                .entity(SimpleResponse.of(status, false, message))
                .type(MediaType.APPLICATION_JSON_TYPE).build();
    }
}
