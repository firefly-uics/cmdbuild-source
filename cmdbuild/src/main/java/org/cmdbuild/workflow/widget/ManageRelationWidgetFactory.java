package org.cmdbuild.workflow.widget;

import java.util.Map;

import org.cmdbuild.model.widget.ManageRelation;
import org.cmdbuild.model.widget.Widget;

public class ManageRelationWidgetFactory extends ValuePairWidgetFactory {

	private final static String WIDGET_NAME = "manageRelation";
	private final static String DOMAIN = "DomainName";
	private final static String FUNCTIONS = "EnabledFunctions";
	private final static String CLASS_NAME = "ClassName";
	private final static String CARD_CQL_SELECTOR = "ObjId";
	private final static String REQUIRED = "Required";
	private final static String IS_DIRECT = "IsDirect";

	@Override
	public String getWidgetName() {
		return WIDGET_NAME; 
	}

	@Override
	protected Widget createWidget(Map<String, String> valueMap) {
		ManageRelation widget = new ManageRelation();

		widget.setDomainName(valueMap.get(DOMAIN));
		widget.setClassName(valueMap.get(CLASS_NAME));
		widget.setObjId(valueMap.get(CARD_CQL_SELECTOR));
		widget.setRequired(readBooleanTrueIfPresent(valueMap.get(REQUIRED)));
		setSource(widget, valueMap.get(IS_DIRECT));
		setEnabledFunctions(widget, valueMap.get(FUNCTIONS));

		return widget;
	}

	private void setSource(ManageRelation widget, String isDirect) {
		if (isDirect != null) {
			final String source = readBooleanTrueIfTrue(isDirect) ? "_1" : "_2";
			widget.setSource(source);
		}
	}

	private void setEnabledFunctions(ManageRelation widget, String functions) {
		if (functions == null) {
			return;
		} else {
			widget.setCanCreateRelation(isEnabled(functions, 0));
			widget.setCanCreateAndLinkCard(isEnabled(functions, 1));
			widget.setMultiSelection(isEnabled(functions, 2));
			widget.setSingleSelection(isEnabled(functions, 3));
			widget.setCanModifyARelation(isEnabled(functions, 4));
			widget.setCanRemoveARelation(isEnabled(functions, 5));
			widget.setCanModifyALinkedCard(isEnabled(functions, 6));
			widget.setCanRemoveALinkedCard(isEnabled(functions, 7));
		}
	}

	private boolean isEnabled(String functions, int index) {
		String s = functions.trim();
		boolean enabled = false;
		try {
			char c = functions.charAt(index);
			enabled = c == '1';
		} catch (IndexOutOfBoundsException e) {
			// ignore
		}

		return enabled;
	}
}
