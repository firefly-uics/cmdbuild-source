package org.cmdbuild.model.data;

import static org.apache.commons.lang.StringUtils.isNotBlank;

import java.util.Map;

import org.apache.commons.lang.Validate;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.cmdbuild.common.Builder;
import org.cmdbuild.services.store.Store.Storable;
import org.joda.time.DateTime;

import com.google.common.collect.Maps;

public class Card implements Storable {

	public static class CardBuilder implements Builder<Card> {

		private Long id;
		private String className;
		private Long classId;
		private DateTime begin;
		private DateTime end;
		private String user;
		private Map<String, Object> attributes = Maps.newHashMap();

		public CardBuilder clone(final Card card) {
			this.id = card.id;
			this.className = card.className;
			this.classId = card.classId;
			this.begin = card.begin;
			this.end = card.end;
			this.user = card.user;
			this.attributes = card.attributes;
			return this;
		}

		public CardBuilder withId(final Long value) {
			this.id = value;
			return this;
		}

		public CardBuilder withClassName(final String value) {
			this.className = value;
			return this;
		}

		public CardBuilder withClassId(final Long value) {
			this.classId = value;
			return this;
		}

		public CardBuilder withBeginDate(final DateTime value) {
			this.begin = value;
			return this;
		}

		public CardBuilder withEndDate(final DateTime value) {
			this.end = value;
			return this;
		}

		public CardBuilder withUser(final String value) {
			this.user = value;
			return this;
		}

		public CardBuilder withAttribute(final String key, final Object value) {
			this.attributes.put(key, value);
			return this;
		}

		public CardBuilder withAllAttributes(final Map<String, Object> values) {
			this.attributes.putAll(values);
			return this;
		}

		public CardBuilder withAllAttributes(final Iterable<Map.Entry<String, Object>> values) {
			for (final Map.Entry<String, Object> entry : values) {
				this.attributes.put(entry.getKey(), entry.getValue());
			}
			return this;
		}

		@Override
		public Card build() {
			Validate.isTrue(isNotBlank(className));
			return new Card(this);
		}

	}

	public static CardBuilder newInstance() {
		return new CardBuilder();
	}

	private final Long id;
	private final String className;
	private final Long classId;
	private final DateTime begin;
	private final DateTime end;
	private final String user;
	private final Map<String, Object> attributes;

	private final transient String toString;

	public Card(final CardBuilder builder) {
		this.id = builder.id;
		this.className = builder.className;
		this.classId = builder.classId;
		this.begin = builder.begin;
		this.end = builder.end;
		this.user = builder.user;
		this.attributes = builder.attributes;
		this.toString = ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}

	@Override
	public String getIdentifier() {
		return getId().toString();
	}

	@Override
	public String toString() {
		return toString;
	}

	public Long getId() {
		return id;
	}

	public String getClassName() {
		return className;
	}

	@Deprecated
	public Long getClassId() {
		return classId;
	}

	public DateTime getBeginDate() {
		return begin;
	}

	public DateTime getEndDate() {
		return end;
	}

	public String getUser() {
		return user;
	}

	public Map<String, Object> getAttributes() {
		return attributes;
	}

	public Object getAttribute(final String key) {
		return attributes.get(key);
	}

	public <T> T getAttribute(final String key, final Class<T> requiredType) {
		return requiredType.cast(attributes.get(key));
	}

}
