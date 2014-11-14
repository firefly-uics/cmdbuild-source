package integration.rest;

import static java.util.Arrays.asList;
import static org.apache.commons.io.IOUtils.toByteArray;
import static org.cmdbuild.service.rest.constants.Serialization.UNDERSCORED_CATEGORY;
import static org.cmdbuild.service.rest.constants.Serialization.UNDERSCORED_DESCRIPTION;
import static org.cmdbuild.service.rest.constants.Serialization.UNDERSCORED_ID;
import static org.cmdbuild.service.rest.model.Builders.newAttachment;
import static org.cmdbuild.service.rest.model.Builders.newMetadata;
import static org.cmdbuild.service.rest.model.Builders.newResponseMultiple;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static support.ServerResource.randomPort;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.activation.DataHandler;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.cmdbuild.common.collect.ChainablePutMap;
import org.cmdbuild.service.rest.CardAttachments;
import org.cmdbuild.service.rest.model.Attachment;
import org.cmdbuild.service.rest.model.Builders;
import org.cmdbuild.service.rest.model.ResponseMultiple;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

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
	public void readAll() throws Exception {
		// given
		final ResponseMultiple<Attachment> sentResponse = newResponseMultiple(Attachment.class) //
				.withElements(asList( //
						newAttachment() //
								.withId("foo") //
								.withCategory("something") //
								.withDescription("bar") //
								.build(), //
						newAttachment() //
								.withId("bar") //
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
		final ResponseMultiple<Map<String, Object>> expectedResponse = Builders
				.<Map<String, Object>> newResponseMultiple() //
				.withElements(Arrays.<Map<String, Object>> asList( //
						ChainablePutMap.of(new HashMap<String, Object>()) //
								.chainablePut(UNDERSCORED_ID, "foo") //
								.chainablePut(UNDERSCORED_CATEGORY, "something") //
								.chainablePut(UNDERSCORED_DESCRIPTION, "bar"), //
						ChainablePutMap.of(new HashMap<String, Object>()) //
								.chainablePut(UNDERSCORED_ID, "bar") //
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
		final GetMethod get = new GetMethod(server.resource("classes/dummy/cards/123/attachments/foo"));
		final int result = httpclient.executeMethod(get);

		// then
		verify(service).read(eq("dummy"), eq(123L), eq("foo"));
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
		assertThat(result, equalTo(204));
	}

}
