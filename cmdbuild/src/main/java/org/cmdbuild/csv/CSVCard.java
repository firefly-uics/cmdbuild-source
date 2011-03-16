package org.cmdbuild.csv;

import java.util.HashMap;
import java.util.Map;

import org.cmdbuild.elements.AttributeValue;
import org.cmdbuild.elements.CardFactoryImpl;
import org.cmdbuild.elements.Lookup;
import org.cmdbuild.elements.Reference;
import org.cmdbuild.elements.filters.AttributeFilter.AttributeFilterType;
import org.cmdbuild.elements.interfaces.IAttribute;
import org.cmdbuild.elements.interfaces.ICard;
import org.cmdbuild.elements.interfaces.CardFactory;
import org.cmdbuild.elements.interfaces.ITable;
import org.cmdbuild.elements.proxy.CardForwarder;
import org.cmdbuild.exception.ORMException;
import org.cmdbuild.exception.NotFoundException.NotFoundExceptionType;
import org.cmdbuild.exception.ORMException.ORMExceptionType;
import org.cmdbuild.services.SchemaCache;
import org.cmdbuild.services.auth.UserContext;

public class CSVCard extends CardForwarder implements Comparable<CSVCard> {

	public static final String invalidXpName = "invalid";
	private final Map<String, String> invalidAttributes = new HashMap<String, String>();

	public CSVCard(ICard c) {
		super(c);
	}

	/*
	 * Generate a new fake Id (not used for card storage) for the CSV imported card
	 */
	public static CSVCard createWithFakeId(ITable table) {
		CSVCard csvCard = CSVCard.create(table);
		csvCard.setValue(CardAttributes.Id.toString(), CSVData.getNextId());
		return csvCard;
	}

	public static CSVCard create(ITable table) {
		ICard card = table.cards().create();
		CSVCard csvCard = new CSVCard(card);
		return csvCard;
	}

	/*
	 * always insert card on save, the fake id is never saved this way
	 */
	@Override
	public boolean isNew() {
		return true;
	}

	public boolean setValidatedFromCSV(String attributeName, String csvValue) {
		AttributeValue av = c.getAttributeValue(attributeName);
		try {
			setValueFromCSV(av, csvValue);
			invalidAttributes.remove(attributeName);
			return true;
		} catch (Exception e) {
			invalidAttributes.put(attributeName, csvValue);
			return false;
		}
	}

	public boolean setValidatedFromJSON(String attributeName, String value, String valueDescription) {
		AttributeValue av = c.getAttributeValue(attributeName);
		try {
			setValueFromJSON(av, value, valueDescription);
			invalidAttributes.remove(attributeName);
			return true;
		} catch (Exception e) {
			invalidAttributes.put(attributeName, value);
			return false;
		}
	}

	private void setValueFromJSON(AttributeValue av, String stringValue, String valueDescription) {
		IAttribute attribute = av.getSchema();
		if (stringValue.length() == 0) {
			av.setValue("");
			return;
		}
		Object value;
		switch (attribute.getType()) {
		case LOOKUP:
			Integer lookupId = Integer.valueOf(stringValue);
			value = SchemaCache.getInstance().getLookup(lookupId);
			if (value == null) {
				throw NotFoundExceptionType.LOOKUP_NOTFOUND.createException(stringValue);
			}
			break;
		case REFERENCE:
			Integer referenceId = Integer.valueOf(stringValue);
			value = new Reference(av.getSchema().getReferenceDirectedDomain(), referenceId, valueDescription);
			break;
		default:
			value = stringValue;
		}
		av.setValue(value);
	}

	private void setValueFromCSV(AttributeValue av, String csvValue) {
		IAttribute attribute = av.getSchema();
		if (csvValue.length() == 0) {
			av.setValue("");
			return;
		}
		switch (attribute.getType()) {
		case LOOKUP:
			Lookup lookup = SchemaCache.getInstance().getLookup(
					attribute.getLookupType().getType(), csvValue );
			if (lookup == null)
				throw NotFoundExceptionType.LOOKUP_NOTFOUND.createException(csvValue);
			else
				av.setValue(lookup);
			break;
		case REFERENCE:
			ITable referenceTable = attribute.getReferenceTarget();
			CardFactory cf = new CardFactoryImpl(referenceTable, UserContext.systemContext());
			ICard card = cf.list().filter(ICard.CardAttributes.Code.toString(), // Description or Code?
					AttributeFilterType.EQUALS, csvValue).get(false);
			av.setValue(new Reference(av.getSchema().getReferenceDirectedDomain(), card.getId(), card.getDescription()));
			break;
		default:
			av.setValue(csvValue);
		}
	}

	public Map<String, Object> getExtendedProperties() {
		Map<String, Object> xp = super.getExtendedProperties();
		if (!xp.containsKey("invalid"))
			xp.put(invalidXpName, invalidAttributes);
		return xp;
	}

	public int compareTo(CSVCard c) {
		if (c.getIdClass() == this.getIdClass())
			return this.getId() - c.getId();
		throw new ClassCastException();
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof CSVCard) {
			CSVCard c = ((CSVCard) o);
			return (this.compareTo(c) == 0);
		}
		return false;
	}

	@Override
	public void save() throws ORMException {
		checkValidity();
		Object prevValue = getValue(CardAttributes.Id.toString());
		try {
			setValue(CardAttributes.Id.toString(), new Integer(-1));
			super.save();
		} finally {
			setValue(CardAttributes.Id.toString(), prevValue);
		}
	}

	private void checkValidity() {
		if (!invalidAttributes.isEmpty())
			throw ORMExceptionType.ORM_CSV_INVALID_ATTRIBUTES.createException();
	}
}
