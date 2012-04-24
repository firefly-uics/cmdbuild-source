package org.cmdbuild.logic;

import java.util.ArrayList;

import org.cmdbuild.dao.function.CMFunction;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.services.auth.UserContext;

/**
 * Business Logic Layer for Data Access
 */
public class DashboardLogic {

	private final CMDataView view;

	// FIXME Temporary constructor before switching to Spring DI
	public DashboardLogic(final UserContext userCtx) {
		view = TemporaryObjectsBeforeSpringDI.getUserContextView(userCtx);
	}

	public DashboardLogic(final CMDataView view) {
		this.view = view;
	}

	public Iterable<DashboardDefinition> listDashboards() {
		return new ArrayList<DashboardDefinition>();
	}

	public Iterable<? extends CMFunction> listDataSources() {
		return view.findAllFunctions();
	}

	/*
	 * DTOs
	 */

	public static class DashboardDefinition {
	}
}
