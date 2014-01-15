package support;

import org.cmdbuild.service.rest.Data;
import org.cmdbuild.service.rest.dto.data.CardDetailResponse;

public class ForwardingData implements Data {

	private Data inner;

	public ForwardingData() {
		this(null);
	}

	public ForwardingData(final Data inner) {
		this.inner = inner;
	}

	public void setInner(final Data inner) {
		this.inner = inner;
	}

	@Override
	public CardDetailResponse getCards(final String name) {
		return inner.getCards(name);
	}

}
