package org.cmdbuild.portlet.layout.widget;

import javax.servlet.http.HttpServletRequest;

import org.cmdbuild.portlet.configuration.CardConfiguration;
import org.cmdbuild.portlet.layout.FormSerializer;
import org.cmdbuild.portlet.operation.CardOperation;
import org.cmdbuild.portlet.operation.RequestParams;
import org.cmdbuild.portlet.operation.ServletOperation;
import org.cmdbuild.portlet.utils.CardUtils;
import org.cmdbuild.portlet.ws.SOAPClient;
import org.cmdbuild.services.soap.Card;
import org.cmdbuild.services.soap.WorkflowWidgetDefinition;
import org.cmdbuild.services.soap.WorkflowWidgetDefinitionParameter;
import org.cmdbuild.services.soap.WorkflowWidgetSubmission;
import org.cmdbuild.services.soap.WorkflowWidgetSubmissionParameter;

public class CreateModifyCardWidget extends WorkflowWidget {

	private enum Params {

		ClassName {

			@Override
			public void handleParam(final CreateModifyCardWidget w, final String value) {
				w.classname = value;
			}
		},
		ReadOnly {

			@Override
			public void handleParam(final CreateModifyCardWidget w, final String value) {
				w.readonly = Boolean.parseBoolean(value);
			}
		},
		Reference {

			@Override
			public void handleParam(final CreateModifyCardWidget w, final String value) {
				w.reference = value;
			}
		},
		id {

			@Override
			public void handleParam(final CreateModifyCardWidget w, final String value) {
				w.id = Integer.valueOf(value);
			}
		},
		outputName {

			@Override
			public void handleParam(final CreateModifyCardWidget w, final String value) {
				w.outputName = value;
			}
		};

		abstract public void handleParam(CreateModifyCardWidget w, String value);
	}

	private String classname;
	private boolean readonly;
	private int cardid = -1;
	private int id;
	private String reference;
	private String outputName;

	public CreateModifyCardWidget(final WorkflowWidgetDefinition definition) {
		super(definition);
		for (final WorkflowWidgetDefinitionParameter parameter : definition.getParameters()) {
			try {
				final Params currentParam = Params.valueOf(parameter.getKey());
				currentParam.handleParam(this, parameter.getValue());
			} catch (final Exception e) {
			}
		}
	}

	@Override
	public String generateHtml(final HttpServletRequest request) {
		final ServletOperation operations = new ServletOperation();
		operations.emptySession(request);
		final SOAPClient client = operations.getClient(request.getSession());
		final StringBuffer layout = new StringBuffer();
		final CardUtils utils = new CardUtils();
		final CardConfiguration config = utils.getCardConfiguration(request);
		final FormSerializer form = new FormSerializer(request.getContextPath());
		String formContent = "";
		if (!"".equals(reference) && id > 0) {
			config.setClassname(classname);
			config.setId(id);
			formContent = form.generateCompiledCardLayout(client, config, readonly).toString();
		} else {
			formContent = form.generateEmptyCardLayout(client, config, classname, readonly).toString();
		}
		layout.append("<div id=\"").append(classname).append("\" class=\"CMDBuildProcessContainer\">");
		layout.append("<form enctype=\"multipart/form-data\" class=\"CMDBuildWorkflowWidgetForm\">");
		appendHiddenIdentifier(layout);
		layout.append(formContent);
		layout.append("</form>");
		layout.append("</div>");
		return layout.toString();
	}

	@Override
	public void manageWidgetSubmission(final HttpServletRequest request, final RequestParams params) {
		final ServletOperation operations = new ServletOperation();
		final SOAPClient client = operations.getClient(request.getSession());
		final Card card = operations.prepareCard(request);
		card.setClassName(classname);
		final CardOperation operation = new CardOperation(client);
		cardid = operation.createCard(card);
	}

	@Override
	public WorkflowWidgetSubmission createSubmissionObject() {
		if (cardid > 0) {
			final WorkflowWidgetSubmission submission = new WorkflowWidgetSubmission();
			submission.setIdentifier(identifier);
			final WorkflowWidgetSubmissionParameter parameter = new WorkflowWidgetSubmissionParameter();
			parameter.setKey(outputName);
			parameter.getValues().add(String.valueOf(cardid));
			submission.getParameters().add(parameter);
			return submission;
		} else {
			return null;
		}
	}
}
