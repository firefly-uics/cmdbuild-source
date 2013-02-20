package org.cmdbuild.services.store;

import java.util.List;

import org.cmdbuild.services.store.Store.Storable;

public interface Store<T extends Storable> {

	public interface Storable {

		Long getId();

	}

	void create(T storable);

	T read(Storable id);

	void update(T storable);

	void delete(Storable id);

	List<T> list();

}
