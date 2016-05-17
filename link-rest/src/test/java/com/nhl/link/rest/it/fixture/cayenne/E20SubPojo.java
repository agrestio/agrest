package com.nhl.link.rest.it.fixture.cayenne;

import com.nhl.link.rest.annotation.LrAttribute;

public class E20SubPojo {

	private String string;

	@LrAttribute
	public String getString() {
		return string;
	}

	public void setString(String string) {
		this.string = string;
	}

}
