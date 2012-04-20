package org.cmdbuild.dms.properties;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.cmdbuild.dms.exception.MissingPropertiesException;

public class NullDmsPropertiesProxy implements InvocationHandler {

	@Override
	public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
		throw new MissingPropertiesException();
	}

	private static Object newInstance() {
		final Class<?> cl = DmsProperties.class;
		final ClassLoader classLoader = cl.getClassLoader();
		final Class<?>[] interfaces = new Class<?>[] { DmsProperties.class };
		return Proxy.newProxyInstance( //
				classLoader, //
				interfaces, //
				new NullDmsPropertiesProxy());
	}

	public static DmsProperties getDmsProperties() {
		final Object proxy = newInstance();
		return DmsProperties.class.cast(proxy);
	}

}
