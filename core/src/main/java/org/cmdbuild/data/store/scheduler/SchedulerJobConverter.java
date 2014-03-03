package org.cmdbuild.data.store.scheduler;

import static org.cmdbuild.common.Constants.DESCRIPTION_ATTRIBUTE;

import java.util.Map;

import org.apache.commons.lang.Validate;
import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.data.store.DataViewStore.BaseStorableConverter;
import org.cmdbuild.model.scheduler.EmailServiceSchedulerJob;
import org.cmdbuild.model.scheduler.SchedulerJob;
import org.cmdbuild.model.scheduler.SchedulerJobVisitor;
import org.cmdbuild.model.scheduler.WorkflowSchedulerJob;

import com.google.common.collect.Maps;

public class SchedulerJobConverter extends BaseStorableConverter<SchedulerJob> {

	private static enum TypeHelper {

		emailService(TYPE_EMAIL) {

			@Override
			protected SchedulerJob create(final CMCard card) {
				return new EmailServiceSchedulerJob(card.getId());
			}

		}, //
		workflow(TYPE_WORKFLOW) {

			@Override
			protected SchedulerJob create(final CMCard card) {
				return new WorkflowSchedulerJob(card.getId());
			}

		}, //
		;

		public static SchedulerJob schedulerJobFrom(final CMCard card) {
			final String type = card.get(JOB_TYPE, String.class);
			for (final TypeHelper element : values()) {
				if (element.attributeValue.equals(type)) {
					return element.create(card);
				}
			}
			throw new IllegalArgumentException("unrecognized type");
		}

		public static TypeHelper of(final SchedulerJob storable) {
			return new SchedulerJobVisitor() {

				private TypeHelper element;

				public TypeHelper type() {
					storable.accept(this);
					Validate.notNull(element, "unrecognized type");
					return element;
				}

				@Override
				public void visit(final EmailServiceSchedulerJob schedulerJob) {
					element = emailService;
				}

				@Override
				public void visit(final WorkflowSchedulerJob schedulerJob) {
					element = workflow;
				}

			}.type();
		}

		private final String attributeValue;

		private TypeHelper(final String attributeValue) {
			this.attributeValue = attributeValue;
		}

		protected abstract SchedulerJob create(CMCard card);

	}

	private static final String CLASSNAME = "_SchedulerJob";

	public static final String CRON_EXPRESSION = "CronExpression";
	public static final String JOB_TYPE = "JobType";
	public static final String RUNNING = "Running";

	private static final String TYPE_EMAIL = "emailService";
	private static final String TYPE_WORKFLOW = "workflow";

	@Override
	public String getClassName() {
		return CLASSNAME;
	}

	@Override
	public SchedulerJob convert(final CMCard card) {
		final SchedulerJob storable = TypeHelper.schedulerJobFrom(card);
		storable.setDescription(card.get(DESCRIPTION_ATTRIBUTE, String.class));
		storable.setCronExpression(card.get(CRON_EXPRESSION, String.class));
		storable.setRunning(card.get(RUNNING, Boolean.class));
		return storable;
	}

	@Override
	public Map<String, Object> getValues(final SchedulerJob storable) {
		final Map<String, Object> values = Maps.newHashMap();
		values.put(DESCRIPTION_ATTRIBUTE, storable.getDescription());
		values.put(CRON_EXPRESSION, storable.getCronExpression());
		values.put(JOB_TYPE, TypeHelper.of(storable).attributeValue);
		values.put(RUNNING, storable.isRunning());
		return values;
	}

}
