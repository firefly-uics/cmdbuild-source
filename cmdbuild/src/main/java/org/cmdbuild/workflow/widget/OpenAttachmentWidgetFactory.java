package org.cmdbuild.workflow.widget;

import java.util.Map;

import org.cmdbuild.model.widget.OpenAttachment;
import org.cmdbuild.model.widget.Widget;

public class OpenAttachmentWidgetFactory extends ValuePairWidgetFactory {

	@Override
	public String getWidgetName() {
		return "openAttachment";
	}

	@Override
	public Widget createWidget(final Map<String, String> valuePairs) {
		return new OpenAttachment();
	}

}
