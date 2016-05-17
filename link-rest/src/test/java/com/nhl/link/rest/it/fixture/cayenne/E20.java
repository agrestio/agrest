package com.nhl.link.rest.it.fixture.cayenne;

import com.nhl.link.rest.annotation.LrRelationship;
import com.nhl.link.rest.it.fixture.cayenne.auto._E20;

public class E20 extends _E20 {

	private static final long serialVersionUID = 1L;

	private E20Pojo pojo;

	@LrRelationship
	public E20Pojo getPojo() {
		return pojo;
	}

	public void setPojo(E20Pojo pojo) {
		this.pojo = pojo;
	}
}
