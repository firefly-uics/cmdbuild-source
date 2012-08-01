package unit.api.fluent.ws;

import static org.apache.commons.lang.StringUtils.EMPTY;
import static org.cmdbuild.api.fluent.ws.WsFluentApiExecutor.attribute;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import org.junit.Test;

public class AttributeValueConversionTest {

	private static final String ATTRIBUTE_NAME = "name";

	@Test
	public void nullConvertedAsEmptyString() throws Exception {
		assertThat(attribute(ATTRIBUTE_NAME, null).getValue(), equalTo(EMPTY));
	}

	@Test
	public void stringConvertedAsIs() throws Exception {
		assertThat(attribute(ATTRIBUTE_NAME, "foo").getValue(), equalTo("foo"));
		assertThat(attribute(ATTRIBUTE_NAME, "bar ").getValue(), equalTo("bar "));
		assertThat(attribute(ATTRIBUTE_NAME, "   baz\t").getValue(), equalTo("   baz\t"));
	}

	@Test
	public void numberConvertedAsToString() throws Exception {
		assertThat(attribute(ATTRIBUTE_NAME, 42).getValue(), equalTo("42"));
		assertThat(attribute(ATTRIBUTE_NAME, 1L).getValue(), equalTo("1"));
		assertThat(attribute(ATTRIBUTE_NAME, 3.14).getValue(), equalTo("3.14"));
	}

	@Test
	public void dateConvertedInStandardFormat() throws Exception {
		final GregorianCalendar calendar = new GregorianCalendar(Locale.ROOT);
		calendar.set(2012, 11, 21, 21, 21, 21);
		final Date time = calendar.getTime();

		assertThat(attribute(ATTRIBUTE_NAME, time).getValue(), equalTo("2012-12-21T21:21:21"));
	}

}
