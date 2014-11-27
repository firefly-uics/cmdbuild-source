package integration.rest;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.apache.commons.lang3.CharEncoding.UTF_8;
import static org.cmdbuild.service.rest.model.Models.newAttachmentMetadata;
import static org.cmdbuild.service.rest.model.Models.newResponseSingle;
import static org.cmdbuild.service.rest.test.ServerResource.randomPort;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.cmdbuild.common.collect.ChainablePutMap;
import org.cmdbuild.service.rest.ProcessInstanceAttachmentMetadata;
import org.cmdbuild.service.rest.model.AttachmentMetadata;
import org.cmdbuild.service.rest.model.Models;
import org.cmdbuild.service.rest.model.ResponseSingle;
import org.cmdbuild.service.rest.model.adapter.AttachmentMetadataAdapter;
import org.cmdbuild.service.rest.test.JsonSupport;
import org.cmdbuild.service.rest.test.ServerResource;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

public class ProcessInstanceAttachmentsMetadataTest {

	private ProcessInstanceAttachmentMetadata service;

	@Rule
	public ServerResource server = ServerResource.newInstance() //
			.withServiceClass(ProcessInstanceAttachmentMetadata.class) //
			.withService(service = mock(ProcessInstanceAttachmentMetadata.class)) //
			.withPort(randomPort()) //
			.build();

	@ClassRule
	public static JsonSupport json = new JsonSupport();

	private HttpClient httpclient;

	@Before
	public void createHttpClient() throws Exception {
		httpclient = new HttpClient();
	}

	@Test
	public void readAll() throws Exception {
		// given
		final AttachmentMetadata attachmentMetadata = newAttachmentMetadata() //
				.withId("baz") //
				.withName("my name is Baz") //
				.withCategory("something") //
				.withDescription("nice to meet you") //
				.build();
		final ResponseSingle<AttachmentMetadata> sentResponse = newResponseSingle(AttachmentMetadata.class) //
				.withElement(attachmentMetadata) //
				.build();
		final ResponseSingle<Map<String, Object>> expectedResponse = Models.<Map<String, Object>> newResponseSingle() //
				.withElement(new AttachmentMetadataAdapter().marshal(attachmentMetadata)) //
				.build();
		when(service.read(anyString(), anyLong(), anyString())) //
				.thenReturn(sentResponse);

		// when
		final GetMethod get = new GetMethod(server.resource("processes/foo/instances/123/attachments/bar/metadata/"));
		final int result = httpclient.executeMethod(get);

		// then
		verify(service).read(eq("foo"), eq(123L), eq("bar"));
		verifyNoMoreInteractions(service);

		assertThat(result, equalTo(200));
		assertThat(json.from(get.getResponseBodyAsString()), equalTo(json.from(expectedResponse)));
	}

	@Test
	public void update() throws Exception {
		// when
		final PutMethod put = new PutMethod(server.resource("processes/foo/instances/123/attachments/bar/metadata/"));
		put.setRequestEntity(new StringRequestEntity("{" //
				+ "    \"_name\" : \"the name\"," //
				+ "    \"_category\" : \"the category\"," //
				+ "    \"_description\" : \"the description\"," //
				+ "    \"first\" : \"1\"," //
				+ "    \"second\" : \"2\"," //
				+ "    \"third\" : \"3\"" //
				+ "}", //
				APPLICATION_JSON, //
				UTF_8) //
		);
		final int result = httpclient.executeMethod(put);

		// then
		final ArgumentCaptor<AttachmentMetadata> attachmentMetadataCaptor = ArgumentCaptor
				.forClass(AttachmentMetadata.class);
		verify(service).update(eq("foo"), eq(123L), eq("bar"), attachmentMetadataCaptor.capture());
		verifyNoMoreInteractions(service);

		final AttachmentMetadata attachment = attachmentMetadataCaptor.getValue();
		assertThat(attachment, equalTo(newAttachmentMetadata() //
				.withName("the name") //
				.withCategory("the category") //
				.withDescription("the description") //
				.withExtra(ChainablePutMap.of(new HashMap<String, Object>()) //
						.chainablePut("first", "1") //
						.chainablePut("second", "2") //
						.chainablePut("third", "3")) //
				.build()));

		assertThat(result, equalTo(204));
	}

}
