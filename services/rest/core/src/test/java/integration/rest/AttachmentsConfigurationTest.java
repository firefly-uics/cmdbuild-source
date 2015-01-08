package integration.rest;

import static java.util.Arrays.asList;
import static org.cmdbuild.service.rest.model.Models.newAttachmentCategory;
import static org.cmdbuild.service.rest.model.Models.newAttribute;
import static org.cmdbuild.service.rest.model.Models.newMetadata;
import static org.cmdbuild.service.rest.model.Models.newResponseMultiple;
import static org.cmdbuild.service.rest.test.HttpClientUtils.contentOf;
import static org.cmdbuild.service.rest.test.HttpClientUtils.statusCodeOf;
import static org.cmdbuild.service.rest.test.ServerResource.randomPort;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.cmdbuild.service.rest.AttachmentsConfiguration;
import org.cmdbuild.service.rest.model.AttachmentCategory;
import org.cmdbuild.service.rest.model.Attribute;
import org.cmdbuild.service.rest.model.ResponseMultiple;
import org.cmdbuild.service.rest.test.JsonSupport;
import org.cmdbuild.service.rest.test.ServerResource;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

public class AttachmentsConfigurationTest {

	private AttachmentsConfiguration service;

	@Rule
	public ServerResource server = ServerResource.newInstance() //
			.withServiceClass(AttachmentsConfiguration.class) //
			.withService(service = mock(AttachmentsConfiguration.class)) //
			.withPort(randomPort()) //
			.build();

	@ClassRule
	public static JsonSupport json = new JsonSupport();

	private HttpClient httpclient;

	@Before
	public void createHttpClient() throws Exception {
		httpclient = HttpClientBuilder.create().build();
	}

	@Test
	public void readCategories() throws Exception {
		// given
		final ResponseMultiple<AttachmentCategory> expectedResponse = newResponseMultiple(AttachmentCategory.class) //
				.withElements(asList( //
						newAttachmentCategory() //
								.withId("foo") //
								.withDescription("bar") //
								.build(), //
						newAttachmentCategory() //
								.withId("bar") //
								.withDescription("baz") //
								.build())) //
				.withMetadata(newMetadata() //
						.withTotal(2L) //
						.build()) //
				.build();
		when(service.readCategories()) //
				.thenReturn(expectedResponse);

		// when
		final HttpGet get = new HttpGet(server.resource("configuration/attachments/categories/"));
		final HttpResponse response = httpclient.execute(get);

		// then
		assertThat(statusCodeOf(response), equalTo(200));
		assertThat(json.from(contentOf(response)), equalTo(json.from(expectedResponse)));
		verify(service).readCategories();
	}

	@Test
	public void readCategoryAttributes() throws Exception {
		// given
		final ResponseMultiple<Attribute> expectedResponse = newResponseMultiple(Attribute.class) //
				.withElements(asList( //
						newAttribute() //
								.withId("foo") //
								.withDescription("bar") //
								.build(), //
						newAttribute() //
								.withId("bar") //
								.withDescription("baz") //
								.build())) //
				.withMetadata(newMetadata() //
						.withTotal(2L) //
						.build()) //
				.build();
		when(service.readCategoryAttributes(anyString())) //
				.thenReturn(expectedResponse);

		// when
		final HttpGet get = new HttpGet(server.resource("configuration/attachments/categories/foo/attributes/"));
		final HttpResponse response = httpclient.execute(get);

		// then
		assertThat(statusCodeOf(response), equalTo(200));
		assertThat(json.from(contentOf(response)), equalTo(json.from(expectedResponse)));
		verify(service).readCategoryAttributes(eq("foo"));
	}

}