package integration.learning;

import static javax.ws.rs.core.HttpHeaders.CONTENT_TYPE;
import static javax.ws.rs.core.MediaType.APPLICATION_FORM_URLENCODED;
import static javax.ws.rs.core.Response.Status.NO_CONTENT;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static support.HttpClientUtils.all;
import static support.HttpClientUtils.param;
import static support.ServerResource.randomPort;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.core.MultivaluedMap;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.runners.MockitoJUnitRunner;

import support.ServerResource;

@RunWith(MockitoJUnitRunner.class)
public class PostAndPutParamsManagement {

	public static final String RESOURCE_WITHOUT_FORM_PARAM = "withoutFormParam";
	public static final String RESOURCE_WITH_FORM_PARAM_ONLY = "withFormParamOnly";
	public static final String RESOURCE_WITH_FORM_PARAM = "withFormParam";

	private static final String FORM_PARAM = "formParam";

	@Consumes(APPLICATION_FORM_URLENCODED)
	public static interface Dummy {

		@POST
		@Path(RESOURCE_WITHOUT_FORM_PARAM)
		void postWithoutFormParam(MultivaluedMap<String, String> formParams);

		@POST
		@Path(RESOURCE_WITH_FORM_PARAM_ONLY)
		void postWithFormParamOnly(@FormParam(FORM_PARAM) String formParam);

		@POST
		@Path(RESOURCE_WITH_FORM_PARAM)
		void postWithFormParam(MultivaluedMap<String, String> formParams, @FormParam(FORM_PARAM) String formParam);

		@PUT
		@Path(RESOURCE_WITHOUT_FORM_PARAM)
		void putWithoutFormParam(MultivaluedMap<String, String> formParams);

		@PUT
		@Path(RESOURCE_WITH_FORM_PARAM_ONLY)
		void putWithFormParamOnly(@FormParam(FORM_PARAM) String formParam);

		@PUT
		@Path(RESOURCE_WITH_FORM_PARAM)
		void putWithFormParam(@FormParam(FORM_PARAM) String formParam, MultivaluedMap<String, String> formParams);

	}

	private static Dummy service;

	@Rule
	public ServerResource server = ServerResource.newInstance() //
			.withServiceClass(Dummy.class) //
			.withService(service = mock(Dummy.class)) //
			.withPort(randomPort()) //
			.build();

	@Captor
	private ArgumentCaptor<MultivaluedMap<String, String>> multivaluedMapCaptor;

	private HttpClient httpclient;

	@Before
	public void createHttpClient() throws Exception {
		httpclient = new HttpClient();
	}

	@Test
	public void postWithoutFormParam() throws Exception {
		// given
		final PostMethod method = new PostMethod(server.resource(RESOURCE_WITHOUT_FORM_PARAM));
		method.setRequestBody(all( //
				param("foo", "oof"), //
				param("bar", "rab"), //
				param("baz", "zab") //
		));

		// when
		final int result = httpclient.executeMethod(method);

		// then
		verify(service).postWithoutFormParam(multivaluedMapCaptor.capture());
		assertThat(result, equalTo(NO_CONTENT.getStatusCode()));
		final MultivaluedMap<String, String> captured = multivaluedMapCaptor.getValue();
		assertThat(captured.getFirst("foo"), equalTo("oof"));
		assertThat(captured.getFirst("bar"), equalTo("rab"));
		assertThat(captured.getFirst("baz"), equalTo("zab"));
	}

	@Test
	public void postWithFormParamOnly() throws Exception {
		// given
		final PostMethod method = new PostMethod(server.resource(RESOURCE_WITH_FORM_PARAM_ONLY));
		method.setRequestBody(all( //
				param(FORM_PARAM, "special"), //
				param("foo", "oof"), //
				param("bar", "rab"), //
				param("baz", "zab") //
		));

		// when
		final int result = httpclient.executeMethod(method);

		// then
		verify(service).postWithFormParamOnly("special");
		assertThat(result, equalTo(NO_CONTENT.getStatusCode()));
	}

