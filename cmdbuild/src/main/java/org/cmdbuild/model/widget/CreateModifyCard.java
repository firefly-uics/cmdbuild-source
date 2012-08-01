package org.cmdbuild.model.widget;

import java.util.Map;

import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.reference.CardReference;
import org.cmdbuild.logic.DataAccessLogic;
import org.cmdbuild.workflow.CMActivityInstance;


public class CreateModifyCard extends Widget {

	public static final String CREATED_CARD_ID_SUBMISSION_PARAM = "output";

	private String idcardcqlselector;
	private String targetClass;
	private boolean readonly;

	private final DataAccessLogic dataAccessLogic;

	public CreateModifyCard(final DataAccessLogic dataAccessLogic) {
		this.dataAccessLogic = dataAccessLogic;
	}

	/**
	 * The name of the variable where to put the selections of the widget during
	 * the save operation
	 */
	private String outputName;

	public String getIdcardcqlselector() {
		return idcardcqlselector;
	}

	public void setIdcardcqlselector(final String idcardcqlselector) {
		this.idcardcqlselector = idcardcqlselector;
	}

	public String getTargetClass() {
		return targetClass;
	}

	public void setTargetClass(final String targetClass) {
		this.targetClass = targetClass;
	}

	public boolean isReadonly() {
		return readonly;
	}

	public void setReadonly(final boolean readonly) {
		this.readonly = readonly;
	}

	public String getOutputName() {
		return outputName;
	}

	public void setOutputName(final String outputName) {
		this.outputName = outputName;
	}

	@Override
	public void save(final CMActivityInstance activityInstance, final Object input, final Map<String, Object> output) throws Exception {
		if (readonly) {
			return;
		}
		if (outputName != null) {
			output.put(outputName, outputValue(input));
		}
	}

	private CardReference outputValue(final Object input) {
		@SuppressWarnings("unchecked") final Map<String, Object> inputMap = (Map<String, Object>) input;
		final Object createdCardId = inputMap.get(CREATED_CARD_ID_SUBMISSION_PARAM);
		final CMCard card = dataAccessLogic.getCard(targetClass, createdCardId);
		return CardReference.newInstance(card);
	}

}