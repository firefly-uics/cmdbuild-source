package org.cmdbuild.data.store.scheduler;

import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Maps.difference;
import static com.google.common.collect.Maps.uniqueIndex;
import static java.util.Arrays.asList;
import static org.apache.commons.lang.SystemUtils.LINE_SEPARATOR;
import static org.cmdbuild.data.store.scheduler.SchedulerJobParameterGroupable.of;

import java.util.Map;

import org.cmdbuild.data.store.Groupable;
import org.cmdbuild.data.store.Storable;
import org.cmdbuild.data.store.Store;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.MapDifference;
import com.google.common.collect.MapDifference.ValueDifference;

/**
 * This {@link Store} handles the saving process of {@link SchedulerJob}
 * elements saving its parameters using a dedicated {@link Store} for
 * {@link SchedulerJobParameter} elements.
 * 
 * @since 2.2
 */
public class AdvancedSchedulerJobStore implements Store<SchedulerJob> {

	private static final Marker MARKER = MarkerFactory.getMarker(AdvancedSchedulerJobStore.class.getName());

	public static final String WORKFLOW_PARAM_CLASSNAME = "classname";
	public static final String WORKFLOW_PARAM_ATTRIBUTES = "attributes";

	private static final String KEY_VALUE_SEPARATOR = "=";

	private static final Function<SchedulerJobParameter, String> BY_NAME = new Function<SchedulerJobParameter, String>() {

		@Override
		public String apply(final SchedulerJobParameter input) {
			return input.getKey();
		}

	};

	private static abstract class Action {

		protected final Store<SchedulerJob> schedulerJobStore;
		protected final Store<SchedulerJobParameter> schedulerJobParameterStore;

		protected Action(final Store<SchedulerJob> schedulerJobStore,
				final Store<SchedulerJobParameter> schedulerJobParameterStore) {
			this.schedulerJobStore = schedulerJobStore;
			this.schedulerJobParameterStore = schedulerJobParameterStore;
		}

		protected void parametersToFields(final WorkflowSchedulerJob schedulerJob) {
			final Iterable<SchedulerJobParameter> parameters = schedulerJobParameterStore.list(of(schedulerJob));
			final Map<String, SchedulerJobParameter> parametersByName = uniqueIndex(parameters, BY_NAME);
			if (parametersByName.containsKey(WORKFLOW_PARAM_CLASSNAME)) {
				schedulerJob.setProcessClass(parametersByName.get(WORKFLOW_PARAM_CLASSNAME).getValue());
			}
			if (parametersByName.containsKey(WORKFLOW_PARAM_ATTRIBUTES)) {
				schedulerJob.setParameters(Splitter.on(LINE_SEPARATOR) //
						.withKeyValueSeparator(KEY_VALUE_SEPARATOR) //
						.split(parametersByName.get(WORKFLOW_PARAM_ATTRIBUTES).getValue()));
			}
		}

	}

	private static class Create extends Action implements SchedulerJobVisitor {

		public Create(final Store<SchedulerJob> schedulerJobStore,
				final Store<SchedulerJobParameter> schedulerJobParameterStore) {
			super(schedulerJobStore, schedulerJobParameterStore);
		}

		public Storable execute(final SchedulerJob storable) {
			final Storable created = schedulerJobStore.create(storable);
			final SchedulerJob readed = schedulerJobStore.read(created);
			readed.accept(this);
			return created;
		}

		@Override
		public void visit(final EmailServiceSchedulerJob schedulerJob) {
			throw new UnsupportedOperationException("TODO");
		}

		@Override
		public void visit(final WorkflowSchedulerJob schedulerJob) {
			schedulerJobParameterStore.create(SchedulerJobParameter.newInstance() //
					.withOwner(schedulerJob.getId()) //
					.withKey(WORKFLOW_PARAM_CLASSNAME) //
					.withValue(schedulerJob.getProcessClass()) //
					.build());
			schedulerJobParameterStore.create(SchedulerJobParameter.newInstance() //
					.withOwner(schedulerJob.getId()) //
					.withKey(WORKFLOW_PARAM_ATTRIBUTES) //
					.withValue(Joiner.on(LINE_SEPARATOR) //
							.withKeyValueSeparator(KEY_VALUE_SEPARATOR) //
							.join(schedulerJob.getParameters())) //
					.build());
		}

	}

	private static class Read extends Action implements SchedulerJobVisitor {

		public Read(final Store<SchedulerJob> schedulerJobStore,
				final Store<SchedulerJobParameter> schedulerJobParameterStore) {
			super(schedulerJobStore, schedulerJobParameterStore);
		}

		public SchedulerJob execute(final Storable storable) {
			final SchedulerJob readed = schedulerJobStore.read(storable);
			readed.accept(this);
			return readed;
		}

		@Override
		public void visit(final EmailServiceSchedulerJob schedulerJob) {
			throw new UnsupportedOperationException("TODO");
		}

		@Override
		public void visit(final WorkflowSchedulerJob schedulerJob) {
			parametersToFields(schedulerJob);
		}

	}

	private static class Update extends Action implements SchedulerJobVisitor {

		public Update(final Store<SchedulerJob> schedulerJobStore,
				final Store<SchedulerJobParameter> schedulerJobParameterStore) {
			super(schedulerJobStore, schedulerJobParameterStore);
		}

		void execute(final SchedulerJob storable) {
			schedulerJobStore.update(storable);
			storable.accept(this);
		}

