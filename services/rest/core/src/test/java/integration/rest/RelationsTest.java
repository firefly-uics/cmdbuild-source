package integration.rest;

import static java.util.Arrays.asList;
import static org.cmdbuild.service.rest.constants.Serialization.CARD_ID;
import static org.cmdbuild.service.rest.constants.Serialization.CLASS_ID;
import static org.cmdbuild.service.rest.constants.Serialization.DOMAIN_SOURCE;
import static org.cmdbuild.service.rest.model.Builders.newCard;
import static org.cmdbuild.service.rest.model.Builders.newMetadata;
import static org.cmdbuild.service.rest.model.Builders.newRelation;
import static org.cmdbuild.service.rest.model.Builders.newResponseMultiple;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
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
import org.apache.commons.httpclient.methods.GetMethod;
import org.cmdbuild.common.collect.ChainablePutMap;
import org.cmdbuild.service.rest.Relations;
import org.cmdbuild.service.rest.model.Builders;
import org.cmdbuild.service.rest.model.Relation;
import org.cmdbuild.service.rest.model.ResponseMultiple;
import org.cmdbuild.service.rest.model.adapter.RelationAdapter;
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
		httpclient = new HttpClient();
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
		final ResponseMultiple<Map<String, Object>> expectedResponse = Builders
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
				.when(service).read(anyString(), anyString(), anyLong(), anyString(), anyInt(), anyInt());

		// when
		final GetMethod get = new GetMethod(server.resource("domains/12/relations/"));
		get.setQueryString(all( //
				param(CLASS_ID, "34"), //
				param(CARD_ID, "56"), //
				param(DOMAIN_SOURCE, "baz") //
		));

		final int result = httpclient.executeMethod(get);

		// then
		verify(service).read(eq("12"), eq("34"), eq(56L), eq("baz"), anyInt(), anyInt());
		assertThat(result, equalTo(200));
		assertThat(json.from(get.getResponseBodyAsString()), equalTo(json.from(expectedResponse)));
	}

}
