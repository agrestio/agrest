package io.agrest.jpa.pocessor;

import io.agrest.jpa.query.JpaQueryBuilder;

/**
 * @since 5.0
 */
public interface JpaResourceEntityExt {
    JpaQueryBuilder getSelect();
}
