package org.cmdbuild.servlets.json.management;

import org.cmdbuild.logic.email.EmailLogic;
import org.cmdbuild.servlets.json.JSONBaseWithSpringContext;
import org.cmdbuild.servlets.json.serializers.JsonWorkflowDTOs.JsonEmail;
import org.cmdbuild.servlets.utils.Parameter;

import com.google.common.base.Function;
import com.google.common.collect.Iterators;

public class Email extends JSONBaseWithSpringContext {

	@JSONExported
	public JsonResponse getEmailList(@Parameter("ProcessId") final Long processCardId) {
		final Iterable<org.cmdbuild.model.Email> emails = emailLogic().getEmails(processCardId);
		return JsonResponse.success(Iterators.transform(emails.iterator(),
				new Function<org.cmdbuild.model.Email, JsonEmail>() {
					@Override
					public JsonEmail apply(final org.cmdbuild.model.Email input) {
						return new JsonEmail(input);
					}
				}));
	}

};
