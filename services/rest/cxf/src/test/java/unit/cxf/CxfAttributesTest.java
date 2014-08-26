package unit.cxf;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import org.cmdbuild.service.rest.ClassAttributes;
import org.cmdbuild.service.rest.DomainAttributes;
import org.cmdbuild.service.rest.ProcessAttributes;
import org.cmdbuild.service.rest.cxf.CxfAttributes;
import org.cmdbuild.service.rest.dto.AttributeDetail;
import org.cmdbuild.service.rest.dto.DetailResponseMetadata;
import org.cmdbuild.service.rest.dto.ListResponse;
import org.cmdbuild.service.rest.serialization.ErrorHandler;
import org.junit.Before;
import org.junit.Test;

public class CxfAttributesTest {

	private ErrorHandler errorHandler;
	private ClassAttributes classAttributes;
	private DomainAttributes domainAttributes;
	private ProcessAttributes processAttributes;

	private CxfAttributes cxfAttributes;

	@Before
	public void setUp() throws Exception {
		errorHandler = mock(ErrorHandler.class);
		classAttributes = mock(ClassAttributes.class);
		domainAttributes = mock(DomainAttributes.class);
		processAttributes = mock(ProcessAttributes.class);
		cxfAttributes = new CxfAttributes(errorHandler, classAttributes, domainAttributes, processAttributes);
	}

	@Test
	public void typeNotFound() throws Exception {
		// when
		cxfAttributes.readAll("foo", "bar", true, 123, 456);

		verify(errorHandler).invalidParam("foo");
		verifyNoMoreInteractions(errorHandler, classAttributes, domainAttributes);
	}

	@Test
	public void classAttributesDelegatedCorrectly() throws Exception {
		// given
		final ListResponse<AttributeDetail> expectedResponse = ListResponse.<AttributeDetail> newInstance() //
				.withElement(AttributeDetail.newInstance() //
						.withName("bar") //
						.build()) //
				.withMetadata(DetailResponseMetadata.newInstance() //
						.withTotal(1L) //
						.build()) //
				.build();
		when(classAttributes.readAll(anyString(), anyBoolean(), anyInt(), anyInt())) //
				.thenReturn(expectedResponse);

		// when
		final ListResponse<AttributeDetail> response = cxfAttributes.readAll("class", "foo", true, 123, 456);

		// then
		assertThat(response, equalTo(expectedResponse));
		verify(classAttributes).readAll("foo", true, 123, 456);
		verifyNoMoreInteractions(errorHandler, classAttributes, domainAttributes);
	}

	@Test
	public void domainAttributesDelegatedCorrectly() throws Exception {
		// given
		final ListResponse<AttributeDetail> expectedResponse = ListResponse.<AttributeDetail> newInstance() //
				.withElement(AttributeDetail.newInstance() //
						.withName("bar") //
						.build()) //
				.withMetadata(DetailResponseMetadata.newInstance() //
						.withTotal(1L) //
						.build()) //
				.build();
		when(domainAttributes.readAll(anyString(), anyBoolean(), anyInt(), anyInt())) //
				.thenReturn(expectedResponse);

		// when
		final ListResponse<AttributeDetail> response = cxfAttributes.readAll("domain", "foo", true, 123, 456);

		// then
		assertThat(response, equalTo(expectedResponse));
		verify(domainAttributes).readAll("foo", true, 123, 456);
		verifyNoMoreInteractions(errorHandler, classAttributes, domainAttributes);
	}

	@Test
	public void processAttributesDelegatedCorrectly() throws Exception {
		// given
		final ListResponse<AttributeDetail> expectedResponse = ListResponse.<AttributeDetail> newInstance() //
				.withElement(AttributeDetail.newInstance() //
						.withName("bar") //
						.build()) //
				.withMetadata(DetailResponseMetadata.newInstance() //
						.withTotal(1L) //
						.build()) //
				.build();
		when(processAttributes.readAll(anyString(), anyBoolean(), anyInt(), anyInt())) //
				.thenReturn(expectedResponse);

		// when
		final ListResponse<AttributeDetail> response = cxfAttributes.readAll("process", "foo", true, 123, 456);

		// then
		assertThat(response, equalTo(expectedResponse));
		verify(processAttributes).readAll("foo", true, 123, 456);
		verifyNoMoreInteractions(errorHandler, classAttributes, domainAttributes, processAttributes);
	}

}
