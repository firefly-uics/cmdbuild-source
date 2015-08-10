package unit.model.widget.customform;

import static java.util.Arrays.asList;
import static org.cmdbuild.model.widget.customform.CustomFormWidgetFactory.CLASSNAME;
import static org.cmdbuild.model.widget.customform.CustomFormWidgetFactory.CONFIGURATION_TYPE;
import static org.cmdbuild.model.widget.customform.CustomFormWidgetFactory.FORM;
import static org.cmdbuild.model.widget.customform.CustomFormWidgetFactory.FUNCTIONNAME;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.HashMap;

import org.cmdbuild.common.collect.ChainablePutMap;
import org.cmdbuild.dao.entry.CMValueSet;
import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.attributetype.CMAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.TextAttributeType;
import org.cmdbuild.dao.function.CMFunction;
import org.cmdbuild.dao.function.CMFunction.CMFunctionParameter;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.exception.CMDBWorkflowException;
import org.cmdbuild.exception.CMDBWorkflowException.WorkflowExceptionType;
import org.cmdbuild.model.widget.customform.CustomForm;
import org.cmdbuild.model.widget.customform.CustomForm.Attribute;
import org.cmdbuild.model.widget.customform.CustomForm.Attribute.Filter;
import org.cmdbuild.model.widget.customform.CustomFormWidgetFactory;
import org.cmdbuild.notification.Notifier;
import org.cmdbuild.services.meta.MetadataStoreFactory;
import org.cmdbuild.services.template.store.TemplateRepository;
import org.junit.Before;
import org.junit.Test;

public class CustomFormWidgetFactoryTest {

	private TemplateRepository templateRespository;
	private Notifier notifier;
	private CMDataView dataView;
	private MetadataStoreFactory metadataStoreFactory;
	private CustomFormWidgetFactory widgetFactory;

	@Before
	public void setUp() throws Exception {
		templateRespository = mock(TemplateRepository.class);
		notifier = mock(Notifier.class);
		dataView = mock(CMDataView.class);
		metadataStoreFactory = mock(MetadataStoreFactory.class);
		widgetFactory = new CustomFormWidgetFactory(templateRespository, notifier, dataView, metadataStoreFactory);
	}

	@Test
	public void undefinedConfigurationTypeProducesNoWidgetAndNotification() throws Exception {
		// given
		final String serialization = CONFIGURATION_TYPE + "=\"foo\"";

		// when
		final CustomForm created = (CustomForm) widgetFactory.createWidget(serialization, mock(CMValueSet.class));

		// then
		assertThat(created, nullValue());
		verify(notifier).warn(
				eq(new CMDBWorkflowException(WorkflowExceptionType.WF_CANNOT_CONFIGURE_CMDBEXTATTR, widgetFactory
						.getWidgetName())));
		verifyNoMoreInteractions(templateRespository, notifier, dataView, metadataStoreFactory);
	}

	@Test
	public void formConfigurationTypeAndMissingDefinitionProducesNoWidgetAndNotification() throws Exception {
		// given
		final String serialization = CONFIGURATION_TYPE + "=\"form\"";

		// when
		final CustomForm created = (CustomForm) widgetFactory.createWidget(serialization, mock(CMValueSet.class));

		// then
		assertThat(created, nullValue());
		verify(notifier).warn(
				eq(new CMDBWorkflowException(WorkflowExceptionType.WF_CANNOT_CONFIGURE_CMDBEXTATTR, widgetFactory
						.getWidgetName())));
		verifyNoMoreInteractions(templateRespository, notifier, dataView, metadataStoreFactory);
	}

	@Test
	public void formConfigurationTypeAndEmptyDefinitionProducesNoWidgetAndNotification() throws Exception {
		// given
		final String serialization = "" //
				+ CONFIGURATION_TYPE + "=\"form\"\n" //
				+ FORM + "=\"\"";

		// when
		final CustomForm created = (CustomForm) widgetFactory.createWidget(serialization, mock(CMValueSet.class));

		// then
		assertThat(created, nullValue());
		verify(notifier).warn(
				eq(new CMDBWorkflowException(WorkflowExceptionType.WF_CANNOT_CONFIGURE_CMDBEXTATTR, widgetFactory
						.getWidgetName())));
		verifyNoMoreInteractions(templateRespository, notifier, dataView, metadataStoreFactory);
	}

	@Test
	public void formConfigurationTypeAndBlankDefinitionProducesNoWidgetAndNotification() throws Exception {
		// given
		final String serialization = "" //
				+ CONFIGURATION_TYPE + "=\"form\"\n" //
				+ FORM + "=\" \"";

		// when
		final CustomForm created = (CustomForm) widgetFactory.createWidget(serialization, mock(CMValueSet.class));

		// then
		assertThat(created, nullValue());
		verify(notifier).warn(
				eq(new CMDBWorkflowException(WorkflowExceptionType.WF_CANNOT_CONFIGURE_CMDBEXTATTR, widgetFactory
						.getWidgetName())));
		verifyNoMoreInteractions(templateRespository, notifier, dataView, metadataStoreFactory);
	}

