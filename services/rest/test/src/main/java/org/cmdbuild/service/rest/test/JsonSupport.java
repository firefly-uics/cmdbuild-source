package org.cmdbuild.service.rest.test;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.rules.ExternalResource;

public class JsonSupport extends ExternalResource {

	private ObjectMapper objectMapper;

	@Override
	protected void before() throws Throwable {
		objectMapper = new ObjectMapper();
	}

	public JsonNode from(final Object o) throws Exception {
		return from(objectMapper.writeValueAsString(o));
	}

	public JsonNode from(final String s) throws Exception {
		return objectMapper.readTree(s);
	}

}
