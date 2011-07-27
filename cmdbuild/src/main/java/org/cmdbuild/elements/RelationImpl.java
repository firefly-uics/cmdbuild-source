package org.cmdbuild.elements;

import java.util.Map;

import org.cmdbuild.elements.interfaces.ICard;
import org.cmdbuild.elements.interfaces.IDomain;
import org.cmdbuild.elements.interfaces.IRelation;
import org.cmdbuild.exception.ORMException.ORMExceptionType;

public class RelationImpl extends AbstractElementImpl implements IRelation {

	private static final long serialVersionUID = 1L;

	private ICard card1;
	private ICard card2;
	private boolean reversed;

	RelationImpl() {
		super();
	}

	// Used by CMBackend... waiting refactoring
	public RelationImpl(IDomain domain, ICard card1, ICard card2) {
		this(null, domain, card1, card2, false);
	}

	RelationImpl(Integer id, IDomain domain, ICard card1, ICard card2, boolean reversed) {
		this.schema = domain;
		getAttributeValue("IdDomain").setValue((Integer) domain.getId());
		this.card1 = card1;
		this.card2 = card2;
		this.reversed = reversed;
		getAttributeValue("Id").setValue(id);
	}

	/**
	 * Quick fix for an awful existing implementation
	 * 
	 * If the relation has been created from a card reference,
	 * update the relation values with the card ones.
	 */
	protected void updateValues() {
		if (this.card1 != null) {
			super.getAttributeValue("IdClass1").setValue((Integer)this.card1.getIdClass());
			super.getAttributeValue("IdObj1").setValue((Integer)this.card1.getId());
		}
		if (this.card2 != null) {
			super.getAttributeValue("IdClass2").setValue((Integer)this.card2.getIdClass());
			super.getAttributeValue("IdObj2").setValue((Integer)this.card2.getId());
		}
	}

	/**
	 * Quick fix for an awful existing implementation
	 */
	@Override
	public AttributeValue getAttributeValue(String attrName) {
		updateValues();
		return super.getAttributeValue(attrName);
	}

	/**
	 * Quick fix for an awful existing implementation
	 */
	@Override
	public Map<String, AttributeValue> getAttributeValueMap() {
		updateValues();
		return super.getAttributeValueMap();
	}

	public ICard getCard1() {
		return card1;
	}

	public void setCard1(ICard card1) {
		this.card1 = card1;
	}

	public ICard getCard2() {
		return card2;
	}

	public void setCard2(ICard card2) {
		this.card2 = card2;
	}

	public IDomain getSchema() {
		return (IDomain)schema;
	}

	public void setSchema(IDomain schema) {
		this.schema = schema;
	}

	public void save() {
		if (isReversed()) {
			throw ORMExceptionType.ORM_READ_ONLY_RELATION.createException();
		}

		setDefaultValueIfPresent("IdDomain", (Integer)schema.getId());
		checkClassesCorrectness();
		updateValues();

		super.save();
	}

	private void checkClassesCorrectness() {
		TableTree.checkIfChild(this.getCard1().getSchema(), this.getSchema().getClass1());
		TableTree.checkIfChild(this.getCard2().getSchema(), this.getSchema().getClass2());
	}

	protected void modify() {
		backend.modifyRelation(this);
	}

	protected int create() {
		return backend.createRelation(this);
	}

	public boolean isReversed() {
		return reversed;
	}

	public DirectedDomain getDirectedDomain() {
		return DirectedDomain.create((IDomain)schema, !reversed);
	}

	@Override
	public String toString() {
		return String.format("%s-%s", getCard1(), getCard2());
	}
}
