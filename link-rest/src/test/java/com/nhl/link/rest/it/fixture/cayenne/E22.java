package com.nhl.link.rest.it.fixture.cayenne;

import com.nhl.link.rest.annotation.LrAttribute;
import com.nhl.link.rest.it.fixture.cayenne.auto._E22;

public class E22 extends _E22 {

	private static final long serialVersionUID = 1L;

	private String prop1;
	private String prop2;

	public void mergeTransient(E22 proto) {

		if (proto.getProp1() != null) {
			setProp1(proto.getProp1());
		}
		
		if (proto.getProp2() != null) {
			setProp2(proto.getProp2());
		}
	}

	@LrAttribute
	public String getProp1() {
		return prop1;
	}

	public void setProp1(String prop1) {
		this.prop1 = prop1;
	}
	
	@LrAttribute
	public String getProp2() {
		return prop2;
	}

	public void setProp2(String prop2) {
		this.prop2 = prop2;
	}
}
