package unit.workflow;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import org.cmdbuild.model.widget.ManageRelation;
import org.cmdbuild.workflow.widget.ManageRelationWidgetFactory;
import org.junit.Test;

public class ManageRelationWidgetFactoryTest {

	@Test
	public void testSource() {
		final ManageRelationWidgetFactory factory = new ManageRelationWidgetFactory();

		ManageRelation w = (ManageRelation) factory.createWidget(
			"IsDirect='true'\n"
		);
		assertThat(w.getSource(), is("_1"));

		w = (ManageRelation) factory.createWidget(
			"IsDirect='false'\n"
		);
		assertThat(w.getSource(), is("_2"));

		w = (ManageRelation) factory.createWidget(
			"IsDirect='asdf asd'\n"
		);
		assertThat(w.getSource(), is("_2"));

		w = (ManageRelation) factory.createWidget("");
		assertThat(w.getSource(), is(nullValue()));
	}

}
