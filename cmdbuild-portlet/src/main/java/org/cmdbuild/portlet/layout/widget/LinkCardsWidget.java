package org.cmdbuild.portlet.layout.widget;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.cmdbuild.portlet.configuration.LinkCardItem;
import org.cmdbuild.portlet.operation.RequestParams;
import org.cmdbuild.portlet.operation.ServletOperation;
import org.cmdbuild.services.soap.WorkflowWidgetDefinition;
import org.cmdbuild.services.soap.WorkflowWidgetDefinitionParameter;
import org.cmdbuild.services.soap.WorkflowWidgetSubmission;
import org.cmdbuild.services.soap.WorkflowWidgetSubmissionParameter;

public class LinkCardsWidget extends WorkflowWidget {

	private enum Params {

		ClassName {

			@Override
			public void handleParam(final LinkCardsWidget w, final String value) {
				w.classname = value;
			}
		},
		SingleSelect {

			@Override
			public void handleParam(final LinkCardsWidget w, final String value) {
				w.singleSelect = Integer.parseInt(value);
			}
		},
		NoSelect {

			@Override
			public void handleParam(final LinkCardsWidget w, final String value) {
				w.noSelect = Integer.parseInt(value);
			}
		},
		Required {

			@Override
			public void handleParam(final LinkCardsWidget w, final String value) {
				w.required = Integer.valueOf(value);
			}
		},
		Filter {

			@Override
			public void handleParam(final LinkCardsWidget w, final String value) {
				w.filter = value;
			}
		},
		DefaultSelection {
			@Override
			public void handleParam(final LinkCardsWidget w, final String value) {
				w.defaultValues = value;
			}
		},
		outputName {

			@Override
			public void handleParam(final LinkCardsWidget w, final String value) {
				w.outputName = value;
			}
		};

		abstract public void handleParam(LinkCardsWidget w, String value);
	}

	private String classname;
	private int singleSelect;
	private int noSelect;
	private int required;
	private String defaultValues;
	private String filter;
	private String outputName;
	private final List<String> values = new ArrayList<String>();
	private static final String ACTION = "action";
	private static final String ID = "id";
	private static final String STOREINSESSION = "add";
	private static final String REMOVEFROMSESSION = "remove";

	public LinkCardsWidget(final WorkflowWidgetDefinition definition) {
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
	public boolean isRequired() {
		if (required > 0) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public String generateHtml(final HttpServletRequest request) {
		final ServletOperation operations = new ServletOperation();
		operations.emptySession(request);
		final StringBuffer layout = new StringBuffer();
		final HttpSession session = request.getSession();
		final LinkCardItem item = getLinkCardItem();
		session.setAttribute(identifier, item);
		layout.append("<div id=\"").append("form_" + identifier).append("\" class=\"CMDBuildProcessContainer\">");
		layout.append("<form enctype=\"multipart/form-data\" class=\"CMDBuildWorkflowWidgetForm\">");
		appendHiddenIdentifier(layout);
		layout.append("<div class=\"CMDBuildLinkCardGridContainer\">");
		layout.append("<table class=\"CMDBuildLinkCardGrid\"></table>");
		layout.append("<div id=\"item_").append(identifier).append("\"></div>");
		if (defaultValues != null) {
			layout.append("<div id=\"defaultSelection_").append(identifier).append("\" style=\"display: none\">")
					.append(defaultValues).append("</div>");
		}
		if (filter != null) {
			layout.append("<div id=\"filter_").append(identifier).append("\" style=\"display: none\">").append(filter)
					.append("</div>");
		}
		layout.append("</div>");
		layout.append("</form>");
		layout.append("</div>");
		return layout.toString();
	}

	@Override
	public void manageWidgetSubmission(final HttpServletRequest request, final RequestParams params) {
		final String action = request.getParameter(ACTION);
		if (action.equals(STOREINSESSION)) {
			final Enumeration parameters = request.getParameterNames();
			while (parameters.hasMoreElements()) {
				final String paramName = parameters.nextElement().toString();
				if (paramName.equals(ID)) {
					values.add(request.getParameter(ID));
				}
			}
		} else if (action.equals(REMOVEFROMSESSION)) {
			values.remove(request.getParameter(ID));
		}
	}

	@Override
	public WorkflowWidgetSubmission createSubmissionObject() {
		final WorkflowWidgetSubmission submission = new WorkflowWidgetSubmission();
		submission.setIdentifier(identifier);
		final WorkflowWidgetSubmissionParameter parameter = new WorkflowWidgetSubmissionParameter();
		parameter.setKey(outputName);
		for (final String value : values) {
			parameter.getValues().add(value);
		}
		submission.getParameters().add(parameter);
		return submission;
	}

	private LinkCardItem getLinkCardItem() {
		final LinkCardItem item = new LinkCardItem();
		item.setClassname(classname);
		item.setFilter(filter);
		item.setIdentifier(identifier);
		item.setLabel(buttonLabel);
		item.setNoSelect(noSelect);
		item.setRequired(required);
		item.setSingleSelect(singleSelect);
		return item;
	}
}
