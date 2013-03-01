package org.cmdbuild.services.store;

import java.util.List;

import org.cmdbuild.services.store.Store.Storable;

public interface Store<T extends Storable> {

	public interface Storable {

		String getIdentifier();

	}

	T create(T storable);

	T read(Storable storable);

	void update(T storable);

	void delete(Storable storable);

	List<T> list();

}
