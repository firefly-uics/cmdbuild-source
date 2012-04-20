package org.cmdbuild.logic;

import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.logic.LogicDTO.Card;
import org.cmdbuild.logic.LogicDTO.DomainWithSource;
import org.cmdbuild.logic.commands.GetRelationHistory;
import org.cmdbuild.logic.commands.GetRelationHistory.GetRelationHistoryResponse;
import org.cmdbuild.logic.commands.GetRelationList;
import org.cmdbuild.logic.commands.GetRelationList.GetRelationListResponse;
import org.cmdbuild.services.auth.UserContext;

/**
 * Business Logic Layer for Data Access
 */
public class DataAccessLogic {

	private final CMDataView view;

	// FIXME Temporary constructor before switching to Spring DI
	public DataAccessLogic(final UserContext userCtx) {
		view = TemporaryObjectsBeforeSpringDI.getUserContextView(userCtx);
	}

	public DataAccessLogic(final CMDataView view) {
		this.view = view;
	}

	public GetRelationListResponse getRelationList(final Card srcCard, final DomainWithSource dom) {
		return new GetRelationList(view).exec(srcCard, dom);
	}

	public GetRelationHistoryResponse getRelationHistory(final Card srcCard) {
		return new GetRelationHistory(view).exec(srcCard);
	}
}
