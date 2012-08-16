package org.cmdbuild.workflow.api;

import static org.apache.commons.lang.StringUtils.EMPTY;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.cmdbuild.workflow.Constants.CURRENT_USER_VARIABLE;

import org.cmdbuild.api.fluent.ws.WsFluentApiExecutor;
import org.cmdbuild.services.soap.Private;
import org.cmdbuild.services.soap.client.CmdbuildSoapClient.PasswordType;
import org.cmdbuild.services.soap.client.CmdbuildSoapClient.SoapClientBuilder;
import org.cmdbuild.services.soap.client.SoapClient;
import org.cmdbuild.workflow.type.ReferenceType;
import org.enhydra.shark.Shark;
import org.enhydra.shark.api.client.wfmc.wapi.WAPI;
import org.enhydra.shark.api.client.wfmc.wapi.WMAttribute;
import org.enhydra.shark.api.client.wfmc.wapi.WMSessionHandle;
import org.enhydra.shark.api.internal.working.CallbackUtilities;

public class SoapSharkWorkflowApiFactory implements SharkWorkflowApiFactory {

	private static class ProcessData {

		public final WMSessionHandle shandle;
		public final String procInstId;

		public ProcessData(final WMSessionHandle shandle, final String procInstId) {
			this.shandle = shandle;
			this.procInstId = procInstId;
		}

	}

	private static final String CMDBUILD_WS_URL_PROPERTY = "org.cmdbuild.ws.url";
	private static final String CMDBUILD_WS_USERNAME_PROPERTY = "org.cmdbuild.ws.username";
	private static final String CMDBUILD_WS_PASSWORD_PROPERTY = "org.cmdbuild.ws.password";

	private static final String URL_SEPARATOR = "/";
	private static final String URL_SUFFIX = "services/soap/Private";

	private static final String USER_SEPARATOR = "#";

	private CallbackUtilities cus;
	private ProcessData processData;

	@Override
	public void setup(final CallbackUtilities cus) {
		setup(cus, null);
	}

	@Override
	public void setup(final CallbackUtilities cus, final WMSessionHandle shandle, final String procInstId) {
		setup(cus, new ProcessData(shandle, procInstId));
	}

	private void setup(final CallbackUtilities cus, final ProcessData processData) {
		this.cus = cus;
		this.processData = processData;
	}

	@Override
	public WorkflowApi createWorkflowApi() {
		final Private proxy = proxy();
		final WsFluentApiExecutor executor = new WsFluentApiExecutor(proxy);
		final WorkflowApi workflowApi = new WorkflowApi(executor, proxy);
		return workflowApi;
	}

	private Private proxy() {
		final String url = completeUrl(cus.getProperty(CMDBUILD_WS_URL_PROPERTY));
		final String username = completeUsername(cus.getProperty(CMDBUILD_WS_USERNAME_PROPERTY),
				currentUserOrEmptyOnError());
		final String password = cus.getProperty(CMDBUILD_WS_PASSWORD_PROPERTY);

		final SoapClient<Private> soapClient = new SoapClientBuilder<Private>() //
				.forClass(Private.class) //
				.withUrl(url) //
				.withUsername(username) //
				.withPasswordType(PasswordType.DIGEST) //
				.withPassword(password) //
				.build();
		return soapClient.getProxy();
	}

	private String completeUrl(final String baseUrl) {
		return new StringBuilder(baseUrl) //
				.append(baseUrl.endsWith(URL_SEPARATOR) ? EMPTY : URL_SEPARATOR) //
				.append(URL_SUFFIX) //
				.toString();
	}

	private String completeUsername(final String wsUsername, final String currentUser) {
		return new StringBuilder(wsUsername) //
				.append(isNotBlank(currentUser) ? USER_SEPARATOR + currentUser : EMPTY) //
				.toString();
	}

	private String currentUserOrEmptyOnError() {
		if (processData != null) {
			return EMPTY;
		}

		try {
			final WMAttribute attribute = wapi().getProcessInstanceAttributeValue(processData.shandle,
					processData.procInstId, CURRENT_USER_VARIABLE);
			final Object value = attribute.getValue();
			final ReferenceType userReference = ReferenceType.class.cast(value);
			return userReference.getDescription();
		} catch (final Throwable e) {
			return EMPTY;
		}
	}

	private WAPI wapi() throws Exception {
		return Shark.getInstance().getWAPIConnection();
	}

}
