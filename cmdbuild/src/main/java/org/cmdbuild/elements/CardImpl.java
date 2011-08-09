package org.cmdbuild.elements;

import java.util.Date;

import org.cmdbuild.elements.filters.OrderFilter;
import org.cmdbuild.elements.interfaces.IAttribute;
import org.cmdbuild.elements.interfaces.ICard;
import org.cmdbuild.elements.interfaces.ITable;
import org.cmdbuild.elements.interfaces.IAttribute.AttributeType;
import org.cmdbuild.exception.NotFoundException;
import org.cmdbuild.exception.ORMException;
import org.cmdbuild.services.auth.UserContext;

public class CardImpl extends AbstractElementImpl implements ICard {

	static OrderFilter[] noOrderFilter = new OrderFilter[0];

	// Used by CMBackend... waiting refactoring
	public CardImpl(int idClass) throws NotFoundException {
		this.schema = UserContext.systemContext().tables().get(idClass);
		this.setIdClass(idClass);
	}

	CardImpl(String tableName) throws NotFoundException {
		this.schema = UserContext.systemContext().tables().get(tableName);
		this.setIdClass(schema.getId());
	}

	// Used by CMBackend... waiting refactoring
	public CardImpl(ITable schema) {
		this.schema = schema;
		this.setIdClass(schema.getId());
	}

	CardImpl(ICard card) throws NotFoundException {
		this(card.getSchema());
		setAttributeValueMap(card.getAttributeValueMap());
	}

	// TODO: is really necessary to return only the description? I had a bad time figuring out that an object was not null (but the description yes...)
	public String toString() {
		return getDescription();
	}

	public ITable getSchema() {
		return (ITable)schema;
	}

	public int getIdClass() {
		try {
			return (Integer)getValue(CardAttributes.ClassId.toString());
		} catch (NotFoundException e) {
			return Integer.valueOf(getSchema().getId());
		}
	}

	// REMOVE THIS!!!!!!
	public void setIdClass(Integer idClass) {
		try {
			setValue(CardAttributes.ClassId.toString(), idClass);
		} catch (NotFoundException e) {
			// Ignore if IdClass is not present
		}
	}

	public String getCode(){
		return (String) getValue(CardAttributes.Code.toString());
	}

	public void setCode(String code){
		getAttributeValue(CardAttributes.Code.toString()).setValue(code);
	}

	public String getDescription(){
		return (String) getValue(CardAttributes.Description.toString());
	}

	public void setDescription(String description){
		getAttributeValue(CardAttributes.Description.toString()).setValue(description);
	}

	public String getUser(){
		return (String) getValue(CardAttributes.User.toString());
	}

	public void setUser(String user){
		setValue(CardAttributes.User.toString(), user);
	}

	public Date getBeginDate(){
		return (Date) getValue(CardAttributes.BeginDate.toString());
	}

	public void setBeginDate(Date date){
		setValue(CardAttributes.BeginDate.toString(), date);
	}

	public String getNotes(){
		return (String) getValue(CardAttributes.Notes.toString());
	}

	public void setNotes(String notes){
		setValue(CardAttributes.Notes.toString(), notes);
	}

	@Override
	protected void modify() {
		backend.modifyCard(this);
		updateReferences();
	}

	@Override
	protected int create() {
		int id = backend.createCard(this);
		updateReferences();
		return id;
	}

	private void updateReferences() {
		assert !isNew();
		for(AttributeValue value: values.values()){
			IAttribute attribute = value.getSchema();
			if (attribute.getType() == AttributeType.REFERENCE && value.isChanged()) {
				// TODO: manage delete reference
				// TODO: relations for reference attribute have to be handled in the DATABASE!
				//value.getReference().getRelation().save();
			}
		}
	}

	@Override
	public void save() throws ORMException {
		setDefaultValueIfPresent(CardAttributes.ClassId.toString(), (Integer)schema.getId());
		super.save();
	}
}
