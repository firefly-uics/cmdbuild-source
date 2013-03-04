package org.cmdbuild.servlets.json.management;

import org.cmdbuild.common.annotations.Legacy;
import org.cmdbuild.common.annotations.OldDao;
import org.cmdbuild.logic.EmailLogic;
import org.cmdbuild.logic.TemporaryObjectsBeforeSpringDI;
import org.cmdbuild.services.auth.UserContext;
import org.cmdbuild.servlets.json.JSONBase;
import org.cmdbuild.servlets.json.serializers.JsonWorkflowDTOs.JsonEmail;
import org.cmdbuild.servlets.utils.Parameter;

import com.google.common.base.Function;
import com.google.common.collect.Iterators;

@Legacy("Move to email widget actions")
public class Email extends JSONBase {

	@OldDao
	@JSONExported
	public JsonResponse getEmailList(@Parameter("ProcessId") final long processCardId, final UserContext userContext) {
		final EmailLogic logic = TemporaryObjectsBeforeSpringDI.getEmailLogic(userContext);
		logic.retrieveEmails();
		final Iterable<EmailLogic.Email> emails = logic.getEmails(processCardId);
		return JsonResponse.success(Iterators.transform(emails.iterator(), new Function<EmailLogic.Email, JsonEmail>() {

			@Override
			public JsonEmail apply(final EmailLogic.Email input) {
				return new JsonEmail(input);
			}
		}));
	}

};