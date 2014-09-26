package integration.rest;

import static java.util.Arrays.asList;
import static javax.ws.rs.core.HttpHeaders.CONTENT_TYPE;
import static javax.ws.rs.core.MediaType.APPLICATION_FORM_URLENCODED;
import static org.cmdbuild.service.rest.constants.Serialization.TYPE;
import static org.cmdbuild.service.rest.constants.Serialization.UNDERSCORED_ACTIVITY;
import static org.cmdbuild.service.rest.constants.Serialization.UNDERSCORED_ADVANCE;
import static org.cmdbuild.service.rest.constants.Serialization.UNDERSCORED_ID;
import static org.cmdbuild.service.rest.constants.Serialization.UNDERSCORED_NAME;
import static org.cmdbuild.service.rest.constants.Serialization.UNDERSCORED_TYPE;
import static org.cmdbuild.service.rest.model.Builders.newMetadata;
import static org.cmdbuild.service.rest.model.Builders.newProcessInstance;
import static org.cmdbuild.service.rest.model.Builders.newResponseMultiple;
import static org.cmdbuild.service.rest.model.Builders.newResponseSingle;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static support.HttpClientUtils.all;
import static support.HttpClientUtils.param;
import static support.ServerResource.randomPort;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.MultivaluedMap;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.cmdbuild.common.collect.ChainablePutMap;
import org.cmdbuild.service.rest.Instances;
import org.cmdbuild.service.rest.model.Builders;
import org.cmdbuild.service.rest.model.ProcessInstance;
import org.cmdbuild.service.rest.model.ResponseMultiple;
import org.cmdbuild.service.rest.model.ResponseSingle;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.runners.MockitoJUnitRunner;

import support.JsonSupport;
import support.ServerResource;

@RunWith(MockitoJUnitRunner.class)
public class InstancesTest {

	private Instances service;

	@Rule
	public ServerResource server = ServerResource.newInstance() //
			.withServiceClass(Instances.class) //
			.withService(service = mock(Instances.class)) //
			.withPort(randomPort()) //
			.build();

	@ClassRule
	public static JsonSupport json = new JsonSupport();

	@Captor
	private ArgumentCaptor<MultivaluedMap<String, String>> multivaluedMapCaptor;

	private HttpClient httpclient;

	@Before
	public void createHttpClient() throws Exception {
		httpclient = new HttpClient();
	}

	@Test
	public void instanceCreated() throws Exception {
		// given
		final ResponseSingle<Long> expectedResponse = newResponseSingle(Long.class) //
				.withElement(123L) //
				.build();
		doReturn(expectedResponse) //
				.when(service).create(any(MultivaluedMap.class), anyString(), anyBoolean());

		// when
		final PostMethod post = new PostMethod(server.resource("process_instances/"));
		post.setRequestBody(all( //
				param(UNDERSCORED_TYPE, "foo"), //
				param(UNDERSCORED_ADVANCE, "true"), //
				param("foo", "bar"), //
				param("bar", "baz"), //
				param("baz", "foo") //
		));
		final int result = httpclient.executeMethod(post);

		// then
		verify(service).create(multivaluedMapCaptor.capture(), eq("foo"), eq(true));
		assertThat(result, equalTo(200));
		assertThat(json.from(post.getResponseBodyAsString()), equalTo(json.from(expectedResponse)));
		final MultivaluedMap<String, String> captured = multivaluedMapCaptor.getValue();
		assertThat(captured.getFirst(UNDERSCORED_TYPE), equalTo("foo"));
		assertThat(captured.getFirst(UNDERSCORED_ADVANCE), equalTo("true"));
		assertThat(captured.getFirst("foo"), equalTo("bar"));
		assertThat(captured.getFirst("bar"), equalTo("baz"));
		assertThat(captured.getFirst("baz"), equalTo("foo"));
	}

	@Test
	public void instanceRead() throws Exception {
		// given
		final String type = "type";
		final Long id = 123L;
		final String name = "foo";
		final Map<String, Object> values = ChainablePutMap.of(new HashMap<String, Object>()) //
				.chainablePut("foo", "bar") //
				.chainablePut("bar", "baz");
		final ResponseSingle<ProcessInstance> sentResponse = newResponseSingle(ProcessInstance.class) //
				.withElement(newProcessInstance() //
						.withType(type) //
						.withId(id) //
						.withName(name) //
						.withValues(values) //
						.build()) //
				.build();
		final ResponseSingle<Map<String, Object>> expectedResponse = Builders.<Map<String, Object>> newResponseSingle() //
				.withElement(ChainablePutMap.of(new HashMap<String, Object>()) //
						.chainablePut(UNDERSCORED_TYPE, type) //
						.chainablePut(UNDERSCORED_ID, id) //
						.chainablePut(UNDERSCORED_NAME, name) //
						.chainablePutAll(values)) //
				.build();
		doReturn(sentResponse) //
				.when(service).read(anyLong(), anyString());

		// when
		final GetMethod get = new GetMethod(server.resource("process_instances/123/"));
		get.setQueryString(all( //
		param(TYPE, "foo") //
		));
		final int result = httpclient.executeMethod(get);

		// then
		verify(service).read(123L, "foo");
		assertThat(result, equalTo(200));
		assertThat(json.from(get.getResponseBodyAsString()), equalTo(json.from(expectedResponse)));
	}

