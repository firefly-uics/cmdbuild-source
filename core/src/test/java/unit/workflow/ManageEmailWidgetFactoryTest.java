package unit.workflow;

import static org.apache.commons.lang.StringUtils.EMPTY;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.cmdbuild.dao.entry.CMValueSet;
import org.cmdbuild.logic.email.EmailLogic;
import org.cmdbuild.logic.email.EmailTemplateLogic;
import org.cmdbuild.model.widget.ManageEmail;
import org.cmdbuild.model.widget.ManageEmail.EmailTemplate;
import org.cmdbuild.notification.Notifier;
import org.cmdbuild.services.template.store.TemplateRepository;
import org.cmdbuild.workflow.widget.ManageEmailWidgetFactory;
import org.junit.Before;
import org.junit.Test;

public class ManageEmailWidgetFactoryTest {

	private static final EmailLogic UNUSED_EMAIL_LOGIC = null;

	private ManageEmailWidgetFactory factory;

	private EmailTemplateLogic emailTemplateLogic;

	@Before
	public void setUp() {
		emailTemplateLogic = mock(EmailTemplateLogic.class);
		factory = new ManageEmailWidgetFactory( //
				mock(TemplateRepository.class), //
				mock(Notifier.class), //
				UNUSED_EMAIL_LOGIC, //
				emailTemplateLogic);
	}

	@Test
	public void singleEmailTemplateDefinition() {
		final ManageEmail w = (ManageEmail) factory.createWidget(EMPTY + //
				"ToAddresses='to@a.a'\n" + //
				"CCAddresses='cc@a.a'\n" + //
				"Subject='the subject'\n" + //
				"Content='the content'\n", //
				mock(CMValueSet.class));

		assertThat(w.getEmailTemplates().size(), equalTo(1));
		final EmailTemplate t = w.getEmailTemplates().get(0);
		assertThat(t.getToAddresses(), equalTo("to@a.a"));
		assertThat(t.getCcAddresses(), equalTo("cc@a.a"));
		assertThat(t.getSubject(), equalTo("the subject"));
		assertThat(t.getContent(), equalTo("the content"));
	}

	@Test
	public void moreThanOneEmailTemplateDefinitions() {
		final ManageEmail w = (ManageEmail) factory.createWidget(EMPTY + //
				"ToAddresses='to@a.a'\n" + //
				"CCAddresses='cc@a.a'\n" + //
				"Subject='the subject'\n" + //
				"Content='the content'\n" + //

				"ToAddresses1='to@a.a 1'\n" + //
				"CCAddresses1='cc@a.a 1'\n" + //
				"Subject1='the subject 1'\n" + //
				"Content1='the content 1'\n" + //

				"Content2='the content 2'\n" + //

				"Condition3='condition'\n", //
				mock(CMValueSet.class));

		assertThat(w.getEmailTemplates().size(), equalTo(4));

		EmailTemplate t = w.getEmailTemplates().get(0);
		assertThat(t.getToAddresses(), equalTo("to@a.a"));
		assertThat(t.getCcAddresses(), equalTo("cc@a.a"));
		assertThat(t.getSubject(), equalTo("the subject"));
		assertThat(t.getContent(), equalTo("the content"));

		t = w.getEmailTemplates().get(1);
		assertThat(t.getToAddresses(), equalTo("to@a.a 1"));
		assertThat(t.getCcAddresses(), equalTo("cc@a.a 1"));
		assertThat(t.getSubject(), equalTo("the subject 1"));
		assertThat(t.getContent(), equalTo("the content 1"));

		t = w.getEmailTemplates().get(2);
		assertThat(t.getContent(), equalTo("the content 2"));

		t = w.getEmailTemplates().get(3);
		assertThat(t.getCondition(), equalTo("condition"));
	}

	@Test
	public void readAlsoTheTemplates() {
		final ManageEmail w = (ManageEmail) factory.createWidget(EMPTY + //
				"ToAddresses='to@a.a'\n" + //

				"ToAddresses1='to@a.a 1'\n" + //

				"Ashibabalea='from Ashi when baba={client:lea}'\n" + //

				"Foo='Bar'\n", //
				mock(CMValueSet.class));

		assertThat(w.getEmailTemplates().size(), equalTo(2));

		final Map<String, String> templates = w.getTemplates();
		assertThat(templates.keySet().size(), equalTo(2));
		assertThat(templates.get("Ashibabalea"), equalTo("from Ashi when baba={client:lea}"));
		assertThat(templates.get("Foo"), equalTo("Bar"));
	}

