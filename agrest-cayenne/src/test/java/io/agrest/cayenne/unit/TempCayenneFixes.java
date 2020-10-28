package io.agrest.cayenne.unit;

import org.apache.cayenne.access.types.CharType;
import org.apache.cayenne.access.types.JsonType;
import org.apache.cayenne.configuration.server.ServerModule;
import org.apache.cayenne.di.Binder;
import org.apache.cayenne.di.Module;
import org.apache.cayenne.value.Json;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

// TODO: remove this once we can upgrade to Cayenne 4.2.M3 that would fix this:
//  https://issues.apache.org/jira/browse/CAY-2685
@Deprecated
public class TempCayenneFixes implements Module {

    @Override
    public void configure(Binder binder) {
        ServerModule.contributeDefaultTypes(binder).add(new XJsonType());
    }

    static class XJsonType extends JsonType {
        private CharType delegate;

        public XJsonType() {
            this.delegate = new CharType(true, false);
        }

        @Override
        public String getClassName() {
            return Json.class.getName();
        }

        @Override
        public void setJdbcObject(PreparedStatement statement, Json json, int pos, int type, int scale) throws Exception {
            String value = json != null ? json.getRawJson() : null;
            delegate.setJdbcObject(statement, value, pos, type, scale);
        }

        @Override
        public Json materializeObject(ResultSet rs, int index, int type) throws Exception {
            String value = delegate.materializeObject(rs, index, type);
            return value != null ? new Json(value) : null;
        }

        @Override
        public Json materializeObject(CallableStatement rs, int index, int type) throws Exception {
            String value = delegate.materializeObject(rs, index, type);
            return value != null ? new Json(value) : null;
        }

        @Override
        public String toString(Json value) {
            return value != null ? value.getRawJson() : null;
        }
    }
}
