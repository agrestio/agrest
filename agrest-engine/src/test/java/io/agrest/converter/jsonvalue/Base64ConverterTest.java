package io.agrest.converter.jsonvalue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.TextNode;
import io.agrest.converter.jsonvalue.Base64Converter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

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
