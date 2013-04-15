package org.cmdbuild.workflow.api;

import static org.apache.commons.lang.StringUtils.EMPTY;
import static org.apache.commons.lang.StringUtils.isBlank;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.cmdbuild.api.fluent.ws.WsFluentApiExecutor.WsType;
import org.cmdbuild.common.Constants;
import org.cmdbuild.workflow.SharkTypeDefaults;
import org.cmdbuild.workflow.type.LookupType;
import org.cmdbuild.workflow.type.ReferenceType;

public abstract class SharkWsTypeConverter {

	protected final WorkflowApi workflowApi;

	public SharkWsTypeConverter(final WorkflowApi workflowApi) {
		this.workflowApi = workflowApi;
	}

	protected String toWsType(final WsType wsType, final Object value) {
		if (value == null) {
			return EMPTY;
		}
		try {
			return unsafeToWsType(wsType, value);
		} catch (final RuntimeException e) {
			// TODO log this failure
			return value.toString();
		}
	}

	private String unsafeToWsType(final WsType wsType, final Object value) {
		switch (wsType) {
		case DATE:
		case TIMESTAMP:
		case TIME:
			return wsDateFormat().format((Date) value);

		case FOREIGNKEY:
		case REFERENCE:
			final ReferenceType reference = (ReferenceType) value;
			if (reference.checkValidity()) {
				return Integer.toString(reference.getId());
			} else {
				return EMPTY;
			}

		case LOOKUP:
			final LookupType lookup = (LookupType) value;
			if (lookup.checkValidity()) {
				return Integer.toString(lookup.getId());
			} else {
				return EMPTY;
			}

		default:
			return value.toString();
		}
	}

	protected Object toClientType(final WsType wsType, final String wsValue) {
		try {
			return unsafeToClientType(wsType, wsValue);
		} catch (final Exception e) {
			throw new IllegalArgumentException("unexpected value format", e);
		}
	}

	protected Object unsafeToClientType(final WsType wsType, final String wsValue) throws NumberFormatException,
			ParseException {
		switch (wsType) {
		case BOOLEAN:
			return isBlank(wsValue) ? SharkTypeDefaults.defaultBoolean() : Boolean.parseBoolean(wsValue);

		case DATE:
		case TIMESTAMP:
		case TIME:
			return isBlank(wsValue) ? SharkTypeDefaults.defaultDate() : wsDateFormat().parse(wsValue);

		case DECIMAL:
		case DOUBLE:
			return isBlank(wsValue) ? SharkTypeDefaults.defaultDouble() : Double.parseDouble(wsValue);

		case FOREIGNKEY:
		case REFERENCE:
			return isBlank(wsValue) ? SharkTypeDefaults.defaultReference() : referenceType(wsValue);

		case INTEGER:
			return isBlank(wsValue) ? SharkTypeDefaults.defaultInteger() : Long.parseLong(wsValue);

		case LOOKUP:
			return isBlank(wsValue) ? SharkTypeDefaults.defaultLookup() : lookupType(wsValue);

		case CHAR:
		case STRING:
		case TEXT:
			return isBlank(wsValue) ? SharkTypeDefaults.defaultString() : wsValue;

		default:
			return wsValue;
		}
	}

	private SimpleDateFormat wsDateFormat() {
		return new SimpleDateFormat(Constants.SOAP_ALL_DATES_PRINTING_PATTERN);
	}

	private ReferenceType referenceType(final String wsValue) {
		final Integer id = Integer.parseInt(wsValue);
		return workflowApi.referenceTypeFrom(id);
	}

	private LookupType lookupType(final String wsValue) {
		final Integer id = Integer.parseInt(wsValue);
		return workflowApi.selectLookupById(id);
	}

}