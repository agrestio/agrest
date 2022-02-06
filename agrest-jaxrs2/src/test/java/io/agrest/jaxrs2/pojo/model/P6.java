package io.agrest.jaxrs2.pojo.model;

import io.agrest.annotation.AgAttribute;
import io.agrest.annotation.AgId;

public class P6 {

	private String stringId;
	private int intProp;

	@AgId
	public String getStringId() {
		return stringId;
	}

	public void setStringId(String stringId) {
		this.stringId = stringId;
	}

	@AgAttribute
	public int getIntProp() {
		return intProp;
	}

	public void setIntProp(int intProp) {
		this.intProp = intProp;
	}
}
