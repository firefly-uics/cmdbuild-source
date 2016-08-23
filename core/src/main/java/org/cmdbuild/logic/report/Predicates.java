package org.cmdbuild.logic.report;

import static com.google.common.collect.Sets.intersection;
import static com.google.common.collect.Sets.newHashSet;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.util.Set;

import org.cmdbuild.auth.user.OperationUser;
import org.cmdbuild.services.store.report.Report;

import com.google.common.base.Predicate;
import com.google.common.base.Supplier;

public class Predicates {

	private static class CurrentGroupAllowed implements Predicate<Report> {

		private final Supplier<OperationUser> supplier;

		public CurrentGroupAllowed(final Supplier<OperationUser> supplier) {
			this.supplier = supplier;
		}

		@Override
		public boolean apply(final Report input) {
			final OperationUser operationUser = supplier.get();
			if (operationUser.hasAdministratorPrivileges()) {
				return true;
			}
			final Set<String> userGroups;
			if (isNotBlank(operationUser.getAuthenticatedUser().getDefaultGroupName())) {
				/*
				 * when the default group is selected all available groups are
				 * considered
				 */
				userGroups = newHashSet(operationUser.getAuthenticatedUser().getGroupNames());
			} else {
				userGroups = newHashSet(operationUser.getPreferredGroup().getName());
			}
			return !intersection(newHashSet(input.getGroups()), userGroups).isEmpty();
		}

	}

	public static Predicate<Report> currentGroupAllowed(final Supplier<OperationUser> supplier) {
		return new CurrentGroupAllowed(supplier);
	}

	private Predicates() {
		// prevents instantiation
	}

}
