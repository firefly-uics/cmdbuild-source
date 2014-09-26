package org.cmdbuild.service.rest.cxf.serialization;

import static com.google.common.collect.Maps.transformEntries;
import static com.google.common.collect.Maps.transformValues;
import static com.google.common.collect.Maps.uniqueIndex;
import static org.cmdbuild.common.utils.guava.Functions.toKey;
import static org.cmdbuild.common.utils.guava.Functions.toValue;
import static org.cmdbuild.service.rest.dto.Builders.newProcessInstance;

import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.Validate;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.service.rest.dto.ProcessInstance;
import org.cmdbuild.workflow.user.UserProcessInstance;

import com.google.common.base.Function;
import com.google.common.collect.Maps.EntryTransformer;

public class ToProcessInstance implements Function<UserProcessInstance, ProcessInstance> {

	public static class Builder implements org.apache.commons.lang3.builder.Builder<ToProcessInstance> {

		private CMClass type;

		private Builder() {
			// use static method
		}

		@Override
		public ToProcessInstance build() {
			validate();
			return new ToProcessInstance(this);
		}

		private void validate() {
			Validate.notNull(type, "missing '%s'", CMClass.class);
		}

		public Builder withType(final CMClass type) {
			this.type = type;
			return this;
		}

	}

	public static Builder newInstance() {
		return new Builder();
	}

	private static final Function<Entry<? extends String, ? extends Object>, String> KEY = toKey();
	private static final Function<Entry<? extends String, ? extends Object>, Object> VALUE = toValue();

	private final CMClass type;

	private ToProcessInstance(final Builder builder) {
		this.type = builder.type;
	}

	@Override
	public ProcessInstance apply(final UserProcessInstance input) {
		final Map<String, Object> values = transformValues(uniqueIndex(input.getAllValues(), KEY), VALUE);
		final EntryTransformer<String, Object, Object> transformer = new EntryTransformer<String, Object, Object>() {

			@Override
			public Object transformEntry(final String key, final Object value) {
				return ValueConverter.newInstance() //
						.withType(type) //
						.build() //
						.convert(key, value);
			}

		};
		return newProcessInstance() //
				.withType(input.getType().getName()) //
				.withId(input.getId()) //
				.withName(input.getProcessInstanceId()) //
				.withValues(transformEntries(values, transformer)) //
				.build();
	}

}
