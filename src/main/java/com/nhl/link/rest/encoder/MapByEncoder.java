package com.nhl.link.rest.encoder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.ws.rs.core.Response.Status;

import org.apache.cayenne.Cayenne;
import org.apache.cayenne.DataObject;
import org.apache.cayenne.exp.Expression;

import com.fasterxml.jackson.core.JsonGenerator;
import com.nhl.link.rest.Entity;
import com.nhl.link.rest.LinkRestException;
import com.nhl.link.rest.converter.StringConverter;
import com.nhl.link.rest.runtime.encoder.IStringConverterFactory;

public class MapByEncoder extends AbstractEncoder {

	private String mapByPath;
	private List<PropertyReader> mapByReaders;
	private Encoder listEncoder;
	private StringConverter fieldNameConverter;
	private Expression filter;

	public MapByEncoder(String mapByPath, Expression filter, Entity<?> mapBy, Encoder listEncoder,
			IStringConverterFactory converterFactory) {

		if (mapBy == null) {
			throw new NullPointerException("Null mapBy");
		}

		this.mapByPath = mapByPath;
		this.mapByReaders = new ArrayList<>();
		this.listEncoder = listEncoder;
		this.filter = filter;

		config(converterFactory, mapBy);
	}

	private void config(IStringConverterFactory converterFactory, Entity<?> mapBy) {

		if (mapBy.isIdIncluded()) {
			validateLeafMapBy(mapBy);
			this.mapByReaders.add(IdReader.idReader);
			this.fieldNameConverter = converterFactory.getConverter(mapBy.getCayenneEntity());
			return;
		}

		if (!mapBy.getAttributes().isEmpty()) {

			validateLeafMapBy(mapBy);

			final String property = mapBy.getAttributes().iterator().next();

			this.mapByReaders.add(new PropertyReader() {

				@Override
				Object get(DataObject object) {
					return object.readProperty(property);
				}
			});

			this.fieldNameConverter = converterFactory.getConverter(mapBy.getCayenneEntity(), property);
			return;
		}

		if (!mapBy.getChildren().isEmpty()) {

			final String property = mapBy.getChildren().keySet().iterator().next();

			this.mapByReaders.add(new PropertyReader() {

				@Override
				Object get(DataObject object) {
					return object.readProperty(property);
				}
			});

			Entity<?> childMapBy = mapBy.getChildren().get(property);
			config(converterFactory, childMapBy);
			return;
		}

		// by default we are dealing with ID
		mapByReaders.add(IdReader.idReader);
	}

	private void validateLeafMapBy(Entity<?> mapBy) {

		if (!mapBy.getChildren().isEmpty()) {

			StringBuilder message = new StringBuilder("'mapBy' path segment '");
			message.append(mapBy.getIncoming().getName()).append(
					"should not have children. Full 'mapBy' path: " + mapByPath);

			throw new LinkRestException(Status.BAD_REQUEST, message.toString());
		}
	}

	@Override
	protected boolean encodeNonNullObject(Object object, JsonGenerator out) throws IOException {

		@SuppressWarnings({ "rawtypes", "unchecked" })
		List<DataObject> objects = (List) object;

		Map<String, List<DataObject>> map = mapBy(objects);

		out.writeStartObject();

		for (Entry<String, List<DataObject>> e : map.entrySet()) {
			out.writeFieldName(e.getKey());
			listEncoder.encode(null, e.getValue(), out);
		}

		out.writeEndObject();
		return true;
	}

	private Object mapByValue(DataObject object) {
		Object result = object;

		for (PropertyReader reader : mapByReaders) {
			if (result == null) {
				break;
			}

			if (result instanceof DataObject) {
				result = reader.get((DataObject) result);
			} else {
				throw new LinkRestException(Status.BAD_REQUEST, "Invalid 'mapBy' path: " + mapByPath);
			}
		}

		return result;
	}

	private Map<String, List<DataObject>> mapBy(List<DataObject> objects) {

		if (objects.isEmpty()) {
			return Collections.emptyMap();
		}

		// though the map is unsorted, it is still in predictable iteration
		// order...
		Map<String, List<DataObject>> map = new LinkedHashMap<String, List<DataObject>>();

		for (DataObject o : objects) {

			// filter objects even before we apply mapping...
			if (filter != null && !filter.match(o)) {
				continue;
			}

			Object key = mapByValue(o);

			// disallow nulls as JSON keys...
			// note that converter below will throw an NPE if we pass NULL
			// further down... the error here has more context.
			if (key == null) {
				throw new LinkRestException(Status.INTERNAL_SERVER_ERROR, "Null mapBy value for key '" + mapByPath
						+ "' and object '" + o.getObjectId() + "'");
			}

			String keyString = fieldNameConverter.asString(key);

			List<DataObject> list = map.get(keyString);
			if (list == null) {
				list = new ArrayList<>();
				map.put(keyString, list);
			}

			list.add(o);
		}

		return map;
	}

	private static abstract class PropertyReader {
		abstract Object get(DataObject object);
	}

	private static final class IdReader extends PropertyReader {

		static PropertyReader idReader = new IdReader();

		@Override
		Object get(DataObject object) {
			return Cayenne.intPKForObject(object);
		}
	}
}
