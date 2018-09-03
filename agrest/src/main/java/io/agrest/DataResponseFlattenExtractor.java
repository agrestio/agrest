package io.agrest;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.regex.Pattern;

import io.agrest.encoder.Encoder;
import io.agrest.encoder.EncoderVisitor;

class DataResponseFlattenExtractor<U> implements EncoderVisitor {

	static enum State {
		matching, collecting, invalid
	}

	private static final Pattern SPLIT_PATH = Pattern.compile("\\.");

	private Collection<U> result;
	private String[] path;
	private Deque<String> stack;
	private State state;

	public DataResponseFlattenExtractor(String path) {
		this.path = path == null || path.length() == 0 ? new String[0] : SPLIT_PATH.split(path);
		this.result = new ArrayList<>();
		this.state = path.length() > 0 ? State.matching : State.collecting;
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
		case invalid:
			return Encoder.VISIT_SKIP_CHILDREN;
		default:
			return Encoder.VISIT_SKIP_CHILDREN;
		}
	}

	@Override
	public void push(String relationship) {
		stack.push(relationship);

		switch (state) {
		case matching:
			if (path[stack.size() - 1].equals(relationship)) {

				if (path.length == stack.size()) {
					state = State.collecting;
				}
			} else {
				state = State.invalid;
			}

			break;
		default:
			throw new IllegalStateException("Unexpected state on push: " + state);
		}
	}

	@Override
	public void pop() {
		stack.pop();

		switch (state) {
		case collecting:
			state = State.matching;
			break;
		default:
			break;
		}
	}
}
