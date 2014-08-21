package integration.learning;

import static com.google.common.base.Defaults.defaultValue;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static support.ServerResource.randomPort;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.cmdbuild.service.rest.dto.AttributeDetail;
import org.cmdbuild.service.rest.dto.ListResponse;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import support.ServerResource;

public class DefaultValues {

	@Path("resource/")
	@Produces(APPLICATION_JSON)
	public static interface Dummy {

		@GET
		@Path("primitivesWithNoDefault")
		ListResponse<AttributeDetail> primitivesWithNoDefaultValue( //
				@QueryParam("int") int i, //
				@QueryParam("long") long l, //
				@QueryParam("boolean") boolean b, //
				@QueryParam("double") double d, //
				@QueryParam("float") float f, //
				@QueryParam("short") short s, //
				@QueryParam("byte") byte by, //
				@QueryParam("char") char c //
		);

		@GET
		@Path("primitivesWithDefault")
		ListResponse<AttributeDetail> primitivesWithDefaultValue( //
				@DefaultValue("42") @QueryParam("int") int i, //
				@DefaultValue("true") @QueryParam("boolean") boolean b //
		);

		@GET
		@Path("objectsWithNoDefault")
		ListResponse<AttributeDetail> objectsWithNoDefaultValue( //
				@QueryParam("s") String s, //
				@QueryParam("i") Integer i, //
				@QueryParam("l") Long l, //
				@QueryParam("b") Boolean b, //
				@QueryParam("double") Double d, //
				@QueryParam("float") Float f, //
				@QueryParam("short") Short sh, //
				@QueryParam("byte") Byte by, //
				@QueryParam("char") Character c //
		);

		@GET
		@Path("objectsWithDefault")
		ListResponse<AttributeDetail> objectsWithDefaultValue( //
				@DefaultValue("foo") @QueryParam("s") String s, //
				@DefaultValue("42") @QueryParam("i") Integer i, //
				@DefaultValue("true") @QueryParam("b") Boolean b //
		);

	}

	private static Dummy service;

	@ClassRule
	public static ServerResource server = ServerResource.newInstance() //
			.withServiceClass(Dummy.class) //
			.withService(service = mock(Dummy.class)) //
			.withPort(randomPort()) //
			.build();

	private HttpClient httpclient;

	@Before
	public void createHttpClient() throws Exception {
		httpclient = new HttpClient();
	}

	@Test
	public void primitivesWithNoDefaultValueFilledWithJavaDefaultValuesExceptChar() throws Exception {
		// when
		final GetMethod get = new GetMethod(server.resource("resource/primitivesWithNoDefault/"));
		httpclient.executeMethod(get);

		// then
		verify(service).primitivesWithNoDefaultValue( //
				eq(defaultValue(int.class)), //
				eq(defaultValue(long.class)), //
				eq(defaultValue(boolean.class)), //
				eq(defaultValue(double.class)), //
				eq(defaultValue(float.class)), //
				eq(defaultValue(short.class)), //
				eq(defaultValue(byte.class)), //
				eq('0') //
				);
		assertThat('0', not(equalTo(defaultValue(char.class))));
	}

	@Test
	public void primitivesWithDefaultValue() throws Exception {
		// when
		final GetMethod get = new GetMethod(server.resource("resource/primitivesWithDefault/"));
		httpclient.executeMethod(get);

		// then
		verify(service).primitivesWithDefaultValue( //
				eq(42), //
				eq(true) //
				);
	}

	@Test
	public void objectsWithNoDefaultValueFilledWithJavaDefaultValuesExceptChar() throws Exception {
		// when
		final GetMethod get = new GetMethod(server.resource("resource/objectsWithNoDefault/"));
		httpclient.executeMethod(get);

		// then
		verify(service).objectsWithNoDefaultValue( //
				isNull(String.class), //
				isNull(Integer.class), //
				isNull(Long.class), //
				isNull(Boolean.class), //
				isNull(Double.class), //
				isNull(Float.class), //
				isNull(Short.class), //
				isNull(Byte.class), //
				isNull(Character.class) //
				);
	}

	@Test
	public void objectsWithDefaultValue() throws Exception {
		// when
		final GetMethod get = new GetMethod(server.resource("resource/objectsWithDefault/"));
		httpclient.executeMethod(get);

		// then
		verify(service).objectsWithDefaultValue( //
				eq("foo"), //
				eq(42), //
				eq(true) //
				);
	}

}
