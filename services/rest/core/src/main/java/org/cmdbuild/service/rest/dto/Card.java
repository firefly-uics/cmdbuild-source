package org.cmdbuild.service.rest.dto;

import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Maps.transformValues;
import static com.google.common.collect.Maps.uniqueIndex;
import static org.cmdbuild.common.utils.guava.Functions.toKey;
import static org.cmdbuild.common.utils.guava.Functions.toValue;
import static org.cmdbuild.service.rest.constants.Serialization.CARD;
import static org.cmdbuild.service.rest.constants.Serialization.ID;
import static org.cmdbuild.service.rest.constants.Serialization.TYPE;
import static org.cmdbuild.service.rest.constants.Serialization.VALUES;

import java.util.Map;
import java.util.Map.Entry;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.cmdbuild.service.rest.dto.adapter.CardAdapter;
import org.cmdbuild.service.rest.dto.adapter.StringObjectMapAdapter;

import com.google.common.base.Function;

@XmlRootElement(name = CARD)
@XmlJavaTypeAdapter(CardAdapter.class)
public class Card {

	public static class Builder implements org.apache.commons.lang3.builder.Builder<Card> {

		private String type;
		private Long id;
		private final Map<String, Object> values = newHashMap();

		private Builder() {
			// use static method
		}

		@Override
		public Card build() {
			validate();
			return new Card(this);
		}

		private void validate() {
			// TODO Auto-generated method stub
		}

		public Builder withType(final String type) {
			this.type = type;
			return this;
		}

		public Builder withId(final Long id) {
			this.id = id;
			return this;
		}

		public Builder withValues(final Iterable<? extends Entry<String, ? extends Object>> values) {
			final Function<Entry<? extends String, ? extends Object>, String> key = toKey();
			final Function<Entry<? extends String, ? extends Object>, Object> value = toValue();
			final Map<String, Object> allValues = transformValues(uniqueIndex(values, key), value);
			return withValues(allValues);
		}

		public Builder withValues(final Map<String, ? extends Object> values) {
			this.values.putAll(values);
			return this;
		}

	}

	public static Builder newInstance() {
		return new Builder();
	}

	Card() {
		// package visibility
	}

	private String type;
	private Long id;
	private Map<String, Object> values;

	private Card(final Builder builder) {
		this.type = builder.type;
		this.id = builder.id;
		this.values = builder.values;
	}

	@XmlAttribute(name = TYPE)
	public String getType() {
		return type;
	}

	void setType(final String type) {
		this.type = type;
	}

	@XmlAttribute(name = ID)
	public Long getId() {
		return id;
	}

	void setId(final Long id) {
		this.id = id;
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
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}

		if (!(obj instanceof Card)) {
			return false;
		}

		final Card other = Card.class.cast(obj);
		return new EqualsBuilder() //
				.append(this.type, other.type) //
				.append(this.id, other.id) //
				.append(this.values, other.values) //
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder() //
				.append(type) //
				.append(id) //
				.append(values) //
				.toHashCode();
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE).toString();
	}

}
