package io.agrest.meta.cayenne;

import io.agrest.meta.AgPersistentAttribute;
import io.agrest.meta.DefaultAgAttribute;
import org.apache.cayenne.exp.parser.ASTDbPath;
import org.apache.cayenne.exp.parser.ASTPath;
import org.apache.cayenne.map.DbAttribute;

/**
 * @since 1.12
 */
public class CayenneAgDbAttribute extends DefaultAgAttribute implements AgPersistentAttribute, CayenneAgAttribute {

    private DbAttribute dbAttribute;

    /**
     * @since 1.24
     */
    public CayenneAgDbAttribute(String name, DbAttribute dbAttribute, Class<?> type) {
        super(name, type);
        this.dbAttribute = dbAttribute;
    }

    @Override
    public ASTPath getPathExp() {
        return new ASTDbPath(dbAttribute.getName());
    }
    
    /**
     * @since 3.4
     */
    @Override
    public DbAttribute getDbAttribute() {
        return dbAttribute;
    }
}
