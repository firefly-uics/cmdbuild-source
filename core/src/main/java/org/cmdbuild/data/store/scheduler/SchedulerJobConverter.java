package org.cmdbuild.data.store.scheduler;

import static org.cmdbuild.common.Constants.DESCRIPTION_ATTRIBUTE;

import java.util.Map;

import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.data.store.DataViewStore.BaseStorableConverter;
import org.cmdbuild.exception.ORMException.ORMExceptionType;
import org.cmdbuild.model.scheduler.SchedulerJob;
import org.cmdbuild.model.scheduler.SchedulerJob.Type;

import com.google.common.collect.Maps;

public class SchedulerJobConverter extends BaseStorableConverter<SchedulerJob> {

	private static final String CLASSNAME = "_SchedulerJob";

	public static final String NOTES = "Notes";
	public static final String CRON_EXPRESSION = "CronExpression";
	public static final String DETAIL = "Detail";
	public static final String JOB_TYPE = "JobType";
	public static final String RUNNING = "Running";

	private static final String KEY_VALUE_SEPARATOR = "=";
	private static final String LF = "\n";

	@Override
	public String getClassName() {
		return CLASSNAME;
	}

	@Override
	public SchedulerJob convert(final CMCard card) {
		final SchedulerJob storable = new SchedulerJob(card.getId());
		storable.setDescription(card.get(DESCRIPTION_ATTRIBUTE, String.class));
		storable.setLegacyParameters(toMap(card.get(NOTES, String.class)));
		storable.setCronExpression(card.get(CRON_EXPRESSION, String.class));
		storable.setDetail(card.get(DETAIL, String.class));
		storable.setType(toType(card.get(JOB_TYPE, String.class)));
		storable.setRunning(card.get(RUNNING, Boolean.class));
		return storable;
	}

	private Map<String, String> toMap(final String value) {
		final Map<String, String> params = Maps.newHashMap();
		if (value != null) {
			for (final String paramLine : value.split(LF)) {
				final String[] paramArray = paramLine.split(KEY_VALUE_SEPARATOR, 2);
				if (paramArray.length == 2) {
					params.put(paramArray[0], paramArray[1]);
				}
			}
		}
		return params;
	}

	private Type toType(final String value) {
		for (final Type type : Type.values()) {
			if (type.name().equals(value)) {
				return type;
			}
		}
		logger.warn("unknown type '{}'", value);
		return null;
	}

	@Override
	public Map<String, Object> getValues(final SchedulerJob storable) {
		final Map<String, Object> values = Maps.newHashMap();
		values.put(DESCRIPTION_ATTRIBUTE, storable.getDescription());
		values.put(NOTES, toString(storable.getLegacyParameters()));
		values.put(CRON_EXPRESSION, storable.getCronExpression());
		values.put(DETAIL, storable.getDetail());
		values.put(JOB_TYPE, toString(storable.getType()));
		values.put(RUNNING, storable.isRunning());
		return values;
	}

	private String toString(final Map<String, String> params) {
		final StringBuilder stringBuilder = new StringBuilder();
		if (params != null) {
			for (final String key : params.keySet()) {
				final String value = params.get(key);
				if (value == null || value.contains(LF)) {
					throw ORMExceptionType.ORM_TYPE_ERROR.createException();
				}
				stringBuilder.append(key).append(KEY_VALUE_SEPARATOR).append(value).append(LF);
			}
		}
		return stringBuilder.toString();
	}

	private String toString(final Type type) {
		return (type == null) ? null : type.name();
	}

}
