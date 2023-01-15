package io.agrest.access;

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
}
