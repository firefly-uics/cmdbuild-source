package org.cmdbuild.workflow.widget;

import java.util.Map;

import org.apache.commons.lang.Validate;
import org.cmdbuild.dao.reference.CardReference;
import org.cmdbuild.logic.data.DataAccessLogic;
import org.cmdbuild.model.widget.CreateModifyCard;
import org.cmdbuild.model.widget.Widget;
import org.cmdbuild.services.TemplateRepository;

public class CreateModifyCardWidgetFactory extends ValuePairWidgetFactory {

	private static final String WIDGET_NAME = "createModifyCard";

	public static final String OBJ_REF = "Reference";
	public static final String CLASS_NAME = "ClassName";
	public static final String OBJ_ID = "ObjId";
	public static final String READONLY = "ReadOnly";

	private final DataAccessLogic dataAccessLogic;

	public CreateModifyCardWidgetFactory(final TemplateRepository templateRespository,
			final DataAccessLogic dataAccessLogic) {
		super(templateRespository);
		this.dataAccessLogic = dataAccessLogic;
	}

	@Override
	public String getWidgetName() {
		return WIDGET_NAME;
	}

	@Override
	protected Widget createWidget(final Map<String, Object> valueMap) {
		final CreateModifyCard widget = new CreateModifyCard(dataAccessLogic);
		if (valueMap.containsKey(OBJ_REF)) {
			configureWidgetFromReference(widget, valueMap);
		} else {
			configureWidgetFromClassName(widget, valueMap);
		}
		widget.setReadonly(readBooleanTrueIfPresent(valueMap.get(READONLY)));
		widget.setOutputName(readString(valueMap.get(OUTPUT_KEY)));

		return widget;
	}

	private void configureWidgetFromClassName(final CreateModifyCard widget, final Map<String, Object> valueMap) {
		final String className = readString(valueMap.get(CLASS_NAME));
		final String cardIdOrCql = readString(valueMap.get(OBJ_ID));
		Validate.notEmpty(className, CLASS_NAME + " is required");

		widget.setTargetClass(className);
		widget.setIdcardcqlselector(cardIdOrCql);
	}

	private void configureWidgetFromReference(final CreateModifyCard widget, final Map<String, Object> valueMap) {
		final CardReference objRef = (CardReference) valueMap.get(OBJ_REF);

		widget.setTargetClass(objRef.getClassName());
		widget.setIdcardcqlselector(objRef.getId().toString());
	}
}