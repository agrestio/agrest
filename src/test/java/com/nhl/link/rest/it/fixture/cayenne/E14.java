package com.nhl.link.rest.it.fixture.cayenne;

import com.nhl.link.rest.annotation.LrAttribute;
import com.nhl.link.rest.it.fixture.cayenne.auto._E14;

public class E14 extends _E14 {

	private static final long serialVersionUID = 1L;

	// this attribute is added via LinKrestBuilder.transientProperty , so should
	// not have an annotation
	public String getPrettyName() {
		return getName() + "_pretty";
	}

	@LrAttribute
	public String getNotSoPrettyName() {
		return getName() + "_notpretty";
	}
}
