package org.cmdbuild.servlets.json.translation;

import static org.cmdbuild.servlets.json.CommunicationConstants.NAME;

import java.util.Collection;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.annotate.JsonProperty;

import com.google.common.collect.Ordering;

public class JsonElement {
	private static final String FIELDS = "fields";
	private static final String CHILDREN = "children";
	private String name;
	private Collection<JsonField> fields;
	private Collection<JsonElement> children;

	private static final Ordering<JsonElement> ORDER_BY_NAME = new Ordering<JsonElement>() {
		@Override
		public int compare(JsonElement left, JsonElement right) {
			return left.getName().compareTo(right.getName());
		}
	};

	public static enum Sorter {
		NAME("name") {
			@Override
			public Ordering<JsonElement> getOrdering() {
				return ORDER_BY_NAME;
			}
		}, 
		UNDEFINED(StringUtils.EMPTY) {
			@Override
			public Ordering<JsonElement> getOrdering() {
				throw new UnsupportedOperationException();
			}
		};
		
		

		private String sorter;

		Sorter(String sorter) {
			this.sorter = sorter;
		}

		public abstract Ordering<JsonElement> getOrdering();
		
		public static Sorter of(final String sorter) {
			for (final Sorter element : values()) {
				if (element.sorter.equalsIgnoreCase(sorter)) {
					return element;
				}
			}
			return UNDEFINED;
		}
	}

	@JsonProperty(NAME)
	public String getName() {
		return name;
	}

	@JsonProperty(FIELDS)
	public Collection<JsonField> getFields() {
		return fields;
	}

	@JsonProperty(CHILDREN)
	public Collection<JsonElement> getChildren() {
		return children;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public void setFields(final Collection<JsonField> fields) {
		this.fields = fields;
	}

	public void setChildren(Collection<JsonElement> children) {
		this.children = children;
	}

}
