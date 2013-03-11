package org.cmdbuild.servlets.json.management;

import org.cmdbuild.logic.TemporaryObjectsBeforeSpringDI;
import org.cmdbuild.logic.email.EmailLogic;
import org.cmdbuild.services.auth.UserContext;
import org.cmdbuild.servlets.json.JSONBase;
import org.cmdbuild.servlets.json.serializers.JsonWorkflowDTOs.JsonEmail;
import org.cmdbuild.servlets.utils.Parameter;

import com.google.common.base.Function;
import com.google.common.collect.Iterators;

public class Email extends JSONBase {

	@JSONExported
	public JsonResponse getEmailList(@Parameter("ProcessId") final Long processCardId) {
		final EmailLogic logic = TemporaryObjectsBeforeSpringDI.getEmailLogic();
		final Iterable<org.cmdbuild.model.Email> emails = logic.getEmails(processCardId);
		return JsonResponse.success(Iterators.transform(emails.iterator(),
				new Function<org.cmdbuild.model.Email, JsonEmail>() {
					@Override
					public JsonEmail apply(final org.cmdbuild.model.Email input) {
						return new JsonEmail(input);
					}
				}));
	}

};
