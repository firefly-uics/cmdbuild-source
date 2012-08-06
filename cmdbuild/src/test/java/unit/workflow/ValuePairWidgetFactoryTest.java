package unit.workflow;

import static org.cmdbuild.workflow.widget.ValuePairWidgetFactory.BUTTON_LABEL;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.cmdbuild.dao.entry.CMValueSet;
import org.cmdbuild.model.widget.Widget;
import org.cmdbuild.workflow.CMActivity.CMActivityWidget;
import org.cmdbuild.workflow.widget.ValuePairWidgetFactory;
import org.junit.Test;

public class ValuePairWidgetFactoryTest {

	private static class FakeWidgetFactory extends ValuePairWidgetFactory {
		public Map<String, Object> valueMap;

		protected Widget createWidget(final Map<String, Object> valueMap) {
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

		factory.createWidget("", mock(CMValueSet.class));

		assertThat(factory.valueMap.size(), is(0));
	}

	@Test
	public void valuesStartingWithADigitAreConvertedToIntegers() {
		final FakeWidgetFactory factory = new FakeWidgetFactory();

		factory.createWidget(
				"A=42\n" +
				"B=4XXX",
				mock(CMValueSet.class)
			);

		assertThat(factory.valueMap.size(), is(2));
		assertThat((Integer)factory.valueMap.get("A"), is(42));
		assertThat(factory.valueMap.get("B"), is(nullValue()));
	}

	@Test
	public void noEqualSignIsOutputValue() {
		final FakeWidgetFactory factory = new FakeWidgetFactory();

		factory.createWidget(
				"A\n",
				mock(CMValueSet.class)
			);

		assertThat(factory.valueMap.size(), is(1));
		assertThat((String)factory.valueMap.get(null), is("A"));
	}

	@Test
	public void emptyAfterEqualSignIsOutputValue() {
		final FakeWidgetFactory factory = new FakeWidgetFactory();

		factory.createWidget(
				"B=",
				mock(CMValueSet.class)
			);

		assertThat(factory.valueMap.size(), is(1));
		assertThat((String)factory.valueMap.get(null), is("B"));
	}

	@Test
	public void singleAndDoubleQuotesAreRemoved() {
		final FakeWidgetFactory factory = new FakeWidgetFactory();

		factory.createWidget(
				"C='XXX'\n" +
				"D=\"YYY\"\n",
				mock(CMValueSet.class)
			);

		assertThat(factory.valueMap.size(), is(2));
		assertThat((String)factory.valueMap.get("C"), is("XXX"));
		assertThat((String)factory.valueMap.get("D"), is("YYY"));
	}

	@Test
	public void clientNamespaceIsTransformedToATemplate() {
		final FakeWidgetFactory factory = new FakeWidgetFactory();

		factory.createWidget("X=client:Var", mock(CMValueSet.class));

		assertThat(factory.valueMap.size(), is(1));
		assertThat((String)factory.valueMap.get("X"), is("{client:Var}"));
	}

	@Test
	public void serverNamespaceRequestsProcessVariables() {
		CMValueSet procInstVars = mock(CMValueSet.class);
		when(procInstVars.get("X")).thenReturn("StringVal");
		when(procInstVars.get("Y")).thenReturn(555);

		final FakeWidgetFactory factory = new FakeWidgetFactory();
		factory.createWidget(
				"XP=X\n" +
				"YP=Y"
			, procInstVars);

		assertThat(factory.valueMap.size(), is(2));
		assertThat((String)factory.valueMap.get("XP"), is("StringVal"));
		assertThat((Integer)factory.valueMap.get("YP"), is(555));
	}

	@Test
	public void filterKeyAlwaysConsideredString() {
		final FakeWidgetFactory factory = new FakeWidgetFactory();

		factory.createWidget(
				"Filter=client:Var",
				mock(CMValueSet.class)
			);

		assertThat((String)factory.valueMap.get("Filter"), is("client:Var"));
	}

	// TODO: dbtmpl:

	@Test
	public void setsTheLabelOnEveryWidget() {
		final FakeWidgetFactory factory = new FakeWidgetFactory();

		CMActivityWidget widget = factory.createWidget(
				BUTTON_LABEL + "='MyLabel'",
				mock(CMValueSet.class)
			);

		assertThat(widget.getLabel(), is("MyLabel"));
	}

	@Test
	public void computesIdFromDefinitionHash() {
		final FakeWidgetFactory factory = new FakeWidgetFactory();

		CMActivityWidget widget = factory.createWidget(
				"SomeDefinitionString",
				mock(CMValueSet.class)
			);

		assertThat(widget.getId(), startsWith("widget-"));
	}
}
