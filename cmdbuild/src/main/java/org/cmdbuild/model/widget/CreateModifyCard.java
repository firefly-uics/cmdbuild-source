package org.cmdbuild.model.widget;

import java.util.Map;

import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.reference.CardReference;
import org.cmdbuild.logic.data.DataAccessLogic;
import org.cmdbuild.workflow.CMActivityInstance;

public class CreateModifyCard extends Widget {

	public static class Submission {
		private Object output;

		public Object getOutput() {
			return output;
		}

		public void setOutput(final Object output) {
			this.output = output;
		}
	}

	public static final String CREATED_CARD_ID_SUBMISSION_PARAM = "output";

	private final DataAccessLogic dataAccessLogic;

	private String idcardcqlselector;
	private String targetClass;
	private boolean readonly;

	/**
	 * The name of the variable where to put the selections of the widget during
	 * the save operation
	 */
	private String outputName;

	public CreateModifyCard() {
		super();
		this.dataAccessLogic = null;
	}

	public CreateModifyCard(final DataAccessLogic dataAccessLogic) {
		this.dataAccessLogic = dataAccessLogic;
	}

	@Override
	public void accept(final WidgetVisitor visitor) {
		visitor.visit(this);
	}

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
	public void save(final CMActivityInstance activityInstance, final Object input, final Map<String, Object> output)
			throws Exception {
		if (readonly) {
			return;
		}
		if (outputName != null) {
			final Submission submission = decodeInput(input);
			output.put(outputName, outputValue(submission));
		}
	}

	private Submission decodeInput(final Object input) {
		if (input instanceof Submission) {
			return (Submission) input;
		} else {
			@SuppressWarnings("unchecked")
			final Map<String, Object> inputMap = (Map<String, Object>) input;
			final Object createdCardId = inputMap.get(CREATED_CARD_ID_SUBMISSION_PARAM);
			final Submission submission = new Submission();
			submission.setOutput(createdCardId);
			return submission;
		}
	}

	private CardReference outputValue(final Submission submission) {
		if (dataAccessLogic == null) {
			throw new UnsupportedOperationException("It can not be used for cards widget");
		}

		final Long createdCardId = (Long) submission.getOutput();
		final CMCard card = dataAccessLogic.fetchCard(targetClass, createdCardId);
		return CardReference.newInstance(card);
	}

}