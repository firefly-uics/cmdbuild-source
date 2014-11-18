package integration.rest;

import static java.util.Arrays.asList;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.apache.commons.io.FileUtils.write;
import static org.apache.commons.io.IOUtils.toByteArray;
import static org.cmdbuild.service.rest.constants.Serialization.UNDERSCORED_ATTACHMENT;
import static org.cmdbuild.service.rest.constants.Serialization.UNDERSCORED_CATEGORY;
import static org.cmdbuild.service.rest.constants.Serialization.UNDERSCORED_DESCRIPTION;
import static org.cmdbuild.service.rest.constants.Serialization.UNDERSCORED_FILE;
import static org.cmdbuild.service.rest.constants.Serialization.UNDERSCORED_ID;
import static org.cmdbuild.service.rest.constants.Serialization.UNDERSCORED_NAME;
import static org.cmdbuild.service.rest.model.Models.newAttachment;
import static org.cmdbuild.service.rest.model.Models.newMetadata;
import static org.cmdbuild.service.rest.model.Models.newResponseMultiple;
import static org.cmdbuild.service.rest.model.Models.newResponseSingle;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static support.ServerResource.randomPort;

import java.io.File;
import java.io.FileInputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.activation.DataHandler;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.StringPart;
import org.cmdbuild.common.collect.ChainablePutMap;
import org.cmdbuild.service.rest.CardAttachments;
import org.cmdbuild.service.rest.model.Attachment;
import org.cmdbuild.service.rest.model.Models;
import org.cmdbuild.service.rest.model.ResponseMultiple;
import org.cmdbuild.service.rest.model.ResponseSingle;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.ArgumentCaptor;