	@Test
	public void formConfigurationTypeAndInvalidDefinitionProducesNoWidgetAndNotification() throws Exception {
		// given
		final String serialization = "" //
				+ CONFIGURATION_TYPE + "=\"form\"\n" //
				+ FORM + "=\"[{name_with_no_quotes: foo}]\"";

		// when
		final CustomForm created = (CustomForm) widgetFactory.createWidget(serialization, mock(CMValueSet.class));

		// then
		assertThat(created, nullValue());
		verify(notifier).warn(
				eq(new CMDBWorkflowException(WorkflowExceptionType.WF_CANNOT_CONFIGURE_CMDBEXTATTR, widgetFactory
						.getWidgetName())));
		verifyNoMoreInteractions(templateRespository, notifier, dataView, metadataStoreFactory);
	}

	@Test
	public void formConfigurationTypeAndValidDefinitionProducesAttributesAndNoNotification() throws Exception {
		// given
		final String serialization = "" //
				+ CONFIGURATION_TYPE + "=\"form\"\n" //
				+ FORM + "=\"[{\"name\": \"foo\"},{\"name\": \"bar\"}]\"";

		// when
		final CustomForm created = (CustomForm) widgetFactory.createWidget(serialization, mock(CMValueSet.class));

		// then
		assertThat(created.getAttributes(), not(empty()));
		assertThat(created.getAttributes(), hasSize(2));
		assertThat(created.getAttributes().get(0).getName(), equalTo("foo"));
		assertThat(created.getAttributes().get(1).getName(), equalTo("bar"));
		verifyNoMoreInteractions(templateRespository, notifier, dataView, metadataStoreFactory);
	}

	@Test
	public void definitionForFormConfigurationSuccessfullyParsed() throws Exception {
		// given
		final String serialization = "" //
				+ CONFIGURATION_TYPE + "=\"form\"\n" //
				+ FORM + "=" //
				+ "    \"[" //
				+ "        {" //
				+ "            \"type\": \"text\"," //
				+ "            \"name\": \"foo\"," //
				+ "            \"description\": \"this is foo\"," //
				+ "            \"unique\": true," //
				+ "            \"mandatory\": true," //
				+ "            \"writable\": true," //
				+ "            \"precision\": 1," //
				+ "            \"scale\": 2," //
				+ "            \"length\": 3," //
				+ "            \"editorType\": \"lol\"," //
				+ "            \"targetClass\": \"some class\"," //
				+ "            \"lookupType\": \"some lookup type\"," //
				+ "            \"filter\": {" //
				+ "                \"expression\": \"the expression\"," //
				+ "                \"context\": {" //
				+ "                    \"foo\": \"bar\"," //
				+ "                    \"bar\": \"baz\"" //
				+ "                }" //
				+ "            }" //
				+ "        }" //
				+ "    ]\"";

		// when
		final CustomForm created = (CustomForm) widgetFactory.createWidget(serialization, mock(CMValueSet.class));

		// then
		assertThat(created.getAttributes(), not(empty()));
		assertThat(created.getAttributes(), hasSize(1));
		final Attribute attribute = created.getAttributes().get(0);
		assertThat(attribute.getType(), equalTo("text"));
		assertThat(attribute.getName(), equalTo("foo"));
		assertThat(attribute.getDescription(), equalTo("this is foo"));
		assertThat(attribute.isUnique(), equalTo(true));
		assertThat(attribute.isMandatory(), equalTo(true));
		assertThat(attribute.isWritable(), equalTo(true));
		assertThat(attribute.getPrecision(), equalTo(1L));
		assertThat(attribute.getScale(), equalTo(2L));
		assertThat(attribute.getLength(), equalTo(3L));
		assertThat(attribute.getEditorType(), equalTo("lol"));
		assertThat(attribute.getTargetClass(), equalTo("some class"));
		assertThat(attribute.getLookupType(), equalTo("some lookup type"));
		assertThat(attribute.getFilter(), equalTo(Filter.class.cast(new Filter() {
			{
				setExpression("the expression"); //
				setContext(ChainablePutMap.of(new HashMap<String, String>()) //
						.chainablePut("foo", "bar") //
						.chainablePut("bar", "baz"));
			}
		})));
		verifyNoMoreInteractions(templateRespository, notifier, dataView, metadataStoreFactory);
	}

