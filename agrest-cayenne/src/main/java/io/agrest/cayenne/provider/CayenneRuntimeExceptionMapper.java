package io.agrest.cayenne.provider;

import io.agrest.SimpleResponse;
import org.apache.cayenne.CayenneRuntimeException;
import org.apache.cayenne.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * A handler for stray CayenneRuntimeExceptions that would log the exception and
 * package the response in familiar JSON format.
 */
@Provider
public class CayenneRuntimeExceptionMapper implements ExceptionMapper<CayenneRuntimeException> {

	private static final Logger LOGGER = LoggerFactory.getLogger(CayenneRuntimeExceptionMapper.class);

	@Override
	public Response toResponse(CayenneRuntimeException ex) {

		LOGGER.warn("Cayenne exception", ex);

		Throwable cause = Util.unwindException(ex);

		String message = cause.getMessage();

		if (message == null) {
			message = "";
		}

		// Cayenne result iterators would sometimes stick the entire cause stack
		// trace in the message...
		if (message.length() > 300) {
			message = message.substring(0, 300) + "...";
		}

		message = "CayenneRuntimeException " + message;

		SimpleResponse body = new SimpleResponse(false, message);
		return Response.status(Status.INTERNAL_SERVER_ERROR).entity(body).type(MediaType.APPLICATION_JSON_TYPE).build();
	}
}
