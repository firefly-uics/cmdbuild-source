package unit.dao.entrytype.attributetype;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import org.cmdbuild.dao.entrytype.attributetype.IpAddressAttributeType;
import org.junit.Before;
import org.junit.Test;

public class IpAddressAttributeTypeTest {

	private IpAddressAttributeType type;

	@Before
	public void setUp() throws Exception {
		type = new IpAddressAttributeType();
	}

	@Test
	public void nullEmptyOrBlankConvertedToNull() throws Exception {
		// given
		final String nullValue = null;
		final String emptyValue = EMPTY;
		final String blankValue = "\t ";

		// when
		final String convertedNullValue = type.convertValue(nullValue);
		final String convertedEmptyValue = type.convertValue(emptyValue);
		final String convertedBlankValue = type.convertValue(blankValue);

		// then
		assertThat(convertedNullValue, is(nullValue()));
		assertThat(convertedEmptyValue, is(nullValue()));
		assertThat(convertedBlankValue, is(nullValue()));
	}

	@Test
	public void invalidIpAddressThrowsException() throws Exception {
		// given
		final String[] invalidValues = new String[] { "foo.bar.baz.lol", //
				"192.168.1.foo", "192.168.foo.1", "192.foo.1.1", "foo.168.1.1", //
				"256.168.1.1", "192.256.1.1", "192.168.256.1", "192.168.1.256", //
				"192.168.1", "192.168", "192." //
		};

		// when
		for (final String value : invalidValues) {
			try {
				type.convertValue(value);
				fail("should not be able to convert " + value);
			} catch (final Exception e) {
				// ok
			}
		}
	}

	@Test(expected = RuntimeException.class)
	public void hostResolutionNotTried() throws Exception {
		// given
		final String hostname = "example.com";

		// when
		type.convertValue(hostname);
	}

	@Test
	public void valueIsTrimmed() throws Exception {
		// given
		final String raw = "\t 192.168.1.1 \n";

		// when
		final String converted = type.convertValue(raw);

		// then
		assertThat(converted, equalTo("192.168.1.1"));
	}

	@Test
	public void invalidClassThrowsException() throws Exception {
		// given
		final String[] invalidValues = new String[] { "192.168.1.1/foo", "192.168.1.1/33" };

		// when
		for (final String value : invalidValues) {
			try {
				type.convertValue(value);
				fail("should not be able to convert " + value);
			} catch (final Exception e) {
				// ok
			}
		}
	}

}
