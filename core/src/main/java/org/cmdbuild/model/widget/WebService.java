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

	private String endPoint, method, nameSpacePrefix, nameSpaceURI, outputName;
	private String[] nodesToUseAsRows, nodesToUseAsColumns;
	private boolean readOnly, mandatory, singleSelect;
	private Map<String, String> callParameters;

	protected class WebServiceAction implements WidgetAction {
		private final Map<String, String> resolvedParams;

		public WebServiceAction(final Map<String, String> resolvedParams) {
			this.resolvedParams = resolvedParams;
		}

		@Override
		public Object execute() throws Exception {

			final SoapServiceBuilder builder = SoapService.newSoapService() //
					.withEndpointUrl(getEndPoint()).callingMethod(getMethod());

			if (!"".equals(getNameSpacePrefix())) {
				builder.withNamespacePrefix(getNameSpacePrefix()) //
						.withNamespaceUri(getNameSpaceURI());
			}

			if (!resolvedParams.isEmpty()) {
				builder.withParameters(resolvedParams);
			}

			final ExternalService service = builder.build();

			Document response = null;
			try {
				response = service.invoke();
			} catch (final WebServiceException e) {
				throw WidgetException.WidgetExceptionType.WIDGET_SERVICE_MALFORMED_REQUEST.createException(e
						.getMessage());
			} catch (final ConnectionException e) {
				throw WidgetException.WidgetExceptionType.WIDGET_SERVICE_CONNECTION_ERROR.createException(e
						.getMessage());
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
			for (final Object node : selectedNodes) {
				selectedNodesAsString.add((String) node);
			}

			output.put(outputName, selectedNodes.toArray());
		}
	}

	@Override
	public void accept(final WidgetVisitor visitor) {
		visitor.visit(this);
	}

	public String getEndPoint() {
		return endPoint;
	}

	public void setEndPoint(final String endPoint) {
		this.endPoint = endPoint;
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(final String method) {
		this.method = method;
	}

	public Map<String, String> getCallParameters() {
		return callParameters;
	}

	public void setCallParameters(final Map<String, String> callParameters) {
		this.callParameters = callParameters;
	}

	public String getNameSpacePrefix() {
		return nameSpacePrefix;
	}

	public void setNameSpacePrefix(final String nameSpacePrefix) {
		this.nameSpacePrefix = nameSpacePrefix;
	}

	public String getNameSpaceURI() {
		return nameSpaceURI;
	}

	public void setNameSpaceURI(final String nameSpaceURI) {
		this.nameSpaceURI = nameSpaceURI;
	}

	public String getOutputName() {
		return outputName;
	}

	public void setOutputName(final String outputName) {
		this.outputName = outputName;
	}

	public String[] getNodesToUseAsRows() {
		return nodesToUseAsRows;
	}

	public void setNodesToUseAsRows(final String[] nodesToUseAsRows) {
		this.nodesToUseAsRows = nodesToUseAsRows;
	}

	public String[] getNodesToUseAsColumns() {
		return nodesToUseAsColumns;
	}

	public void setNodesToUseAsColumns(final String[] nodesToUseAsColumns) {
		this.nodesToUseAsColumns = nodesToUseAsColumns;
	}

	public boolean isReadOnly() {
		return readOnly;
	}

	public void setReadOnly(final boolean readOnly) {
		this.readOnly = readOnly;
	}

	public boolean isMandatory() {
		return mandatory;
	}

	public void setMandatory(final boolean mandatory) {
		this.mandatory = mandatory;
	}

	public boolean isSingleSelect() {
		return singleSelect;
	}

	public void setSingleSelect(final boolean singleSelect) {
		this.singleSelect = singleSelect;
	}

	@Override
	protected WidgetAction getActionCommand(final String action, final Map<String, Object> params,
			final Map<String, Object> dsVars) {

		// cast to string the objects in the map
		final Map<String, String> stringParams = new HashMap<String, String>();
		for (final String paramName : params.keySet()) {
			stringParams.put(paramName, (String) params.get(paramName));
		}

		return new WebServiceAction(stringParams);
	}

}
