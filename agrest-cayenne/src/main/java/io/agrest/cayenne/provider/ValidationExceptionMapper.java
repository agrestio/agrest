package io.agrest.cayenne.provider;

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

    private static final String ERROR_MESSAGE_EN = "Object validation failed. There were %s failure(s).";

    @Override
    public AgException toAgException(ValidationException e) {

        ValidationResult validation = e.getValidationResult();
        int status = HttpStatus.BAD_REQUEST;

        // TODO: perhaps we can convert this to a response with a list of failed properties that can be analyzed
        //  on the client? For now log details, return a generic validation message to avoid leaking too much
        //  server internals
        LOGGER.info("{} ({})", status, validation);

        return AgException.of(status, e, ERROR_MESSAGE_EN, validation.getFailures().size());
    }

}
