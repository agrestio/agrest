package com.nhl.link.rest.meta.cayenne;

import org.apache.cayenne.exp.parser.ASTObjPath;
import org.apache.cayenne.exp.parser.ASTPath;
import org.apache.cayenne.map.DbAttribute;
import org.apache.cayenne.map.ObjAttribute;
import org.apache.cayenne.util.ToStringBuilder;

import com.nhl.link.rest.meta.LrPersistentAttribute;

/**
 * @since 1.12
 */
public class CayenneLrAttribute implements LrPersistentAttribute {

	private ObjAttribute objAttribute;
	private Class<?> type;

	@Deprecated
	public CayenneLrAttribute(ObjAttribute objAttribute) {
		this.objAttribute = objAttribute;
		this.type = CayenneAwareLrDataMap.getJavaTypeForTypeName(objAttribute.getType());
	}

	/**
	 * @since 1.24
     */
	public CayenneLrAttribute(ObjAttribute objAttribute, Class<?> type) {
		this.objAttribute = objAttribute;
		this.type = type;
	}

	@Override
	public String getName() {
		return objAttribute.getName();
	}

	@Override
	public ASTPath getPathExp() {
		return new ASTObjPath(getName());
	}

	@Override
	public ObjAttribute getObjAttribute() {
		return objAttribute;
	}

	@Override
	public DbAttribute getDbAttribute() {
		return objAttribute.getDbAttribute();
	}

	@Override
	public String getJavaType() {
		return objAttribute.getType();
	}

	@Override
	public Class<?> getType() {
		return type;
	}

	@Override
	public int getJdbcType() {
		// TODO: this check won't be needed as soon as we switch POJOs to
		// LrDataMap instead of Cayenne DataMap. Until then we have to be
		// dealing with POJOs mapped vai CayenneLr* model.

		return objAttribute.getDbAttribute() != null ? objAttribute.getDbAttribute().getType() : Integer.MIN_VALUE;
	}

	@Override
	public String toString() {

		ToStringBuilder tsb = new ToStringBuilder(this);
		tsb.append("name", objAttribute.getName());
		return tsb.toString();
	}
}
