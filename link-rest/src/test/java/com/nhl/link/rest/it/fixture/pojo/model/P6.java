package com.nhl.link.rest.it.fixture.pojo.model;

import com.nhl.link.rest.annotation.LrAttribute;
import com.nhl.link.rest.annotation.LrId;

public class P6 {

	private String stringId;
	private int intProp;

	@LrId
	public String getStringId() {
		return stringId;
	}

	public void setStringId(String stringId) {
		this.stringId = stringId;
	}

	@LrAttribute
	public int getIntProp() {
		return intProp;
	}

	public void setIntProp(int intProp) {
		this.intProp = intProp;
	}
}
