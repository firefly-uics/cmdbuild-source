package org.cmdbuild.model.widget;

public class ManageRelation extends Widget {
	/*
	 * Domain to which show the relations
	 */
	private String domainName;

	/*
	 * Class name of the card to use as reference
	 */
	private String className;

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

}
