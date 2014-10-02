package org.cmdbuild.common.reflect;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class AnnouncingInvocationHandler<T> implements InvocationHandler {

	public static interface Announceable {

		void announce(Method method, Object[] args);

	}

	public static <T> AnnouncingInvocationHandler of(final T delegate, final Announceable announceable) {
		return new AnnouncingInvocationHandler(delegate, announceable);
	}

	private final T delegate;
	private final Announceable announceable;

	private AnnouncingInvocationHandler(final T delegate, final Announceable announceable) {
		this.delegate = delegate;
		this.announceable = announceable;
	}

	@Override
	public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
		announceable.announce(method, args);
		return method.invoke(delegate, args);
	}

}
