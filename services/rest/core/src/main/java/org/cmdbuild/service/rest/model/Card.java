package org.cmdbuild.service.rest.model;

import static org.cmdbuild.service.rest.constants.Serialization.CARD;
import static org.cmdbuild.service.rest.constants.Serialization.TYPE;
import static org.cmdbuild.service.rest.constants.Serialization.VALUES;

import java.util.Map;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.cmdbuild.service.rest.model.adapter.CardAdapter;
import org.cmdbuild.service.rest.model.adapter.StringObjectMapAdapter;

@XmlRootElement(name = CARD)
@XmlJavaTypeAdapter(CardAdapter.class)
public class Card extends AbstractModelWithId {

	private String type;
	private Map<String, Object> values;

	Card() {
		// package visibility
	}

	@XmlAttribute(name = TYPE)
	public String getType() {
		return type;
	}

	void setType(final String type) {
		this.type = type;
	}

	@XmlElement(name = VALUES)
	@XmlJavaTypeAdapter(StringObjectMapAdapter.class)
	public Map<String, Object> getValues() {
		return values;
	}

	void setValues(final Map<String, Object> values) {
		this.values = values;
	}

	@Override
	protected boolean doEquals(final Object obj) {
		if (this == obj) {
			return true;
		}

		if (!(obj instanceof Card)) {
			return false;
		}

		final Card other = Card.class.cast(obj);
		return new EqualsBuilder() //
				.append(this.type, other.type) //
				.append(this.getId(), other.getId()) //
				.append(this.values, other.values) //
				.isEquals();
	}

	@Override
	protected int doHashCode() {
		return new HashCodeBuilder() //
				.append(type) //
				.append(getId()) //
				.append(values) //
				.toHashCode();
	}

}