import support.JsonSupport;
import support.ServerResource;

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
	public void create() throws Exception {
		// given
		final File file = temporaryFolder.newFile();
		write(file, "blah blah blah");
		final ResponseSingle<String> expectedResponse = newResponseSingle(String.class) //
				.withElement("the id") //
				.build();
		doReturn(expectedResponse) //
				.when(service).create(anyString(), anyLong(), any(Attachment.class), any(DataHandler.class));

		// when
		final PostMethod post = new PostMethod(server.resource("classes/dummy/cards/123/attachments/"));
		final Part[] parts = { new StringPart(UNDERSCORED_ATTACHMENT, "{" //
				+ "    \"_name\" : \"the name\"," //
				+ "    \"_category\" : \"the category\"," //
				+ "    \"_description\" : \"the description\"," //
				+ "    \"foo\" : \"bar\"," //
				+ "    \"bar\" : \"baz\"," //
				+ "    \"baz\" : \"foo\"" //
				+ "}") {

			@Override
			public String getContentType() {
				return APPLICATION_JSON;
			}

		}, new FilePart(UNDERSCORED_FILE, file) };
		post.setRequestEntity(new MultipartRequestEntity(parts, post.getParams()));
		final int result = httpclient.executeMethod(post);

		// then
		final ArgumentCaptor<Attachment> attachmentCaptor = ArgumentCaptor.forClass(Attachment.class);
		final ArgumentCaptor<DataHandler> dataHandlerCaptor = ArgumentCaptor.forClass(DataHandler.class);
		verify(service).create(eq("dummy"), eq(123L), attachmentCaptor.capture(), //
				dataHandlerCaptor.capture() //
				);
		verifyNoMoreInteractions(service);

		final Attachment attachment = attachmentCaptor.getValue();
		assertThat(attachment, equalTo(newAttachment() //
				.withName("the name") //
				.withCategory("the category") //
				.withDescription("the description") //
				.withMetadata(ChainablePutMap.of(new HashMap<String, Object>()) //
						.chainablePut("foo", "bar") //
						.chainablePut("bar", "baz") //
						.chainablePut("baz", "foo")) //
				.build()));
		final DataHandler dataHandler = dataHandlerCaptor.getValue();
		assertThat(toByteArray(new FileInputStream(file)), equalTo(toByteArray(dataHandler.getInputStream())));

		assertThat(result, equalTo(200));
		assertThat(json.from(post.getResponseBodyAsString()), equalTo(json.from(expectedResponse)));
	}

	@Test
	public void readAll() throws Exception {
		// given
		final ResponseMultiple<Attachment> sentResponse = newResponseMultiple(Attachment.class) //
				.withElements(asList( //
						newAttachment() //
								.withId("foo") //
								.withName("FOO") //
								.withCategory("something") //
								.withDescription("bar") //
								.build(), //
						newAttachment() //
								.withId("bar") //
								.withName("BAR") //
								.withCategory("something else") //
								.withDescription("baz") //
								.withMetadata(ChainablePutMap.of(new HashMap<String, Object>()) //
										.chainablePut("first", "1") //
										.chainablePut("second", "2") //
								) //
								.build())) //
				.withMetadata(newMetadata() //
						.withTotal(2L) //
						.build()) //
				.build();
		@SuppressWarnings("unchecked")
		final ResponseMultiple<Map<String, Object>> expectedResponse = Models
				.<Map<String, Object>> newResponseMultiple() //
				.withElements(Arrays.<Map<String, Object>> asList( //
						ChainablePutMap.of(new HashMap<String, Object>()) //
								.chainablePut(UNDERSCORED_ID, "foo") //
								.chainablePut(UNDERSCORED_NAME, "FOO") //
								.chainablePut(UNDERSCORED_CATEGORY, "something") //
								.chainablePut(UNDERSCORED_DESCRIPTION, "bar"), //
						ChainablePutMap.of(new HashMap<String, Object>()) //
								.chainablePut(UNDERSCORED_ID, "bar") //
								.chainablePut(UNDERSCORED_NAME, "BAR") //
								.chainablePut(UNDERSCORED_CATEGORY, "something else") //
								.chainablePut(UNDERSCORED_DESCRIPTION, "baz") //
								.chainablePut("first", "1") //
								.chainablePut("second", "2") //
						)) //
				.withMetadata(newMetadata() //
						.withTotal(2L) //
						.build()) //
				.build();
		when(service.read(anyString(), anyLong())) //
				.thenReturn(sentResponse);

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
	public void update() throws Exception {
		// given
		final File file = temporaryFolder.newFile();
		write(file, "blah blah blah");

		// when
		final PutMethod put = new PutMethod(server.resource("classes/dummy/cards/123/attachments/somefile/"));
		final Part[] parts = { new StringPart(UNDERSCORED_ATTACHMENT, "{" //
				+ "    \"_name\" : \"the name\"," //
				+ "    \"_category\" : \"the category\"," //
				+ "    \"_description\" : \"the description\"," //
				+ "    \"foo\" : \"bar\"," //
				+ "    \"bar\" : \"baz\"," //
				+ "    \"baz\" : \"foo\"" //
				+ "}") {

			@Override
			public String getContentType() {
				return APPLICATION_JSON;
			}

		}, new FilePart(UNDERSCORED_FILE, file) };
		put.setRequestEntity(new MultipartRequestEntity(parts, put.getParams()));
		final int result = httpclient.executeMethod(put);

		// then
		final ArgumentCaptor<Attachment> attachmentCaptor = ArgumentCaptor.forClass(Attachment.class);
		final ArgumentCaptor<DataHandler> dataHandlerCaptor = ArgumentCaptor.forClass(DataHandler.class);
		verify(service).update(eq("dummy"), eq(123L), eq("somefile"), attachmentCaptor.capture(), //
				dataHandlerCaptor.capture() //
				);
		verifyNoMoreInteractions(service);

		final Attachment attachment = attachmentCaptor.getValue();
		assertThat(attachment, equalTo(newAttachment() //
				.withName("the name") //
				.withCategory("the category") //
				.withDescription("the description") //
				.withMetadata(ChainablePutMap.of(new HashMap<String, Object>()) //
						.chainablePut("foo", "bar") //
						.chainablePut("bar", "baz") //
						.chainablePut("baz", "foo")) //
				.build()));
		final DataHandler dataHandler = dataHandlerCaptor.getValue();
		assertThat(toByteArray(new FileInputStream(file)), equalTo(toByteArray(dataHandler.getInputStream())));

		assertThat(result, equalTo(204));
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
