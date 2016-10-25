package unit.common;

import static org.cmdbuild.common.Builders.identity;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.cmdbuild.common.Builder;
import org.junit.Test;

public class BuildersTest {

	@Test
	public void identityReturnsAlwaysTheSameValue() throws Exception {
		// given
		final Object value = new Object();

		// when
		final Builder<Object> identity = identity(value);

		// then
		assertThat(identity.build(), equalTo(value));
		assertThat(identity.build(), equalTo(value));
	}

}