	@Test
	public void instancesRead() throws Exception {
		// given
		final String type = "type";
		final Long firstId = 123L;
		final String firstName = "foo";
		final Long secondId = 456L;
		final String secondName = "bar";
		final Map<String, Object> firstValues = ChainablePutMap.of(new HashMap<String, Object>()) //
				.chainablePut("foo", "bar") //
				.chainablePut("bar", "baz");
		final Map<String, Object> secondValues = ChainablePutMap.of(new HashMap<String, Object>()) //
				.chainablePut("bar", "baz");
		final ResponseMultiple<ProcessInstance> sentResponse = newResponseMultiple(ProcessInstance.class) //
				.withElements(asList( //
						newProcessInstance() //
								.withType(type) //
								.withId(firstId) //
								.withName(firstName) //
								.withValues(firstValues) //
								.build(), //
						newProcessInstance() //
								.withType(type) //
								.withId(secondId) //
								.withName(secondName) //
								.withValues(secondValues) //
								.build() //
						)) //
				.withMetadata(newMetadata() //
						.withTotal(2L) //
						.build()) //
				.build();
		@SuppressWarnings("unchecked")
		final ResponseMultiple<Map<String, Object>> expectedResponse = Builders
				.<Map<String, Object>> newResponseMultiple() //
				.withElements(Arrays.<Map<String, Object>> asList( //
						ChainablePutMap.of(new HashMap<String, Object>()) //
								.chainablePut(UNDERSCORED_TYPE, type) //
								.chainablePut(UNDERSCORED_ID, firstId) //
								.chainablePut(UNDERSCORED_NAME, firstName) //
								.chainablePutAll(firstValues), //
						ChainablePutMap.of(new HashMap<String, Object>()) //
								.chainablePut(UNDERSCORED_TYPE, type) //
								.chainablePut(UNDERSCORED_ID, secondId) //
								.chainablePut(UNDERSCORED_NAME, secondName) //
								.chainablePutAll(secondValues) //
						)) //
				.withMetadata(newMetadata() //
						.withTotal(2L) //
						.build()) //
				.build();
		doReturn(sentResponse) //
				.when(service).read(anyString(), anyInt(), anyInt());

		// when
		final GetMethod get = new GetMethod(server.resource("process_instances/"));
		get.setQueryString(all( //
		param(UNDERSCORED_TYPE, "foo") //
		));
		final int result = httpclient.executeMethod(get);

		// then
		assertThat(result, equalTo(200));
		assertThat(json.from(get.getResponseBodyAsString()), equalTo(json.from(expectedResponse)));
	}

	@Test
	public void instanceUpdated() throws Exception {
		// when
		final PutMethod put = new PutMethod(server.resource("process_instances/123/"));
		put.addRequestHeader(CONTENT_TYPE, APPLICATION_FORM_URLENCODED);
		put.setQueryString(all( //
				param(UNDERSCORED_TYPE, "foo"), //
				param(UNDERSCORED_ACTIVITY, "bar"), //
				param(UNDERSCORED_ADVANCE, "true"), //
				param("foo", "bar"), //
				param("bar", "baz"), //
				param("baz", "foo") //
		));
		final int result = httpclient.executeMethod(put);

		// then
		verify(service).update(eq(123L), eq("foo"), eq("bar"), eq(true), multivaluedMapCaptor.capture());
		assertThat(result, equalTo(204));
		final MultivaluedMap<String, String> captured = multivaluedMapCaptor.getValue();
		assertThat(captured.getFirst(UNDERSCORED_ACTIVITY), equalTo("bar"));
		assertThat(captured.getFirst(UNDERSCORED_ADVANCE), equalTo("true"));
		assertThat(captured.getFirst("foo"), equalTo("bar"));
		assertThat(captured.getFirst("bar"), equalTo("baz"));
		assertThat(captured.getFirst("baz"), equalTo("foo"));
	}

	@Test
	public void instanceDeleted() throws Exception {
		// when
		final DeleteMethod delete = new DeleteMethod(server.resource("process_instances/123/"));
		delete.setQueryString(all( //
		param(TYPE, "foo") //
		));
		final int result = httpclient.executeMethod(delete);

		// then
		verify(service).delete(eq(123L), eq("foo"));
		assertThat(result, equalTo(204));
	}

}
