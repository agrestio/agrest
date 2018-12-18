package io.agrest.meta;

import org.apache.cayenne.exp.parser.ASTDbPath;
import org.apache.cayenne.exp.parser.ASTPath;
import org.apache.cayenne.dba.TypesMapping;
import org.apache.cayenne.map.DbAttribute;
import org.apache.cayenne.map.ObjAttribute;

/**
 * @since 1.12
 */
public class CayenneAgDbAttribute extends CayenneAgAttribute {

    private static ObjAttribute fakeObjAttribute(String name, DbAttribute dbAttr) {
        ObjAttribute a = new ObjAttribute(name) {
            @Override
            public DbAttribute getDbAttribute() {
                return dbAttr;
            }
        };
        a.setDbAttributePath(dbAttr.getName());

        String javaType = TypesMapping.getJavaBySqlType(dbAttr.getType());
        if (javaType == null) {
            throw new NullPointerException("Java type not found for SQL type: " + dbAttr.getType());
        }
        a.setType(javaType);
        return a;
    }

    private DbAttribute dbAttribute;

    /**
     * @since 1.24
     */
    public CayenneAgDbAttribute(String name, DbAttribute dbAttr, Class<?> type) {
        super(fakeObjAttribute(name, dbAttr), type);
        dbAttribute = dbAttr;
    }

    @Override
    public int getJdbcType() {
        return dbAttribute.getType();
    }

    @Override
    public ASTPath getPathExp() {
        return new ASTDbPath(dbAttribute.getName());
    }

}
