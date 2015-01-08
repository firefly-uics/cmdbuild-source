package integration.rest;

import static java.util.Arrays.asList;
import static org.apache.http.entity.ContentType.APPLICATION_JSON;
import static org.cmdbuild.service.rest.constants.Serialization.CARD_ID;
import static org.cmdbuild.service.rest.constants.Serialization.CLASS_ID;
import static org.cmdbuild.service.rest.constants.Serialization.DOMAIN_SOURCE;
import static org.cmdbuild.service.rest.constants.Serialization.UNDERSCORED_ID;
import static org.cmdbuild.service.rest.constants.Serialization.UNDERSCORED_TYPE;
import static org.cmdbuild.service.rest.model.Models.newCard;
import static org.cmdbuild.service.rest.model.Models.newMetadata;
import static org.cmdbuild.service.rest.model.Models.newRelation;
import static org.cmdbuild.service.rest.model.Models.newResponseMultiple;
import static org.cmdbuild.service.rest.model.Models.newResponseSingle;
import static org.cmdbuild.service.rest.test.HttpClientUtils.contentOf;
import static org.cmdbuild.service.rest.test.HttpClientUtils.statusCodeOf;
import static org.cmdbuild.service.rest.test.ServerResource.randomPort;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.MultivaluedMap;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.cmdbuild.common.collect.ChainablePutMap;
import org.cmdbuild.service.rest.Relations;
import org.cmdbuild.service.rest.model.Models;
import org.cmdbuild.service.rest.model.Relation;
import org.cmdbuild.service.rest.model.ResponseMultiple;
import org.cmdbuild.service.rest.model.ResponseSingle;
import org.cmdbuild.service.rest.model.adapter.RelationAdapter;
import org.cmdbuild.service.rest.test.JsonSupport;
import org.cmdbuild.service.rest.test.ServerResource;
import org.codehaus.jackson.node.ObjectNode;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class RelationsTest {

	private Relations service;

	@Rule
	public ServerResource server = ServerResource.newInstance() //
			.withServiceClass(Relations.class) //
			.withService(service = mock(Relations.class)) //
			.withPort(randomPort()) //
			.build();

	@ClassRule
	public static JsonSupport json = new JsonSupport();

	@Captor
	private ArgumentCaptor<MultivaluedMap<String, String>> multivaluedMapCaptor;

	private HttpClient httpclient;

	@Before
	public void createHttpClient() throws Exception {
		httpclient = HttpClientBuilder.create().build();
	}

	@Test
	public void relationCreated() throws Exception {
		// given
		final ResponseSingle<Long> expectedResponse = newResponseSingle(Long.class) //
				.withElement(123L) //
				.build();
		doReturn(expectedResponse) //
				.when(service).create(anyString(), any(Relation.class));

		// when
		final HttpPost post = new HttpPost(server.resource("domains/12/relations/"));
		final ObjectNode node = json.newObject();
		node.put(UNDERSCORED_ID, 34L);
		node.put(UNDERSCORED_TYPE, "foo");
		node.put("foo", "FOO");
		node.put("bar", "BAR");
		node.put("baz", "BAZ");
		post.setEntity(new StringEntity(node.toString(), APPLICATION_JSON));
		final HttpResponse response = httpclient.execute(post);

		// then
		assertThat(statusCodeOf(response), equalTo(200));
		assertThat(json.from(contentOf(response)), equalTo(json.from(expectedResponse)));

		final ArgumentCaptor<Relation> relationCaptor = ArgumentCaptor.forClass(Relation.class);
		verify(service).create(eq("12"), relationCaptor.capture());

		final Relation captured = relationCaptor.getValue();
		assertThat(captured.getType(), equalTo("foo"));
		assertThat(captured.getId(), equalTo(34L));

		final Map<String, Object> values = captured.getValues();
		assertThat(values, hasEntry("foo", (Object) "FOO"));
		assertThat(values, hasEntry("bar", (Object) "BAR"));
		assertThat(values, hasEntry("baz", (Object) "BAZ"));
	}

	@Test
	public void relationsRead() throws Exception {
		// given
		final String type = "12";
		final Relation firstRelation = newRelation() //
				.withType(type) //
				.withId(78L) //
				.withSource(newCard() //
						.withId(1L) //
						.build()) //
				.withDestination(newCard() //
						.withId(2L) //
						.build()) //
				.withValues(ChainablePutMap.of(new HashMap<String, String>()) //
						.chainablePut("foo", "bar") //
						.chainablePut("bar", "baz")) //
				.build();
		final Relation secondRelation = newRelation() //
				.withType(type) //
				.withId(90L) //
				.withSource(newCard() //
						.withId(3L) //
						.build()) //
				.withDestination(newCard() //
						.withId(4L) //
						.build()) //
				.withValues(ChainablePutMap.of(new HashMap<String, String>()) //
						.chainablePut("bar", "baz")) //
				.build();
		final ResponseMultiple<Relation> sentResponse = newResponseMultiple(Relation.class) //
				.withElements(asList(firstRelation, secondRelation)) //
				.withMetadata(newMetadata() //
						.withTotal(2L) //
						.build()) //
				.build();
		final RelationAdapter relationAdapter = new RelationAdapter();
		@SuppressWarnings("unchecked")
		final ResponseMultiple<Map<String, Object>> expectedResponse = Models
				.<Map<String, Object>> newResponseMultiple() //
				.withElements(Arrays.<Map<String, Object>> asList( //
						relationAdapter.marshal(firstRelation), //
						relationAdapter.marshal(secondRelation) //
						)) //
				.withMetadata(newMetadata() //
						.withTotal(2L) //
						.build()) //
				.build();
		doReturn(sentResponse) //
				.when(service).read(anyString(), anyInt(), anyInt());

		// when
		final HttpGet get = new HttpGet(new URIBuilder(server.resource("domains/12/relations/")) //
				.setParameter(CLASS_ID, "34") //
				.setParameter(CARD_ID, "56") //
				.setParameter(DOMAIN_SOURCE, "baz") //
				.build());
		final HttpResponse response = httpclient.execute(get);

		// then
		assertThat(statusCodeOf(response), equalTo(200));
		assertThat(json.from(contentOf(response)), equalTo(json.from(expectedResponse)));

		verify(service).read(eq("12"), anyInt(), anyInt());
	}

}