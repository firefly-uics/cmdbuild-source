package integration.rest;

import static java.util.Arrays.asList;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.apache.commons.io.FileUtils.write;
import static org.apache.commons.io.IOUtils.toByteArray;
import static org.apache.http.Consts.UTF_8;
import static org.cmdbuild.service.rest.test.HttpClientUtils.contentOf;
import static org.cmdbuild.service.rest.test.HttpClientUtils.statusCodeOf;
import static org.cmdbuild.service.rest.test.ServerResource.randomPort;
import static org.cmdbuild.service.rest.v2.constants.Serialization.FILE;
import static org.cmdbuild.service.rest.v2.constants.Serialization.ICON;
import static org.cmdbuild.service.rest.v2.model.Models.newIcon;
import static org.cmdbuild.service.rest.v2.model.Models.newMetadata;
import static org.cmdbuild.service.rest.v2.model.Models.newResponseMultiple;
import static org.cmdbuild.service.rest.v2.model.Models.newResponseSingle;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.activation.DataHandler;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.HttpClientBuilder;
import org.cmdbuild.service.rest.test.JsonSupport;
import org.cmdbuild.service.rest.test.ServerResource;
import org.cmdbuild.service.rest.v2.Icons;
import org.cmdbuild.service.rest.v2.model.Icon;
import org.cmdbuild.service.rest.v2.model.ResponseMultiple;
import org.cmdbuild.service.rest.v2.model.ResponseSingle;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.ArgumentCaptor;

public class IconsTest {

	private Icons service;

	@Rule
	public ServerResource server = ServerResource.newInstance() //
			.withServiceClass(Icons.class) //
			.withService(service = mock(Icons.class)) //
			.withPortRange(randomPort()) //
			.build();

	@ClassRule
	public static JsonSupport json = new JsonSupport();

	@Rule
	public TemporaryFolder temporaryFolder = new TemporaryFolder();

	private HttpClient httpclient;

	@Before
	public void createHttpClient() throws Exception {
		httpclient = HttpClientBuilder.create().build();
	}

	@Test
	public void create() throws Exception {
		// given
		final File file = temporaryFolder.newFile();
		write(file, "blah blah blah");
		final ResponseSingle<Long> expectedResponse = newResponseSingle(Long.class) //
				.withElement(42L) //
				.withMetadata(newMetadata() //
						// nothing to add, just needed for simplify assertions
						.build()) //
				.build();
		doReturn(expectedResponse) //
				.when(service).create(any(Icon.class), any(DataHandler.class));

		// when
		final HttpPost post = new HttpPost(server.resource("icons/"));
		final MultipartEntity multipartEntity = new MultipartEntity();
		multipartEntity.addPart(ICON,
				new StringBody("{" //
						+ "	\"type\": \"the type\"," //
						+ "	\"details\": {" //
						+ "		\"foo\": \"Foo\"," //
						+ "		\"bar\": \"Bar\"," //
						+ "		\"baz\": \"Baz\"" //
						+ "	}" //
						+ "}", APPLICATION_JSON, UTF_8));
		multipartEntity.addPart(FILE, new FileBody(file));
		post.setEntity(multipartEntity);
		final HttpResponse response = httpclient.execute(post);

		// then
		assertThat(statusCodeOf(response), equalTo(200));
		assertThat(json.from(contentOf(response)), equalTo(json.from(expectedResponse)));

		final ArgumentCaptor<Icon> iconCaptor = ArgumentCaptor.forClass(Icon.class);
		final ArgumentCaptor<DataHandler> dataHandlerCaptor = ArgumentCaptor.forClass(DataHandler.class);
		verify(service).create(iconCaptor.capture(), dataHandlerCaptor.capture());
		verifyNoMoreInteractions(service);

		final Icon icon = iconCaptor.getValue();
		assertThat(icon,
				equalTo(newIcon() //
						.withId(null) //
						.withType("the type") //
						.setDetail("foo", "Foo") //
						.setDetail("bar", "Bar") //
						.setDetail("baz", "Baz") //
						.build()));

		final DataHandler dataHandler = dataHandlerCaptor.getValue();
		assertThat(toString(dataHandler.getInputStream()), equalTo(toString(new FileInputStream(file))));
	}

