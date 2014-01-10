package support;

import org.cmdbuild.service.rest.Schema;
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
	public ClassDetailResponse getClasses() {
		return inner.getClasses();
	}

}
