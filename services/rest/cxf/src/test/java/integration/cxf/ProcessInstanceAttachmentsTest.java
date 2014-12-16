package integration.cxf;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.apache.commons.io.FileUtils.write;
import static org.apache.commons.io.IOUtils.toByteArray;
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
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.io.File;
import java.io.FileInputStream;

import javax.activation.DataHandler;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.StringPart;
import org.cmdbuild.service.rest.cxf.ProcessInstanceAttachments;
import org.cmdbuild.service.rest.model.Attachment;
import org.cmdbuild.service.rest.model.ResponseSingle;
import org.cmdbuild.service.rest.test.JsonSupport;
import org.cmdbuild.service.rest.test.ServerResource;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.ArgumentCaptor;

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
		final PostMethod post = new PostMethod(server.resource("processes/foo/instances/123/attachments/"));
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
		final ArgumentCaptor<Attachment> attachmentCaptor = ArgumentCaptor.forClass(Attachment.class);
		final ArgumentCaptor<DataHandler> dataHandlerCaptor = ArgumentCaptor.forClass(DataHandler.class);
		verify(service).create(eq("foo"), eq(123L), attachmentCaptor.capture(), dataHandlerCaptor.capture());
		verifyNoMoreInteractions(service);

		final Attachment attachment = attachmentCaptor.getValue();
		assertThat(attachment.getDescription(), equalTo("the description"));

		final DataHandler dataHandler = dataHandlerCaptor.getValue();
		assertThat(toByteArray(new FileInputStream(file)), equalTo(toByteArray(dataHandler.getInputStream())));

		assertThat(result, equalTo(200));
		assertThat(json.from(post.getResponseBodyAsString()), equalTo(json.from(expectedResponse)));
	}

	@Test
	public void update() throws Exception {
		// given
		final File file = temporaryFolder.newFile();
		write(file, "blah blah blah");

		// when
		final PutMethod put = new PutMethod(server.resource("processes/foo/instances/123/attachments/bar/"));
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
		final ArgumentCaptor<Attachment> attachmentCaptor = ArgumentCaptor.forClass(Attachment.class);
		final ArgumentCaptor<DataHandler> dataHandlerCaptor = ArgumentCaptor.forClass(DataHandler.class);
		verify(service).update(eq("foo"), eq(123L), eq("bar"), attachmentCaptor.capture(), dataHandlerCaptor.capture());
		verifyNoMoreInteractions(service);

		final Attachment attachment = attachmentCaptor.getValue();
		assertThat(attachment.getDescription(), equalTo("the new description"));

		final DataHandler dataHandler = dataHandlerCaptor.getValue();
		assertThat(toByteArray(new FileInputStream(file)), equalTo(toByteArray(dataHandler.getInputStream())));

		assertThat(result, equalTo(204));
	}

}
