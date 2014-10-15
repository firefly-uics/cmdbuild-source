package integration.rest;

import static java.util.Arrays.asList;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.apache.commons.lang3.CharEncoding.UTF_8;
import static org.cmdbuild.service.rest.constants.Serialization.LIMIT;
import static org.cmdbuild.service.rest.constants.Serialization.START;
import static org.cmdbuild.service.rest.model.Builders.newMetadata;
import static org.cmdbuild.service.rest.model.Builders.newProcessInstance;
import static org.cmdbuild.service.rest.model.Builders.newResponseMultiple;
import static org.cmdbuild.service.rest.model.Builders.newResponseSingle;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
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

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.cmdbuild.common.collect.ChainablePutMap;
import org.cmdbuild.service.rest.ProcessInstances;
import org.cmdbuild.service.rest.model.Builders;
import org.cmdbuild.service.rest.model.ProcessInstance;
import org.cmdbuild.service.rest.model.ProcessInstanceAdvanceable;
import org.cmdbuild.service.rest.model.ResponseMultiple;
import org.cmdbuild.service.rest.model.ResponseSingle;
import org.cmdbuild.service.rest.model.adapter.ProcessInstanceAdapter;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import support.JsonSupport;
import support.ServerResource;

public class ProcessInstancesTest {

	private ProcessInstances service;

	@Rule
	public ServerResource server = ServerResource.newInstance() //
			.withServiceClass(ProcessInstances.class) //
			.withService(service = mock(ProcessInstances.class)) //
			.withPort(randomPort()) //
			.build();

	@ClassRule
	public static JsonSupport json = new JsonSupport();

	private HttpClient httpclient;
	private ProcessInstanceAdapter adapter;

	@Before
	public void createHttpClient() throws Exception {
		httpclient = new HttpClient();
		adapter = new ProcessInstanceAdapter();
	}

	@Test
	public void instanceCreated() throws Exception {
		// given
		final ResponseSingle<Long> expectedResponse = newResponseSingle(Long.class) //
				.withElement(123L) //
				.build();
		doReturn(expectedResponse) //
				.when(service).create(anyString(), any(ProcessInstanceAdvanceable.class));

		// when
		final PostMethod post = new PostMethod(server.resource("processes/12/instances/"));
		post.setRequestEntity(new StringRequestEntity( //
				"{\"_id\" : 34, \"_type\" : \"56\", \"_advance\" : true, \"bar\" : \"BAR\", \"baz\" : \"BAZ\"}", //
				APPLICATION_JSON, //
				UTF_8) //
		);
		final int result = httpclient.executeMethod(post);

		// then
		final ArgumentCaptor<ProcessInstanceAdvanceable> captor = ArgumentCaptor
				.forClass(ProcessInstanceAdvanceable.class);
		verify(service).create(eq("12"), captor.capture());
		assertThat(result, equalTo(200));
		assertThat(json.from(post.getResponseBodyAsString()), equalTo(json.from(expectedResponse)));
		final ProcessInstanceAdvanceable captured = captor.getValue();
		assertThat(captured.getId(), equalTo(34L));
		assertThat(captured.getType(), equalTo("56"));
		assertThat(captured.isAdvance(), equalTo(true));
		assertThat(captured.getValues(), hasEntry("bar", (Object) "BAR"));
		assertThat(captured.getValues(), hasEntry("baz", (Object) "BAZ"));
	}

