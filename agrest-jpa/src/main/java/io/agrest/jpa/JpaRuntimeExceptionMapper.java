package io.agrest.jpa;

import io.agrest.AgException;
import io.agrest.HttpStatus;
import io.agrest.spi.AgExceptionMapper;
import jakarta.persistence.PersistenceException;

public class JpaRuntimeExceptionMapper implements AgExceptionMapper<PersistenceException> {

    @Override
    public AgException toAgException(PersistenceException e) {
        return AgException.of(HttpStatus.INTERNAL_SERVER_ERROR, e, "PersistenceException %s", e.getMessage());
    }
}
