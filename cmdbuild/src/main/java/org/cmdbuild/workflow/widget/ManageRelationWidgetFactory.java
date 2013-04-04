package org.cmdbuild.workflow.widget;

import java.util.Map;

import org.apache.commons.lang.Validate;
import org.apache.cxf.common.util.StringUtils;
import org.cmdbuild.dao.reference.CardReference;
import org.cmdbuild.elements.interfaces.IDomain;
import org.cmdbuild.logic.DataAccessLogic;
import org.cmdbuild.model.widget.ManageRelation;
import org.cmdbuild.model.widget.Widget;
import org.cmdbuild.services.TemplateRepository;
import org.cmdbuild.services.auth.UserContext;

public class ManageRelationWidgetFactory extends ValuePairWidgetFactory {

	private final static String WIDGET_NAME = "manageRelation";

	public final static String DOMAIN = "DomainName";
	private final static String FUNCTIONS = "EnabledFunctions";
	public final static String CLASS_NAME = "ClassName";
	public final static String CARD_CQL_SELECTOR = "ObjId";
	public static final String OBJ_REF = "ObjRef";
	public final static String REQUIRED = "Required";
	public final static String IS_DIRECT = "IsDirect";
	
	private final DataAccessLogic dataAccessLogic;

	public ManageRelationWidgetFactory(final TemplateRepository templateRespository, final DataAccessLogic dataAccessLogic) {
		super(templateRespository);
		this.dataAccessLogic = dataAccessLogic;
	}

	@Override
	public String getWidgetName() {
		return WIDGET_NAME;
	}

	@Override
	protected Widget createWidget(final Map<String, Object> valueMap) {
		final String className;
		final ManageRelation widget = new ManageRelation(dataAccessLogic);

		widget.setOutputName(readString(valueMap.get(OUTPUT_KEY)));
		widget.setDomainName(readString(valueMap.get(DOMAIN)));
		
		if (valueMap.containsKey(OBJ_REF)) {
			className = configureWidgetFromReference(widget, valueMap);
		} else {
			className = configureWidgetFromClassName(widget, valueMap);
		}
		widget.setRequired(readBooleanTrueIfPresent(valueMap.get(REQUIRED)));
		setSource(widget, valueMap.get(IS_DIRECT));
		setEnabledFunctions(widget, readString(valueMap.get(FUNCTIONS)));

		configureWidgetDestinationClassName(widget, readString(valueMap.get(DOMAIN)), className);
		
		return widget;
	}

	private void configureWidgetDestinationClassName(final ManageRelation widget, final String domainName, final String className) {
		if (!StringUtils.isEmpty(domainName)) {
			final IDomain domain = UserContext.systemContext().domains().get(domainName);
			final String class1 = domain.getClass1().getName();
			final String class2 = domain.getClass2().getName();
			
			final String destinationClassName = class1.equals(className) ? class2 : class1;
			widget.setDestinationClassName(destinationClassName);
		} else {
			widget.setDestinationClassName(readString(null));
		}
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
	
	private String configureWidgetFromClassName(final ManageRelation widget, final Map<String, Object> valueMap) {
		final String className = readString(valueMap.get(CLASS_NAME));
		final String cardIdOrCql = readString(valueMap.get(CARD_CQL_SELECTOR));
		Validate.notEmpty(className, CLASS_NAME + " is required");

		widget.setClassName(className);
		widget.setObjId(cardIdOrCql);
		
		return className;
	}

	private String configureWidgetFromReference(final ManageRelation widget, final Map<String, Object> valueMap) {
		final CardReference objRef = (CardReference) valueMap.get(OBJ_REF);
		final String className = readString(objRef.getClassName());
		final String cardId = readString(objRef.getId().toString()); 
		
		widget.setClassName(className);
		widget.setObjId(cardId);
		
		return className;
	}
}
