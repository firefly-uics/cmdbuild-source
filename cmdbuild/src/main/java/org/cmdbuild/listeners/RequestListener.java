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

		private CMDBContext(HttpServletRequest request) {
			this.request = request;
		}

		public void pushWarning(CMDBException w) {
			warnings.add(w);
		}

		public List<CMDBException> getWarnings() {
			return warnings;
		}

		private HttpServletRequest getRequest() {
			return request;
		}
	}

	static private ThreadLocal<CMDBContext> requestContext = new ThreadLocal<CMDBContext>();

	static public CMDBContext getCurrentRequest() {
		return requestContext.get();
	}

	static public Object getCurrentSessionObject(String name) {
		HttpSession session = getOrCreateSession();
		if (session != null) {
			return session.getAttribute(name);
		} else {
			return null;
		}
	}

	static public void setCurrentSessionObject(String name, Object value) {
		HttpSession session = getOrCreateSession();
		if (session != null) {
			session.setAttribute(name, value);
		}
	}

	static public void removeCurrentSessionObject(String name) {
		HttpSession session = getOrCreateSession();
		if (session != null) {
			session.removeAttribute(name);
		}
	}

	private static HttpSession getOrCreateSession() {
		CMDBContext ctx = getCurrentRequest();
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

	private static void initSession(HttpSession session) {
		int sessionTimeout = CmdbuildProperties.getInstance().getSessionTimoutOrZero();
		if (sessionTimeout > 0) {
			session.setMaxInactiveInterval(sessionTimeout);
		}
	}

	public void requestInitialized(ServletRequestEvent sre) {
		ServletRequest req = sre.getServletRequest();
		if (req instanceof HttpServletRequest) {
			CMDBContext currentRequestContext = new CMDBContext((HttpServletRequest) req);
			requestContext.set(currentRequestContext);
		}
	}

	public void requestDestroyed(ServletRequestEvent sre) {
		DBService.releaseConnection();
		requestContext.remove();
	}
}
