package com.nhl.link.rest.client.runtime.run;

import java.util.Objects;
import java.util.Optional;

import javax.ws.rs.client.WebTarget;

import com.nhl.link.rest.client.protocol.Expression;
import com.nhl.link.rest.client.protocol.Include;
import com.nhl.link.rest.client.protocol.LrcRequest;

/**
 * @since 2.0
 */
public class TargetBuilder {

	private static final String CAYENNE_EXP = "cayenneExp";
	private static final String START = "start";
	private static final String LIMIT = "limit";
	private static final String SORT = "sort";
	private static final String EXCLUDE = "exclude";
	private static final String INCLUDE = "include";

	public static TargetBuilder target(WebTarget target) {
		return new TargetBuilder(target);
	}

	private WebTarget target;
	private LrcRequest request;

	private TargetBuilder(WebTarget target) {
		this.target = Objects.requireNonNull(target);
	}

	public TargetBuilder request(LrcRequest request) {

		this.request = Objects.requireNonNull(request);
		return this;
	}

	public WebTarget build() {

		Objects.requireNonNull(request);

		WebTarget newTarget = target;

		Optional<Long> start = request.getStart();
		if (start.isPresent()) {
			newTarget = newTarget.queryParam(START, start.get());
		}

		Optional<Long> limit = request.getLimit();
		if (limit.isPresent()) {
			newTarget = newTarget.queryParam(LIMIT, limit.get());
		}

		for (String exclude : request.getExcludes()) {
			newTarget = newTarget.queryParam(EXCLUDE, exclude);
		}

		RequestEncoder encoder = RequestEncoder.encoder();

		Optional<Expression> exp = request.getCayenneExp();
		if (exp.isPresent()) {
			newTarget = newTarget.queryParam(CAYENNE_EXP, encoder.encode(exp.get()));
		}

		if (!request.getOrderings().isEmpty()) {
			newTarget = newTarget.queryParam(SORT, encoder.encode(request.getOrderings()));
		}
		
		for (Include include : request.getIncludes()) {
			newTarget = newTarget.queryParam(INCLUDE, encoder.encode(include));
		}

		return newTarget;
	}
}
