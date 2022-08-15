package io.agrest.cayenne.spi;

import io.agrest.AgException;
import io.agrest.HttpStatus;
import io.agrest.spi.AgExceptionMapper;
import org.apache.cayenne.validation.ValidationException;
import org.apache.cayenne.validation.ValidationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @since 5.0
 */
public class ValidationExceptionMapper implements AgExceptionMapper<ValidationException> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ValidationExceptionMapper.class);

    private static final String ERROR_MESSAGE_SINGULAR = "Object validation failed";
    private static final String ERROR_MESSAGE_PLURAL = "Object validation failed with %s errors";

    @Override
    public AgException toAgException(ValidationException e) {

        ValidationResult validation = e.getValidationResult();
        int status = HttpStatus.BAD_REQUEST;

        // TODO: perhaps we can convert this to a response with a list of failed properties that can be analyzed
        //  on the client? For now log details, return a generic validation message to avoid leaking too much
        //  server internals
        LOGGER.info("{} ({})", status, validation);

        String clientMessage = validation.getFailures().size() == 1
                ? ERROR_MESSAGE_SINGULAR
                : String.format(ERROR_MESSAGE_PLURAL, validation.getFailures().size());

        return AgException.of(status, e, clientMessage);
    }

}
