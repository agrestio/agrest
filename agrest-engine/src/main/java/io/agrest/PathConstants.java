package io.agrest;

import io.agrest.access.PathChecker;

public interface PathConstants {

    char DOT = '.';
    String ID_PK_ATTRIBUTE = "id";

    /**
     * @deprecated in favor of {@link PathChecker#exceedsLength(String)}. The new size
     * limit is 1000 chars.
     */
    @Deprecated(since = "5.0")
    int MAX_PATH_LENGTH = 300;
}
