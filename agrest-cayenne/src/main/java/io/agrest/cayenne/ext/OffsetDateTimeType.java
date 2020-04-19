package io.agrest.cayenne.ext;

import org.apache.cayenne.access.types.ExtendedType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

public class OffsetDateTimeType implements ExtendedType<OffsetDateTime> {

	private static final DateTimeFormatter DEFAULT_FORMATTER = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
	
	@Override
	public String getClassName() {
		return OffsetDateTime.class.getName();
	}
	
	@Override
	public void setJdbcObject(PreparedStatement statement, OffsetDateTime value, int pos, int type, int scale)
			throws Exception {
        statement.setString(pos, DEFAULT_FORMATTER.format(value));
	}

	private OffsetDateTime fromString(String val) {
		return null != val ? OffsetDateTime.from(DEFAULT_FORMATTER.parse(val)): null;
	}
	
	@Override
	public OffsetDateTime materializeObject(ResultSet rs, int index, int type) throws Exception {
		return fromString(rs.getString(index));
	}

	@Override
	public OffsetDateTime materializeObject(CallableStatement rs, int index, int type) throws Exception {
		return fromString(rs.getString(index));
	}

	@Override
	public String toString(OffsetDateTime value) {
		return (null != value) ? ('\'' + value.toString() + '\''): "NULL";
	}
}