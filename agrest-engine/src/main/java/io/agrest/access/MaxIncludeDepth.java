package io.agrest.access;

/**
 * A policy for the maximum depth of relationship includes.
 *
 * @since 5.0
 */
public class MaxIncludeDepth {

    private static final int DEFAULT_MAX_INCLUDE_DEPTH = 100;

    private final int depth;

    public static MaxIncludeDepth ofDefault() {
        return new MaxIncludeDepth(DEFAULT_MAX_INCLUDE_DEPTH);
    }

    /**
     * Creates a policy for the maximum depth of relationship includes. It is applied to select and update requests.
     * Includes are counted from the roor of the request. Only non-negative depth are allowed. Zero depth blocks all
     * relationships, one - anything beyond direct relationships, and so on. This policy does not affect attribute
     * includes (either root or nested).
     */
    public static MaxIncludeDepth of(int depth) {
        if (depth < 0) {
            throw new IllegalArgumentException("Negative max include depth is invalid: " + depth);
        }

        return new MaxIncludeDepth(depth);
    }

    protected MaxIncludeDepth(int depth) {
        this.depth = depth;
    }

    public int getDepth() {
        return depth;
    }
}
