package org.cmdbuild.services.bim;

import java.util.List;
import java.util.Map;

import org.cmdbuild.dao.query.CMQueryResult;
import org.cmdbuild.dao.query.CMQueryRow;
import org.cmdbuild.dao.view.CMDataView;

public interface BimDataView {

	CMQueryResult fetchCardsOfClassInContainer(String className, long containerId, String containerAttribute);

	Map<String, String> fetchBimDataOfRow(CMQueryRow row, String className, String containerId,
			String containerClassName);

	CMDataView getDataView();

	long getId(String key, String className);

	Map<String, Long> fetchIdAndIdClassFromGlobalId(String globalId);

	Map<String, int[]> fetchIdAndIdClassForGlobalIdMap(Map<Long, String> globalIdMap);

}