	@Test
	public void instanceRead() throws Exception {
		// given
		final ProcessInstance processInstance = newProcessInstance() //
				.withType("123") //
				.withId(456L) //
				.withName("foo") //
				.withValues(ChainablePutMap.of(new HashMap<String, Object>()) //
						.chainablePut("foo", "bar") //
						.chainablePut("bar", "baz")) //
				.build();
		final ResponseSingle<ProcessInstance> sentResponse = newResponseSingle(ProcessInstance.class) //
				.withElement(processInstance) //
				.build();
		final ResponseSingle<Map<String, Object>> expectedResponse = Builders.<Map<String, Object>> newResponseSingle() //
				.withElement(adapter.marshal(processInstance)) //
				.build();
		doReturn(sentResponse) //
				.when(service).read(anyString(), anyLong());

		// when
		final GetMethod get = new GetMethod(server.resource("processes/123/instances/456/"));
		final int result = httpclient.executeMethod(get);

		// then
		verify(service).read(eq("123"), eq(456L));
		assertThat(result, equalTo(200));
		assertThat(json.from(get.getResponseBodyAsString()), equalTo(json.from(expectedResponse)));
	}

	@Test
	public void instancesRead() throws Exception {
		// given
		final ProcessInstance first = newProcessInstance() //
				.withType("12") //
				.withId(34L) //
				.withName("foo") //
				.withValues(ChainablePutMap.of(new HashMap<String, Object>()) //
						.chainablePut("foo", "bar") //
						.chainablePut("bar", "baz")) //
				.build();
		final ProcessInstance second = newProcessInstance() //
				.withType("12") //
				.withId(56L) //
				.withName("bar") //
				.withValues(ChainablePutMap.of(new HashMap<String, Object>()) //
						.chainablePut("bar", "baz")) //
				.build();
		final ResponseMultiple<ProcessInstance> sentResponse = newResponseMultiple(ProcessInstance.class) //
				.withElements(asList(first, second)) //
				.withMetadata(newMetadata() //
						.withTotal(2L) //
						.build()) //
				.build();
		final ResponseMultiple<Map<String, Object>> expectedResponse = Builders
				.<Map<String, Object>> newResponseMultiple() //
				.withElement(adapter.marshal(first)) //
				.withElement(adapter.marshal(second)) //
				.withMetadata(newMetadata() //
						.withTotal(2L) //
						.build()) //
				.build();
		doReturn(sentResponse) //
				.when(service).read(anyString(), anyInt(), anyInt());

		// when
		final GetMethod get = new GetMethod(server.resource("processes/12/instances/"));
		get.setQueryString(all( //
				param(LIMIT, "456"), //
				param(START, "789") //
		));
		final int result = httpclient.executeMethod(get);

		// then
		verify(service).read(eq("12"), eq(456), eq(789));
		assertThat(result, equalTo(200));
		assertThat(json.from(get.getResponseBodyAsString()), equalTo(json.from(expectedResponse)));
	}

	@Test
	public void instanceUpdated() throws Exception {
		// when
		final PutMethod put = new PutMethod(server.resource("processes/12/instances/34/"));
		put.setRequestEntity(new StringRequestEntity( //
				"{\"_id\" : 56, \"_type\" : \"78\", \"_activity\" : \"90\", \"_advance\" : true, \"bar\" : \"BAR\", \"baz\" : \"BAZ\"}", //
				APPLICATION_JSON, //
				UTF_8) //
		);
		final int result = httpclient.executeMethod(put);

		// then
		final ArgumentCaptor<ProcessInstanceAdvanceable> captor = ArgumentCaptor
				.forClass(ProcessInstanceAdvanceable.class);
		verify(service).update(eq("12"), eq(34L), captor.capture());
		assertThat(result, equalTo(204));
		final ProcessInstanceAdvanceable captured = captor.getValue();
		assertThat(captured.getActivity(), equalTo("90"));
		assertThat(captured.isAdvance(), equalTo(true));
		assertThat(captured.getValues(), hasEntry("bar", (Object) "BAR"));
		assertThat(captured.getValues(), hasEntry("baz", (Object) "BAZ"));
	}

	@Test
	public void instanceDeleted() throws Exception {
		// when
		final DeleteMethod delete = new DeleteMethod(server.resource("processes/123/instances/456/"));
		final int result = httpclient.executeMethod(delete);

		// then
		verify(service).delete(eq("123"), eq(456L));
		assertThat(result, equalTo(204));
	}

}
