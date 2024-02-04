package io.agrest.access;

import io.agrest.AgException;
import io.agrest.PathConstants;

/**
 * A policy defining maximum String length and number of relationships (depths) of property paths.
 *
 * @since 5.0
 */
public class PathChecker {

    private static final int DEFAULT_DEPTH = 100;

    // TODO: make this a configurable object property
    private static final int MAX_PATH_LENGTH = 1000;

    private final int depth;

    public static PathChecker ofDefault() {
        return new PathChecker(DEFAULT_DEPTH);
    }

    /**
     * Creates a policy for the maximum depth of relationship paths, such as includes. Depth is counted from the root of
     * the request. Only non-negative depths are allowed. Zero depth blocks all relationships, "1" - blocks anything
     * beyond direct relationships, and so on. Attribute paths are not counted towards depth (either root or nested).
     */
    public static PathChecker of(int depth) {
        if (depth < 0) {
            throw new IllegalArgumentException("Negative max include depth is invalid: " + depth);
        }

        return new PathChecker(depth);
    }

    /**
     * Checks whether a given path exceeds hardcoded max length in characters. Unlike {@link #exceedsDepth(String)},
     * this method is intended for fast sanity checks that don't require String content analysis.
     */
    public static void exceedsLength(String path) {
        if (path != null && path.length() > MAX_PATH_LENGTH) {
            throw AgException.badRequest("Include/exclude path too long: %s", path);
        }
    }

    protected PathChecker(int depth) {
        this.depth = depth;
    }

    public int getDepth() {
        return depth;
    }


    /**
     * Checks whether a given path exceeds this max depth. If the depth is exceeded, an exception is thrown. Successful
     * return is somewhat fuzzy, because the last component may be an attribute or a relationship, and we have no way
     * of telling without resolving the full path against the model. So the method will also succeed when there is one
     * extra path component above the threshold.
     */
    public void exceedsDepth(String path) {

        if (path == null) {
            return;
        }

        exceedsLength(path);

        int len = path.length();
        if (len <= depth) {
            return;
        }

        int dots = 0;
        for (int i = 0; i < len; i++) {
            if (path.charAt(i) == PathConstants.DOT) {

                // fuzzy logic: treating the last path component as an attribute, and do not count it in depth total
                if (++dots > depth) {
                    throw AgException.badRequest(
                            "Path exceeds the max allowed depth of %s, the remaining path '%s' can't be processed",
                            depth,
                            path);
                }
            }
        }

        // if no dots are found, allow even if the depth == 0 (same fuzzy logic as mentioned above)
    }
}
