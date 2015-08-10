package unit.model.widget.customform;

import static org.cmdbuild.model.widget.customform.CustomFormWidgetFactory.CONFIGURATION_TYPE;
import static org.cmdbuild.model.widget.customform.CustomFormWidgetFactory.FORM;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.HashMap;

import org.cmdbuild.common.collect.ChainablePutMap;
import org.cmdbuild.dao.entry.CMValueSet;
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
		verifyNoMoreInteractions(templateRespository, notifier);
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
		verifyNoMoreInteractions(templateRespository, notifier);
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
		verifyNoMoreInteractions(templateRespository, notifier);
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
		verifyNoMoreInteractions(templateRespository, notifier);
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
		verifyNoMoreInteractions(templateRespository, notifier);
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
		verifyNoMoreInteractions(templateRespository, notifier);
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
		verifyNoMoreInteractions(templateRespository, notifier);
	}

}
