package com.nhl.link.rest.it.fixture.cayenne;

import com.nhl.link.rest.it.fixture.cayenne.auto._E4;

public class E4 extends _E4 {

	private static final long serialVersionUID = 1L;

	public String getDerived() {
		return getCVarchar() + "$";
	}
}
