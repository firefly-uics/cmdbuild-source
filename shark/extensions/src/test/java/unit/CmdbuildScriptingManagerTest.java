package unit;

import static org.enhydra.shark.scripting.StandardScriptingManager.JAVA_LANGUAGE_SCRIPT;
import static org.enhydra.shark.scripting.StandardScriptingManager.JAVA_SCRIPT;
import static org.enhydra.shark.scripting.StandardScriptingManager.PYTHON_SCRIPT;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

import java.util.HashMap;

import org.cmdbuild.api.fluent.FluentApi;
import org.cmdbuild.api.fluent.FluentApiExecutor;
import org.cmdbuild.workflow.CmdbuildScriptingManager;
import org.enhydra.shark.api.client.wfmc.wapi.WMSessionHandle;
import org.enhydra.shark.api.internal.scripting.Evaluator;
import org.enhydra.shark.api.internal.scripting.ScriptingManager;
import org.enhydra.shark.api.internal.working.CallbackUtilities;
import org.junit.Before;
import org.junit.Test;

public class CmdbuildScriptingManagerTest {

	private static final FluentApi FLUENT_API = new FluentApi(mock(FluentApiExecutor.class));

	private static final WMSessionHandle UNUSED_WMSESSION_HANDLE = null;

	private ScriptingManager scriptingManager;

	@Before
	public void createAndConfigureScriptingManager() throws Exception {
		scriptingManager = new CmdbuildScriptingManager() {

			@Override
			protected FluentApi initApi(final CallbackUtilities cus) throws ClassNotFoundException,
					InstantiationException, IllegalAccessException {
				return FLUENT_API;
			}

		};
		scriptingManager.configure(mock(CallbackUtilities.class));
	}

	@Test
	public void nullNameReturnsNullEvaluator() throws Exception {
		assertThat(evaluatorForLanguage(null), is(nullValue()));
	}

	@Test
	public void defaultScriptingEnginesAreBeanshellJavascriptAndPython() throws Exception {
		assertThat(evaluatorForLanguage(JAVA_LANGUAGE_SCRIPT), not(is(nullValue())));
		assertThat(evaluatorForLanguage(JAVA_SCRIPT), not(is(nullValue())));
		assertThat(evaluatorForLanguage(PYTHON_SCRIPT), not(is(nullValue())));
	}

	@Test
	public void defaultEvaluatorsNotInitizedIfMissingConfiguration() throws Exception {
		final ScriptingManager scriptingManager = new CmdbuildScriptingManager();
		// skipping configuration
		assertThat(scriptingManager.getEvaluator(UNUSED_WMSESSION_HANDLE, JAVA_LANGUAGE_SCRIPT), is(nullValue()));
	}

	@Test
	public void otherEvaluatorsAreNotImplemented() throws Exception {
		assertThat(evaluatorForLanguage("text/groovy"), is(nullValue()));
		assertThat(evaluatorForLanguage("foo"), is(nullValue()));
		assertThat(evaluatorForLanguage("text/sql"), is(nullValue()));
	}

	@Test
	public void fluentApiSuccessfullyInjected() throws Exception {
		final String expression = "cmdb instanceof org.cmdbuild.api.fluent.FluentApi";
		assertThat((Boolean) eval(expression), equalTo(true));
	}

	private Object eval(final String expression) throws Exception {
		return evaluatorForLanguage(JAVA_LANGUAGE_SCRIPT) //
				.evaluateExpression(UNUSED_WMSESSION_HANDLE, //
						"procId", //
						"actId", //
						expression, //
						new HashMap<String, Object>(), //
						Object.class);
	}

	private Evaluator evaluatorForLanguage(final String language) throws Exception {
		return scriptingManager.getEvaluator(UNUSED_WMSESSION_HANDLE, language);
	}

}
