package org.cmdbuild.shark.toolagent;

import java.util.List;

import org.cmdbuild.shark.util.CmdbuildUtils;
import org.cmdbuild.workflow.type.ReferenceType;
import org.enhydra.shark.api.internal.toolagent.AppParameter;
import org.cmdbuild.services.soap.Card;
import org.cmdbuild.services.soap.Filter;
import org.cmdbuild.services.soap.Private;
import org.cmdbuild.services.soap.Query;

public class GetCurrentUserReferenceToolAgent extends AbstractCmdbuildWSToolAgent {

	private final static int OutParamCurrentUser = 1;

	private final static String USERCLASSNAME = "User";
	private final static String USER_USERNAME_ATTRIBUTE = "Username";

	@Override
	protected void invokeWebService(final Private stub, final AppParameter[] params, final String toolInfoID)
			throws Exception {
		final String currentUserName = getCurrentUserName(stub);
		final Card userCard = getcurrentUserCard(stub, currentUserName);
		final ReferenceType userReference = CmdbuildUtils.createReferenceFromWSCard(userCard);

		setOutputParameter(params, OutParamCurrentUser, userReference);
	}

	private String getCurrentUserName(final Private stub) throws Exception {
		return CmdbuildUtils.getCurrentUserNameForProcessInstance(stub, cmdbuildProcessClass, cmdbuildProcessId);
	}

	private Card getcurrentUserCard(final Private stub, final String currentUserName) throws Exception {
		final List<Card> userCardList = stub.getCardList(USERCLASSNAME, null, buildUserNameQuery(currentUserName),
				null, null, null, null, null).getCards();
		if (userCardList != null) {
			return userCardList.get(0);
		} else {
			return null;
		}
	}

	private Query buildUserNameQuery(final String currentUserName) {
		final Query query = new Query();
		final Filter filter = new Filter();
		filter.setName(USER_USERNAME_ATTRIBUTE);
		filter.getValue().add(currentUserName);
		filter.setOperator("EQUALS");
		query.setFilter(filter);
		return query;
	}

	private void setOutputParameter(final AppParameter[] params, final int index, final Object value) {
		params[index].the_value = value;
	}
}
