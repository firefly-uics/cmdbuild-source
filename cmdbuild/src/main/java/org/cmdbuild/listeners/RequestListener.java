package org.cmdbuild.listeners;

import java.util.LinkedList;
import java.util.List;

import javax.servlet.ServletRequest;
import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.cmdbuild.config.CmdbuildProperties;
import org.cmdbuild.exception.CMDBException;
import org.cmdbuild.services.DBService;

public class RequestListener implements ServletRequestListener {

	public class CMDBContext {
		HttpServletRequest request;
		List<CMDBException> warnings = new LinkedList<CMDBException>();

		private CMDBContext(final HttpServletRequest request) {
			this.request = request;
		}

		public void pushWarning(final CMDBException w) {
			warnings.add(w);
		}

		public List<CMDBException> getWarnings() {
			return warnings;
		}

		private HttpServletRequest getRequest() {
			return request;
		}
	}

	private static ThreadLocal<CMDBContext> requestContext = new ThreadLocal<CMDBContext>();

	public static CMDBContext getCurrentRequest() {
		return requestContext.get();
	}

	public static Object getCurrentSessionObject(final String name) {
		final HttpSession session = getOrCreateSession();
		if (session != null) {
			return session.getAttribute(name);
		} else {
			return null;
		}
	}

	public static void setCurrentSessionObject(final String name, final Object value) {
		final HttpSession session = getOrCreateSession();
		if (session != null) {
			session.setAttribute(name, value);
		}
	}

	public static void removeCurrentSessionObject(final String name) {
		final HttpSession session = getOrCreateSession();
		if (session != null) {
			session.removeAttribute(name);
		}
	}

	private static HttpSession getOrCreateSession() {
		final CMDBContext ctx = getCurrentRequest();
		HttpSession session = null;
		if (ctx != null) {
			ctx.getRequest().getSession(false);
			if (session == null) {
				session = ctx.getRequest().getSession(true);
				initSession(session);
			}
		}
		return session;
	}

	private static void initSession(final HttpSession session) {
		final int sessionTimeout = CmdbuildProperties.getInstance().getSessionTimoutOrZero();
		if (sessionTimeout > 0) {
			session.setMaxInactiveInterval(sessionTimeout);
		}
	}

	@Override
	public void requestInitialized(final ServletRequestEvent sre) {
		final ServletRequest req = sre.getServletRequest();
		if (req instanceof HttpServletRequest) {
			final CMDBContext currentRequestContext = new CMDBContext((HttpServletRequest) req);
			requestContext.set(currentRequestContext);
		}
	}

	@Override
	public void requestDestroyed(final ServletRequestEvent sre) {
		DBService.releaseConnection();
		requestContext.remove();
	}
}
