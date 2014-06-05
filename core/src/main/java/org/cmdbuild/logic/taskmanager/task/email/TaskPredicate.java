package org.cmdbuild.logic.taskmanager.task.email;

import com.google.common.base.Predicate;

enum TaskPredicate implements Predicate<ReadEmailTask> {

	SEND_NOTIFICATION() {

		@Override
		public boolean apply(final ReadEmailTask input) {
			return input.isNotificationActive();
		}

	}, //
	STORE_ATTACHMENTS() {

		@Override
		public boolean apply(final ReadEmailTask input) {
			return input.isAttachmentsActive();
		}

	}, //
	START_PROCESS() {

		@Override
		public boolean apply(final ReadEmailTask input) {
			return input.isWorkflowActive();
		}

	}, //
	;

}