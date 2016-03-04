package com.nhl.link.rest.provider;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nhl.link.rest.LinkRestException;
import com.nhl.link.rest.SimpleResponse;

@Provider
public class LinkRestExceptionMapper implements ExceptionMapper<LinkRestException> {

	private static final Logger LOGGER = LoggerFactory.getLogger(LinkRestExceptionMapper.class);

	@Override
	public Response toResponse(LinkRestException exception) {

		String message = exception.getMessage();
		String causeMessage = exception.getCause() != null && exception.getCause() != exception
				? exception.getCause().getMessage() : null;
		Status status = exception.getStatus();

		if (LOGGER.isInfoEnabled()) {
			StringBuilder log = new StringBuilder();
			log.append(status.getStatusCode()).append(" ").append(status.getReasonPhrase());

			if (message != null) {
				log.append(" (").append(message).append(")");
			}
			
			if(causeMessage != null) {
				log.append(" [cause: ").append(causeMessage).append("]");
			}

			// include stack trace in debug mode...
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug(log.toString(), exception);
			} else {
				LOGGER.info(log.toString());
			}
		}

		SimpleResponse body = new SimpleResponse(false, message);
		return Response.status(status).entity(body).type(MediaType.APPLICATION_JSON_TYPE).build();
	}
}
