package unit.template;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import java.util.HashMap;

import org.cmdbuild.utils.template.ParameterMapEngine;
import org.junit.Test;

@SuppressWarnings("serial")
public class ParameterMapEngineTest {

	@Test
	public void evaluatesToNullIfParameterNotPresent() {
		ParameterMapEngine engine = new ParameterMapEngine(new HashMap<String, Object>());
		assertThat(engine.eval("Any Name"), is(nullValue()));
	}

	@Test
	public void evaluatesValuesAsTheyWerePut() {
		assertIdentityEvaluation("A string");
		assertIdentityEvaluation(Integer.valueOf(42));
		assertIdentityEvaluation(Long.valueOf(123456789L));
		assertIdentityEvaluation(new Object());
	}

	private void assertIdentityEvaluation(final Object value) {
		final String name = "parameter";
		ParameterMapEngine engine = new ParameterMapEngine(new HashMap<String, Object>() {{
			put(name, value);
		}});
		assertThat(engine.eval(name), is(value));
	}
}
