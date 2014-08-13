package support;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import com.google.common.reflect.AbstractInvocationHandler;

public class ForwardingProxy<T> {

	private static class DelegatingInvocationHandler<T> extends AbstractInvocationHandler {

		private T delegate;

		@Override
		protected Object handleInvocation(final Object proxy, final Method method, final Object[] args)
				throws Throwable {
			return method.invoke(delegate, args);
		}

		public void set(final T delegate) {
			this.delegate = delegate;
		}

	}

	public static <T> ForwardingProxy<T> of(final Class<T> type) {
		final DelegatingInvocationHandler<T> invocationHandler = new DelegatingInvocationHandler<T>();
		final Object proxy = Proxy.newProxyInstance( //
				ForwardingProxy.class.getClassLoader(), //
				new Class<?>[] { type }, //
				invocationHandler);
		final T instance = type.cast(proxy);
		return new ForwardingProxy<T>(instance, invocationHandler);
	}

	private final T proxy;
	private final DelegatingInvocationHandler<T> invocationHandler;

	private ForwardingProxy(final T proxy, final DelegatingInvocationHandler<T> invocationHandler) {
		this.proxy = proxy;
		this.invocationHandler = invocationHandler;
	}

	public T get() {
		return proxy;
	}

	public void set(final T delegate) {
		invocationHandler.set(delegate);
	}

}