	@Test
	public void postWithFormParamFormParamMustBeSpecifiedAfterMultivaluedMap() throws Exception {
		// given
		final PostMethod method = new PostMethod(server.resource(RESOURCE_WITH_FORM_PARAM));
		method.setRequestBody(all( //
				param(FORM_PARAM, "special"), //
				param("foo", "oof"), //
				param("bar", "rab"), //
				param("baz", "zab") //
		));

		// when
		final int result = httpclient.executeMethod(method);

		// then
		verify(service).postWithFormParam(multivaluedMapCaptor.capture(), eq("special"));
		assertThat(result, equalTo(NO_CONTENT.getStatusCode()));
		final MultivaluedMap<String, String> captured = multivaluedMapCaptor.getValue();
		assertThat(captured.getFirst(FORM_PARAM), equalTo("special"));
		assertThat(captured.getFirst("foo"), equalTo("oof"));
		assertThat(captured.getFirst("bar"), equalTo("rab"));
		assertThat(captured.getFirst("baz"), equalTo("zab"));
	}

	@Test
	public void putWithoutFormParamRequiresRequestHeader() throws Exception {
		// given
		final PutMethod method = new PutMethod(server.resource(RESOURCE_WITHOUT_FORM_PARAM));
		method.addRequestHeader(CONTENT_TYPE, APPLICATION_FORM_URLENCODED);
		method.setQueryString(all( //
				param("foo", "oof"), //
				param("bar", "rab"), //
				param("baz", "zab") //
		));

		// when
		final int result = httpclient.executeMethod(method);

		// then
		verify(service).putWithoutFormParam(multivaluedMapCaptor.capture());
		assertThat(result, equalTo(NO_CONTENT.getStatusCode()));
		final MultivaluedMap<String, String> captured = multivaluedMapCaptor.getValue();
		assertThat(captured.getFirst("foo"), equalTo("oof"));
		assertThat(captured.getFirst("bar"), equalTo("rab"));
		assertThat(captured.getFirst("baz"), equalTo("zab"));
	}

	@Test
	public void putWithFormParamOnly() throws Exception {
		// given
		final PutMethod method = new PutMethod(server.resource(RESOURCE_WITH_FORM_PARAM_ONLY));
		method.setQueryString(all( //
				param(FORM_PARAM, "special"), //
				param("foo", "oof"), //
				param("bar", "rab"), //
				param("baz", "zab") //
		));

		// when
		final int result = httpclient.executeMethod(method);

		// then
		verify(service).putWithFormParamOnly("special");
		assertThat(result, equalTo(NO_CONTENT.getStatusCode()));
	}

	@Test
	public void putWithFormFormParamCanBePutBeforeOrAfterMultivaluedMapAndParamRequiresRequestHeader() throws Exception {
		// given
		final PutMethod method = new PutMethod(server.resource(RESOURCE_WITH_FORM_PARAM));
		method.addRequestHeader(CONTENT_TYPE, APPLICATION_FORM_URLENCODED);
		method.setQueryString(all( //
				param(FORM_PARAM, "special"), //
				param("foo", "oof"), //
				param("bar", "rab"), //
				param("baz", "zab") //
		));

		// when
		final int result = httpclient.executeMethod(method);

		// then
		verify(service).putWithFormParam(eq("special"), multivaluedMapCaptor.capture());
		assertThat(result, equalTo(NO_CONTENT.getStatusCode()));
		final MultivaluedMap<String, String> captured = multivaluedMapCaptor.getValue();
		assertThat(captured.getFirst(FORM_PARAM), equalTo("special"));
		assertThat(captured.getFirst("foo"), equalTo("oof"));
		assertThat(captured.getFirst("bar"), equalTo("rab"));
		assertThat(captured.getFirst("baz"), equalTo("zab"));
	}

}
