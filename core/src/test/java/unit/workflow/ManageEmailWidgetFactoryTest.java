package unit.workflow;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

import java.util.Map;

import org.cmdbuild.dao.entry.CMValueSet;
import org.cmdbuild.logic.email.EmailLogic;
import org.cmdbuild.model.widget.ManageEmail;
import org.cmdbuild.model.widget.ManageEmail.EmailTemplate;
import org.cmdbuild.notification.Notifier;
import org.cmdbuild.services.template.store.TemplateRepository;
import org.cmdbuild.workflow.widget.ManageEmailWidgetFactory;
import org.cmdbuild.workflow.widget.ValuePairWidgetFactory;
import org.junit.Test;

public class ManageEmailWidgetFactoryTest {

	private static final EmailLogic UNUSED_EMAIL_LOGIC = null;

	private final ValuePairWidgetFactory factory;

	public ManageEmailWidgetFactoryTest() {
		factory = new ManageEmailWidgetFactory(mock(TemplateRepository.class), mock(Notifier.class), UNUSED_EMAIL_LOGIC);
	}

	@Test
	public void singleEmailTemplateDefinition() {
		final ManageEmail w = (ManageEmail) factory.createWidget("ToAddresses='to@a.a'\n" + "CCAddresses='cc@a.a'\n"
				+ "Subject='the subject'\n" + "Content='the content'\n", mock(CMValueSet.class));

		assertThat(w.getEmailTemplates().size(), is(1));
		final EmailTemplate t = w.getEmailTemplates().get(0);
		assertThat(t.getToAddresses(), is("to@a.a"));
		assertThat(t.getCcAddresses(), is("cc@a.a"));
		assertThat(t.getSubject(), is("the subject"));
		assertThat(t.getContent(), is("the content"));
	}

	@Test
	public void moreThanOneEmailTemplateDefinitions() {
		final ManageEmail w = (ManageEmail) factory.createWidget("ToAddresses='to@a.a'\n" + "CCAddresses='cc@a.a'\n"
				+ "Subject='the subject'\n" + "Content='the content'\n" +

				"ToAddresses1='to@a.a 1'\n" + "CCAddresses1='cc@a.a 1'\n" + "Subject1='the subject 1'\n"
				+ "Content1='the content 1'\n" +

				"Content2='the content 2'\n" +

				"Condition3='condition'\n", mock(CMValueSet.class));

		assertThat(w.getEmailTemplates().size(), is(4));

		EmailTemplate t = w.getEmailTemplates().get(0);
		assertThat(t.getToAddresses(), is("to@a.a"));
		assertThat(t.getCcAddresses(), is("cc@a.a"));
		assertThat(t.getSubject(), is("the subject"));
		assertThat(t.getContent(), is("the content"));

		t = w.getEmailTemplates().get(1);
		assertThat(t.getToAddresses(), is("to@a.a 1"));
		assertThat(t.getCcAddresses(), is("cc@a.a 1"));
		assertThat(t.getSubject(), is("the subject 1"));
		assertThat(t.getContent(), is("the content 1"));

		t = w.getEmailTemplates().get(2);
		assertThat(t.getContent(), is("the content 2"));

		t = w.getEmailTemplates().get(3);
		assertThat(t.getCondition(), is("condition"));
	}

	@Test
	public void readAlsoTheTemplates() {
		final ManageEmail w = (ManageEmail) factory.createWidget("ToAddresses='to@a.a'\n" +

		"ToAddresses1='to@a.a 1'\n" +

		"Ashibabalea='from Ashi when baba={client:lea}'\n" +

		"Foo='Bar'\n", mock(CMValueSet.class));

		assertThat(w.getEmailTemplates().size(), is(2));

		final Map<String, String> templates = w.getTemplates();
		assertThat(templates.keySet().size(), is(2));
		assertThat(templates.get("Ashibabalea"), is("from Ashi when baba={client:lea}"));
		assertThat(templates.get("Foo"), is("Bar"));
	}
}
