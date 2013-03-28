package org.cmdbuild.common.utils;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class UnsupportedProxyFactory<T> {

	private final Class<T> type;

	public UnsupportedProxyFactory(final Class<T> type) {
		this.type = type;
	}

	public T create() {
		return type.cast(Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[] { type },
				new InvocationHandler() {
					@Override
					public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
						throw new UnsupportedOperationException();
					}
				}));
	}

	public static <T> UnsupportedProxyFactory<T> of(final Class<T> type) {
		return new UnsupportedProxyFactory<T>(type);
	}

}
