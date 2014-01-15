package integration.rest;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.cmdbuild.service.rest.Data;
import org.cmdbuild.service.rest.Schema;
import org.cmdbuild.service.rest.dto.AttributeDetail;
import org.cmdbuild.service.rest.dto.AttributeDetailResponse;
import org.cmdbuild.service.rest.dto.CardDetail;
import org.cmdbuild.service.rest.dto.CardDetailResponse;
import org.cmdbuild.service.rest.dto.ClassDetail;
import org.cmdbuild.service.rest.dto.ClassDetailResponse;
import org.cmdbuild.service.rest.dto.LookupDetail;
import org.cmdbuild.service.rest.dto.LookupDetailResponse;
import org.cmdbuild.service.rest.dto.LookupTypeDetail;
import org.cmdbuild.service.rest.dto.LookupTypeDetailResponse;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import support.ForwardingData;
import support.ForwardingSchema;
import support.JsonSupport;
import support.ServerResource;

public class DataTest {

	private final ForwardingData forwardingData = new ForwardingData();
	private Data service;

	@Rule
	public ServerResource server = ServerResource.newInstance() //
			.withServiceClass(Data.class) //
			.withService(forwardingData) //
			.withPort(8080) //
			.build();

	@Rule
	public JsonSupport json = new JsonSupport();

	private HttpClient httpclient;

	@Before
	public void mockService() throws Exception {
		service = mock(Data.class);
		forwardingData.setInner(service);
	}

	@Before
	public void createHttpClient() throws Exception {
		httpclient = new HttpClient();
	}

	@Test
	public void getCards() throws Exception {
		// given
		final CardDetailResponse expectedResponse = CardDetailResponse.newInstance() //
				.withDetails(asList( //
						CardDetail.newInstance() //
								.withId(123L) //
								.build(), //
						CardDetail.newInstance() //
								.withId(456L) //
								.build())) //
				.withTotal(2) //
				.build();
		when(service.getCards("foo")) //
				.thenReturn(expectedResponse);

		// when
		final GetMethod get = new GetMethod("http://localhost:8080/data/classes/foo/");
		final int result = httpclient.executeMethod(get);

		// then
		assertThat(result, equalTo(200));
		assertThat(json.from(get.getResponseBodyAsString()), equalTo(json.from(expectedResponse)));
	}

}
