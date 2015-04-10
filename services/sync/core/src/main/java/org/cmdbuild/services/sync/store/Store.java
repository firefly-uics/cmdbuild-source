package org.cmdbuild.services.sync.store;

public interface Store {

	void create(Entry<? extends Type> entry);

	Iterable<Entry<?>> readAll();

	void update(Entry<? extends Type> entry);

	void delete(Entry<? extends Type> entry);

}
