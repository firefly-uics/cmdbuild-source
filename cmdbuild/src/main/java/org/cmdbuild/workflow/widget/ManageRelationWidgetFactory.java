package org.cmdbuild.workflow.widget;

import java.util.Map;

import org.apache.commons.lang.Validate;
import org.cmdbuild.dao.reference.CardReference;
import org.cmdbuild.model.widget.ManageRelation;
import org.cmdbuild.model.widget.Widget;
import org.cmdbuild.services.TemplateRepository;

public class ManageRelationWidgetFactory extends ValuePairWidgetFactory {

	private final static String WIDGET_NAME = "manageRelation";

	public final static String DOMAIN = "DomainName";
	private final static String FUNCTIONS = "EnabledFunctions";
	public final static String CLASS_NAME = "ClassName";
	public final static String CARD_CQL_SELECTOR = "ObjId";
	public static final String OBJ_REF = "ObjRef";
	public final static String REQUIRED = "Required";
	public final static String IS_DIRECT = "IsDirect";

	public ManageRelationWidgetFactory(final TemplateRepository templateRespository) {
		super(templateRespository);
	}

	@Override
	public String getWidgetName() {
		return WIDGET_NAME;
	}

	@Override
	protected Widget createWidget(final Map<String, Object> valueMap) {
		final ManageRelation widget = new ManageRelation();

		widget.setDomainName(readString(valueMap.get(DOMAIN)));
		if (valueMap.containsKey(OBJ_REF)) {
			configureWidgetFromReference(widget, valueMap);
		} else {
			configureWidgetFromClassName(widget, valueMap);
		}
		widget.setRequired(readBooleanTrueIfPresent(valueMap.get(REQUIRED)));
		setSource(widget, valueMap.get(IS_DIRECT));
		setEnabledFunctions(widget, readString(valueMap.get(FUNCTIONS)));

		return widget;
	}

	private void setSource(final ManageRelation widget, final Object isDirect) {
		if (isDirect != null) {
			final String source = readBooleanTrueIfTrue(isDirect) ? "_1" : "_2";
			widget.setSource(source);
		}
	}

	private void setEnabledFunctions(final ManageRelation widget, final String functions) {
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

	private boolean isEnabled(final String functions, final int index) {
		boolean enabled = false;
		try {
			final char c = functions.charAt(index);
			enabled = c == '1';
		} catch (final IndexOutOfBoundsException e) {
			// ignore
		}

		return enabled;
	}
	
	private void configureWidgetFromClassName(final ManageRelation widget, final Map<String, Object> valueMap) {
		final String className = readString(valueMap.get(CLASS_NAME));
		final String cardIdOrCql = readString(valueMap.get(CARD_CQL_SELECTOR));
		Validate.notEmpty(className, CLASS_NAME + " is required");

		widget.setClassName(className);
		widget.setObjId(cardIdOrCql);
	}

	private void configureWidgetFromReference(final ManageRelation widget, final Map<String, Object> valueMap) {
		final CardReference objRef = (CardReference) valueMap.get(OBJ_REF);

		widget.setClassName(readString(objRef.getClassName()));
		widget.setObjId(readString(objRef.getId().toString()));
	}
}
