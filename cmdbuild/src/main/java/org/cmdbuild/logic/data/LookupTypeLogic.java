package org.cmdbuild.logic.data;

import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.services.store.DataViewStore;
import org.cmdbuild.services.store.Store;
import org.cmdbuild.services.store.Store.Storable;

import com.google.common.base.Predicate;
import com.google.common.collect.Sets;

import static com.google.common.collect.Iterables.*;

public class LookupTypeLogic {

	public static final class LookupTypeDto implements Storable {

		public final String name;
		public final String parentType;

		private final transient int hashCode;
		private final transient String toString;

		public LookupTypeDto(final String name, final String parentType) {
			this.name = name;
			this.parentType = parentType;

			this.hashCode = new HashCodeBuilder() //
					.append(this.name) //
					.append(this.parentType) //
					.toHashCode();
			this.toString = new ToStringBuilder(this, ToStringStyle.SIMPLE_STYLE) //
					.append("name", name) //
					.append("parent", parentType) //
					.toString();
		}

		@Override
		public String getIdentifier() {
			return name;
		}

		@Override
		public int hashCode() {
			return hashCode;
		}

		@Override
		public boolean equals(final Object obj) {
			if (obj == this) {
				return true;
			}
			if (!(obj instanceof LookupTypeDto)) {
				return false;
			}
			final LookupTypeDto other = LookupTypeDto.class.cast(obj);
			return new EqualsBuilder() //
					.append(name, other.name) //
					.append(parentType, other.parentType) //
					.isEquals();
		}

		@Override
		public String toString() {
			return toString;
		}

	}

	private final Store<LookupTypeDto> store;

	public LookupTypeLogic(final CMDataView view) {
		store = new DataViewStore<LookupTypeDto>(view, new DataViewStore.BaseStorableConverter<LookupTypeDto>() {

			private static final String LOOKUP_TABLE_NAME = "LookUp";

			@Override
			public String getClassName() {
				return LOOKUP_TABLE_NAME;
			}

			@Override
			public LookupTypeDto convert(final CMCard card) {
				final String type = card.get("Type", String.class);
				final String parentType = card.get("ParentType", String.class);
				return new LookupTypeDto(type, parentType);
			}

			@Override
			public Map<String, Object> getValues(final LookupTypeDto storable) {
				// TODO Auto-generated method stub
				return null;
			}

		});
	}

	public Iterable<LookupTypeDto> getAllTypes() {
		final Iterable<LookupTypeDto> allWithDuplicateTypes = store.list();
		return filter(allWithDuplicateTypes, new Predicate<LookupTypeDto>() {

			private final Set<LookupTypeDto> uniques = Sets.newHashSet();

			@Override
			public boolean apply(final LookupTypeDto input) {
				return uniques.add(input);
			}
		});
	}

}
