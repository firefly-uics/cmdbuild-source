package org.cmdbuild.workflow.widget;

import java.util.Map;

import org.apache.commons.lang.Validate;
import org.cmdbuild.logic.DataAccessLogic;
import org.cmdbuild.model.widget.CreateModifyCard;
import org.cmdbuild.model.widget.Widget;

public class CreateModifyCardWidgetFactory extends ValuePairWidgetFactory {
	
	private static final String WIDGET_NAME = "createModifyCard";
	private static final String OBJ_REF = "Reference";
	private static final String CLASS_NAME = "ClassName";
	private static final String OBJ_ID = "ObjId";
	private static final String READONLY = "ReadOnly";

	private final DataAccessLogic dataAccessLogic;

	public CreateModifyCardWidgetFactory(final DataAccessLogic dataAccessLogic) {
		this.dataAccessLogic = dataAccessLogic;
	}

	@Override
	public String getWidgetName() {
		return WIDGET_NAME;
	}

	@Override
	protected Widget createWidget(final Map<String, Object> valueMap) {
		CreateModifyCard widget = new CreateModifyCard(dataAccessLogic);
		if (valueMap.containsKey(OBJ_REF)) {
			configureWidgetFromReference(widget, valueMap);
		} else {
			configureWidgetFromClassName(widget, valueMap);
		}
		widget.setOutputName(readString(valueMap.get(OUTPUT_KEY)));

		return widget;
	}

	private void configureWidgetFromClassName(CreateModifyCard widget, final Map<String, Object> valueMap) {
		final String className = readString(valueMap.get(CLASS_NAME));
		Validate.notEmpty(className, CLASS_NAME + " is required");

		widget.setTargetClass(className);
		widget.setIdcardcqlselector(readString(valueMap.get(OBJ_ID)));
		widget.setReadonly(readBooleanTrueIfPresent(valueMap.get(READONLY)));
	}

	private void configureWidgetFromReference(CreateModifyCard widget, final Map<String, Object> valueMap) {
		throw new UnsupportedOperationException("Not implemented yet");
	}
}