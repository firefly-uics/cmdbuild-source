package org.cmdbuild.logic.data.access.lock;

import com.google.common.base.Optional;

public interface LockableStore<M extends LockableStore.Metadata> {

	interface Metadata {

	}

	void add(Lockable lockable, M metadata);

	void remove(Lockable lockable);

	boolean isPresent(Lockable lockable);

	Optional<M> get(Lockable lockable);

	void removeAll();

}
