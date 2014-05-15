package com.nhl.link.rest.runtime.parser;

import java.util.AbstractMap;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.core.MultivaluedMap;

class EmptyMultiValuedMap extends AbstractMap<String, List<String>> implements MultivaluedMap<String, String> {

	private static EmptyMultiValuedMap EMPTY_MAP = new EmptyMultiValuedMap();

	public static final MultivaluedMap<String, String> map() {
		return EMPTY_MAP;
	}

	public int size() {
		return 0;
	}

	public boolean isEmpty() {
		return true;
	}

	public boolean containsKey(Object key) {
		return false;
	}

	public boolean containsValue(Object value) {
		return false;
	}

	public List<String> get(Object key) {
		return null;
	}

	public Set<String> keySet() {
		return Collections.emptySet();
	}

	public Collection<List<String>> values() {
		return Collections.emptySet();
	}

	public Set<Map.Entry<String, List<String>>> entrySet() {
		return Collections.emptySet();
	}

	public boolean equals(Object o) {
		return (o instanceof Map) && ((Map<?, ?>) o).isEmpty();
	}

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
