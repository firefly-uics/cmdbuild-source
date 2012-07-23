package unit.workflow;

import static org.cmdbuild.workflow.widget.ValuePairWidgetFactory.BUTTON_LABEL;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertThat;

import java.util.Map;

import org.cmdbuild.model.widget.Widget;
import org.cmdbuild.workflow.CMActivity.CMActivityWidget;
import org.cmdbuild.workflow.widget.ValuePairWidgetFactory;
import org.junit.Test;

public class ValuePairWidgetFactoryTest {

	private static class FakeWidgetFactory extends ValuePairWidgetFactory {
		public Map<String, String> valueMap;

		protected Widget createWidget(final Map<String, String> valueMap) {
			this.valueMap = valueMap;
			return new Widget() {};
		}

		public String getWidgetName() {
			throw new UnsupportedOperationException("Should not be used");
		}
	}

	@Test
	public void emptyDefinitionHasNoValues() {
		final FakeWidgetFactory factory = new FakeWidgetFactory();

		factory.createWidget("");

		assertThat(factory.valueMap.size(), is(0));
	}

	@Test
	public void valuesStartingWithADigitAreLeftUntouched() {
		final FakeWidgetFactory factory = new FakeWidgetFactory();

		factory.createWidget(
				"A=42\n" +
				"B=4XXX"
			);

		assertThat(factory.valueMap.size(), is(2));
		assertThat(factory.valueMap.get("A"), is("42"));
		assertThat(factory.valueMap.get("B"), is("4XXX"));
	}

	@Test
	public void noEqualSignOrEmptyAfterAreConsideredNull() {
		final FakeWidgetFactory factory = new FakeWidgetFactory();

		factory.createWidget(
				"A\n" +
				"B="
			);

		assertThat(factory.valueMap.size(), is(2));
		assertThat(factory.valueMap.get("A"), is(nullValue()));
		assertThat(factory.valueMap.get("B"), is(nullValue()));
	}

	@Test
	public void singleAndDoubleQuotesAreRemoved() {
		final FakeWidgetFactory factory = new FakeWidgetFactory();

		factory.createWidget(
				"C='XXX'\n" +
				"D=\"YYY\"\n"
			);

		assertThat(factory.valueMap.size(), is(2));
		assertThat(factory.valueMap.get("C"), is("XXX"));
		assertThat(factory.valueMap.get("D"), is("YYY"));
	}

	@Test
	public void clientNamespaceIsTransformedToATemplate() {
		final FakeWidgetFactory factory = new FakeWidgetFactory();

		factory.createWidget("X=client:Var");

		assertThat(factory.valueMap.size(), is(1));
		assertThat(factory.valueMap.get("X"), is("{client:Var}"));
	}

	// TODO: server variables, Filter, dbtmpl:

	@Test
	public void setsTheLabelOnEveryWidget() {
		final FakeWidgetFactory factory = new FakeWidgetFactory();

		CMActivityWidget widget = factory.createWidget(
				BUTTON_LABEL + "='MyLabel'"
			);

		assertThat(widget.getLabel(), is("MyLabel"));
	}

	@Test
	public void computesIdFromDefinitionHash() {
		final FakeWidgetFactory factory = new FakeWidgetFactory();

		CMActivityWidget widget = factory.createWidget("SomeDefinitionString");

		assertThat(widget.getId(), startsWith("widget-"));
	}
}