	@Test
	public void readAll() throws Exception {
		// given
		final ResponseMultiple<Icon> expectedResponse = newResponseMultiple(Icon.class) //
				.withElements(asList( //
						newIcon() //
								.withId("the_id") //
								.withType("the type") //
								.setDetail("foo", "Foo") //
								.build(),
						newIcon() //
								.withId("another id") //
								.withType("another type") //
								.setDetail("bar", "Bar") //
								.build(),
						newIcon() //
								.withId("yet another id") //
								.withType("yet another type") //
								.setDetail("baz", "Baz") //
								.build())) //
				.withMetadata(newMetadata() //
						.withTotal(42L) //
						.build()) //
				.build();
		when(service.read()) //
				.thenReturn(expectedResponse);

		// when
		final HttpGet get = new HttpGet(new URIBuilder(server.resource("icons/")) //
				.build());
		final HttpResponse response = httpclient.execute(get);

		// then
		assertThat(statusCodeOf(response), equalTo(200));
		assertThat(json.from(contentOf(response)), equalTo(json.from(expectedResponse)));

		verify(service).read();
	}

	@Test
	public void read() throws Exception {
		// given
		final ResponseSingle<Icon> expectedResponse = newResponseSingle(Icon.class) //
				.withElement(newIcon() //
						.withId("the_id") //
						.withType("the type") //
						.setDetail("foo", "Foo") //
						.setDetail("bar", "Bar") //
						.setDetail("baz", "Baz") //
						.build()) //
				.withMetadata(newMetadata() //
						// nothing to add, just needed for simplify assertions
						.build()) //
				.build();
		when(service.read(anyString())) //
				.thenReturn(expectedResponse);

		// when
		final HttpGet get = new HttpGet(new URIBuilder(server.resource("icons/the_id/")) //
				.build());
		final HttpResponse response = httpclient.execute(get);

		// then
		assertThat(statusCodeOf(response), equalTo(200));
		assertThat(json.from(contentOf(response)), equalTo(json.from(expectedResponse)));

		verify(service).read(eq("the_id"));
	}

	@Test
	public void download() throws Exception {
		// given
		final File file = temporaryFolder.newFile();
		final DataHandler expectedResponse = new DataHandler(file.toURI().toURL());
		when(service.download(anyString())) //
				.thenReturn(expectedResponse);

		// when
		final HttpGet get = new HttpGet(server.resource("icons/the_id/something"));
		final HttpResponse response = httpclient.execute(get);

		// then
		verify(service).download(eq("the_id"));
		verifyNoMoreInteractions(service);

		assertThat(statusCodeOf(response), equalTo(200));
		assertThat(toByteArray(contentOf(response)), equalTo(toByteArray(expectedResponse.getInputStream())));
	}

	@Test
	@Ignore("data handler seems correct but it looks empty even if it's referring to the right file")
	public void update() throws Exception {
		// given
		final File file = temporaryFolder.newFile();
		write(file, "blah blah blah");

		// when
		final HttpPut put = new HttpPut(server.resource("icons/the_id/"));
		final MultipartEntity multipartEntity = new MultipartEntity();
		multipartEntity.addPart(FILE, new FileBody(file));
		put.setEntity(multipartEntity);
		final HttpResponse response = httpclient.execute(put);

		// then
		assertThat(statusCodeOf(response), equalTo(204));

		final ArgumentCaptor<DataHandler> dataHandlerCaptor = ArgumentCaptor.forClass(DataHandler.class);
		verify(service).update(eq("the_id"), dataHandlerCaptor.capture());
		verifyNoMoreInteractions(service);

		final DataHandler dataHandler = dataHandlerCaptor.getValue();
		assertThat(toString(new FileInputStream(file)), equalTo(toString(dataHandler.getInputStream())));
	}

	@Test
	public void delete() throws Exception {
		// when
		final HttpDelete delete = new HttpDelete(server.resource("icons/the_id/"));
		final HttpResponse response = httpclient.execute(delete);

		// then
		verify(service).delete(eq("the_id"));
		verifyNoMoreInteractions(service);

		assertThat(statusCodeOf(response), equalTo(204));
	}

	private static String toString(final InputStream inputStream) throws IOException {
		final String string = IOUtils.toString(inputStream);
		IOUtils.closeQuietly(inputStream);
		return string;
	}

}
