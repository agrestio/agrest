package io.agrest.jaxrs3.junit.pojo;

import io.agrest.annotation.AgAttribute;
import io.agrest.annotation.AgRelationship;

public class P2 {

	private String name;
	private P1 p1;

	@AgAttribute
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@AgRelationship
	public P1 getP1() {
		return p1;
	}

	public void setP1(P1 p1) {
		this.p1 = p1;
	}
}
