package org.cmdbuild.logic.data.access.lock;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.cmdbuild.logic.data.access.lock.LockManager.Lockable;

class LockableCard extends Lockable {

	private final Long id;

	LockableCard(final Long id) {
		this.id = id;
	}

	@Override
	protected boolean doEquals(final Object obj) {
		if (obj == this) {
			return true;
		}
		if (!(obj instanceof LockableCard)) {
			return false;
		}
		final LockableCard other = LockableCard.class.cast(obj);
		return new EqualsBuilder() //
				.append(this.id, other.id) //
				.isEquals();
	}

	@Override
	protected int doHashCode() {
		return new HashCodeBuilder() //
				.append(LockableCard.class) //
				.append(id) //
				.toHashCode();
	}

}
