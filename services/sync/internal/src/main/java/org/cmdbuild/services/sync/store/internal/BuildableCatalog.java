package org.cmdbuild.services.sync.store.internal;

import java.util.Collection;
import java.util.Collections;

import static org.apache.commons.lang3.ObjectUtils.*;
import org.cmdbuild.services.sync.store.Type;

import static com.google.common.base.Predicates.*;
import static com.google.common.collect.FluentIterable.*;
import static com.google.common.collect.Iterables.*;
import static com.google.common.collect.Sets.*;

public class BuildableCatalog implements Catalog {

	public static class Builder implements org.apache.commons.lang3.builder.Builder<BuildableCatalog> {

		private static final Iterable<? extends Type> NO_TYPES = Collections.emptyList();

		public final Collection<Type> types = newHashSet();

		private Builder() {
			// use factory method
		}

		@Override
		public BuildableCatalog build() {
			validate();
			return new BuildableCatalog(this);
		}

		private void validate() {
			// TODO Auto-generated method stub
		}

		public Builder withTypes(final Iterable<? extends Type> types) {
			addAll(this.types, from(defaultIfNull(types, NO_TYPES)).filter(notNull()));
			return this;
		}

	}

	public static Builder newInstance() {
		return new Builder();
	}

	private final Iterable<Type> types;

	private BuildableCatalog(final Builder builder) {
		this.types = builder.types;
	}

	@Override
	public Iterable<Type> getTypes() {
		return types;
	}

}
