package org.cmdbuild.servlets.json.serializers;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.cmdbuild.logic.translation.DefaultTranslationLogic.DESCRIPTION_FOR_CLIENT;
import static org.cmdbuild.servlets.json.CommunicationConstants.DEFAULT_DESCRIPTION;
import static org.cmdbuild.servlets.json.CommunicationConstants.DESCRIPTION;
import static org.cmdbuild.servlets.json.CommunicationConstants.GROUPS;
import static org.cmdbuild.servlets.json.CommunicationConstants.ID;
import static org.cmdbuild.servlets.json.CommunicationConstants.QUERY;
import static org.cmdbuild.servlets.json.CommunicationConstants.TITLE;
import static org.cmdbuild.servlets.json.CommunicationConstants.TYPE;

import org.cmdbuild.logic.translation.ReportTranslation;
import org.cmdbuild.logic.translation.TranslationFacade;
import org.cmdbuild.model.Report;
import org.json.JSONException;
import org.json.JSONObject;

public class ReportSerializer {
	
	private final TranslationFacade translationFacade;

	public ReportSerializer(final TranslationFacade translationFacade) {
		this.translationFacade = translationFacade;
	}
	
	public JSONObject toClient(final Report report) throws JSONException {
		
		final ReportTranslation translationObject = ReportTranslation.newInstance() //
				.withField(DESCRIPTION_FOR_CLIENT) //
				.withName(report.getCode()) //
				.build();
		String translatedDescription = translationFacade.read(translationObject);
		
		JSONObject serializer = new JSONObject();
		serializer.put(ID, report.getId());
		serializer.put(TITLE, report.getCode());
		serializer.put(DESCRIPTION, defaultIfNull(translatedDescription,report.getDescription()));
		serializer.put(DEFAULT_DESCRIPTION, report.getDescription());
		serializer.put(TYPE, report.getType());
		serializer.put(QUERY, report.getQuery());
		serializer.put(GROUPS, report.getGroups());
		return serializer;
	}
}
