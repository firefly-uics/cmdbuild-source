package unit.template;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.cmdbuild.utils.template.TemplateResolver;
import org.cmdbuild.utils.template.TemplateResolverEngine;
import org.junit.Test;

public class TemplateResolverTest {

	@Test
	public void aSimpleStringIsKeptAsItIs() {
		String template = "A simple string";
		TemplateResolver tr = TemplateResolver.newInstanceBuilder().build();
		assertThat(tr.simpleEval(template), is(template));
	}

	@Test
	public void inexistentEngineIsExpandedWithNull() {
		TemplateResolverEngine engine = mock(TemplateResolverEngine.class);
		when(engine.eval(anyString())).thenReturn(null);

		TemplateResolver tr = TemplateResolver.newInstanceBuilder().build();

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
		TemplateResolver tr = TemplateResolver.newInstanceBuilder()
			.withEngine("e1",  engineWithParam("param", value))
			.build();

		assertThat(tr.simpleEval("{e1:param}"), is(String.valueOf(value)));
	}

	@Test
	public void leadingPartsAreKeptIntact() {
		TemplateResolver tr = TemplateResolver.newInstanceBuilder()
			.withEngine("e1", engineWithParam("param", 42))
			.build();

		assertThat(tr.simpleEval("XXX{e1:param}"), is(String.format("XXX%s", 42)));
	}

	private TemplateResolverEngine engineWithParam(final String name, final Object value) {
		TemplateResolverEngine engine = mock(TemplateResolverEngine.class);
		when(engine.eval(name)).thenReturn(value);
		return engine;
	}

	@Test
	public void moreThanOneParameterIsExpanded() {
		final Object value1 = 42, value2 = "st";
		TemplateResolverEngine engine = mock(TemplateResolverEngine.class);
		when(engine.eval("param1")).thenReturn(value1);
		when(engine.eval("param2")).thenReturn(value2);

		TemplateResolver tr = TemplateResolver.newInstanceBuilder()
			.withEngine("e1", engine)
			.build();

		assertThat(tr.simpleEval("XXX{e1:param1}YYY{e1:param2}ZZZ"), is(
				String.format("XXX%sYYY%sZZZ", value1, value2)));
	}

}
