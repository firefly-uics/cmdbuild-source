package org.cmdbuild.service.rest.dto;

import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Maps.transformValues;
import static com.google.common.collect.Maps.uniqueIndex;
import static org.cmdbuild.common.utils.guava.Functions.toKey;
import static org.cmdbuild.common.utils.guava.Functions.toValue;
import static org.cmdbuild.service.rest.constants.Serialization.NAME;
import static org.cmdbuild.service.rest.constants.Serialization.PROCESS_INSTANCE;
import static org.cmdbuild.service.rest.constants.Serialization.VALUES;

import java.util.Map;
import java.util.Map.Entry;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.cmdbuild.service.rest.dto.adapter.ProcessInstanceAdapter;

import com.google.common.base.Function;

@XmlRootElement(name = PROCESS_INSTANCE)
@XmlJavaTypeAdapter(ProcessInstanceAdapter.class)
public class ProcessInstance {

	public static class Builder implements org.apache.commons.lang3.builder.Builder<ProcessInstance> {

		private static final Function<Entry<? extends String, ? extends Object>, String> KEY = toKey();
		private static final Function<Entry<? extends String, ? extends Object>, Object> VALUE = toValue();

		private String name;
		private final Map<String, Object> values = newHashMap();

		private Builder() {
			// use static method
		}

		@Override
		public ProcessInstance build() {
			validate();
			return new ProcessInstance(this);
		}

		private void validate() {
			// TODO Auto-generated method stub
		}

		public Builder withName(final String name) {
			this.name = name;
			return this;
		}

		public Builder withValues(final Iterable<? extends Entry<String, ? extends Object>> values) {
			return withValues(transformValues(uniqueIndex(values, KEY), VALUE));
		}

		public Builder withValues(final Map<String, ? extends Object> values) {
			this.values.putAll(values);
			return this;
		}

	}

	public static Builder newInstance() {
		return new Builder();
	}

	ProcessInstance() {
		// package visibility
	}

	private String name;
	private Map<String, Object> values;

	private ProcessInstance(final Builder builder) {
		this.name = builder.name;
		this.values = builder.values;
	}

	@XmlAttribute(name = NAME)
	public String getName() {
		return name;
	}

	void setName(final String name) {
		this.name = name;
	}

	@XmlAttribute(name = VALUES)
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

		if (!(obj instanceof ProcessInstance)) {
			return false;
		}

		final ProcessInstance other = ProcessInstance.class.cast(obj);
		return new EqualsBuilder() //
				.append(this.name, other.name) //
				.append(this.values, other.values) //
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder() //
				.append(name) //
				.append(values) //
				.toHashCode();
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE).toString();
	}

}
