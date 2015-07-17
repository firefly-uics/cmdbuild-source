package org.cmdbuild.logic.data.access.lock;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.cmdbuild.logic.data.access.lock.LockManager.Lockable;

class LockableInstanceActivity extends Lockable {

	private final Long instanceId;
	private final String activityId;

	LockableInstanceActivity(final Long instanceId, final String activityId) {
		this.instanceId = instanceId;
		this.activityId = activityId;
	}

	@Override
	protected boolean doEquals(final Object obj) {
		if (obj == this) {
			return true;
		}
		if (!(obj instanceof LockableInstanceActivity)) {
			return false;
		}
		final LockableInstanceActivity other = LockableInstanceActivity.class.cast(obj);
		return new EqualsBuilder() //
				.append(this.instanceId, other.instanceId) //
				.append(this.activityId, other.activityId) //
				.isEquals();
	}

	@Override
	protected int doHashCode() {
		return new HashCodeBuilder() //
				.append(LockableInstanceActivity.class) //
				.append(instanceId) //
				.append(activityId) //
				.toHashCode();
	}

}
