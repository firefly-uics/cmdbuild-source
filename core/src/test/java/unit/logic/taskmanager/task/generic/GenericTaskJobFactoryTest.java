package unit.logic.taskmanager.task.generic;

import static com.google.common.base.Optional.of;
import static com.google.common.collect.Iterables.elementsEqual;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import org.cmdbuild.data.store.email.EmailAccount;
import org.cmdbuild.data.store.email.EmailAccountFacade;
import org.cmdbuild.logic.email.EmailTemplateLogic;
import org.cmdbuild.logic.email.EmailTemplateSenderFactory;
import org.cmdbuild.logic.taskmanager.task.generic.GenericTask;
import org.cmdbuild.logic.taskmanager.task.generic.GenericTaskJobFactory;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import com.google.common.base.Supplier;

public class GenericTaskJobFactoryTest {

	private EmailAccountFacade emailAccountFacade;
	private EmailTemplateLogic emailTemplateLogic;
	private EmailTemplateSenderFactory emailTemplateSenderFactory;
	private GenericTaskJobFactory underTest;

	@Before
	public void setUp() {
		emailAccountFacade = mock(EmailAccountFacade.class);
		emailTemplateLogic = mock(EmailTemplateLogic.class);
		emailTemplateSenderFactory = mock(EmailTemplateSenderFactory.class);
		underTest = new GenericTaskJobFactory(emailAccountFacade, emailTemplateLogic, emailTemplateSenderFactory);
	}

	@Test
	public void jobDoesNothingWhenEmailIsNotActive() throws Exception {
		// given
		final GenericTask task = GenericTask.newInstance() //
				.withId(42L) //
				.withEmailActive(false) //
				.build();

		// when
		underTest.create(task, true).execute();

		// then
		verifyNoMoreInteractions(emailAccountFacade, emailTemplateLogic, emailTemplateSenderFactory);
	}

	@Test
	public void jobCanBeCreatedWithNoErrorsEvenIfAllParametersAreMissing() throws Exception {
		// given
		final GenericTask task = GenericTask.newInstance() //
				.withId(42L) //
				.withEmailActive(true) //
				.build();

		// when
		underTest.create(task, true);

		// then
		verifyNoMoreInteractions(emailAccountFacade, emailTemplateLogic, emailTemplateSenderFactory);
	}

	@Test
	public void jobDelegatesToComponentTheSendOfTheMail() throws Exception {
		// given
		final GenericTask task = GenericTask.newInstance() //
				.withId(42L) //
				.withEmailActive(true) //
				.build();
		final EmailTemplateSenderFactory.Builder queue = mock(EmailTemplateSenderFactory.Builder.class);
		doReturn(queue) //
				.when(emailTemplateSenderFactory).queued();
		doReturn(queue) //
				.when(queue).withEmailAccountSupplier(any(Supplier.class));
		doReturn(queue) //
				.when(queue).withEmailTemplateSupplier(any(Supplier.class));
		doReturn(queue) //
				.when(queue).withReference(anyLong());
		final EmailTemplateSenderFactory.EmailTemplateSender sender = mock(
				EmailTemplateSenderFactory.EmailTemplateSender.class);
		doReturn(sender) //
				.when(queue).build();

		// when
		underTest.create(task, true).execute();

		// then
		final ArgumentCaptor<Supplier> captor = ArgumentCaptor.forClass(Supplier.class);
		verify(emailTemplateSenderFactory).queued();
		verify(queue).withEmailAccountSupplier(captor.capture());
		verify(queue).withEmailTemplateSupplier(captor.capture());
		verify(queue).withReference(eq(42L));
		verify(queue).build();
		verify(sender).execute();

		verifyNoMoreInteractions(emailAccountFacade, emailTemplateLogic, emailTemplateSenderFactory, queue, sender);
	}

	@Test
	public void templateIsReadedOnlyOnceAndTemplateAccountHasPriorityOverSpecifiedOne() throws Exception {
		// given
		final GenericTask task = GenericTask.newInstance() //
				.withId(42L) //
				.withEmailActive(true) //
				.withEmailTemplate("email template") //
				.withEmailAccount("email account") //
				.build();
		final EmailTemplateSenderFactory.Builder queue = mock(EmailTemplateSenderFactory.Builder.class);
		doReturn(queue) //
				.when(emailTemplateSenderFactory).queued();
		doReturn(queue) //
				.when(queue).withEmailAccountSupplier(any(Supplier.class));
		doReturn(queue) //
				.when(queue).withEmailTemplateSupplier(any(Supplier.class));
		doReturn(queue) //
				.when(queue).withReference(anyLong());
		final EmailTemplateSenderFactory.EmailTemplateSender sender = mock(
				EmailTemplateSenderFactory.EmailTemplateSender.class);
		doReturn(sender) //
				.when(queue).build();
		final EmailAccount account = mock(EmailAccount.class);
		doReturn(of(account)) // "
				.when(emailAccountFacade).firstOfOrDefault(any(Iterable.class));
		final EmailTemplateLogic.Template template = mock(EmailTemplateLogic.Template.class);
		doReturn("email account from template") //
				.when(template).getAccount();
		doReturn(template) //
				.when(emailTemplateLogic).read(anyString());

		// when
		underTest.create(task, true).execute();

		// then
		final ArgumentCaptor<Supplier> suppliersCaptor = ArgumentCaptor.forClass(Supplier.class);
		verify(emailTemplateSenderFactory).queued();
		verify(queue).withEmailAccountSupplier(suppliersCaptor.capture());
		verify(queue).withEmailTemplateSupplier(suppliersCaptor.capture());
		verify(queue).withReference(eq(42L));
		verify(queue).build();
		verify(sender).execute();

		final Supplier<?> accounSupplier = suppliersCaptor.getAllValues().get(0);
		assertThat((EmailAccount) accounSupplier.get(), equalTo(account));
		final ArgumentCaptor<Iterable> accounts = ArgumentCaptor.forClass(Iterable.class);
		verify(emailAccountFacade).firstOfOrDefault(accounts.capture());
		assertThat(elementsEqual(accounts.getValue(), asList("email account from template", "email account")),
				equalTo(true));

		final Supplier<?> templateSupplier = suppliersCaptor.getAllValues().get(1);
		assertThat((EmailTemplateLogic.Template) templateSupplier.get(), equalTo(template));
		verify(emailTemplateLogic).read(eq("email template"));

		verifyNoMoreInteractions(emailAccountFacade, emailTemplateLogic, emailTemplateSenderFactory, queue, sender);
	}

}
