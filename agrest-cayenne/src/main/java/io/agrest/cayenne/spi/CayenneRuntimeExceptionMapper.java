package io.agrest.cayenne.spi;

import io.agrest.AgException;
import io.agrest.HttpStatus;
import io.agrest.spi.AgExceptionMapper;
import org.apache.cayenne.CayenneRuntimeException;
import org.apache.cayenne.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A handler for stray CayenneRuntimeExceptions that would log the exception and package the response in familiar JSON
 * format.
 *
 * @since 5.0
 */
public class CayenneRuntimeExceptionMapper implements AgExceptionMapper<CayenneRuntimeException> {

    private static final Logger LOGGER = LoggerFactory.getLogger(CayenneRuntimeExceptionMapper.class);

    @Override
    public AgException toAgException(CayenneRuntimeException e) {

        LOGGER.warn("Cayenne exception", e);

        Throwable cause = Util.unwindException(e);

        String message = cause.getMessage();

        if (message == null) {
            message = "";
        }

        // Cayenne result iterators would sometimes stick the entire cause stack
        // trace in the message...
        if (message.length() > 300) {
            message = message.substring(0, 300) + "...";
        }

        return AgException.of(HttpStatus.INTERNAL_SERVER_ERROR, e, "CayenneRuntimeException %s", message);
    }
}
