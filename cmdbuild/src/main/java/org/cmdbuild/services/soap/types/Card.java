package org.cmdbuild.services.soap.types;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.cmdbuild.dao.attribute.AbstractDateAttribute;
import org.cmdbuild.elements.AttributeValue;
import org.cmdbuild.elements.interfaces.ICard;
import org.cmdbuild.exception.NotFoundException;
import org.cmdbuild.logger.Log;

public class Card {

	public enum ValueSerializer {
		LEGACY {
			@Override
			public String serialize(final AttributeValue value) {
				return value.toString();
			}
		},
		HACK {
			@Override
			public String serialize(final AttributeValue attributeValue) {
				switch (attributeValue.getSchema().getType()) {
				case DATE:
				case TIME:
				case TIMESTAMP:
					return attributeValue.isNull() ? attributeValue.toString() : dateAsString(attributeValue);
				default:
					return attributeValue.toString();
				}
			}

			private String dateAsString(final AttributeValue attributeValue) {
				return dateFormat().format(attributeValue.getDate());
			}

			private SimpleDateFormat dateFormat() {
				return new SimpleDateFormat(AbstractDateAttribute.SOAP_DATETIME_FORMAT);
			}
		};

		public abstract String serialize(final AttributeValue value);

		public static ValueSerializer forLongDateFormat(final boolean enableLongDateFormat) {
			if (enableLongDateFormat) {
				return HACK;
			} else {
				return LEGACY;
			}
		}
	}

	private String className;
	private String description;
	private int id;
	private Calendar beginDate;
	private Calendar endDate;
	private String user;
	private List<Attribute> attributeList;
	private List<Metadata> metadata;

	public Card() {
	}

	public Card(final ICard card) {
		this(card, ValueSerializer.LEGACY);
	}

	public Card(final ICard card, final ValueSerializer valueSerializer) {
		setup(card);
		final List<Attribute> attrs = new ArrayList<Attribute>();
		for (final AttributeValue av : card.getAttributeValueMap().values()) {
			final Attribute tmp = new Attribute();
			final String name = av.getSchema().getName();
			final String value = valueSerializer.serialize(av);
			tmp.setName(name);
			tmp.setValue(value);
			if (av.getId() != null) {
				tmp.setCode(av.getId().toString());
			}
			attrs.add(tmp);
		}
		this.setAttributeList(attrs);
	}

	public Card(final ICard card, final Attribute[] attrs, final ValueSerializer valueSerializer) {

		AttributeValue value;
		Attribute attribute;
		final List<Attribute> list = new ArrayList<Attribute>();
		Log.SOAP.debug("Filtering card with following attributes");
		for (final Attribute a : attrs) {
			final String name = a.getName();
			if (name != null && (!(name.equals("")))) {
				value = card.getAttributeValue(name);
				attribute = new Attribute();
				attribute.setName(name);
				if (value != null) {
					attribute.setValue(valueSerializer.serialize(value));
				}
				if (value.getId() != null) {
					attribute.setCode(value.getId().toString());
				}
				Log.SOAP.debug("Attribute name=" + name + ", value=" + value);
				final String attributeName = attribute.getName();
				if (!attributeName.equals("Id") && !attributeName.equals("ClassName")
						&& !attributeName.equals("BeginDate") && !attributeName.equals("User")
						&& !attributeName.equals("EndDate")) {
					list.add(attribute);
				}
			}

			this.setAttributeList(list);
		}
		setup(card);
	}

	public Card(final ICard card, final Attribute[] attrs) {
		this(card, attrs, ValueSerializer.LEGACY);
	}

	private void setup(final ICard card) {
		final int id = card.getId();
		this.setId(id);
		this.setClassName(card.getSchema().getName());
		this.setDescription(card.getSchema().getDescription());
		this.setUser(card.getUser());

		// Convert Date to Calendar
		final Calendar beginDate = Calendar.getInstance();
		beginDate.setTime(card.getBeginDate());
		this.setBeginDate(beginDate);
		try {
			final Date endDate = card.getAttributeValue("EndDate").getDate();
			final Calendar endDateCalendar = Calendar.getInstance();
			endDateCalendar.setTime(endDate);
			this.setEndDate(endDateCalendar);
		} catch (final NotFoundException e) {
		}
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(final String classname) {
		this.className = classname;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(final String description) {
		this.description = description;
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
