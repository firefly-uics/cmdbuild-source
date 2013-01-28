package org.cmdbuild.model.widget;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.reference.CardReference;
import org.cmdbuild.logic.data.DataAccessLogic;
import org.cmdbuild.workflow.CMActivityInstance;

public class ManageRelation extends Widget {

	public static class Submission {
		private List<Object> output;

		public List<Object> getOutput() {
			return output;
		}

		public void setOutput(final List<Object> output) {
			this.output = output;
		}
	}

	public static final String CREATED_CARD_ID_SUBMISSION_PARAM = "output";

	/*
	 * Domain to which show the relations
	 */
	private String domainName;

	/*
	 * Class name of the card to use as reference
	 */
	private String className;

	/*
	 * Class name of the card to use as reference
	 */
	private String destinationClassName;

	/*
	 * the id of the card to use as reference or a client variable
	 */
	private String objId;

	/*
	 * if false the activity could not be advanced
	 */
	private boolean required;

	/*
	 * to define the EntryType that is source for the relation
	 */
	private String source;

	/*
	 * ################################# EnabledFunctions
	 * ################################# /
	 * 
	 * /* it is possible to select more than one relations
	 */
	private boolean multiSelection;

	/*
	 * it is possible to select only one relations
	 * 
	 * Clearly is a design mistake, what's if multiSelection and singleSelection
	 * are both true?
	 */
	private boolean singleSelection;

	/*
	 * It is possible create a relation with an existing card
	 */
	private boolean canCreateRelation;

	/*
	 * It is possible modify an existing relation in the domain
	 */
	private boolean canModifyARelation;

	/*
	 * It is possible remove a relation in the domain
	 */
	private boolean canRemoveARelation;

	/*
	 * It is possible create a card and link it to the card via the given domain
	 */
	private boolean canCreateAndLinkCard;

	/*
	 * It is possible modify a card in relation with the card for the given
	 * domain
	 */
	private boolean canModifyALinkedCard;

	/*
	 * It is possible delete a card in relation with the card for the given
	 * domain and the associated relation
	 */
	private boolean canDeleteALinkedCard;

	/*
	 * The name of the variable where to put the selections of the widget during
	 * the save operation
	 */
	private String outputName;

	private final DataAccessLogic dataAccessLogic;

	public ManageRelation(final DataAccessLogic dataAccessLogic) {
		this.dataAccessLogic = dataAccessLogic;
	}

	@Override
	public void accept(final WidgetVisitor visitor) {
		visitor.visit(this);
	}

	public String getDomainName() {
		return domainName;
	}

	public void setDomainName(final String domainName) {
		this.domainName = domainName;
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(final String className) {
		this.className = className;
	}

	public boolean isRequired() {
		return required;
	}

	public void setRequired(final boolean required) {
		this.required = required;
	}

	public String getObjId() {
		return objId;
	}

	public void setObjId(final String cardCQLSelector) {
		this.objId = cardCQLSelector;
	}

	public boolean isMultiSelection() {
		return multiSelection;
	}

	public void setMultiSelection(final boolean multiSelection) {
		this.multiSelection = multiSelection;
	}

	public boolean isSingleSelection() {
		return singleSelection;
	}

	public void setSingleSelection(final boolean singleSelection) {
		this.singleSelection = singleSelection;
	}

	public String getSource() {
		return source;
	}

	public void setSource(final String source) {
		this.source = source;
	}

	public boolean isCanCreateRelation() {
		return canCreateRelation;
	}

	public void setCanCreateRelation(final boolean canCreateRelation) {
		this.canCreateRelation = canCreateRelation;
	}

	public boolean isCanModifyARelation() {
		return canModifyARelation;
	}

	public void setCanModifyARelation(final boolean canModifyARelation) {
		this.canModifyARelation = canModifyARelation;
	}

	public boolean isCanRemoveARelation() {
		return canRemoveARelation;
	}

	public void setCanRemoveARelation(final boolean canRemoveARelation) {
		this.canRemoveARelation = canRemoveARelation;
	}

	public boolean isCanCreateAndLinkCard() {
		return canCreateAndLinkCard;
	}

	public void setCanCreateAndLinkCard(final boolean canCreateAndLinkCard) {
		this.canCreateAndLinkCard = canCreateAndLinkCard;
	}

	public boolean isCanModifyALinkedCard() {
		return canModifyALinkedCard;
	}

	public void setCanModifyALinkedCard(final boolean canModifyALinkedCard) {
		this.canModifyALinkedCard = canModifyALinkedCard;
	}

	public boolean isCanDeleteALinkedCard() {
		return canDeleteALinkedCard;
	}

	public void setCanRemoveALinkedCard(final boolean canDeleteALinkedCard) {
		this.canDeleteALinkedCard = canDeleteALinkedCard;
	}

	public void setDestinationClassName(final String destinationClassName) {
		this.destinationClassName = destinationClassName;
	}

	public String getDestinationClassName() {
		return destinationClassName;
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
			final Map<String, List<Object>> inputMap = (Map<String, List<Object>>) input;
			final List<Object> selectedCardIds = inputMap.get(CREATED_CARD_ID_SUBMISSION_PARAM);
			final Submission submission = new Submission();
			submission.setOutput(selectedCardIds);
			return submission;
		}
	}

	private CardReference[] outputValue(final Submission submission) {
		final List<Object> selectedCardIds = submission.getOutput();
		final List<CardReference> selectedCards = new ArrayList<CardReference>(selectedCardIds.size());
		for (final Object cardId : selectedCardIds) {
			Integer cardIdInt = (Integer)cardId;
			final CMCard card = dataAccessLogic.fetchCard(destinationClassName, cardIdInt);
			final CardReference cardReference = CardReference.newInstance(card);
			if (cardReference != null) {
				selectedCards.add(cardReference);
			}
		}
		return selectedCards.toArray(new CardReference[selectedCards.size()]);
	}

}
