package org.cmdbuild.report;

import java.math.BigDecimal;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

import net.sf.jasperreports.engine.JRParameter;

import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.dao.entrytype.CMEntryType;
import org.cmdbuild.dao.entrytype.attributetype.BooleanAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.CMAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.DateAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.DoubleAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.IntegerAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.StringAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.TimeAttributeType;
import org.cmdbuild.exception.ReportException.ReportExceptionType;
import org.cmdbuild.logger.Log;

public class RPSimple extends ReportParameter {

	protected RPSimple(final JRParameter jrParameter) {
		super();
		setJrParameter(jrParameter);
		if (getJrParameter() == null || getFullName() == null || getFullName().equals("")) {
			throw ReportExceptionType.REPORT_INVALID_PARAMETER_FORMAT.createException();
		}
	}

	@Override
	public void parseValue(final String newValue) {
		try {
			if (newValue != null && !newValue.equals("")) {

				if (getJrParameter().getValueClass() == String.class) {
					setValue(newValue);
				} else if (getJrParameter().getValueClass() == Integer.class
						|| getJrParameter().getValueClass() == Number.class) {
					setValue(Integer.parseInt(newValue));
				} else if (getJrParameter().getValueClass() == Long.class) {
					setValue(Long.parseLong(newValue));
				} else if (getJrParameter().getValueClass() == Short.class) {
					setValue(Short.parseShort(newValue));
				} else if (getJrParameter().getValueClass() == BigDecimal.class) {
					setValue(new BigDecimal(Integer.parseInt(newValue)));
				} else if (getJrParameter().getValueClass() == Date.class) {
					setValue(new SimpleDateFormat("dd/MM/yy").parse(newValue));
				} else if (getJrParameter().getValueClass() == Timestamp.class) {
					final Date date = new SimpleDateFormat("dd/MM/yy HH:mm:ss").parse(newValue);
					setValue(new Timestamp(date.getTime()));
				}

				else if (getJrParameter().getValueClass() == Time.class) {
					final Date date = new SimpleDateFormat("dd/MM/yy HH:mm:ss").parse(newValue);
					setValue(new Time(date.getTime()));
				}

				else if (getJrParameter().getValueClass() == Double.class) {
					setValue(Double.parseDouble(newValue));
				} else if (getJrParameter().getValueClass() == Float.class) {
					setValue(Float.parseFloat(newValue));
				} else if (getJrParameter().getValueClass() == Boolean.class) {
					setValue(Boolean.parseBoolean(newValue));
				} else {
					throw ReportExceptionType.REPORT_INVALID_PARAMETER_CLASS.createException();
				}
			}
		} catch (final Exception e) {
			Log.REPORT.error("Invalid parameter value \"" + newValue + "\" for \"" + getJrParameter().getValueClass()
					+ "\"", e);
			throw ReportExceptionType.REPORT_INVALID_PARAMETER_VALUE.createException();
		}
	}

	protected class ReportCMAttribute implements CMAttribute {

		final CMAttributeType<?> type;
		final String name, description, defaultValue;

		ReportCMAttribute(final CMAttributeType<?> type, final String name, final String description,
				final String defaultValue) {
			this.type = type;
			this.name = name;
			this.description = description;
			this.defaultValue = defaultValue;
		}

		@Override
		public boolean isActive() {
			return true;
		}

		@Override
		public CMEntryType getOwner() {
			return null;
		}

		@Override
		public CMAttributeType<?> getType() {
			return type;
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public String getDescription() {
			return description;
		}

		@Override
		public boolean isSystem() {
			return false;
		}

		@Override
		public boolean isInherited() {
			return false;
		}

		@Override
		public boolean isDisplayableInList() {
			return true;
		}

		@Override
		public boolean isMandatory() {
			return true;
		}

		@Override
		public boolean isUnique() {
			return false;
		}

		@Override
		public Mode getMode() {
			return Mode.WRITE;
		}

		@Override
		public int getIndex() {
			return 0;
		}

		@Override
		public String getDefaultValue() {
			if (hasDefaultValue()) {
				return defaultValue;
			}
			return "";
		}

		@Override
		public String getGroup() {
			return null;
		}

		@Override
		public int getClassOrder() {
			return 0;
		}

		@Override
		public String getEditorType() {
			return "";
		}

		@Override
		public String getForeignKeyDestinationClassName() {
			return "";
		}
	};

	@Override
	public CMAttribute createCMDBuildAttribute() {
		final CMAttributeType<?> type;
		final int length;

		// set class
		if (getJrParameter().getValueClass() == String.class) {
			type = new StringAttributeType(100);

		} else if (getJrParameter().getValueClass() == Integer.class || getJrParameter().getValueClass() == Long.class
				|| getJrParameter().getValueClass() == Short.class
				|| getJrParameter().getValueClass() == BigDecimal.class
				|| getJrParameter().getValueClass() == Number.class) {

			type = new IntegerAttributeType();
			length = 20;

		} else if (getJrParameter().getValueClass() == Date.class) {
			type = new DateAttributeType();

		} else if (getJrParameter().getValueClass() == Timestamp.class
				|| getJrParameter().getValueClass() == Time.class) {

			type = new TimeAttributeType();

		} else if (getJrParameter().getValueClass() == Double.class || getJrParameter().getValueClass() == Float.class) {
			type = new DoubleAttributeType();
			length = 20;

		} else if (getJrParameter().getValueClass() == Boolean.class) {
			type = new BooleanAttributeType();
		} else {
			throw ReportExceptionType.REPORT_INVALID_PARAMETER_CLASS.createException();
		}

		return new ReportCMAttribute(type, getName(), getDescription(), getDefaultValue());
	}
}
