package org.cmdbuild.workflow;

import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.logic.email.EmailLogic;
import org.cmdbuild.services.TemplateRepository;
import org.cmdbuild.workflow.widget.CalendarWidgetFactory;
import org.cmdbuild.workflow.widget.CreateModifyCardWidgetFactory;
import org.cmdbuild.workflow.widget.LinkCardsWidgetFactory;
import org.cmdbuild.workflow.widget.ManageEmailWidgetFactory;
import org.cmdbuild.workflow.widget.ManageRelationWidgetFactory;
import org.cmdbuild.workflow.widget.OpenAttachmentWidgetFactory;
import org.cmdbuild.workflow.widget.OpenNoteWidgetFactory;
import org.cmdbuild.workflow.widget.OpenReportWidgetFactory;
import org.cmdbuild.workflow.widget.WebServiceWidgetFactory;
import org.cmdbuild.workflow.xpdl.ValuePairXpdlExtendedAttributeWidgetFactory;

public class DefaultXpdlExtendedAttributeWidgetFactory extends ValuePairXpdlExtendedAttributeWidgetFactory {

	public DefaultXpdlExtendedAttributeWidgetFactory(final TemplateRepository templateRepository,
			final CMDataView dataView, final EmailLogic emailLogic) {
		addWidgetFactory(new CalendarWidgetFactory(templateRepository));
		addWidgetFactory(new CreateModifyCardWidgetFactory(templateRepository, dataView));
		addWidgetFactory(new LinkCardsWidgetFactory(templateRepository));
		addWidgetFactory(new ManageRelationWidgetFactory(templateRepository, dataView));
		addWidgetFactory(new ManageEmailWidgetFactory(templateRepository, emailLogic));
		addWidgetFactory(new OpenAttachmentWidgetFactory(templateRepository));
		addWidgetFactory(new OpenNoteWidgetFactory(templateRepository));
		addWidgetFactory(new OpenReportWidgetFactory(templateRepository));
		addWidgetFactory(new WebServiceWidgetFactory(templateRepository));
	}

}
