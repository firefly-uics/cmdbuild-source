package org.cmdbuild.data.store.lookup;

import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.data.store.DataViewStore;

public class DataViewLookupStore extends DataViewStore<LookupDto> {

	public DataViewLookupStore(final CMDataView view, final StorableConverter<LookupDto> converter) {
		super(view, converter);
	}

	@Override
	public void delete(final Storable storable) {
		throw new UnsupportedOperationException();
	}

}
