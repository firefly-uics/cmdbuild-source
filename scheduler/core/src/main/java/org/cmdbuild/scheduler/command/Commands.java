package org.cmdbuild.scheduler.command;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.cmdbuild.scheduler.logging.LoggingSupport;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import com.google.common.base.Predicate;

public class Commands {

	private static class NullCommand implements Command {

		@Override
		public void execute() {
			// nothing to do
		}

	}

	private static class SafeCommand extends ForwardingCommand {

		private static final Logger logger = LoggingSupport.logger;
		private static final Marker marker = MarkerFactory.getMarker(SafeCommand.class.getName());

		public static SafeCommand of(final Command delegate) {
			final Object proxy = Proxy.newProxyInstance( //
					SafeCommand.class.getClassLoader(), //
					new Class<?>[] { Command.class }, //
					new InvocationHandler() {

						@Override
						public Object invoke(final Object proxy, final Method method, final Object[] args)
								throws Throwable {
							try {
								return method.invoke(delegate, args);
							} catch (final Throwable e) {
								logger.warn(marker, "error calling method '{}'", method);
								logger.warn(marker, "\tcaused by", e);
								return null;
							}
						}

					});
			final Command proxiedAction = Command.class.cast(proxy);
			return new SafeCommand(proxiedAction);
		}

		private SafeCommand(final Command delegate) {
			super(delegate);
		}

	}

	private static class ComposeOnExeption extends ForwardingCommand {

		private static final Logger logger = LoggingSupport.logger;
		private static final Marker marker = MarkerFactory.getMarker(ComposeOnExeption.class.getName());

		private final Command onException;

		public ComposeOnExeption(final Command delegate, final Command onException) {
			super(delegate);
			this.onException = onException;
		}

		@Override
		public void execute() {
			try {
				super.execute();
			} catch (final Exception e) {
				logger.warn(marker, "error executing command", e);
				onException.execute();
			}
		}

	}

	private static class Conditional extends ForwardingCommand {

		private final Predicate<Void> predicate;

		public Conditional(final Command delegate, final Predicate<Void> predicate) {
			super(delegate);
			this.predicate = predicate;
		}

		@Override
		public void execute() {
			if (predicate.apply(null)) {
				super.execute();
			}
		}

	}

	private static final NullCommand INSTANCE = new NullCommand();

	public static Command nullCommand() {
		return INSTANCE;
	}

	public static Command safe(final Command delegate) {
		return SafeCommand.of(delegate);
	}

	public static Command composeOnExeption(final Command delegate, final Command onException) {
		return new ComposeOnExeption(delegate, onException);
	}

	public static Command conditional(final Command delegate, final Predicate<Void> predicate) {
		return new Conditional(delegate, predicate);
	}

	private Commands() {
		// prevents instantiation
	}

}
