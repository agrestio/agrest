package com.nhl.link.rest.runtime.parser;

import java.util.AbstractMap;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.core.MultivaluedMap;

/**
 * @since 1.5
 */
public class EmptyMultiValuedMap extends AbstractMap<String, List<String>> implements MultivaluedMap<String, String> {

	private static EmptyMultiValuedMap EMPTY_MAP = new EmptyMultiValuedMap();

	public static final MultivaluedMap<String, String> map() {
		return EMPTY_MAP;
	}

	@Override
	public int size() {
		return 0;
	}

	@Override
	public boolean isEmpty() {
		return true;
	}

	@Override
	public boolean containsKey(Object key) {
		return false;
	}

	@Override
	public boolean containsValue(Object value) {
		return false;
	}

	@Override
	public List<String> get(Object key) {
		return null;
	}

	@Override
	public Set<String> keySet() {
		return Collections.emptySet();
	}

	@Override
	public Collection<List<String>> values() {
		return Collections.emptySet();
	}

	@Override
	public Set<Map.Entry<String, List<String>>> entrySet() {
		return Collections.emptySet();
	}

	@Override
	public boolean equals(Object o) {
		return (o instanceof Map) && ((Map<?, ?>) o).isEmpty();
	}

	@Override
	public int hashCode() {
		return 0;
	}

	@Override
	public void putSingle(String key, String value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void add(String key, String value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void addFirst(String key, String value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void addAll(String key, List<String> valueList) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void addAll(String key, String... newValues) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean equalsIgnoreValueOrder(MultivaluedMap<String, String> otherMap) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getFirst(String key) {
		return null;
	}

}
