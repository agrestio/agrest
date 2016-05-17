package com.nhl.link.rest.it.fixture.cayenne;

import com.nhl.link.rest.annotation.LrAttribute;
import com.nhl.link.rest.annotation.LrRelationship;

public class E20Pojo {

	private String string;
	private int integer;
	private E20SubPojo subPojo;

	@LrAttribute
	public String getString() {
		return string;
	}

	public void setString(String string) {
		this.string = string;
	}

	@LrAttribute
	public int getInteger() {
		return integer;
	}

	public void setInteger(int integer) {
		this.integer = integer;
	}
	
	@LrRelationship
	public E20SubPojo getSubPojo() {
		return subPojo;
	}
	
	public void setSubPojo(E20SubPojo subPojo) {
		this.subPojo = subPojo;
	}
}
