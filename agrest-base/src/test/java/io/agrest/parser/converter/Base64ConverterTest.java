package io.agrest.parser.converter;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.TextNode;

public class Base64ConverterTest {

	@Test
	public void testValueNonNull() {
		Base64Converter converter = new Base64Converter();
		JsonNode node = new TextNode("bXl0ZXN0");
		byte[] bytes = (byte[]) converter.valueNonNull(node);
		assertNotNull(bytes);
		assertArrayEquals(new byte[] { 'm', 'y', 't', 'e', 's', 't' }, bytes);
	}
}
