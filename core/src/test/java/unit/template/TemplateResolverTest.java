package unit.template;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.cmdbuild.utils.template.TemplateResolver;
import org.cmdbuild.utils.template.TemplateResolverEngine;
import org.cmdbuild.utils.template.TemplateResolverImpl;
import org.junit.Test;

public class TemplateResolverTest {

	@Test
	public void aSimpleStringIsKeptAsItIs() {
		final String template = "A simple string";
		final TemplateResolver tr = TemplateResolverImpl.newInstanceBuilder().build();
		assertThat(tr.simpleEval(template), is(template));
	}

	@Test
	public void inexistentEngineIsExpandedWithNull() {
		final TemplateResolverEngine engine = mock(TemplateResolverEngine.class);
		when(engine.eval(anyString())).thenReturn(null);

		final TemplateResolver tr = TemplateResolverImpl.newInstanceBuilder().build();

		assertThat(tr.simpleEval("{e1:param}"), is(String.valueOf((Object) null)));
	}

	@Test
	public void inexistentVariablesAreExpandedWithNull() {
		assertSingleParamIsItsStringRepresentation(null);
	}

	@Test
	public void simpleVariablesAreExpandedWithEngineEvaluation() {
		assertSingleParamIsItsStringRepresentation("string param");
		assertSingleParamIsItsStringRepresentation(Integer.valueOf(42));
	}

	private void assertSingleParamIsItsStringRepresentation(final Object value) {
		final TemplateResolver tr = TemplateResolverImpl.newInstanceBuilder()
				.withEngine(engineWithParam("param", value), "e1").build();

		assertThat(tr.simpleEval("{e1:param}"), is(String.valueOf(value)));
	}

	@Test
	public void leadingPartsAreKeptIntact() {
		final TemplateResolver tr = TemplateResolverImpl.newInstanceBuilder()
				.withEngine(engineWithParam("param", 42), "e1").build();

		assertThat(tr.simpleEval("XXX{e1:param}"), is(String.format("XXX%s", 42)));
	}

	private TemplateResolverEngine engineWithParam(final String name, final Object value) {
		final TemplateResolverEngine engine = mock(TemplateResolverEngine.class);
		when(engine.eval(name)).thenReturn(value);
		return engine;
	}

	@Test
	public void moreThanOneParameterIsExpanded() {
		final Object value1 = 42, value2 = "st";
		final TemplateResolverEngine engine = mock(TemplateResolverEngine.class);
		when(engine.eval("param1")).thenReturn(value1);
		when(engine.eval("param2")).thenReturn(value2);

		final TemplateResolver tr = TemplateResolverImpl.newInstanceBuilder().withEngine(engine, "e1").build();

		assertThat(tr.simpleEval("XXX{e1:param1}YYY{e1:param2}ZZZ"), is(String.format("XXX%sYYY%sZZZ", value1, value2)));
	}

}
