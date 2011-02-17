package org.cmdbuild.servlet.util;

import javax.portlet.PortletSession;
import javax.servlet.http.HttpSession;

public class SessionUtils {

	private SessionUtils() {
		// hidden
	}

	@SuppressWarnings("unchecked")
	public static <T> T getAttribute(final HttpSession session, final String name) {
		// TODO checks
		return (T) session.getAttribute(name);
	}

	@SuppressWarnings("unchecked")
	public static <T> T getAttribute(final PortletSession session, final String name) {
		// TODO checks
		return (T) session.getAttribute(name);
	}

}
