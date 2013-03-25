package org.cmdbuild.workflow.widget;

import java.util.Map;

import org.cmdbuild.model.widget.WebService;
import org.cmdbuild.model.widget.Widget;
import org.cmdbuild.services.TemplateRepository;

public class WebServiceWidgetFactory extends ValuePairWidgetFactory {

	private static final String WIDGET_NAME = "webService";
	private static final String ENDPOINT = "EndPoint";
	private static final String METHOD = "Method";
	private static final String NS_PREFIX = "NameSpacePrefix";
	private static final String NS_URI = "NameSpaceURI";
	private static final String SELECTABLE_NODE_NAME = "SelectableNodeName";

	public WebServiceWidgetFactory(TemplateRepository templateRespository) {
		super(templateRespository);
	}

	@Override
	public String getWidgetName() {
		return WIDGET_NAME;
	}

	@Override
	protected Widget createWidget(Map<String, Object> valueMap) {
		WebService webService = new WebService();
		webService.setEndPoint(readString(valueMap.get(ENDPOINT)));
		webService.setMethod(readString(valueMap.get(METHOD)));
		webService.setNameSpacePrefix(readString(valueMap.get(NS_PREFIX)));
		webService.setNameSpaceURI(readString(valueMap.get(NS_URI)));
		webService.setCallParameters(extractUnmanagedStringParameters(valueMap, BUTTON_LABEL, ENDPOINT, METHOD, NS_PREFIX, NS_URI, SELECTABLE_NODE_NAME));
		webService.setOutputName(readString(valueMap.get(OUTPUT_KEY)));
		webService.setSelectableNodeName(readString(valueMap.get(SELECTABLE_NODE_NAME)));

		return webService;
	}

}
