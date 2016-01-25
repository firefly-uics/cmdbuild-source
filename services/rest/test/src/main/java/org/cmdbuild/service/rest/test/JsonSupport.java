package org.cmdbuild.service.rest.test;

import static org.codehaus.jackson.map.SerializationConfig.Feature.FAIL_ON_EMPTY_BEANS;

import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.node.ObjectNode;
import org.junit.rules.ExternalResource;

public class JsonSupport extends ExternalResource {

	private ObjectMapper objectMapper;

	@Override
	protected void before() throws Throwable {
		objectMapper = new ObjectMapper();
		final SerializationConfig serializationConfig = objectMapper.getSerializationConfig() //
				.without(FAIL_ON_EMPTY_BEANS);
		objectMapper.setSerializationConfig(serializationConfig);
	}

	public JsonNode from(final Object o) throws Exception {
		return from(objectMapper.writeValueAsString(o));
	}

	public JsonNode from(final InputStream is) throws Exception {
		final String s = IOUtils.toString(is);
		return from(s);
	}

	public JsonNode from(final String s) throws Exception {
		return objectMapper.readTree(s);
	}

	public ObjectNode newObject() {
		return objectMapper.createObjectNode();
	}

}
