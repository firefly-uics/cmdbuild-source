package unit.workflow;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;
import static org.mockito.Mockito.*;

import org.cmdbuild.workflow.CMActivity.CMActivityWidget;
import org.cmdbuild.workflow.xpdl.ValuePairWidgetFactory;
import org.cmdbuild.workflow.xpdl.ValuePairXpdlExtendedAttributeWidgetFactory;
import org.cmdbuild.workflow.xpdl.XpdlExtendedAttribute;
import org.junit.Test;

public class ValuePairWidgetFactoryTest {

	private final ValuePairXpdlExtendedAttributeWidgetFactory widgetFactory;

	public ValuePairWidgetFactoryTest() {
		widgetFactory = new ValuePairXpdlExtendedAttributeWidgetFactory();
	}

	@Test
	public void createReturnsNullOnUnsupportedWidgets() {
		assertNull(createWidget("Unsupported", "Something"));
	}

	@Test
	public void createDelegatesCreationToTheSpecificWidgetFactory() {
		ValuePairWidgetFactory aFactory = mock(ValuePairWidgetFactory.class);
		CMActivityWidget aWidget = addFactoryReturningWidget(aFactory, "A");

		assertNull(createWidget("A", "Serialization"));
		assertNull(createWidget(null, "Serialization"));

		widgetFactory.addWidgetFactory(aFactory);

		assertThat(createWidget("A", "Serialization"), is(aWidget));

		verify(aFactory, times(1)).createWidget(eq("Serialization"));
	}

	@Test
	public void widgetSerializationCannotBeNull() {
		ValuePairWidgetFactory aFactory = mock(ValuePairWidgetFactory.class);
		addFactoryReturningWidget(aFactory, "A");

		widgetFactory.addWidgetFactory(aFactory);

		assertNull(createWidget("A", null));

		verify(aFactory.createWidget(anyString()), never());
	}

	/*
	 * Utils
	 */

	private CMActivityWidget addFactoryReturningWidget(final ValuePairWidgetFactory aFactory, final String name) {
		CMActivityWidget aWidget =  mock(CMActivityWidget.class);
		when(aFactory.getWidgetName()).thenReturn(name);
		when(aFactory.createWidget(anyString())).thenReturn(aWidget);
		return aWidget;
	}

	private CMActivityWidget createWidget(final String key, final String value) {
		return widgetFactory.createWidget(new XpdlExtendedAttribute(key, value));
	}

}
