package org.cmdbuild.elements;

import java.util.Date;

import org.cmdbuild.elements.filters.OrderFilter;
import org.cmdbuild.elements.interfaces.ICard;
import org.cmdbuild.elements.interfaces.ITable;
import org.cmdbuild.exception.NotFoundException;
import org.cmdbuild.exception.ORMException;
import org.cmdbuild.services.auth.UserContext;
import org.cmdbuild.services.auth.UserOperations;

public class CardImpl extends AbstractElementImpl implements ICard {

	static OrderFilter[] noOrderFilter = new OrderFilter[0];

	// Used by CMBackend... waiting refactoring
	public CardImpl(final int idClass) throws NotFoundException {
		this.schema = UserOperations.from(UserContext.systemContext()).tables().get(idClass);
		this.setIdClass(idClass);
	}

	CardImpl(final String tableName) throws NotFoundException {
		this.schema = UserOperations.from(UserContext.systemContext()).tables().get(tableName);
		this.setIdClass(schema.getId());
	}

	// Used by CMBackend... waiting refactoring
	public CardImpl(final ITable schema) {
		this.schema = schema;
		this.setIdClass(schema.getId());
	}

	CardImpl(final ICard card) throws NotFoundException {
		this(card.getSchema());
		setAttributeValueMap(card.getAttributeValueMap());
	}

	// TODO: is really necessary to return only the description? I had a bad
	// time figuring out that an object was not null (but the description
	// yes...)
	@Override
	public String toString() {
		return getDescription();
	}

	@Override
	public ITable getSchema() {
		return (ITable) schema;
	}

	@Override
	public int getIdClass() {
		try {
			return (Integer) getValue(CardAttributes.ClassId.toString());
		} catch (final NotFoundException e) {
			return Integer.valueOf(getSchema().getId());
		}
	}

	// REMOVE THIS!!!!!!
	@Override
	public void setIdClass(final Integer idClass) {
		try {
			setValue(CardAttributes.ClassId.toString(), idClass);
		} catch (final NotFoundException e) {
			// Ignore if IdClass is not present
		}
	}

	@Override
	public String getCode() {
		return (String) getValue(CardAttributes.Code.toString());
	}

	@Override
	public void setCode(final String code) {
		getAttributeValue(CardAttributes.Code.toString()).setValue(code);
	}

	@Override
	public String getDescription() {
		return (String) getValue(CardAttributes.Description.toString());
	}

	@Override
	public void setDescription(final String description) {
		getAttributeValue(CardAttributes.Description.toString()).setValue(description);
	}

	@Override
	public String getUser() {
		return (String) getValue(CardAttributes.User.toString());
	}

	@Override
	public void setUser(final String user) {
		setValue(CardAttributes.User.toString(), user);
	}

	@Override
	public Date getBeginDate() {
		return (Date) getValue(CardAttributes.BeginDate.toString());
	}

	@Override
	public void setBeginDate(final Date date) {
		setValue(CardAttributes.BeginDate.toString(), date);
	}

	@Override
	public String getNotes() {
		return (String) getValue(CardAttributes.Notes.toString());
	}

	@Override
	public void setNotes(final String notes) {
		setValue(CardAttributes.Notes.toString(), notes);
	}

	@Override
	protected void modify() {
		backend.modifyCard(this);
	}

	@Override
	protected int create() {
		final int id = backend.createCard(this);
		return id;
	}

	@Override
	public void save() throws ORMException {
		setDefaultValueIfPresent(CardAttributes.ClassId.toString(), schema.getId());
		super.save();
	}
}
