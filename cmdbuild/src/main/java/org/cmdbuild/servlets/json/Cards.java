package org.cmdbuild.servlets.json;

import static org.cmdbuild.services.json.dto.JsonResponse.success;
import static org.cmdbuild.servlets.json.CommunicationConstants.ID;

import org.cmdbuild.services.json.dto.JsonResponse;
import org.cmdbuild.servlets.utils.Parameter;

public class Cards extends JSONBaseWithSpringContext {

	@JSONExported
	public JsonResponse lock( //
			@Parameter(value = ID) final Long cardId //
	) {
		lockLogic().lockCard(cardId);
		return success();
	}

	@JSONExported
	public JsonResponse unlock( //
			@Parameter(value = ID) final Long cardId //
	) {
		lockLogic().unlockCard(cardId);
		return success();
	}

	@Admin
	@JSONExported
	public JsonResponse unlockAll() {
		lockLogic().unlockAllCards();
		return success();
	}

}
