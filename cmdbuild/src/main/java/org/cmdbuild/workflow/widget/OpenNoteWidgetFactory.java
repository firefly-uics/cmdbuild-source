package org.cmdbuild.workflow.widget;

import java.util.Map;

import org.cmdbuild.model.widget.OpenNote;
import org.cmdbuild.model.widget.Widget;

public class OpenNoteWidgetFactory extends ValuePairWidgetFactory {

	@Override
	public String getWidgetName() {
		return "openNote";
	}

	@Override
	public Widget createWidget(final Map<String, String> valuePairs) {
		return new OpenNote();
	}

}
