package org.cmdbuild.logic.email;

import org.cmdbuild.data.store.email.EmailStatus;
import org.cmdbuild.logic.email.EmailLogic.Email;

import com.google.common.base.Predicate;

public class Predicates {

	private static class StatusIs implements Predicate<Email> {

		private final EmailStatus value;

		public StatusIs(final EmailStatus value) {
			this.value = value;
		}

		@Override
		public boolean apply(final Email input) {
			return value.equals(input.getStatus());
		};

	}

	public static Predicate<Email> statusIs(final EmailStatus value) {
		return new StatusIs(value);
	}

	private Predicates() {
		// prevents instantiation
	}

}
