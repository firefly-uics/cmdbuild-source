package org.cmdbuild.model.widget;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.cmdbuild.exception.WidgetException;
import org.cmdbuild.model.widget.service.ExternalService;
import org.cmdbuild.model.widget.service.soap.SoapService;
import org.cmdbuild.model.widget.service.soap.SoapService.SoapServiceBuilder;
import org.cmdbuild.model.widget.service.soap.exception.ConnectionException;
import org.cmdbuild.model.widget.service.soap.exception.WebServiceException;
import org.cmdbuild.workflow.CMActivityInstance;
import org.w3c.dom.Document;


public class WebService extends Widget {

	private final String SELECTED_NODE_KEY = "output";

	private String endPoint, method, nameSpacePrefix, nameSpaceURI, selectableNodeName;
	private Map<String, String> callParameters;

	private String outputName;

	protected class WebServiceAction implements WidgetAction {
		private Map<String, String> resolvedParams;

		public WebServiceAction(Map<String, String> resolvedParams) {
			this.resolvedParams = resolvedParams;
		}

		@Override
		public Object execute() throws Exception {

			SoapServiceBuilder builder = SoapService.newSoapService() //
				.withEndpointUrl(getEndPoint())
				.callingMethod(getMethod());

			if (!"".equals(getNameSpacePrefix())) {
				builder.withNamespacePrefix(getNameSpacePrefix()) //
				.withNamespaceUri(getNameSpaceURI());
			}

			if (!resolvedParams.isEmpty()) {
				builder.withParameters(resolvedParams);
			}

			ExternalService service = builder.build();

			Document response = null;
			try {
				response = service.invoke();
			} catch (WebServiceException e) {
				throw WidgetException.WidgetExceptionType.WIDGET_SERVICE_MALFORMED_REQUEST.createException(e.getMessage());
			} catch (ConnectionException e) {
				throw WidgetException.WidgetExceptionType.WIDGET_SERVICE_CONNECTION_ERROR.createException(e.getMessage());
			}

			return response;
		}
	}

	@Override
	public void save(final CMActivityInstance activityInstance, final Object input, final Map<String, Object> output)
			throws Exception {
		if (outputName != null) {
			@SuppressWarnings("unchecked")
			final Map<String, List<Object>> inputMap = (Map<String, List<Object>>) input;
			final List<Object> selectedNodes = inputMap.get(SELECTED_NODE_KEY);

			// cast to string the selected nodes
			final List<String> selectedNodesAsString = new LinkedList<String>();
			for (Object node : selectedNodes) {
				selectedNodesAsString.add((String) node);
			}

			output.put(outputName, selectedNodes.toArray());
		}
	}

	@Override
	public void accept(WidgetVisitor visitor) {
		visitor.visit(this);
	}

	public String getEndPoint() {
		return endPoint;
	}

	public void setEndPoint(String endPoint) {
		this.endPoint = endPoint;
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public Map<String, String> getCallParameters() {
		return callParameters;
	}

	public void setCallParameters(Map<String, String> callParameters) {
		this.callParameters = callParameters;
	}

	public String getNameSpacePrefix() {
		return nameSpacePrefix;
	}

	public void setNameSpacePrefix(String nameSpacePrefix) {
		this.nameSpacePrefix = nameSpacePrefix;
	}

	public String getNameSpaceURI() {
		return nameSpaceURI;
	}

	public void setNameSpaceURI(String nameSpaceURI) {
		this.nameSpaceURI = nameSpaceURI;
	}

	public String getOutputName() {
		return outputName;
	}

	public void setOutputName(String outputName) {
		this.outputName = outputName;
	}

	public String getSelectableNodeName() {
		return selectableNodeName;
	}

	public void setSelectableNodeName(String selectableNodeName) {
		this.selectableNodeName = selectableNodeName;
	}

	@Override
	protected WidgetAction getActionCommand(String action,
			Map<String, Object> params, Map<String, Object> dsVars) {

		// cast to string the objects in the map
		Map<String, String> stringParams = new HashMap<String, String>();
		for (String paramName: params.keySet()) {
			stringParams.put(paramName, (String) params.get(paramName));
		}

		return new WebServiceAction(stringParams);
	}

}
