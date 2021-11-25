package io.agrest.pojo.model;

import io.agrest.annotation.AgAttribute;
import io.agrest.annotation.AgId;
import io.agrest.annotation.AgRelationship;

public class P11 {

	private String stringId;
	private int intProp;
	private P6 p6;

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

	@AgRelationship
	public P6 getP6() {
		return p6;
	}
}
