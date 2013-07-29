package org.cmdbuild.logic;

import java.util.List;

import org.cmdbuild.data.store.DataViewStore;
import org.cmdbuild.model.bim.BIMProject;

public class BIMLogic implements Logic {

	private DataViewStore<BIMProject> store;

	public BIMLogic(final DataViewStore<BIMProject> store) {
		this.store = store;
	}

	public List<BIMProject> read() {
		return store.list();
	}

}
