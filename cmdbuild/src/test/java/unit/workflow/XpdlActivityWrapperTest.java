package unit.workflow;

import static org.cmdbuild.workflow.xpdl.XpdlActivityWrapper.VARIABLE_PREFIX;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.cmdbuild.workflow.xpdl.CMActivityVariableToProcess;
import org.cmdbuild.workflow.xpdl.CMActivityVariableToProcess.Type;
import org.cmdbuild.workflow.xpdl.XpdlActivity;
import org.cmdbuild.workflow.xpdl.XpdlActivity.XpdlVariableSuffix;
import org.cmdbuild.workflow.xpdl.XpdlActivityWrapper;
import org.cmdbuild.workflow.xpdl.XpdlDocument;
import org.junit.Test;

public class XpdlActivityWrapperTest {

	private XpdlActivity xpdlActivity;
	private XpdlActivityWrapper wrapper;

	public XpdlActivityWrapperTest() {
		final XpdlDocument doc = new XpdlDocument("PKG");
		xpdlActivity = doc.createProcess("PRO").createActivity("ACT");
		wrapper = new XpdlActivityWrapper(xpdlActivity);
	}

	@Test
	public void extractsNoVariablesIfNoExtendedAttributes() {
		assertThat(wrapper.getVariables().size(), is(0));
	}

	@Test
	public void extractsNoVariablesForInvalidEntries() {
		xpdlActivity.addExtendedAttribute("Rubbish", "Foo");
		xpdlActivity.addExtendedAttribute(VARIABLE_PREFIX + XpdlVariableSuffix.VIEW, null);

		assertThat(wrapper.getVariables().size(), is(0));
	}

	@Test
	public void variablesAreExtracted() {
		CMActivityVariableToProcess var;

		xpdlActivity.addExtendedAttribute(VARIABLE_PREFIX + XpdlVariableSuffix.VIEW, "Bar");

		assertThat(wrapper.getVariables().size(), is(1));

		var = wrapper.getVariables().get(0);
		assertThat(var.getName(), is("Bar"));
		assertThat(var.getType(), is(Type.READ_ONLY));

		xpdlActivity.addExtendedAttribute(VARIABLE_PREFIX + XpdlVariableSuffix.UPDATE, "Foo");
		xpdlActivity.addExtendedAttribute(VARIABLE_PREFIX + XpdlVariableSuffix.UPDATEREQUIRED, "Baz");

		assertThat(wrapper.getVariables().size(), is(3));

		var = wrapper.getVariables().get(1);
		assertThat(var.getName(), is("Foo"));
		assertThat(var.getType(), is(Type.READ_WRITE));

		var = wrapper.getVariables().get(2);
		assertThat(var.getName(), is("Baz"));
		assertThat(var.getType(), is(Type.READ_WRITE_REQUIRED));
	}
}
