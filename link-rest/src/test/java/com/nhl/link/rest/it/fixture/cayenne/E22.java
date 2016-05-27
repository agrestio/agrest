package com.nhl.link.rest.it.fixture.cayenne;

import com.nhl.link.rest.annotation.LrRelationship;
import com.nhl.link.rest.it.fixture.cayenne.auto._E22;

public class E22 extends _E22 {

	private static final long serialVersionUID = 1L;

	private E22Pojo pojo;

	@LrRelationship
	public E22Pojo getPojo() {
		return pojo;
	}

	public void setPojo(E22Pojo pojo) {
		this.pojo = pojo;
	}
}
