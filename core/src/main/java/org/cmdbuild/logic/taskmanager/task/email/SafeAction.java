package org.cmdbuild.logic.taskmanager.task.email;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

class SafeAction extends ForwardingAction {

	public static SafeAction of(final Action delegate) {
		final Object proxy = Proxy.newProxyInstance( //
				SafeAction.class.getClassLoader(), //
				new Class<?>[] { Action.class }, //
				new InvocationHandler() {

					@Override
					public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
						try {
							return method.invoke(delegate, args);
						} catch (final Throwable e) {
							return null;
						}
					}

				});
		final Action proxiedAction = Action.class.cast(proxy);
		return new SafeAction(proxiedAction);
	}

	private final Action delegate;

	private SafeAction(final Action delegate) {
		this.delegate = delegate;
	}

	@Override
	protected Action delegate() {
		return delegate;
	}

}