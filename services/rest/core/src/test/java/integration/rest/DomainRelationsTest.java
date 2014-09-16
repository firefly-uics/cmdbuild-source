package integration.rest;

import static java.util.Arrays.asList;
import static org.cmdbuild.service.rest.constants.Serialization.CARD_ID;
import static org.cmdbuild.service.rest.constants.Serialization.CLASS_ID;
import static org.cmdbuild.service.rest.constants.Serialization.DOMAIN_SOURCE;
import static org.cmdbuild.service.rest.constants.Serialization.UNDERSCORED_DESTINATION;
import static org.cmdbuild.service.rest.constants.Serialization.UNDERSCORED_ID;
import static org.cmdbuild.service.rest.constants.Serialization.UNDERSCORED_SOURCE;
import static org.cmdbuild.service.rest.constants.Serialization.UNDERSCORED_TYPE;
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
import org.cmdbuild.service.rest.DomainRelations;
import org.cmdbuild.service.rest.dto.DetailResponseMetadata;
import org.cmdbuild.service.rest.dto.ListResponse;
import org.cmdbuild.service.rest.dto.Relation;
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
public class DomainRelationsTest {

	private DomainRelations service;

	@Rule
	public ServerResource server = ServerResource.newInstance() //
			.withServiceClass(DomainRelations.class) //
			.withService(service = mock(DomainRelations.class)) //
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
		final String type = "foo";
		final Long firstId = 123L;
		final Long secondId = 456L;
		final Map<String, String> firstValues = ChainablePutMap.of(new HashMap<String, String>()) //
				.chainablePut("foo", "bar") //
				.chainablePut("bar", "baz");
		final Map<String, String> secondValues = ChainablePutMap.of(new HashMap<String, String>()) //
				.chainablePut("bar", "baz");
		final ListResponse<Relation> sentResponse = ListResponse.newInstance(Relation.class) //
				.withElements(asList( //
						Relation.newInstance() //
								.withType(type) //
								.withId(firstId) //
								.withValues(firstValues) //
								.build(), //
						Relation.newInstance() //
								.withType(type) //
								.withId(secondId) //
								.withValues(secondValues) //
								.build() //
						)) //
				.withMetadata(DetailResponseMetadata.newInstance() //
						.withTotal(2L) //
						.build()) //
				.build();
		@SuppressWarnings("unchecked")
		final ListResponse<Map<String, Object>> expectedResponse = ListResponse.<Map<String, Object>> newInstance() //
				.withElements(Arrays.<Map<String, Object>> asList( //
						ChainablePutMap.of(new HashMap<String, Object>()) //
								.chainablePut(UNDERSCORED_TYPE, type) //
								.chainablePut(UNDERSCORED_ID, firstId) //
								.chainablePut(UNDERSCORED_SOURCE, null) //
								.chainablePut(UNDERSCORED_DESTINATION, null) //
								.chainablePutAll(firstValues), //
						ChainablePutMap.of(new HashMap<String, Object>()) //
								.chainablePut(UNDERSCORED_TYPE, type) //
								.chainablePut(UNDERSCORED_ID, secondId) //
								.chainablePut(UNDERSCORED_SOURCE, null) //
								.chainablePut(UNDERSCORED_DESTINATION, null) //
								.chainablePutAll(secondValues) //
						)) //
				.withMetadata(DetailResponseMetadata.newInstance() //
						.withTotal(2L) //
						.build()) //
				.build();
		doReturn(sentResponse) //
				.when(service).read(anyString(), anyString(), anyLong(), anyString(), anyInt(), anyInt());

		// when
		final GetMethod get = new GetMethod(server.resource("domains/foo/relations/"));
		get.setQueryString(all( //
				param(CLASS_ID, "bar"), //
				param(CARD_ID, "42"), //
				param(DOMAIN_SOURCE, "baz") //
		));

		final int result = httpclient.executeMethod(get);

		// then
		verify(service).read(eq("foo"), eq("bar"), eq(42L), eq("baz"), anyInt(), anyInt());
		assertThat(result, equalTo(200));
		assertThat(json.from(get.getResponseBodyAsString()), equalTo(json.from(expectedResponse)));
	}
}
