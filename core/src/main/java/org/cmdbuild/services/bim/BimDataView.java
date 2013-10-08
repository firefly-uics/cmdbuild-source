package org.cmdbuild.services.bim;

import java.util.Map;

import org.cmdbuild.dao.query.CMQueryResult;
import org.cmdbuild.dao.query.CMQueryRow;
import org.cmdbuild.dao.view.CMDataView;

public interface BimDataView {
	
	CMQueryResult fetchCardsOfClassInContainer(String className, long containerId);

	Map<String, String> fetchBimDataOfRow(CMQueryRow row, String className);
	
	CMDataView getDataView();

	long getId(String key, String className);
	
}
