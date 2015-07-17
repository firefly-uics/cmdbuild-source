package org.cmdbuild.logic.data.access.lock;

import static org.apache.commons.lang3.builder.ToStringBuilder.reflectionToString;
import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

public interface LockManager {

	abstract class Lockable {

		@Override
		public final boolean equals(final Object obj) {
			return doEquals(obj);
		}

		protected abstract boolean doEquals(Object obj);

		@Override
		public final int hashCode() {
			return doHashCode();
		}

		protected abstract int doHashCode();

		@Override
		public final String toString() {
			return reflectionToString(this, SHORT_PREFIX_STYLE);
		}

	}

	void lock(Lockable lockable);

	void unlock(Lockable lockable);

	void unlockAll();

	void checkNotLocked(Lockable lockable);

	void checkLockedbyUser(Lockable lockable, String userName);

}