		@Override
		public void visit(final EmailServiceSchedulerJob schedulerJob) {
			throw new UnsupportedOperationException("TODO");
		}

		@Override
		public void visit(final WorkflowSchedulerJob schedulerJob) {
			final java.util.List<SchedulerJobParameter> updatedParameters = asList( //
					SchedulerJobParameter.newInstance() //
							.withOwner(schedulerJob.getId()) //
							.withKey(WORKFLOW_PARAM_CLASSNAME) //
							.withValue(schedulerJob.getProcessClass()) //
							.build(), //
					SchedulerJobParameter.newInstance() //
							.withOwner(schedulerJob.getId()) //
							.withKey(WORKFLOW_PARAM_ATTRIBUTES) //
							.withValue(Joiner.on(LINE_SEPARATOR) //
									.withKeyValueSeparator(KEY_VALUE_SEPARATOR) //
									.join(schedulerJob.getParameters())) //
							.build() //
			);
			final Map<String, SchedulerJobParameter> updatedParametersByName = uniqueIndex(updatedParameters, BY_NAME);
			final Iterable<SchedulerJobParameter> readedParameters = schedulerJobParameterStore.list(of(schedulerJob));
			final Map<String, SchedulerJobParameter> readedParametersByName = uniqueIndex(readedParameters, BY_NAME);
			final MapDifference<String, SchedulerJobParameter> difference = difference(updatedParametersByName,
					readedParametersByName);
			for (final SchedulerJobParameter element : difference.entriesOnlyOnLeft().values()) {
				schedulerJobParameterStore.create(element);
			}
			for (final ValueDifference<SchedulerJobParameter> valueDifference : difference.entriesDiffering().values()) {
				final SchedulerJobParameter element = valueDifference.leftValue();
				schedulerJobParameterStore.update(element);
			}
			for (final SchedulerJobParameter element : difference.entriesOnlyOnRight().values()) {
				schedulerJobParameterStore.delete(element);
			}
		}

	}

	private static class Delete extends Action {

		public Delete(final Store<SchedulerJob> schedulerJobStore,
				final Store<SchedulerJobParameter> schedulerJobParameterStore) {
			super(schedulerJobStore, schedulerJobParameterStore);
		}

		void execute(final Storable storable) {
			final SchedulerJob readed = schedulerJobStore.read(storable);

			for (final SchedulerJobParameter element : schedulerJobParameterStore.list(of(readed))) {
				schedulerJobParameterStore.delete(element);
			}

			schedulerJobStore.delete(storable);
		}

	}

	private static class List extends Action implements SchedulerJobVisitor {

		public List(final Store<SchedulerJob> schedulerJobStore,
				final Store<SchedulerJobParameter> schedulerJobParameterStore) {
			super(schedulerJobStore, schedulerJobParameterStore);
		}

		java.util.List<SchedulerJob> execute() {
			return from(schedulerJobStore.list()) //
					.transform(addParameters()) //
					.toImmutableList();
		}

		public java.util.List<SchedulerJob> execute(final Groupable groupable) {
			return from(schedulerJobStore.list(groupable)) //
					.transform(addParameters()) //
					.toImmutableList();
		}

		private Function<SchedulerJob, SchedulerJob> addParameters() {
			return new Function<SchedulerJob, SchedulerJob>() {

				@Override
				public SchedulerJob apply(final SchedulerJob input) {
					input.accept(List.this);
					return input;
				}

			};
		}

		@Override
		public void visit(final EmailServiceSchedulerJob schedulerJob) {
			throw new UnsupportedOperationException("TODO");
		}

		@Override
		public void visit(final WorkflowSchedulerJob schedulerJob) {
			parametersToFields(schedulerJob);
		}

	}

	private final Create create;
	private final Read read;
	private final Update update;
	private final Delete delete;
	private final List list;

	public AdvancedSchedulerJobStore(final Store<SchedulerJob> schedulerJobStore,
			final Store<SchedulerJobParameter> schedulerJobParameterStore) {
		this.create = new Create(schedulerJobStore, schedulerJobParameterStore);
		this.read = new Read(schedulerJobStore, schedulerJobParameterStore);
		this.update = new Update(schedulerJobStore, schedulerJobParameterStore);
		this.delete = new Delete(schedulerJobStore, schedulerJobParameterStore);
		this.list = new List(schedulerJobStore, schedulerJobParameterStore);
	}

	@Override
	public Storable create(final SchedulerJob storable) {
		logger.info(MARKER, "creating new element '{}'", storable);
		return create.execute(storable);
	}

	@Override
	public SchedulerJob read(final Storable storable) {
		logger.info(MARKER, "reading existing element '{}'", storable);
		return read.execute(storable);
	}

	@Override
	public void update(final SchedulerJob storable) {
		logger.info(MARKER, "updating existing element '{}'", storable);
		update.execute(storable);
	}

	@Override
	public void delete(final Storable storable) {
		logger.info(MARKER, "deleting existing element '{}'", storable);
		delete.execute(storable);
	}

	@Override
	public java.util.List<SchedulerJob> list() {
		logger.info(MARKER, "getting all existing elements");
		return list.execute();
	}

	@Override
	public java.util.List<SchedulerJob> list(final Groupable groupable) {
		logger.info(MARKER, "getting all existing elements for group '{}'", groupable);
		return list.execute(groupable);
	}

}
