package integration.cxf;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.apache.commons.io.FileUtils.write;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.cmdbuild.service.rest.constants.Serialization.ATTACHMENT;
import static org.cmdbuild.service.rest.constants.Serialization.FILE;
import static org.cmdbuild.service.rest.model.Models.newResponseSingle;
import static org.cmdbuild.service.rest.test.ServerResource.randomPort;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.activation.DataHandler;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.StringPart;
import org.apache.commons.io.IOUtils;
import org.cmdbuild.service.rest.cxf.CardAttachments;
import org.cmdbuild.service.rest.model.Attachment;
import org.cmdbuild.service.rest.model.ResponseSingle;
import org.cmdbuild.service.rest.test.JsonSupport;
import org.cmdbuild.service.rest.test.ServerResource;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.ArgumentCaptor;

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
	public void createWithBothAttachmentAndFile() throws Exception {
		// given
		final File file = temporaryFolder.newFile();
		write(file, "blah blah blah");
		final ResponseSingle<String> expectedResponse = newResponseSingle(String.class) //
				.withElement("the id") //
				.build();
		doReturn(expectedResponse) //
				.when(service).create(anyString(), anyLong(), any(Attachment.class), any(DataHandler.class));

		// when
		final PostMethod post = new PostMethod(server.resource("classes/foo/cards/123/attachments/"));
		final Part[] parts = { new StringPart(ATTACHMENT, EMPTY //
				+ "{" //
				+ "\"_description\": \"the description\"" //
				+ "}") {

			@Override
			public String getContentType() {
				return APPLICATION_JSON;
			}

		}, new FilePart(FILE, file) };
		post.setRequestEntity(new MultipartRequestEntity(parts, post.getParams()));
		final int result = httpclient.executeMethod(post);

		// then
		assertThat(result, equalTo(200));
		assertThat(json.from(post.getResponseBodyAsString()), equalTo(json.from(expectedResponse)));

		final ArgumentCaptor<Attachment> attachmentCaptor = ArgumentCaptor.forClass(Attachment.class);
		final ArgumentCaptor<DataHandler> dataHandlerCaptor = ArgumentCaptor.forClass(DataHandler.class);
		verify(service).create(eq("foo"), eq(123L), attachmentCaptor.capture(), dataHandlerCaptor.capture());
		verifyNoMoreInteractions(service);

		final Attachment attachment = attachmentCaptor.getValue();
		assertThat(attachment.getDescription(), equalTo("the description"));

		final DataHandler dataHandler = dataHandlerCaptor.getValue();
		assertThat(toString(dataHandler.getInputStream()), equalTo(toString(new FileInputStream(file))));
	}

	@Test
	@Ignore("data handler seems correct but it looks empty even if it's referring to the right file")
	public void createWithFileOnly() throws Exception {
		// given
		final File file = temporaryFolder.newFile();
		write(file, "blah blah blah");
		final ResponseSingle<String> expectedResponse = newResponseSingle(String.class) //
				.withElement("the id") //
				.build();
		doReturn(expectedResponse) //
				.when(service).create(anyString(), anyLong(), any(Attachment.class), any(DataHandler.class));

		// when
		final PostMethod post = new PostMethod(server.resource("classes/foo/cards/123/attachments/"));
		final Part[] parts = { new FilePart(FILE, file) };
		post.setRequestEntity(new MultipartRequestEntity(parts, post.getParams()));
		final int result = httpclient.executeMethod(post);

		// then
		assertThat(result, equalTo(200));
		assertThat(json.from(post.getResponseBodyAsString()), equalTo(json.from(expectedResponse)));

		final ArgumentCaptor<DataHandler> dataHandlerCaptor = ArgumentCaptor.forClass(DataHandler.class);
		verify(service).create(eq("foo"), eq(123L), isNull(Attachment.class), dataHandlerCaptor.capture());
		verifyNoMoreInteractions(service);

		final DataHandler dataHandler = dataHandlerCaptor.getValue();
		assertThat(toString(dataHandler.getInputStream()), equalTo(toString(new FileInputStream(file))));
	}

	@Test
	public void updateWithBothAttachmentAndFile() throws Exception {
		// given
		final File file = temporaryFolder.newFile();
		write(file, "blah blah blah");

		// when
		final PutMethod put = new PutMethod(server.resource("classes/foo/cards/123/attachments/bar/"));
		final Part[] parts = { new StringPart(ATTACHMENT, EMPTY //
				+ "{" //
				+ "\"_description\": \"the new description\"" //
				+ "}") {

			@Override
			public String getContentType() {
				return APPLICATION_JSON;
			}

		}, new FilePart(FILE, file) };
		put.setRequestEntity(new MultipartRequestEntity(parts, put.getParams()));
		final int result = httpclient.executeMethod(put);

		// then
		assertThat(result, equalTo(204));

		final ArgumentCaptor<Attachment> attachmentCaptor = ArgumentCaptor.forClass(Attachment.class);
		final ArgumentCaptor<DataHandler> dataHandlerCaptor = ArgumentCaptor.forClass(DataHandler.class);
		verify(service).update(eq("foo"), eq(123L), eq("bar"), attachmentCaptor.capture(), dataHandlerCaptor.capture());
		verifyNoMoreInteractions(service);

		final Attachment attachment = attachmentCaptor.getValue();
		assertThat(attachment.getDescription(), equalTo("the new description"));

		final DataHandler dataHandler = dataHandlerCaptor.getValue();
		assertThat(toString(new FileInputStream(file)), equalTo(toString(dataHandler.getInputStream())));
	}

	@Test
	@Ignore("data handler seems correct but it looks empty even if it's referring to the right file")
	public void updateWithFileOnly() throws Exception {
		// given
		final File file = temporaryFolder.newFile();
		write(file, "blah blah blah");

		// when
		final PutMethod put = new PutMethod(server.resource("classes/foo/cards/123/attachments/bar/"));
		final Part[] parts = { new FilePart(FILE, file) };
		put.setRequestEntity(new MultipartRequestEntity(parts, put.getParams()));
		final int result = httpclient.executeMethod(put);

		// then
		assertThat(result, equalTo(204));

		final ArgumentCaptor<DataHandler> dataHandlerCaptor = ArgumentCaptor.forClass(DataHandler.class);
		verify(service).update(eq("foo"), eq(123L), eq("bar"), isNull(Attachment.class), dataHandlerCaptor.capture());
		verifyNoMoreInteractions(service);

		final DataHandler dataHandler = dataHandlerCaptor.getValue();
		assertThat(toString(new FileInputStream(file)), equalTo(toString(dataHandler.getInputStream())));
	}

	@Test
	public void updateWithAttachmentOnly() throws Exception {
		// given
		final File file = temporaryFolder.newFile();
		write(file, "blah blah blah");

		// when
		final PutMethod put = new PutMethod(server.resource("classes/foo/cards/123/attachments/bar/"));
		final Part[] parts = { new StringPart(ATTACHMENT, EMPTY //
				+ "{" //
				+ "\"_description\": \"the new description\"" //
				+ "}") {

			@Override
			public String getContentType() {
				return APPLICATION_JSON;
			}

		} };
		put.setRequestEntity(new MultipartRequestEntity(parts, put.getParams()));
		final int result = httpclient.executeMethod(put);

		// then
		assertThat(result, equalTo(204));

		final ArgumentCaptor<Attachment> attachmentCaptor = ArgumentCaptor.forClass(Attachment.class);
		verify(service).update(eq("foo"), eq(123L), eq("bar"), attachmentCaptor.capture(), isNull(DataHandler.class));
		verifyNoMoreInteractions(service);

		final Attachment attachment = attachmentCaptor.getValue();
		assertThat(attachment.getDescription(), equalTo("the new description"));
	}

	private static String toString(final InputStream inputStream) throws IOException {
		final String string = IOUtils.toString(inputStream);
		IOUtils.closeQuietly(inputStream);
		return string;
	}

}
