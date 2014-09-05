package org.cmdbuild.common.reflect;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class AnnouncingInvocationHandler extends ForwardingInvocationHandler {

	public static interface Announceable {

		void announce(Method method, Object[] args);

	}

	public static AnnouncingInvocationHandler of(final InvocationHandler delegate, final Announceable announceable) {
		return new AnnouncingInvocationHandler(delegate, announceable);
	}

	private final Announceable announceable;

	private AnnouncingInvocationHandler(final InvocationHandler delegate, final Announceable announceable) {
		super(delegate);
		this.announceable = announceable;
	}

	@Override
	public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
		announceable.announce(method, args);
		return super.invoke(proxy, method, args);
	}

}
