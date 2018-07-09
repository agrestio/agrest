package com.nhl.link.rest.client.runtime.run;

import java.util.Objects;
import java.util.Optional;

import javax.ws.rs.client.WebTarget;

import com.nhl.link.rest.client.protocol.Expression;
import com.nhl.link.rest.client.protocol.Include;
import com.nhl.link.rest.client.protocol.LrcRequest;

import static com.nhl.link.rest.Term.CAYENNE_EXP;
import static com.nhl.link.rest.Term.EXCLUDE;
import static com.nhl.link.rest.Term.INCLUDE;
import static com.nhl.link.rest.Term.LIMIT;
import static com.nhl.link.rest.Term.SORT;
import static com.nhl.link.rest.Term.START;

/**
 * @since 2.0
 */
public class TargetBuilder {

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
			newTarget = newTarget.queryParam(START.toString(), start.get());
		}

		Optional<Long> limit = request.getLimit();
		if (limit.isPresent()) {
			newTarget = newTarget.queryParam(LIMIT.toString(), limit.get());
		}

		for (String exclude : request.getExcludes()) {
			newTarget = newTarget.queryParam(EXCLUDE.toString(), exclude);
		}

		RequestEncoder encoder = RequestEncoder.encoder();

		Optional<Expression> exp = request.getCayenneExp();
		if (exp.isPresent()) {
			newTarget = newTarget.queryParam(CAYENNE_EXP.toString(), encoder.encode(exp.get()));
		}

		if (!request.getOrderings().isEmpty()) {
			newTarget = newTarget.queryParam(SORT.toString(), encoder.encode(request.getOrderings()));
		}
		
		for (Include include : request.getIncludes()) {
			newTarget = newTarget.queryParam(INCLUDE.toString(), encoder.encode(include));
		}

		return newTarget;
	}
}
