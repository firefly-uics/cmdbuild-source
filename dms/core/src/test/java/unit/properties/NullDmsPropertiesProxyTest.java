package unit.properties;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;

import org.cmdbuild.dms.exception.MissingPropertiesException;
import org.cmdbuild.dms.properties.DmsProperties;
import org.cmdbuild.dms.properties.NullDmsPropertiesProxy;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class NullDmsPropertiesProxyTest {

	private final Object object;
	private final Method method;

	@Parameters
	public static Collection<Object[]> data() {
		final DmsProperties dmsProperties = NullDmsPropertiesProxy.getDmsProperties();
		final Collection<Object[]> parameters = new ArrayList<Object[]>();
		final Class<?> dmsPropertiesClass = DmsProperties.class;
		final Method[] methods = dmsPropertiesClass.getMethods();
		for (final Method method : methods) {
			parameters.add(new Object[] { dmsProperties, method });
		}
		return parameters;
	}

	public NullDmsPropertiesProxyTest(final Object object, final Method method) {
		this.object = object;
		this.method = method;
	}

	@Test(expected = MissingPropertiesException.class)
	public void everyMethodCalledMustThowInvocationTargetExceptionBecauseMissingPropertiesException() throws Throwable {
		try {
			final Class<?>[] parameterTypes = method.getParameterTypes();
			final Object[] parameters = new Object[parameterTypes.length];
			method.invoke(object, parameters);
		} catch (final InvocationTargetException e) {
			throw e.getCause();
		}
	}
}
