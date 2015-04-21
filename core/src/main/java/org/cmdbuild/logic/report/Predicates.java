package org.cmdbuild.logic.report;

import java.util.Arrays;
import java.util.List;

import org.cmdbuild.auth.UserStore;
import org.cmdbuild.auth.user.OperationUser;
import org.cmdbuild.services.store.report.Report;

import com.google.common.base.Predicate;

public class Predicates {

	private static class CurrentGroupAllowed implements Predicate<Report> {

		private final UserStore userStore;

		public CurrentGroupAllowed(final UserStore userStore) {
			this.userStore = userStore;
		}

		@Override
		public boolean apply(final Report input) {
			final OperationUser operationUser = userStore.getUser();
			if (operationUser.hasAdministratorPrivileges()) {
				return true;
			}
			final List<String> allowedGroupIdsForThisReport = Arrays.asList(input.getGroups());
			final String groupUsedForLogin = operationUser.getPreferredGroup().getName();
			if (allowedGroupIdsForThisReport.contains(groupUsedForLogin.toString())) {
				return true;
			}
			return false;
		}

	}

	public static Predicate<Report> currentGroupAllowed(final UserStore userStore) {
		return new CurrentGroupAllowed(userStore);
	}

	private Predicates() {
		// prevents instantiation
	}

}
