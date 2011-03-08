package org.cmdbuild.shark.toolagent;

import java.util.Map;

import org.apache.ws.security.handler.WSHandlerConstants;
import org.cmdbuild.services.soap.Private;
import org.cmdbuild.shark.util.ClientPasswordCallback;
import org.cmdbuild.shark.util.CmdbuildUtils;

public abstract class AbstractCmdbuildWSToolAgent extends AbstractWSToolAgent {

	protected final void addPerformerWSUserPassword(Private stub, Map<String, Object> outProps) throws Exception {
		String currentUsername = CmdbuildUtils.getCurrentUserNameForProcessInstance(stub, cmdbuildProcessClass, cmdbuildProcessId);
		String currentGroupname = CmdbuildUtils.getCurrentGroupName(shandle);
		StringBuffer wsUsername = new StringBuffer(cus.getProperty(AbstractWSToolAgent.USER_ATTRIBUTE));
		wsUsername.append("#").append(currentUsername);
		if (currentGroupname != null) {
			wsUsername.append("@").append(currentGroupname);
		}
		String password = cus.getProperty(AbstractWSToolAgent.PASSWORD_ATTRIBUTE);
		String username = wsUsername.toString();
		ClientPasswordCallback pwdCallback = new ClientPasswordCallback(username, password);
		outProps.put(WSHandlerConstants.USER, wsUsername.toString());
        outProps.put(WSHandlerConstants.PW_CALLBACK_REF, pwdCallback);
	}
}
