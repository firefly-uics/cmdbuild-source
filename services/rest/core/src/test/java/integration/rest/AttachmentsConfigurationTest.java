package integration.rest;

import static java.util.Arrays.asList;
import static org.cmdbuild.service.rest.model.Models.newAttachmentCategory;
import static org.cmdbuild.service.rest.model.Models.newAttribute;
import static org.cmdbuild.service.rest.model.Models.newMetadata;
import static org.cmdbuild.service.rest.model.Models.newResponseMultiple;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static support.ServerResource.randomPort;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.cmdbuild.service.rest.AttachmentsConfiguration;
import org.cmdbuild.service.rest.model.AttachmentCategory;
import org.cmdbuild.service.rest.model.Attribute;
import org.cmdbuild.service.rest.model.ResponseMultiple;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import support.JsonSupport;
import support.ServerResource;

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
		httpclient = new HttpClient();
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
		final GetMethod get = new GetMethod(server.resource("configuration/attachments/categories/"));
		final int result = httpclient.executeMethod(get);

		// then
		verify(service).readCategories();
		assertThat(result, equalTo(200));
		assertThat(json.from(get.getResponseBodyAsString()), equalTo(json.from(expectedResponse)));
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
		final GetMethod get = new GetMethod(server.resource("configuration/attachments/categories/foo/attributes/"));
		final int result = httpclient.executeMethod(get);

		// then
		verify(service).readCategoryAttributes(eq("foo"));
		assertThat(result, equalTo(200));
		assertThat(json.from(get.getResponseBodyAsString()), equalTo(json.from(expectedResponse)));
	}

}
