package unit.api.fluent.ws;

import static java.util.Arrays.asList;
import static org.cmdbuild.api.fluent.ws.WsFluentApiExecutor.attribute;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static utils.matchers.AttributeListMatcher.containsAttribute;

import java.util.Map;

import org.cmdbuild.api.fluent.CallFunction;
import org.cmdbuild.services.soap.Attribute;
import org.junit.Before;
import org.junit.Test;

public class CallFunctionTest extends AbstractWsFluentApiTest {

	private static final String FUNCTION_NAME = "function";

	private static final String IN_PARAMETER_1 = "foo";
	private static final String IN_PARAMETER_1_VALUE = randomString();
	private static final String IN_PARAMETER_2 = "bar";
	private static final String IN_PARAMETER_2_VALUE = randomString();
	private static final String OUT_PARAMETER_1 = "baz";
	private static final String OUT_PARAMETER_1_VALUE = randomString();

	private CallFunction callFunction;

	@Before
	public void createExistingCard() throws Exception {
		callFunction = api() //
				.callFunction(FUNCTION_NAME) // \
				.with(IN_PARAMETER_1, IN_PARAMETER_1_VALUE) //
				.with(IN_PARAMETER_2, IN_PARAMETER_2_VALUE);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void parametersPassedToProxyWhenExecutingCallableFunction() {
		when(proxy().callFunction( //
				anyString(), //
				anyListOf(Attribute.class)) //
		).thenReturn(asList(attribute(OUT_PARAMETER_1, OUT_PARAMETER_1_VALUE)));

		callFunction.execute();

		verify(proxy()).callFunction( //
				eq(callFunction.getFunctionName()), //
				argThat(allOf( //
						containsAttribute(IN_PARAMETER_1, IN_PARAMETER_1_VALUE), //
						containsAttribute(IN_PARAMETER_2, IN_PARAMETER_2_VALUE))));
		verifyNoMoreInteractions(proxy());
	}

	@Test
	public void outputAttributesAreConvertedToMapStringStringWhenExecutingCallableFunction() {
		when(proxy().callFunction( //
				anyString(), //
				anyListOf(Attribute.class)) //
		).thenReturn(asList(attribute(OUT_PARAMETER_1, OUT_PARAMETER_1_VALUE)));

		final Map<String, String> outputs = callFunction.execute();
		assertThat(outputs.size(), equalTo(1));
		assertThat(outputs.get(OUT_PARAMETER_1), equalTo(OUT_PARAMETER_1_VALUE));
	}

}
