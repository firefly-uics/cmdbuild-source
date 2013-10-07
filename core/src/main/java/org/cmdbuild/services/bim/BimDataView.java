package org.cmdbuild.services.bim;

import java.util.Map;

import org.cmdbuild.dao.query.CMQueryResult;
import org.cmdbuild.dao.query.CMQueryRow;

public interface BimDataView {
	
	CMQueryResult fetchCardsOfClassInContainer(String className, long containerId);

	Map<String, String> fetchBimDataOfRow(CMQueryRow row, String className);
	
}
