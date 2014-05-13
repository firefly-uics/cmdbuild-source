package org.cmdbuild.services.sync.store;

import static com.google.common.collect.Maps.difference;
import static com.google.common.collect.Maps.uniqueIndex;

import java.util.Map;

import org.apache.commons.lang3.Validate;
import org.cmdbuild.services.sync.Synchronizer;

import com.google.common.base.Function;
import com.google.common.collect.MapDifference;
import com.google.common.collect.MapDifference.ValueDifference;

public class StoreSynchronizer implements Synchronizer {

	public static class Builder implements org.apache.commons.lang3.builder.Builder<StoreSynchronizer> {

		private Store left;
		private Store right;
		private Store target;

		private Builder() {
			// use factory method
		}

		@Override
		public StoreSynchronizer build() {
			validate();
			return new StoreSynchronizer(this);
		}

		private void validate() {
			Validate.notNull(left, "missing left");
			Validate.notNull(right, "missing right");
			Validate.notNull(target, "missing target");
		}

		public Builder setLeft(final Store left) {
			this.left = left;
			return this;
		}

		public Builder setRight(final Store right) {
			this.right = right;
			return this;
		}

		public Builder setTarget(final Store target) {
			this.target = target;
			return this;
		}

	}

	public static Builder newInstance() {
		return new Builder();
	}

	private static Function<? super Entry<?>, Key> BY_KEY = new Function<Entry<?>, Key>() {

		@Override
		public Key apply(final Entry<?> input) {
			return input.getKey();
		}

	};

	private final Store left;
	private final Store right;
	private final Store target;

	private StoreSynchronizer(final Builder builder) {
		this.left = builder.left;
		this.right = builder.right;
		this.target = builder.target;
	}

	@Override
	public void sync() {
		final Map<Key, Entry<?>> sourceData = uniqueIndex(left.readAll(), BY_KEY);
		final Map<Key, Entry<?>> targetData = uniqueIndex(right.readAll(), BY_KEY);

		final MapDifference<Key, Entry<?>> mapDifference = difference(sourceData, targetData);
		final Map<Key, Entry<?>> toCreate = mapDifference.entriesOnlyOnLeft();
		final Map<Key, ValueDifference<Entry<?>>> toUpdate = mapDifference.entriesDiffering();
		final Map<Key, Entry<?>> toRemove = mapDifference.entriesOnlyOnRight();

		for (final Map.Entry<Key, Entry<?>> element : toCreate.entrySet()) {
			final Entry<?> entry = element.getValue();
			target.create(entry);
		}
		for (final Map.Entry<Key, ValueDifference<Entry<?>>> element : toUpdate.entrySet()) {
			final ValueDifference<Entry<?>> valueDifference = element.getValue();
			final Entry<?> entry = valueDifference.leftValue();
			target.update(entry);
		}
		for (final Map.Entry<Key, Entry<?>> element : toRemove.entrySet()) {
			final Entry<?> entry = element.getValue();
			target.delete(entry);
		}
	}

}
