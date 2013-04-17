package org.cmdbuild.services.soap.types;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.cmdbuild.logger.Log;
import org.joda.time.DateTime;

public class Card {

	public static class ValueSerializer {

		private final org.cmdbuild.model.data.Card cardModel;

		public ValueSerializer(final org.cmdbuild.model.data.Card cardModel) {
			this.cardModel = cardModel;
		}

		public final String serializeValueForAttribute(final String attributeName) {
			final Object attributeValue = cardModel.getAttribute(attributeName);
			if (attributeValue == null) {
				return StringUtils.EMPTY;
			} else {
				final Object convertedValue = cardModel.getType().getAttribute(attributeName).getType()
						.convertValue(attributeValue);
				return convertedValue != null ? convertedValue.toString() : StringUtils.EMPTY;
			}
		}

	}

	private String className;
	private int id;
	private Calendar beginDate;
	private Calendar endDate;
	private String user;
	private List<Attribute> attributeList;
	private List<Metadata> metadata;

	public Card() {
	}

	public Card(final org.cmdbuild.model.data.Card cardModel) {
		this(cardModel, new ValueSerializer(cardModel));
	}

	public Card(final org.cmdbuild.model.data.Card cardModel, final ValueSerializer valueSerializer) {
		setup(cardModel);
		final List<Attribute> attrs = new ArrayList<Attribute>();
		for (final Entry<String, Object> entry : cardModel.getAttributes().entrySet()) {
			final Attribute tmpAttribute = new Attribute();
			final String attributeName = entry.getKey();
			final String value = valueSerializer.serializeValueForAttribute(attributeName);
			tmpAttribute.setName(attributeName);
			tmpAttribute.setValue(value);
			if (cardModel.getId() != null) {
				tmpAttribute.setCode(cardModel.getId().toString());
			}
			attrs.add(tmpAttribute);
		}
		this.setAttributeList(attrs);
	}

	public Card(final org.cmdbuild.model.data.Card cardModel, final Attribute[] attrs,
			final ValueSerializer valueSerializer) {
		Attribute attribute;
		final List<Attribute> list = new ArrayList<Attribute>();
		Log.SOAP.debug("Filtering card with following attributes");
		for (final Attribute a : attrs) {
			final String name = a.getName();
			if (name != null && !name.equals(StringUtils.EMPTY)) {
				Object attributeValue = cardModel.getAttribute(name);
				attribute = new Attribute();
				attribute.setName(name);
				if (attributeValue != null) {
					attribute.setValue(valueSerializer.serializeValueForAttribute(name));
				}
				if (cardModel.getId() != null) {
					attribute.setCode(cardModel.getId().toString());
				}
				Log.SOAP.debug("Attribute name=" + name + ", value=" + valueSerializer.serializeValueForAttribute(name));
				final String attributeName = attribute.getName();
				if (!attributeName.equals("Id") && !attributeName.equals("ClassName")
						&& !attributeName.equals("BeginDate") && !attributeName.equals("User")
						&& !attributeName.equals("EndDate")) {
					list.add(attribute);
				}
			}

			this.setAttributeList(list);
		}
		setup(cardModel);
	}

	public Card(final org.cmdbuild.model.data.Card cardModel, final Attribute[] attrs) {
		this(cardModel, attrs, new ValueSerializer(cardModel));
	}

	protected void setup(final org.cmdbuild.model.data.Card cardModel) {
		final int id = cardModel.getId().intValue();
		this.setId(id);
		this.setClassName(cardModel.getClassName());
		this.setUser(cardModel.getUser());
		this.setBeginDate(cardModel.getBeginDate().toGregorianCalendar());
		final DateTime endDate = cardModel.getEndDate();
		this.setEndDate(endDate != null ? endDate.toGregorianCalendar() : null);
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(final String classname) {
		this.className = classname;
	}

	public int getId() {
		return id;
	}

	public void setId(final int id) {
		this.id = id;
	}

	public Calendar getBeginDate() {
		return beginDate;
	}

	public void setBeginDate(final Calendar beginDate) {
		this.beginDate = beginDate;
	}

	public Calendar getEndDate() {
		return endDate;
	}

	public void setEndDate(final Calendar endDate) {
		this.endDate = endDate;
	}

	public String getUser() {
		return user;
	}

	public void setUser(final String user) {
		this.user = user;
	}

	public List<Attribute> getAttributeList() {
		return attributeList;
	}

	public void setAttributeList(final List<Attribute> attributeList) {
		this.attributeList = attributeList;
	}

	public List<Metadata> getMetadata() {
		return metadata;
	}

	public void setMetadata(final List<Metadata> metadata) {
		this.metadata = metadata;
	}

	@Override
	public String toString() {
		String attributes = "";
		final Iterator<Attribute> itr = attributeList.iterator();
		while (itr.hasNext()) {
			attributes += itr.next().toString();
		}
		return "[className: " + className + " id:" + id + " attributes: " + attributes + "]";
	}
}
