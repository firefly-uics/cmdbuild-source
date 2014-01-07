package test.services.setup;

import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

import org.cmdbuild.auth.acl.PrivilegeContext;
import org.cmdbuild.logic.setup.SetUpLogic.Module;
import org.cmdbuild.logic.setup.SetUpLogic.ModulesHandler;
import org.cmdbuild.services.setup.PrivilegedModule;
import org.cmdbuild.services.setup.PrivilegedModulesHandler;
import org.junit.Before;
import org.junit.Test;

public class PrivilegedModulesHandlerTest {

	private ModulesHandler moduleHandler;
	private PrivilegedModulesHandler privilegedModuleHandler;

	@Before
	public void setUp() throws Exception {
		moduleHandler = mock(ModulesHandler.class);

		final PrivilegeContext privilegeContext = mock(PrivilegeContext.class);

		privilegedModuleHandler = new PrivilegedModulesHandler(moduleHandler, privilegeContext);
	}

	@Test
	public void returnedModuleIsWrapped() throws Exception {
		// when
		final Module module = privilegedModuleHandler.get("foo");

		// then
		assertThat(module, instanceOf(PrivilegedModule.class));
	}

}
