package com.nhl.link.rest.runtime.encoder;

import java.sql.Types;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.ws.rs.core.Response.Status;

import com.nhl.link.rest.LinkRestException;
import com.nhl.link.rest.encoder.converter.GenericConverter;
import com.nhl.link.rest.encoder.converter.ISODateConverter;
import com.nhl.link.rest.encoder.converter.ISODateTimeConverter;
import com.nhl.link.rest.encoder.converter.ISOTimeConverter;
import com.nhl.link.rest.encoder.converter.StringConverter;
import com.nhl.link.rest.meta.LrAttribute;
import com.nhl.link.rest.meta.LrEntity;
import com.nhl.link.rest.meta.LrPersistentAttribute;

public class StringConverterFactory implements IStringConverterFactory {

	// these are explicit overrides for named attributes
	private Map<String, StringConverter> convertersByPath;

	public StringConverterFactory() {
		this.convertersByPath = new ConcurrentHashMap<String, StringConverter>();
	}

	@Override
	public StringConverter getConverter(LrEntity<?> entity) {
		return getConverter(entity, null);
	}

	@Override
	public StringConverter getConverter(LrEntity<?> entity, String attributeName) {
		String key = attributeName != null ? entity.getName() + "." + attributeName : entity.getName();

		StringConverter converter = convertersByPath.get(key);
		if (converter == null) {
			converter = buildConverter(entity, attributeName);
			convertersByPath.put(key, converter);
		}

		return converter;
	}

	protected StringConverter buildConverter(LrEntity<?> entity, String attributeName) {

		if (attributeName == null) {
			// root object encoder... assuming we'll get ID as number
			return GenericConverter.converter();
		}

		LrAttribute attribute = entity.getAttribute(attributeName);

		if (attribute == null) {
			throw new LinkRestException(Status.BAD_REQUEST, "Invalid attribute: '" + entity.getName() + "."
					+ attributeName + "'");
		}

		if (AttributeEncoderFactory.UTIL_DATE.equals(attribute.getType())) {

			if (attribute instanceof LrPersistentAttribute) {
				LrPersistentAttribute persistentAttribute = (LrPersistentAttribute) attribute;
				int dbType = persistentAttribute.getJdbcType();

				if (dbType == Types.DATE) {
					return ISODateConverter.converter();
				}

				if (dbType == Types.TIME) {
					return ISOTimeConverter.converter();
				}
			}

			// JDBC TIMESTAMP or something entirely unrecognized
			return ISODateTimeConverter.converter();
		}
		// less common cases of mapping to java.sql.* types...
		else if (AttributeEncoderFactory.SQL_TIMESTAMP.equals(attribute.getType())) {
			return ISODateTimeConverter.converter();
		} else if (AttributeEncoderFactory.SQL_DATE.equals(attribute.getType())) {
			return ISODateConverter.converter();
		} else if (AttributeEncoderFactory.SQL_TIME.equals(attribute.getType())) {
			return ISOTimeConverter.converter();
		}

		return GenericConverter.converter();
	}

}
