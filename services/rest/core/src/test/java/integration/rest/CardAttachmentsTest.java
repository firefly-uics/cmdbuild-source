package integration.rest;

import static java.util.Arrays.asList;
import static org.apache.commons.io.IOUtils.toByteArray;
import static org.cmdbuild.service.rest.model.Models.newAttachment;
import static org.cmdbuild.service.rest.model.Models.newMetadata;
import static org.cmdbuild.service.rest.model.Models.newResponseMultiple;
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

import javax.activation.DataHandler;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.cmdbuild.service.rest.CardAttachments;
import org.cmdbuild.service.rest.model.Attachment;
import org.cmdbuild.service.rest.model.ResponseMultiple;
import org.cmdbuild.service.rest.test.JsonSupport;
import org.cmdbuild.service.rest.test.ServerResource;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class CardAttachmentsTest {

	private CardAttachments service;

	@Rule
	public ServerResource server = ServerResource.newInstance() //
			.withServiceClass(CardAttachments.class) //
			.withService(service = mock(CardAttachments.class)) //
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
		final ResponseMultiple<Attachment> expectedResponse = newResponseMultiple(Attachment.class) //
				.withElements(asList( //
						newAttachment() //
								.withId("foo") //
								.withDescription("this is foo") //
								.build(), //
						newAttachment() //
								.withId("bar") //
								.withDescription("this is bar") //
								.build(), //
						newAttachment() //
								.withId("baz") //
								.withDescription("this is baz") //
								.build())) //
				.withMetadata(newMetadata() //
						.withTotal(42L) //
						.build()) //
				.build();
		when(service.read(anyString(), anyLong())) //
				.thenReturn(expectedResponse);

		// when
		final GetMethod get = new GetMethod(server.resource("classes/dummy/cards/123/attachments/"));
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
		final File file = temporaryFolder.newFile();
		final DataHandler expectedResponse = new DataHandler(file.toURI().toURL());
		when(service.read(anyString(), anyLong(), anyString())) //
				.thenReturn(expectedResponse);

		// when
		final GetMethod get = new GetMethod(server.resource("classes/dummy/cards/123/attachments/foo/"));
		final int result = httpclient.executeMethod(get);

		// then
		verify(service).read(eq("dummy"), eq(123L), eq("foo"));
		verifyNoMoreInteractions(service);

		assertThat(result, equalTo(200));
		assertThat(toByteArray(get.getResponseBodyAsStream()), equalTo(toByteArray(expectedResponse.getInputStream())));
	}

	@Test
	public void delete() throws Exception {
		// when
		final DeleteMethod delete = new DeleteMethod(server.resource("classes/dummy/cards/123/attachments/foo/"));
		final int result = httpclient.executeMethod(delete);

		// then
		verify(service).delete(eq("dummy"), eq(123L), eq("foo"));
		verifyNoMoreInteractions(service);

		assertThat(result, equalTo(204));
	}

}
