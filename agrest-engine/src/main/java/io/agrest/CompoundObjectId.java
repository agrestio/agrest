package io.agrest;

import io.agrest.meta.AgAttribute;
import io.agrest.base.jsonvalueconverter.Normalizer;

import javax.ws.rs.core.Response;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * A multi-value id.
 * 
 * @since 1.24
 */
public class CompoundObjectId extends BaseObjectId {

	private Map<String, Object> id;

	public CompoundObjectId(Map<String, Object> id) {
		this.id = id;
	}

	@Override
	public Object get(String attributeName) {
		return id.get(attributeName);
	}

	@Override
	public Object get() {
		return id;
	}

	@Override
	public int size() {
		return id.size();
	}

	@Override
	protected Map<String, Object> asMap(Collection<AgAttribute> idAttributes) {

		Map<String, Object> idMap = new HashMap<>();
		for (AgAttribute idAttribute : idAttributes) {
			Object idValue = Normalizer.normalize(id.get(idAttribute.getName()), idAttribute.getType());
			if (idValue == null) {
				throw new AgException(Response.Status.INTERNAL_SERVER_ERROR,
						"Failed to build a compound ID: one of the entity's ID parts is missing in this ID object: "
								+ idAttribute.getName());
			}

			idMap.put(idAttribute.getName(), idValue);
		}
		return idMap;
	}

	@Override
	public String toString() {
		return mapToString(id);
	}

	@Override
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}

		if (!(object instanceof CompoundObjectId)) {
			return false;
		}

		return id.equals(((CompoundObjectId)object).id);
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}

	public static String mapToString(Map<String, Object> m) {

		StringBuilder buf = new StringBuilder("{");

		Iterator<Map.Entry<String, Object>> iterator = m.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<String, Object> entry = iterator.next();

			buf.append(entry.getKey());
			buf.append(":");
			buf.append(entry.getValue());

			if (iterator.hasNext()) {
				buf.append(",");
			}
		}
		buf.append("}");

		return buf.toString();
	}
}
