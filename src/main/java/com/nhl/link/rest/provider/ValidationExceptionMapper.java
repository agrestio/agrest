package com.nhl.link.rest.provider;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.apache.cayenne.validation.ValidationException;
import org.apache.cayenne.validation.ValidationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nhl.link.rest.SimpleResponse;

/**
 * @since 1.1
 */
@Provider
public class ValidationExceptionMapper implements ExceptionMapper<ValidationException> {

	private static final Logger LOGGER = LoggerFactory.getLogger(ValidationExceptionMapper.class);

	private static final String ERROR_MESSAGE_EN = "Object validation failed. There were %s failure(s).";

	@Override
	public Response toResponse(ValidationException exception) {

		ValidationResult validation = exception.getValidationResult();
		Status status = Status.BAD_REQUEST;

		// TODO: perhaps we can convert this in a true DataResponse with a list
		// of failed properties that can be analyzed on the client?

		// for now log details, return a generic validation message to avoid
		// leaking too much server internals

		if (LOGGER.isInfoEnabled()) {
			StringBuilder log = new StringBuilder();
			log.append(status.getStatusCode()).append(" ").append(status.getReasonPhrase());
			log.append(" (").append(validation).append(")");
			LOGGER.info(log.toString());
		}

		// TODO: localize error message
		String message = String.format(ERROR_MESSAGE_EN, validation.getFailures().size());

		SimpleResponse body = new SimpleResponse(false, message);
		return Response.status(status).entity(body).type(MediaType.APPLICATION_JSON_TYPE).build();
	}

}
