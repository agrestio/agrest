package io.agrest.it.fixture.pojo.model;

import io.agrest.annotation.LrAttribute;
import io.agrest.annotation.LrId;

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
