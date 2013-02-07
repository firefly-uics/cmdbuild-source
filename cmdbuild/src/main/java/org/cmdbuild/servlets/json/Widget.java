package org.cmdbuild.servlets.json;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cmdbuild.elements.interfaces.ICard;
import org.cmdbuild.elements.interfaces.ITable;
import org.cmdbuild.logic.TemporaryObjectsBeforeSpringDI;
import org.cmdbuild.logic.WorkflowLogic;
import org.cmdbuild.services.auth.UserContext;
import org.cmdbuild.services.store.DBClassWidgetStore;
import org.cmdbuild.servlets.json.management.JsonResponse;
import org.cmdbuild.servlets.utils.Parameter;
import org.cmdbuild.workflow.CMActivity;
import org.cmdbuild.workflow.CMActivityInstance;
import org.cmdbuild.workflow.CMActivityWidget;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

public class Widget extends JSONBase {

	@JSONExported
	public JsonResponse callWidget(@Parameter("id") final int cardId, @Parameter("className") final String className,
			@Parameter("widgetId") final String widgetId,
			@Parameter(required = false, value = "action") final String action,
			@Parameter(required = false, value = "params") final String jsonParams) throws Exception {

		final Map<String, Object> params = readParams(jsonParams);

		final ITable entryType = buildTable(className);
		final ICard card = buildCard(className, cardId);
		final DBClassWidgetStore classWidgets = new DBClassWidgetStore(entryType);

		return JsonResponse.success(classWidgets.executeAction(widgetId, action, params, card));
	}

	@JSONExported
	public JsonResponse callWidget(final UserContext userCtx, @Parameter("id") final int processCardId,
			@Parameter("className") final String className, @Parameter("activityId") final String activityInstanceId,
			@Parameter("widgetId") final String widgetId,
			@Parameter(required = false, value = "action") final String action,
			@Parameter(required = false, value = "params") final String jsonParams) throws Exception {

		final Map<String, Object> params = readParams(jsonParams);

		Object response = null;
		final ITable entryType = buildTable(className);
		final WorkflowLogic logic = TemporaryObjectsBeforeSpringDI.getWorkflowLogic();

		final List<CMActivityWidget> widgets;

		if (processCardId > 0) {
			final CMActivityInstance activityInstance = logic.getActivityInstance(className, new Long(processCardId),
					activityInstanceId);
			widgets = activityInstance.getWidgets();
		} else {
			// For a new process, there isn't activity instances. So retrieve
			// the start activity
			// and look for them widgets
			final CMActivity activity = logic.getStartActivity(className);
			widgets = activity.getWidgets();
		}

		for (final CMActivityWidget widget : widgets) {
			if (widget.getId().equals(widgetId)) {
				response = widget.executeAction(action, params, null); // TODO I
																		// don't
																		// know
																		// WTF
																		// pass
																		// instead
																		// of
																		// null,
				// something for the server side TemplateResolver
			}
		}

		return JsonResponse.success(response);
	}

	private Map<String, Object> readParams(final String jsonParams) throws IOException, JsonParseException,
			JsonMappingException {
		final Map<String, Object> params;

		if (jsonParams == null) {
			params = new HashMap<String, Object>();
		} else {
			final ObjectMapper mapper = new ObjectMapper();
			params = new ObjectMapper().readValue(jsonParams,
					mapper.getTypeFactory().constructMapType(HashMap.class, String.class, Object.class));
		}
		return params;
	}

}
