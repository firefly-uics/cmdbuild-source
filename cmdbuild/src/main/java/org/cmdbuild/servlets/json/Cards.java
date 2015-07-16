package org.cmdbuild.servlets.json;

import static org.cmdbuild.services.json.dto.JsonResponse.failure;
import static org.cmdbuild.services.json.dto.JsonResponse.success;
import static org.cmdbuild.servlets.json.CommunicationConstants.ID;

import org.cmdbuild.exception.CMDBException;
import org.cmdbuild.services.json.dto.JsonResponse;
import org.cmdbuild.servlets.utils.Parameter;

public class Cards extends JSONBaseWithSpringContext {

	@JSONExported
	public JsonResponse lock( //
			@Parameter(value = ID) final Long cardId //
	) {
		JsonResponse response;
		try {
			userDataAccessLogic().lockCard(cardId);
			response = success();
		} catch (final Exception e) {
			if (e instanceof CMDBException) {
				notifier().warn(CMDBException.class.cast(e));
			}
			response = failure(e);
		}
		return response;
	}

	@JSONExported
	public JsonResponse unlock( //
			@Parameter(value = ID) final Long cardId //
	) {
		userDataAccessLogic().unlockCard(cardId);
		return success();
	}

	@Admin
	@JSONExported
	public JsonResponse unlockAll() {
		userDataAccessLogic().unlockAllCards();
		return success();
	}

}
