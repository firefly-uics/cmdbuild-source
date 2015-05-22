package org.cmdbuild.logic.email;

import org.cmdbuild.logic.email.EmailLogic.Email;
import org.cmdbuild.logic.email.EmailLogic.Status;

import com.google.common.base.Predicate;

public class Predicates {

	private static class StatusIs implements Predicate<Email> {

		private final Status value;

		public StatusIs(final Status value) {
			this.value = value;
		}

		@Override
		public boolean apply(final Email input) {
			return value.equals(input.getStatus());
		};

	}

	public static Predicate<Email> statusIs(final Status value) {
		return new StatusIs(value);
	}

	private Predicates() {
		// prevents instantiation
	}

}
