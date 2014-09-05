package org.cmdbuild.service.rest.cxf;

import static org.apache.commons.lang3.StringUtils.equalsIgnoreCase;

import org.cmdbuild.service.rest.Attributes;
import org.cmdbuild.service.rest.ClassAttributes;
import org.cmdbuild.service.rest.DomainAttributes;
import org.cmdbuild.service.rest.ProcessAttributes;
import org.cmdbuild.service.rest.dto.AttributeDetail;
import org.cmdbuild.service.rest.dto.ListResponse;

public class CxfAttributes implements Attributes {

	private static enum Type {
		CLASS("class"), //
		DOMAIN("domain"), //
		PROCESS("process"), //
		UNKNOWN(null), //
		;

		private final String value;

		private Type(final String value) {
			this.value = value;
		}

		public static Type of(final String value) {
			for (final Type element : values()) {
				if (equalsIgnoreCase(element.value, value)) {
					return element;
				}
			}
			return UNKNOWN;
		}

	}

	private final ErrorHandler errorHandler;
	private final ClassAttributes classAttributes;
	private final DomainAttributes domainAttributes;
	private final ProcessAttributes processAttributes;

	public CxfAttributes(final ErrorHandler errorHandler, final ClassAttributes classAttributes,
			final DomainAttributes domainAttributes, final ProcessAttributes processAttributes) {
		this.errorHandler = errorHandler;
		this.classAttributes = classAttributes;
		this.domainAttributes = domainAttributes;
		this.processAttributes = processAttributes;
	}

	@Override
	public ListResponse<AttributeDetail> readAll(final String type, final String id, final boolean activeOnly,
			final Integer limit, final Integer offset) {
		final ListResponse<AttributeDetail> response;
		switch (Type.of(type)) {
		case CLASS:
			response = classAttributes.readAll(id, activeOnly, limit, offset);
			break;

		case DOMAIN:
			response = domainAttributes.readAll(id, activeOnly, limit, offset);
			break;

		case PROCESS:
			response = processAttributes.readAll(id, activeOnly, limit, offset);
			break;

		default:
			errorHandler.invalidType(type);
			response = null;
		}
		return response;
	}

}
