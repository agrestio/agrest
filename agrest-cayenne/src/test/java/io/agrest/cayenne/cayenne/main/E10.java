package io.agrest.cayenne.cayenne.main;

import io.agrest.annotation.AgAttribute;
import io.agrest.annotation.AgRelationship;
import io.agrest.cayenne.cayenne.main.auto._E10;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

public class E10 extends _E10 {

	@Override
	@AgAttribute(writable = false)
	public Boolean getCBoolean() {
		return super.getCBoolean();
	}

	@Override
	@AgRelationship(writable = false)
	public List<E11> getE11s() {
		return super.getE11s();
	}

	@Override
	@AgAttribute(readable = false, writable = false)
	public Date getCDate() {
		return super.getCDate();
	}

	@Override
	@AgAttribute(readable = false, writable = false)
	public BigDecimal getCDecimal() {
		return super.getCDecimal();
	}

	@Override
	@AgAttribute(readable = false, writable = false)
	public Date getCTime() {
		return super.getCTime();
	}

	@Override
	@AgAttribute(readable = false, writable = false)
	public Date getCTimestamp() {
		return super.getCTimestamp();
	}

	@Override
	@AgAttribute(readable = false, writable = false)
	public String getCVarchar() {
		return super.getCVarchar();
	}
}
