package integration.cxf;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.apache.commons.io.FileUtils.write;
import static org.apache.http.Consts.UTF_8;
import static org.cmdbuild.service.rest.constants.Serialization.ATTACHMENT;
import static org.cmdbuild.service.rest.constants.Serialization.FILE;
import static org.cmdbuild.service.rest.model.Models.newResponseSingle;
import static org.cmdbuild.service.rest.test.HttpClientUtils.contentOf;
import static org.cmdbuild.service.rest.test.HttpClientUtils.statusCodeOf;
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

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.HttpClientBuilder;
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
		httpclient = HttpClientBuilder.create().build();
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
		final HttpPost post = new HttpPost(server.resource("classes/foo/cards/123/attachments/"));
		final MultipartEntity multipartEntity = new MultipartEntity();
		multipartEntity.addPart(ATTACHMENT, new StringBody("{" //
				+ "\"_description\": \"the description\"" //
				+ "}", APPLICATION_JSON, UTF_8));
		multipartEntity.addPart(FILE, new FileBody(file));
		post.setEntity(multipartEntity);
		final HttpResponse response = httpclient.execute(post);

		// then
		assertThat(statusCodeOf(response), equalTo(200));
		assertThat(json.from(contentOf(response)), equalTo(json.from(expectedResponse)));

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
		final HttpPost post = new HttpPost(server.resource("classes/foo/cards/123/attachments/"));
		final MultipartEntity multipartEntity = new MultipartEntity();
		multipartEntity.addPart(FILE, new FileBody(file));
		post.setEntity(multipartEntity);
		final HttpResponse response = httpclient.execute(post);

		// then
		assertThat(statusCodeOf(response), equalTo(200));
		assertThat(json.from(contentOf(response)), equalTo(json.from(expectedResponse)));

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
		final HttpPut put = new HttpPut(server.resource("classes/foo/cards/123/attachments/bar/"));
		final MultipartEntity multipartEntity = new MultipartEntity();
		multipartEntity.addPart(ATTACHMENT, new StringBody("{" //
				+ "\"_description\": \"the new description\"" //
				+ "}", APPLICATION_JSON, UTF_8));
		multipartEntity.addPart(FILE, new FileBody(file));
		put.setEntity(multipartEntity);
		final HttpResponse response = httpclient.execute(put);

		// then
		assertThat(statusCodeOf(response), equalTo(204));

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
		final HttpPut put = new HttpPut(server.resource("classes/foo/cards/123/attachments/bar/"));
		final MultipartEntity multipartEntity = new MultipartEntity();
		multipartEntity.addPart(FILE, new FileBody(file));
		put.setEntity(multipartEntity);
		final HttpResponse response = httpclient.execute(put);

		// then
		assertThat(statusCodeOf(response), equalTo(204));

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
		final HttpPut put = new HttpPut(server.resource("classes/foo/cards/123/attachments/bar/"));
		final MultipartEntity multipartEntity = new MultipartEntity();
		multipartEntity.addPart(ATTACHMENT, new StringBody("{" //
				+ "\"_description\": \"the new description\"" //
				+ "}", APPLICATION_JSON, UTF_8));
		put.setEntity(multipartEntity);
		final HttpResponse response = httpclient.execute(put);

		// then
		assertThat(statusCodeOf(response), equalTo(204));

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