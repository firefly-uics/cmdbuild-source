package org.cmdbuild.report;

import static org.apache.commons.lang.RandomStringUtils.randomNumeric;
import static org.cmdbuild.services.store.report.JDBCReportStore.REPORT_CLASS_NAME;
import groovy.lang.GroovyShell;
import groovy.lang.Script;

import java.sql.Time;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JRPropertiesMap;

import org.cmdbuild.common.Constants;
import org.cmdbuild.common.utils.UnsupportedProxyFactory;
import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.dao.entrytype.CMEntryType;
import org.cmdbuild.dao.entrytype.CMIdentifier;
import org.cmdbuild.dao.entrytype.ForwardingEntryType;
import org.cmdbuild.dao.entrytype.attributetype.CMAttributeType;
import org.cmdbuild.exception.ReportException.ReportExceptionType;

/**
 * 
 * Wrapper for user-defined Jasper Parameter
 * 
 * AVAILABLE FORMATS FOR JRPARAMETER NAME 1) reference: "label.class.attribute"
 * - ie: User.Users.Description 2) lookup: "label.lookup.lookuptype" - ie:
 * Brand.Lookup.Brands 3) simple: "label" - ie: My parameter
 * 
 * Notes: - The description property overrides the label value - Reference or
 * lookup parameters will always be integers while simple parameters will match
 * original parameter class - All custom parameters are required; set a property
 * (in iReport) with name="required" and value="false" to override
 * 
 */
public abstract class ReportParameter {

	private JRParameter jrParameter;
	private Object parameterValue;

	// regular expression matching lookup and reference parameters format
	protected static final String regExpLR = "[\\w\\s]*\\.\\w*\\.[\\w\\s]*";

	// create the right subclass
	public static ReportParameter parseJrParameter(final JRParameter jrParameter) {
		if (jrParameter == null || jrParameter.getName() == null || jrParameter.getName().equals("")) {
			throw ReportExceptionType.REPORT_INVALID_PARAMETER_FORMAT.createException();
		}

		final String iReportParamName = jrParameter.getName();
		if (iReportParamName.indexOf(".") == -1) {
			return new RPSimple(jrParameter);
		} else {
			if (!iReportParamName.matches(regExpLR)) {
				throw ReportExceptionType.REPORT_INVALID_PARAMETER_FORMAT.createException();
			}

			final String[] split = iReportParamName.split("\\.");
			if (split[1].equalsIgnoreCase("lookup")) {
				return new RPLookup(jrParameter);
			} else {
				return new RPReference(jrParameter);
			}
		}
	}

	abstract public CMAttributeType<?> getCMAttributeType();

	public CMAttribute createCMDBuildAttribute() {
		return new ReportCMAttribute(getCMAttributeType(), this);
	}

	public String getDefaultValue() {
		if (jrParameter.getDefaultValueExpression() != null) {
			final GroovyShell shell = new GroovyShell();
			final Script sc = shell.parse(jrParameter.getDefaultValueExpression().getText());
			final Object result = sc.run();

			if (result != null) {
				// date
				if (jrParameter.getValueClass() == Date.class) {
					final SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_FOUR_DIGIT_YEAR_FORMAT);
					return sdf.format(result);
				}

				// timestamp
				else if (jrParameter.getValueClass() == Timestamp.class || jrParameter.getValueClass() == Time.class) {
					final SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATETIME_TWO_DIGIT_YEAR_FORMAT);
					return sdf.format(result);
				}

				// other
				return result.toString();

			}
		}
		return null;
	}

	public boolean hasDefaultValue() {
		return (jrParameter.getDefaultValueExpression() != null
				&& jrParameter.getDefaultValueExpression().getText() != null && !jrParameter
				.getDefaultValueExpression().getText().equals(""));
	}

	public void setJrParameter(final JRParameter jrParameter) {
		this.jrParameter = jrParameter;
	}

	public JRParameter getJrParameter() {
		return jrParameter;
	}

	public String getName() {
		return getFullNameSplit()[0];
	}

	public String getFullName() {
		return jrParameter.getName();
	}

	public String[] getFullNameSplit() {
		return getFullName().split("\\.");
	}

	public String getDescription() {
		final String desc = jrParameter.getDescription();
		if (desc == null || desc.equals("")) {
			return getName();
		} else {
			return desc;
		}
	}

	public void parseValue(final String newValue) {
		setValue(newValue);
	}

	public void setValue(final Object parameterValue) {
		this.parameterValue = parameterValue;
	}

	public Object getValue() {
		return parameterValue;
	}

	public boolean isRequired() {
		final JRPropertiesMap properties = jrParameter.getPropertiesMap();
		final String required = properties.getProperty("required");
		if (required != null && required.equalsIgnoreCase("false")) {
			return false;
		}
		return true;
	}

	/*
	 * CMAttribute representation of ReportParameter
	 */
	private static class ReportCMAttribute implements CMAttribute {

		private static final CMEntryType UNSUPPORTED = UnsupportedProxyFactory.of(CMEntryType.class).create();
		private static final CMEntryType OWNER = new ForwardingEntryType(UNSUPPORTED) {

			private final long FAKE_ID = 0L;

			/**
			 * This {@link CMIdentifier} is completely fake but it's formally
			 * correct. It has been created to avoid problems with attributes
			 * serialization.
			 */
			private final CMIdentifier FAKE_IDENTIFIER = new CMIdentifier() {

				private final String localname = REPORT_CLASS_NAME + "_" + randomNumeric(10);
				private final String namespace = REPORT_CLASS_NAME + "_" + randomNumeric(10);

				@Override
				public String getLocalName() {
					return localname;
				}

				@Override
				public String getNameSpace() {
					return namespace;
				}

			};

			/*
			 * Should be the only methods called.
			 */

			@Override
			public Long getId() {
				return FAKE_ID;
			};

			@Override
			public CMIdentifier getIdentifier() {
				return FAKE_IDENTIFIER;
			};

		};

		private final CMAttributeType<?> type;
		private final ReportParameter rp;

		public ReportCMAttribute(final CMAttributeType<?> type, final ReportParameter rp) {
			this.type = type;
			this.rp = rp;
		}

		@Override
		public boolean isActive() {
			return true;
		}

		@Override
		public CMEntryType getOwner() {
			return OWNER;
		}

		@Override
		public CMAttributeType<?> getType() {
			return type;
		}

		@Override
		public String getName() {
			return rp.getFullName();
		}

		@Override
		public String getDescription() {
			return rp.getDescription();
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
			return rp.isRequired();
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
			if (rp.hasDefaultValue()) {
				return rp.getDefaultValue();
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
		public String getFilter() {
			return "";
		}

		@Override
		public String getForeignKeyDestinationClassName() {
			return "";
		}
	};
}