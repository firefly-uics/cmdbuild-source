package org.cmdbuild.model.widget.customform;

import static org.apache.commons.lang3.builder.ToStringBuilder.reflectionToString;
import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

import java.util.Map;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

@XmlRootElement
public class Attribute {

	@XmlRootElement
	public static class Filter {

		private static final String EXPRESSION = "expression";
		private static final String CONTEXT = "context";

		private String expression;
		private Map<String, String> context;

		@XmlAttribute(name = EXPRESSION)
		public String getExpression() {
			return expression;
		}

		public void setExpression(final String text) {
			this.expression = text;
		}

		@XmlElementWrapper(name = CONTEXT)
		public Map<String, String> getContext() {
			return context;
		}

		public void setContext(final Map<String, String> context) {
			this.context = context;
		}

		@Override
		public boolean equals(final Object obj) {
			if (obj == this) {
				return true;
			}
			if (!(obj instanceof Attribute.Filter)) {
				return false;
			}
			final Attribute.Filter other = Attribute.Filter.class.cast(obj);
			return new EqualsBuilder() //
					.append(this.expression, other.expression) //
					.append(this.context, other.context) //
					.isEquals();
		}

		@Override
		public int hashCode() {
			return new HashCodeBuilder() //
					.append(expression) //
					.append(context) //
					.toHashCode();
		}

		@Override
		public String toString() {
			return reflectionToString(this, SHORT_PREFIX_STYLE);
		}

	}

	private static final String TYPE = "type";
	private static final String NAME = "name";
	private static final String DESCRIPTION = "description";
	private static final String UNIQUE = "unique";
	private static final String MANDATORY = "mandatory";
	private static final String WRITABLE = "writable";
	private static final String PRECISION = "precision";
	private static final String SCALE = "scale";
	private static final String LENGTH = "length";
	private static final String EDITOR_TYPE = "editorType";
	private static final String TARGET_CLASS = "targetClass";
	private static final String LOOKUP_TYPE = "lookupType";
	private static final String FILTER = "filter";

	private String type;
	private String name;
	private String description;
	private boolean unique;
	private boolean mandatory;
	private boolean writable = true;
	private Long precision;
	private Long scale;
	private Long length;
	private String editorType;
	private String targetClass;
	private String lookupType;
	private Attribute.Filter filter;

	@XmlAttribute(name = TYPE)
	public String getType() {
		return type;
	}

	public void setType(final String type) {
		this.type = type;
	}

	@XmlAttribute(name = NAME)
	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	@XmlAttribute(name = DESCRIPTION)
	public String getDescription() {
		return description;
	}

	public void setDescription(final String description) {
		this.description = description;
	}

	@XmlAttribute(name = UNIQUE)
	public boolean isUnique() {
		return unique;
	}

	public void setUnique(final boolean unique) {
		this.unique = unique;
	}

	@XmlAttribute(name = MANDATORY)
	public boolean isMandatory() {
		return mandatory;
	}

	public void setMandatory(final boolean mandatory) {
		this.mandatory = mandatory;
	}

	@XmlAttribute(name = WRITABLE)
	public boolean isWritable() {
		return writable;
	}

	public void setWritable(final boolean writable) {
		this.writable = writable;
	}

	@XmlAttribute(name = PRECISION)
	public Long getPrecision() {
		return precision;
	}

	public void setPrecision(final Long precision) {
		this.precision = precision;
	}

	@XmlAttribute(name = SCALE)
	public Long getScale() {
		return scale;
	}

	public void setScale(final Long scale) {
		this.scale = scale;
	}

	@XmlAttribute(name = LENGTH)
	public Long getLength() {
		return length;
	}

	public void setLength(final Long length) {
		this.length = length;
	}

	@XmlAttribute(name = EDITOR_TYPE)
	public String getEditorType() {
		return editorType;
	}

	public void setEditorType(final String editorType) {
		this.editorType = editorType;
	}

	@XmlAttribute(name = TARGET_CLASS)
	public String getTargetClass() {
		return targetClass;
	}

	public void setTargetClass(final String targetClass) {
		this.targetClass = targetClass;
	}

	@XmlAttribute(name = LOOKUP_TYPE)
	public String getLookupType() {
		return lookupType;
	}

	public void setLookupType(final String lookupType) {
		this.lookupType = lookupType;
	}

	@XmlElement(name = FILTER, nillable = true)
	public Attribute.Filter getFilter() {
		return filter;
	}

	public void setFilter(final Attribute.Filter filter) {
		this.filter = filter;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}

		if (!(obj instanceof Attribute)) {
			return false;
		}

		final Attribute other = Attribute.class.cast(obj);
		return new EqualsBuilder() //
				.append(this.type, other.type) //
				.append(this.name, other.name) //
				.append(this.description, other.description) //
				.append(this.unique, other.unique) //
				.append(this.mandatory, other.mandatory) //
				.append(this.writable, other.writable) //
				.append(this.precision, other.precision) //
				.append(this.scale, other.scale) //
				.append(this.length, other.length) //
				.append(this.editorType, other.editorType) //
				.append(this.targetClass, other.targetClass) //
				.append(this.lookupType, other.lookupType) //
				.append(this.filter, other.filter) //
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder() //
				.append(type) //
				.append(name) //
				.append(description) //
				.append(unique) //
				.append(mandatory) //
				.append(writable) //
				.append(precision) //
				.append(scale) //
				.append(length) //
				.append(editorType) //
				.append(targetClass) //
				.append(lookupType) //
				.append(filter) //
				.toHashCode();
	}

	@Override
	public String toString() {
		return reflectionToString(this, SHORT_PREFIX_STYLE);
	}

}