	@Test
	public void emailTemplateApplied() {
		when(emailTemplateLogic.read("foo")).thenReturn(new org.cmdbuild.model.email.EmailTemplate() {
			{
				setName("foo");
				setTo("to@example.com");
				setCC("cc@example.com");
				setSubject("the subject");
				setBody("the content");
			}
		});

		final ManageEmail w = (ManageEmail) factory.createWidget(EMPTY + //
				"Template='foo'\n", //
				mock(CMValueSet.class));

		verify(emailTemplateLogic).read("foo");

		assertThat(w.getEmailTemplates().size(), equalTo(1));
		final EmailTemplate t = w.getEmailTemplates().get(0);
		assertThat(t.getToAddresses(), equalTo("to@example.com"));
		assertThat(t.getCcAddresses(), equalTo("cc@example.com"));
		assertThat(t.getSubject(), equalTo("the subject"));
		assertThat(t.getContent(), equalTo("the content"));
	}

	@Test
	public void multipleEmailTemplatesApplied() {
		when(emailTemplateLogic.read("foo")) //
				.thenReturn(new org.cmdbuild.model.email.EmailTemplate() {
					{
						setName("foo");
						setTo("foo_to@example.com");
						setCC("foo_cc@example.com");
						setSubject("subject of foo");
						setBody("content of foo");
					}
				});
		when(emailTemplateLogic.read("bar")) //
				.thenReturn(new org.cmdbuild.model.email.EmailTemplate() {
					{
						setName("bar");
						setTo("bar_to@example.com");
						setCC("bar_cc@example.com");
						setSubject("subject of bar");
						setBody("content of bar");
					}
				});

		final ManageEmail w = (ManageEmail) factory.createWidget(EMPTY + //
				"Template1='foo'\n" + //
				"Template2='bar'\n", //
				mock(CMValueSet.class));

		verify(emailTemplateLogic, times(2)).read(anyString());

		assertThat(w.getEmailTemplates().size(), equalTo(2));

		// needs to be sorted since we don't know how they are internally sorted
		final List<EmailTemplate> emailTemplates = w.getEmailTemplates();
		Collections.sort(emailTemplates, new Comparator<EmailTemplate>() {
			@Override
			public int compare(final EmailTemplate o1, final EmailTemplate o2) {
				return o1.getToAddresses().compareTo(o2.getToAddresses());
			};
		});

		final EmailTemplate t0 = w.getEmailTemplates().get(0);
		assertThat(t0.getToAddresses(), equalTo("bar_to@example.com"));
		assertThat(t0.getCcAddresses(), equalTo("bar_cc@example.com"));
		assertThat(t0.getSubject(), equalTo("subject of bar"));
		assertThat(t0.getContent(), equalTo("content of bar"));

		final EmailTemplate t1 = w.getEmailTemplates().get(1);
		assertThat(t1.getToAddresses(), equalTo("foo_to@example.com"));
		assertThat(t1.getCcAddresses(), equalTo("foo_cc@example.com"));
		assertThat(t1.getSubject(), equalTo("subject of foo"));
		assertThat(t1.getContent(), equalTo("content of foo"));
	}

	@Test
	public void emailTemplateCanBeOverrideInSomeParts() {
		when(emailTemplateLogic.read("foo")).thenReturn(new org.cmdbuild.model.email.EmailTemplate() {
			{
				setName("foo");
				setTo("to@example.com");
				setCC("cc@example.com");
				setSubject("the subject");
				setBody("the content");
			}
		});

		final ManageEmail w = (ManageEmail) factory.createWidget(EMPTY + //
				"Template='foo'\n" + //
				"ToAddresses='lol@example.com'\n" + //
				"Subject='rotfl'\n", //
				mock(CMValueSet.class));

		verify(emailTemplateLogic).read("foo");

		assertThat(w.getEmailTemplates().size(), equalTo(1));
		final EmailTemplate t = w.getEmailTemplates().get(0);
		assertThat(t.getToAddresses(), equalTo("lol@example.com"));
		assertThat(t.getCcAddresses(), equalTo("cc@example.com"));
		assertThat(t.getSubject(), equalTo("rotfl"));
		assertThat(t.getContent(), equalTo("the content"));
	}

	@Test
	public void noSubjectPrefixFalseIfNotSpecified() {
		final ManageEmail w = (ManageEmail) factory.createWidget(EMPTY + //
				"ToAddresses='to@a.a'\n" + //
				"CCAddresses='cc@a.a'\n" + //
				"Subject='the subject'\n" + //
				"Content='the content'\n", //
				mock(CMValueSet.class));

		assertThat(w.getEmailTemplates().size(), equalTo(1));
		final EmailTemplate t = w.getEmailTemplates().get(0);
		assertThat(t.isNoSubjectPrefix(), equalTo(false));
	}

	@Test
	public void noSubjectPrefixSpecifiedAsTrue() {
		final ManageEmail w = (ManageEmail) factory.createWidget(EMPTY + //
				"ToAddresses='to@a.a'\n" + //
				"CCAddresses='cc@a.a'\n" + //
				"Subject='the subject'\n" + //
				"Content='the content'\n" + //
				"NoSubjectPrefix='true'\n", //
				mock(CMValueSet.class));

		assertThat(w.getEmailTemplates().size(), equalTo(1));
		final EmailTemplate t = w.getEmailTemplates().get(0);
		assertThat(t.isNoSubjectPrefix(), equalTo(true));
	}

}
