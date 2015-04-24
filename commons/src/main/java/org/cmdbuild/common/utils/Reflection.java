package org.cmdbuild.common.utils;

import static org.apache.commons.lang3.StringUtils.defaultString;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import com.google.common.reflect.AbstractInvocationHandler;

public class Reflection {

	private static class UnsupportedInvocationHandler extends AbstractInvocationHandler {

		private final String message;

		public UnsupportedInvocationHandler(final String message) {
			this.message = message;
		}

		@Override
		protected Object handleInvocation(final Object proxy, final Method method, final Object[] args)
				throws Throwable {
			throw new UnsupportedOperationException(defaultString(message));
		}

	}

	public static InvocationHandler unsupported(final String message) {
		return new UnsupportedInvocationHandler(message);
	}

	private Reflection() {
		// prevents instantiation
	}

}
