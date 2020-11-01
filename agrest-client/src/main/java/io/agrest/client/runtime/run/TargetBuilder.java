package io.agrest.client.runtime.run;

import io.agrest.base.protocol.AgProtocol;
import io.agrest.client.protocol.AgcRequest;
import io.agrest.client.protocol.Expression;
import io.agrest.client.protocol.Include;

import javax.ws.rs.client.WebTarget;
import java.util.Objects;
import java.util.Optional;

/**
 * @since 2.0
 */
public class TargetBuilder {

	public static TargetBuilder target(WebTarget target) {
		return new TargetBuilder(target);
	}

	private final WebTarget target;
	private AgcRequest request;

	private TargetBuilder(WebTarget target) {
		this.target = Objects.requireNonNull(target);
	}

	public TargetBuilder request(AgcRequest request) {

		this.request = Objects.requireNonNull(request);
		return this;
	}

	public WebTarget build() {

		Objects.requireNonNull(request);

		WebTarget newTarget = target;

		Optional<Long> start = request.getStart();
		if (start.isPresent()) {
			newTarget = newTarget.queryParam(AgProtocol.start.name(), start.get());
		}

		Optional<Long> limit = request.getLimit();
		if (limit.isPresent()) {
			newTarget = newTarget.queryParam(AgProtocol.limit.name(), limit.get());
		}

		for (String exclude : request.getExcludes()) {
			newTarget = newTarget.queryParam(AgProtocol.exclude.name(), exclude);
		}

		RequestEncoder encoder = RequestEncoder.encoder();

		Optional<Expression> exp = request.getCayenneExp();
		if (exp.isPresent()) {
			newTarget = newTarget.queryParam(AgProtocol.exp.name(), encoder.encode(exp.get()));
		}

		if (!request.getOrderings().isEmpty()) {
			newTarget = newTarget.queryParam(AgProtocol.sort.name(), encoder.encode(request.getOrderings()));
		}
		
		for (Include include : request.getIncludes()) {
			newTarget = newTarget.queryParam(AgProtocol.include.name(), encoder.encode(include));
		}

		return newTarget;
	}
}
