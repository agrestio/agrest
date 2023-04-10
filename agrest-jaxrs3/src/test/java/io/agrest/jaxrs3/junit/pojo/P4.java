package io.agrest.jaxrs3.junit.pojo;

import io.agrest.annotation.AgRelationship;

public class P4 {

	private P3 p3;

	@AgRelationship
	public P3 getP3() {
		return p3;
	}

	public void setP3(P3 p3) {
		this.p3 = p3;
	}
}
