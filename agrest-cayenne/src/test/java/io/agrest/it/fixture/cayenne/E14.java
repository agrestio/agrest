package io.agrest.it.fixture.cayenne;

import io.agrest.annotation.AgAttribute;
import io.agrest.annotation.AgRelationship;
import io.agrest.it.fixture.cayenne.auto._E14;
import io.agrest.it.fixture.pojo.model.P7;

public class E14 extends _E14 {

	private static final long serialVersionUID = 1L;

	private P7 p7;

	@AgAttribute
	public String getPrettyName() {
		return getName() + "_pretty";
	}

	@AgRelationship
	public P7 getP7() {
		return p7;
	}

	public void setP7(P7 p7) {
		this.p7 = p7;
	}

}
