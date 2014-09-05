package integration.rest;

import static org.cmdbuild.service.rest.constants.Serialization.INSTANCE;
import static org.cmdbuild.service.rest.constants.Serialization.TYPE;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static support.HttpClientUtils.all;
import static support.HttpClientUtils.param;
import static support.ServerResource.randomPort;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.cmdbuild.service.rest.Activities;
import org.cmdbuild.service.rest.dto.DetailResponseMetadata;
import org.cmdbuild.service.rest.dto.ListResponse;
import org.cmdbuild.service.rest.dto.ProcessActivity;
import org.cmdbuild.service.rest.dto.ProcessActivityDefinition;
import org.cmdbuild.service.rest.dto.SimpleResponse;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import support.JsonSupport;
import support.ServerResource;

public class ActivitiesTest {

	private Activities service;

	@Rule
	public ServerResource server = ServerResource.newInstance() //
			.withServiceClass(Activities.class) //
			.withService(service = mock(Activities.class)) //
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
	public void activitiesRead() throws Exception {
		// given
		final ListResponse<ProcessActivity> response = ListResponse.newInstance(ProcessActivity.class) //
				.withElement(ProcessActivity.newInstance() //
						// not important
						.build() //
				) //
				.withMetadata(DetailResponseMetadata.newInstance() //
						// not important
						.build() //
				).build();
		doReturn(response) //
				.when(service).read(anyString(), anyLong());

		// when
		final GetMethod get = new GetMethod(server.resource("process_activities/"));
		get.setQueryString(all( //
				param(TYPE, "foo"), //
				param(INSTANCE, "123") //
		));
		final int result = httpclient.executeMethod(get);

		// then
		verify(service).read("foo", 123L);
		verifyNoMoreInteractions(service);
		assertThat(result, equalTo(200));
		assertThat(json.from(get.getResponseBodyAsString()), equalTo(json.from(response)));
	}

	@Test
	public void activityRead() throws Exception {
		// given
		final SimpleResponse<ProcessActivityDefinition> expectedResponse = SimpleResponse
				.newInstance(ProcessActivityDefinition.class) //
				.withElement(ProcessActivityDefinition.newInstance() //
						// not important
						.build()) //
				.build();
		doReturn(expectedResponse) //
				.when(service).read(anyString(), anyString(), anyLong());

		// when
		final GetMethod get = new GetMethod(server.resource("process_activities/bar/"));
		get.setQueryString(all( //
				param(TYPE, "foo"), //
				param(INSTANCE, "123") //
		));
		final int result = httpclient.executeMethod(get);

		// then
		verify(service).read("bar", "foo", 123L);
		verifyNoMoreInteractions(service);
		assertThat(result, equalTo(200));
		assertThat(json.from(get.getResponseBodyAsString()), equalTo(json.from(expectedResponse)));
	}

}
