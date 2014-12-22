package integration.rest;

import static java.util.Arrays.asList;
import static org.apache.commons.io.IOUtils.toByteArray;
import static org.cmdbuild.service.rest.model.Models.newAttachment;
import static org.cmdbuild.service.rest.model.Models.newMetadata;
import static org.cmdbuild.service.rest.model.Models.newResponseMultiple;
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

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.activation.DataHandler;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.cmdbuild.common.collect.ChainablePutMap;
import org.cmdbuild.service.rest.ProcessInstanceAttachments;
import org.cmdbuild.service.rest.model.Attachment;
import org.cmdbuild.service.rest.model.Models;
import org.cmdbuild.service.rest.model.ResponseMultiple;
import org.cmdbuild.service.rest.model.ResponseSingle;
import org.cmdbuild.service.rest.model.adapter.AttachmentAdapter;
import org.cmdbuild.service.rest.test.JsonSupport;
import org.cmdbuild.service.rest.test.ServerResource;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class ProcessInstanceAttachmentsTest {

	private ProcessInstanceAttachments service;

	@Rule
	public ServerResource server = ServerResource.newInstance() //
			.withServiceClass(ProcessInstanceAttachments.class) //
			.withService(service = mock(ProcessInstanceAttachments.class)) //
			.withPort(randomPort()) //
			.build();

	@ClassRule
	public static JsonSupport json = new JsonSupport();

	@Rule
	public TemporaryFolder temporaryFolder = new TemporaryFolder();

	private HttpClient httpclient;

	@Before
	public void createHttpClient() throws Exception {
		httpclient = new HttpClient();
	}

	@Test
	public void readAll() throws Exception {
		// given
		final Attachment foo = newAttachment() //
				.withId("foo") //
				.withDescription("this is foo") //
				.build();
		final Attachment bar = newAttachment() //
				.withId("bar") //
				.withDescription("this is bar") //
				.build();
		final Attachment baz = newAttachment() //
				.withId("baz") //
				.withDescription("this is baz") //
				.build();
		final ResponseMultiple<Attachment> sentResponse = newResponseMultiple(Attachment.class) //
				.withElements(asList(foo, bar, baz)) //
				.withMetadata(newMetadata() //
						.withTotal(42L) //
						.build()) //
				.build();
		final AttachmentAdapter adapter = new AttachmentAdapter();
		final ResponseMultiple<Map<String, Object>> expectedResponse = Models
				.<Map<String, Object>> newResponseMultiple() //
				.withElements(asList(adapter.marshal(foo), adapter.marshal(bar), adapter.marshal(baz))) //
				.withMetadata(newMetadata() //
						.withTotal(42L) //
						.build()) //
				.build();
		when(service.read(anyString(), anyLong())) //
				.thenReturn(sentResponse);

		// when
		final GetMethod get = new GetMethod(server.resource("processes/dummy/instances/123/attachments/"));
		final int result = httpclient.executeMethod(get);

		// then
		verify(service).read(eq("dummy"), eq(123L));
		verifyNoMoreInteractions(service);

		assertThat(result, equalTo(200));
		assertThat(json.from(get.getResponseBodyAsString()), equalTo(json.from(expectedResponse)));
	}

	@Test
	public void read() throws Exception {
		// given
		final Attachment attachmentMetadata = newAttachment() //
				.withId("baz") //
				.withName("my name is Baz") //
				.withCategory("something") //
				.withDescription("nice to meet you") //
				.withMetadata(ChainablePutMap.of(new HashMap<String, Object>()) //
						.chainablePut("first", 1) //
						.chainablePut("second", "2")) //
				.build();
		final ResponseSingle<Attachment> sentResponse = newResponseSingle(Attachment.class) //
				.withElement(attachmentMetadata) //
				.build();
		final ResponseSingle<Map<String, Object>> expectedResponse = Models.<Map<String, Object>> newResponseSingle() //
				.withElement(new AttachmentAdapter().marshal(attachmentMetadata)) //
				.build();
		when(service.read(anyString(), anyLong(), anyString())) //
				.thenReturn(sentResponse);

		// when
		final GetMethod get = new GetMethod(server.resource("processes/foo/instances/123/attachments/bar/"));
		final int result = httpclient.executeMethod(get);

		// then
		verify(service).read(eq("foo"), eq(123L), eq("bar"));
		verifyNoMoreInteractions(service);

		assertThat(result, equalTo(200));
		assertThat(json.from(get.getResponseBodyAsString()), equalTo(json.from(expectedResponse)));
	}

	@Test
	public void download() throws Exception {
		// given
		final File file = temporaryFolder.newFile();
		final DataHandler expectedResponse = new DataHandler(file.toURI().toURL());
		when(service.download(anyString(), anyLong(), anyString())) //
				.thenReturn(expectedResponse);

		// when
		final GetMethod get = new GetMethod(server.resource("processes/dummy/instances/123/attachments/foo/file/"));
		final int result = httpclient.executeMethod(get);

		// then
		verify(service).download(eq("dummy"), eq(123L), eq("foo"));
		verifyNoMoreInteractions(service);

		assertThat(result, equalTo(200));
		assertThat(toByteArray(get.getResponseBodyAsStream()), equalTo(toByteArray(expectedResponse.getInputStream())));
	}

	@Test
	public void delete() throws Exception {
		// when
		final DeleteMethod delete = new DeleteMethod(server.resource("processes/dummy/instances/123/attachments/foo/"));
		final int result = httpclient.executeMethod(delete);

		// then
		verify(service).delete(eq("dummy"), eq(123L), eq("foo"));
		verifyNoMoreInteractions(service);

		assertThat(result, equalTo(204));
	}

}
