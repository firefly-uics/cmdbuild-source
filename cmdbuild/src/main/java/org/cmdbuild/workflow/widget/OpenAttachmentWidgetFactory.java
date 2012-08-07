package org.cmdbuild.workflow.widget;

import java.util.Map;

import org.cmdbuild.model.widget.OpenAttachment;
import org.cmdbuild.model.widget.Widget;
import org.cmdbuild.services.TemplateRepository;

public class OpenAttachmentWidgetFactory extends ValuePairWidgetFactory {

	public OpenAttachmentWidgetFactory(final TemplateRepository templateRespository) {
		super(templateRespository);
	}

	@Override
	public String getWidgetName() {
		return "openAttachment";
	}

	@Override
	public Widget createWidget(final Map<String, Object> valuePairs) {
		return new OpenAttachment();
	}

}
