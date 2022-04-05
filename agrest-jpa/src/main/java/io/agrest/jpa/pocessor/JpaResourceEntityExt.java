package io.agrest.jpa.pocessor;

import jakarta.persistence.Query;

/**
 * @since 5.0
 */
public interface JpaResourceEntityExt {

    Query getSelect();
}
