package support;

import org.cmdbuild.service.rest.Schema;
import org.cmdbuild.service.rest.dto.AttributeDetailResponse;
import org.cmdbuild.service.rest.dto.ClassDetailResponse;

public class ForwardingSchema implements Schema {

	private Schema inner;

	public ForwardingSchema() {
		this(null);
	}

	public ForwardingSchema(final Schema inner) {
		this.inner = inner;
	}

	public void setInner(final Schema inner) {
		this.inner = inner;
	}

	@Override
	public ClassDetailResponse getClasses(final boolean activeOnly) {
		return inner.getClasses(activeOnly);
	}

	@Override
	public AttributeDetailResponse getAttributes(final String name, final boolean activeOnly) {
		return inner.getAttributes(name, activeOnly);
	}

}
