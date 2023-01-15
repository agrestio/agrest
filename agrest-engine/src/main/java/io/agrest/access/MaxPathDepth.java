package io.agrest.access;

import io.agrest.AgException;
import io.agrest.PathConstants;

/**
 * A policy for the maximum depth of relationships in paths.
 *
 * @since 5.0
 */
public class MaxPathDepth {

    private static final int DEFAULT_DEPTH = 100;
    private final int depth;

    public static MaxPathDepth ofDefault() {
        return new MaxPathDepth(DEFAULT_DEPTH);
    }

    /**
     * Creates a policy for the maximum depth of relationship paths, such as includes. Depth is counted from the root of
     * the request. Only non-negative depths are allowed. Zero depth blocks all relationships, "1" - blocks anything
     * beyond direct relationships, and so on. Attribute paths are not counted towards depth (either root or nested).
     */
    public static MaxPathDepth of(int depth) {
        if (depth < 0) {
            throw new IllegalArgumentException("Negative max include depth is invalid: " + depth);
        }

        return new MaxPathDepth(depth);
    }

    protected MaxPathDepth(int depth) {
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
    public void checkExceedsDepth(String path) {

        if (path == null) {
            return;
        }

        int len = path.length();
        if (len <= depth) {
            return;
        }

        if (depth == 0) {
            throw AgException.badRequest(
                    "Path exceeds the max allowed depth of %s, the remaining path '%s' can't be processed",
                    depth,
                    path);
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
    }
}
