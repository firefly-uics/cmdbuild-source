package org.cmdbuild.services.store;

import java.util.List;

import org.cmdbuild.services.store.Store.Storable;

public interface Store<T extends Storable> {

	public interface Storable {
		Long getIdentifier();
	}

	void create(T storable);

	T read(Long identifier);

	void update(T storable);

	void delete(T storable);

	List<T> list();
}
