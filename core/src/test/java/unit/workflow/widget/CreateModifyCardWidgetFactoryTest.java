package unit.workflow.widget;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import org.cmdbuild.dao.entry.CMValueSet;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.exception.CMDBWorkflowException;
import org.cmdbuild.model.widget.CreateModifyCard;
import org.cmdbuild.notification.Notifier;
import org.cmdbuild.services.template.store.TemplateRepository;
import org.cmdbuild.workflow.widget.CreateModifyCardWidgetFactory;
import org.junit.Test;

public class CreateModifyCardWidgetFactoryTest {

	@Test
	public void refernenceIsNullAndClassNameIsMissing() throws Exception {
		// given
		final TemplateRepository templateRepository = mock(TemplateRepository.class);
		final Notifier notifier = mock(Notifier.class);
		final CMDataView dataView = mock(CMDataView.class);
		final CreateModifyCardWidgetFactory factory = new CreateModifyCardWidgetFactory(templateRepository, notifier,
				dataView);

		// when
		final CreateModifyCard output = (CreateModifyCard) factory.createWidget(EMPTY //
				+ "Reference=null\n" //
				, mock(CMValueSet.class));

		// then
		verify(notifier).warn(any(CMDBWorkflowException.class));
		verifyNoMoreInteractions(templateRepository, notifier, dataView);

		assertThat(output, nullValue());
	}

	@Test
	public void refernenceIsNullAndClassNameIsEmpty() throws Exception {
		// given
		final TemplateRepository templateRepository = mock(TemplateRepository.class);
		final Notifier notifier = mock(Notifier.class);
		final CMDataView dataView = mock(CMDataView.class);
		final CreateModifyCardWidgetFactory factory = new CreateModifyCardWidgetFactory(templateRepository, notifier,
				dataView);

		// when
		final CreateModifyCard output = (CreateModifyCard) factory.createWidget(EMPTY //
				+ "Reference=null\n" //
				+ "ClassName=null" //
				, mock(CMValueSet.class));

		// then
		verify(notifier).warn(any(CMDBWorkflowException.class));
		verifyNoMoreInteractions(templateRepository, notifier, dataView);

		assertThat(output, nullValue());
	}

	@Test
	public void refernenceIsNullAndClassNameIsBlank() throws Exception {
		// given
		final TemplateRepository templateRepository = mock(TemplateRepository.class);
		final Notifier notifier = mock(Notifier.class);
		final CMDataView dataView = mock(CMDataView.class);
		final CreateModifyCardWidgetFactory factory = new CreateModifyCardWidgetFactory(templateRepository, notifier,
				dataView);

		// when
		final CreateModifyCard output = (CreateModifyCard) factory.createWidget(EMPTY //
				+ "Reference=null\n" //
				+ "ClassName= \t" //
				, mock(CMValueSet.class));

		// then
		verify(notifier).warn(any(CMDBWorkflowException.class));
		verifyNoMoreInteractions(templateRepository, notifier, dataView);

		assertThat(output, nullValue());
	}

	@Test
	public void refernenceIsNullAndClassNameIsValid() throws Exception {
		// given
		final TemplateRepository templateRepository = mock(TemplateRepository.class);
		final Notifier notifier = mock(Notifier.class);
		final CMDataView dataView = mock(CMDataView.class);
		final CreateModifyCardWidgetFactory factory = new CreateModifyCardWidgetFactory(templateRepository, notifier,
				dataView);

		// when
		final CreateModifyCard output = (CreateModifyCard) factory.createWidget(EMPTY //
				+ "Reference=null\n" //
				+ "ClassName='Test'" //
				, mock(CMValueSet.class));

		// then
		verifyNoMoreInteractions(templateRepository, notifier, dataView);

		assertThat(output.getTargetClass(), equalTo("Test"));
		assertThat(output.getIdcardcqlselector(), nullValue());
	}

}
