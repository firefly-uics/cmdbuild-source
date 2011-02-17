package org.cmdbuild.servlets.json.management;

import java.io.IOException;

import javax.mail.MessagingException;

import org.cmdbuild.elements.interfaces.ICard;
import org.cmdbuild.elements.interfaces.ITableFactory;
import org.cmdbuild.elements.interfaces.ProcessType;
import org.cmdbuild.elements.wrappers.EmailCard;
import org.cmdbuild.exception.CMDBException;
import org.cmdbuild.listeners.RequestListener;
import org.cmdbuild.services.email.EmailService;
import org.cmdbuild.servlets.json.JSONBase;
import org.cmdbuild.servlets.json.serializers.Serializer;
import org.cmdbuild.servlets.utils.Parameter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Email extends JSONBase {

	@JSONExported
	public JSONObject getEmailList(
			@Parameter("ProcessInstanceId") int processId,
			ITableFactory tf,
	        JSONObject output) throws MessagingException, IOException, JSONException {
		ICard processCard = tf.get(ProcessType.BaseTable).cards().get(processId);
		try {
			EmailService.syncEmail();
		} catch (CMDBException e) {
			RequestListener.getCurrentRequest().pushWarning(e);
		}
	    JSONArray jsonEmails = new JSONArray();
	    for (ICard email : EmailCard.list(processCard)) {
	    	jsonEmails.put(Serializer.serializeCard(email, true));
	    }
	    output.put("rows", jsonEmails);
	    return output;
	}
};