	@Test
	public void classConfigurationTypeAndMissingClassProducesNoWidgetAndNotification() throws Exception {
		// given
		final String serialization = "" //
				+ CONFIGURATION_TYPE + "=\"class\"\n" //
				+ CLASSNAME + "=\"foo\"";
		doReturn(null) //
				.when(dataView).findClass(any(String.class));

		// when
		final CustomForm created = (CustomForm) widgetFactory.createWidget(serialization, mock(CMValueSet.class));

		// then
		assertThat(created, nullValue());
		verify(dataView).findClass(eq("foo"));
		verify(notifier).warn(
				eq(new CMDBWorkflowException(WorkflowExceptionType.WF_CANNOT_CONFIGURE_CMDBEXTATTR, widgetFactory
						.getWidgetName())));
		verifyNoMoreInteractions(templateRespository, notifier, dataView, metadataStoreFactory);
	}

	@Test
	public void attributesForClassSuccessfullyConverted() throws Exception {
		// given
		final String serialization = "" //
				+ CONFIGURATION_TYPE + "=\"class\"\n" //
				+ CLASSNAME + "=\"foo\"";
		final CMClass target = mock(CMClass.class);
		doReturn(target) //
				.when(dataView).findClass(any(String.class));
		final CMAttribute first = attribute(new TextAttributeType(), "bar");
		final CMAttribute second = attribute(new TextAttributeType(), "baz");
		doReturn(asList(first, second)) //
				.when(target).getAttributes();

		// when
		final CustomForm created = (CustomForm) widgetFactory.createWidget(serialization, mock(CMValueSet.class));

		// then
		assertThat(created.getAttributes(), not(empty()));
		assertThat(created.getAttributes(), hasSize(2));
		assertThat(created.getAttributes().get(0).getName(), equalTo("bar"));
		// TODO test all attribute conversion
		assertThat(created.getAttributes().get(1).getName(), equalTo("baz"));
		verify(dataView).findClass(eq("foo"));
		verifyNoMoreInteractions(templateRespository, notifier, dataView, metadataStoreFactory);
	}

	@Test
	public void functionConfigurationTypeAndMissingFunctionProducesNoWidgetAndNotification() throws Exception {
		// given
		final String serialization = "" //
				+ CONFIGURATION_TYPE + "=\"function\"\n" //
				+ FUNCTIONNAME + "=\"foo\"";
		doReturn(null) //
				.when(dataView).findFunctionByName(any(String.class));

		// when
		final CustomForm created = (CustomForm) widgetFactory.createWidget(serialization, mock(CMValueSet.class));

		// then
		assertThat(created, nullValue());
		verify(dataView).findFunctionByName(eq("foo"));
		verify(notifier).warn(
				eq(new CMDBWorkflowException(WorkflowExceptionType.WF_CANNOT_CONFIGURE_CMDBEXTATTR, widgetFactory
						.getWidgetName())));
		verifyNoMoreInteractions(templateRespository, notifier, dataView, metadataStoreFactory);
	}

	@Test
	public void attributesForFunctionSuccessfullyConverted() throws Exception {
		// given
		final String serialization = "" //
				+ CONFIGURATION_TYPE + "=\"function\"\n" //
				+ FUNCTIONNAME + "=\"foo\"";
		final CMFunction target = mock(CMFunction.class);
		doReturn(target) //
				.when(dataView).findFunctionByName(any(String.class));
		final CMFunctionParameter first = parameter(new TextAttributeType(), "bar");
		final CMFunctionParameter second = parameter(new TextAttributeType(), "baz");
		doReturn(asList(first, second)) //
				.when(target).getInputParameters();

		// when
		final CustomForm created = (CustomForm) widgetFactory.createWidget(serialization, mock(CMValueSet.class));

		// then
		assertThat(created.getAttributes(), not(empty()));
		assertThat(created.getAttributes(), hasSize(2));
		assertThat(created.getAttributes().get(0).getName(), equalTo("bar"));
		// TODO test all attribute conversion
		assertThat(created.getAttributes().get(1).getName(), equalTo("baz"));
		verify(dataView).findFunctionByName(eq("foo"));
		verifyNoMoreInteractions(templateRespository, notifier, dataView, metadataStoreFactory);
	}

	private static CMAttribute attribute(final CMAttributeType<?> type, final String name) {
		final CMAttribute output = mock(CMAttribute.class);
		doReturn(type) //
				.when(output).getType();
		doReturn(name) //
				.when(output).getName();
		return output;
	}

	private static CMFunctionParameter parameter(final CMAttributeType<?> type, final String name) {
		final CMFunctionParameter output = mock(CMFunctionParameter.class);
		doReturn(type) //
				.when(output).getType();
		doReturn(name) //
				.when(output).getName();
		return output;
	}

}
