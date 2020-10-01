package io.agrest;

import io.agrest.encoder.Encoder;
import io.agrest.encoder.EncoderVisitor;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.regex.Pattern;

class DataResponseFlattenExtractor<U> implements EncoderVisitor {

    private static final Pattern SPLIT_PATH = Pattern.compile("\\.");

    private final Collection<U> result;
    private final String[] path;
    private final Deque<String> stack;
    private State state;

    public DataResponseFlattenExtractor(String path) {
        this.path = path == null || path.length() == 0 ? new String[0] : SPLIT_PATH.split(path);
        this.result = new ArrayList<>();
        this.state = path != null && path.length() > 0 ? State.matching : State.collecting;
        this.stack = new ArrayDeque<>();
    }

    public Collection<U> getResult() {
        return result;
    }

    @SuppressWarnings("unchecked")
    @Override
    public int visit(Object object) {

        switch (state) {
            case matching:
                return Encoder.VISIT_CONTINUE;
            case collecting:
                result.add((U) object);
                return Encoder.VISIT_SKIP_CHILDREN;
            default:
                return Encoder.VISIT_SKIP_CHILDREN;
        }
    }

    @Override
    public void push(String relationship) {
        stack.push(relationship);

        if (state == State.matching) {
            if (path[stack.size() - 1].equals(relationship)) {

                if (path.length == stack.size()) {
                    state = State.collecting;
                }
            } else {
                state = State.invalid;
            }
        } else {
            throw new IllegalStateException("Unexpected state on push: " + state);
        }
    }

    @Override
    public void pop() {
        stack.pop();

        if (state == State.collecting) {
            state = State.matching;
        }
    }

    enum State {
        matching, collecting, invalid
    }
}
