package org.cmdbuild.services.soap.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cmdbuild.services.auth.UserContext;
import org.cmdbuild.services.soap.operation.ECard;
import org.cmdbuild.services.soap.types.Attribute;
import org.cmdbuild.services.soap.types.Card;

public class WorkflowUtils {
	
	private UserContext userCtx;

	public WorkflowUtils(UserContext userCtx) {
		this.userCtx = userCtx;
	}
	
	public String getProcessInstanceId(Card card) {
		ECard cardOperation = new ECard(userCtx);
		Attribute[] attributeListFilter = new Attribute[1];
		Attribute processCode = new Attribute();
		processCode.setName("ProcessCode");
		attributeListFilter[0] = processCode;
		Card actualProcessCard = cardOperation.getCard(card.getClassName(), card.getId(), attributeListFilter);
		List<Attribute> processAttributeList = actualProcessCard.getAttributeList();
		String processInstanceId = "";
		for (Attribute attribute : processAttributeList){
			if (attribute.getName().equalsIgnoreCase("ProcessCode"))
				processInstanceId = attribute.getValue();
		}
		return processInstanceId;
	}
	
	public Map<String, String> setCardSystemValues(Card card) {
		Map<String, String> params = new HashMap<String, String>();
		if (card.getId() > 0)
			params.put("Id", String.valueOf(card.getId()));
		if (card.getUser() != null && !card.equals(""))
			params.put("User", card.getUser());
		if (card.getBeginDate() != null && !card.equals(""))
			params.put("BeginDate", dateToString(card.getBeginDate()));
		if (card.getEndDate() != null && !card.equals(""))
			params.put("EndDate", dateToString(card.getEndDate()));
		if (card.getUser() != null && !card.equals(""))
			params.put("User", card.getUser());
		return params;
	}

	public String dateToString(Calendar beginDate) {
		String date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(beginDate.getTime());
		return date;
	}


}
