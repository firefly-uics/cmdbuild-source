package org.cmdbuild.service.rest.cxf.serialization;

import static com.google.common.collect.FluentIterable.from;
import static org.cmdbuild.data.store.lookup.Functions.toLookupId;
import static org.cmdbuild.data.store.lookup.Predicates.defaultLookup;
import static org.cmdbuild.service.rest.model.Models.newProcessWithFullDetails;

import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.data.store.lookup.Lookup;
import org.cmdbuild.service.rest.model.ProcessWithFullDetails;
import org.cmdbuild.workflow.LookupHelper;

import com.google.common.base.Function;
import com.google.common.base.Optional;

public class ToFullProcessDetail implements Function<CMClass, ProcessWithFullDetails> {

	public static class Builder implements org.apache.commons.lang3.builder.Builder<ToFullProcessDetail> {

		private LookupHelper lookupHelper;

		private Builder() {
			// use static method
		}

		public Builder withLookupHelper(final LookupHelper lookupHelper) {
			this.lookupHelper = lookupHelper;
			return this;
		}

		@Override
		public ToFullProcessDetail build() {
			return new ToFullProcessDetail(this);
		}

	}

	public static Builder newInstance() {
		return new Builder();
	}

	private final LookupHelper lookupHelper;

	private ToFullProcessDetail(final Builder builder) {
		this.lookupHelper = builder.lookupHelper;
	}

	@Override
	public ProcessWithFullDetails apply(final CMClass input) {
		final CMClass parent = input.getParent();
		final Iterable<Lookup> lookups = lookupHelper.allLookups();
		final Iterable<Long> allLookupIds = from(lookups) //
				.transform(toLookupId());
		final Optional<Long> firstLookup = from(allLookupIds).first();
		final Long _firstLookup = firstLookup.isPresent() ? firstLookup.get() : null;
		final Optional<Long> firstDefaultLookup = from(lookups) //
				.filter(defaultLookup()) //
				.transform(toLookupId()) //
				.first();
		final Long defaultStatus = firstDefaultLookup.isPresent() ? firstDefaultLookup.get() : _firstLookup;
		return newProcessWithFullDetails() //
				.withId(input.getName()) //
				.withName(input.getName()) //
				.withDescription(input.getDescription()) //
				.thatIsPrototype(input.isSuperclass()) //
				.withDescriptionAttributeName(input.getDescriptionAttributeName()) //
				.withStatuses(allLookupIds) //
				.withDefaultStatus(defaultStatus) //
				.withParent((parent == null) ? null : parent.getName()) //
				.build();
	}

}
