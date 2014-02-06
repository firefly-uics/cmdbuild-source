package org.cmdbuild.services.bim;

import java.util.Map;

import org.cmdbuild.dao.query.CMQueryResult;
import org.cmdbuild.dao.query.CMQueryRow;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.services.bim.connector.DefaultBimDataView.BimObjectCard;

public interface BimDataView {

	CMQueryResult fetchCardsOfClassInContainer(String className, long containerId, String containerAttribute);

	Map<String, String> fetchBimDataOfRow(CMQueryRow row, String className, String containerId,
			String containerClassName);

	long getMatchingId(String key, String className);

	Map<String, Long> fetchIdAndIdClassFromGlobalId(String globalId);

	Map<String, BimObjectCard> fetchIdAndIdClassForGlobalIdMap(Map<Long, String> globalIdMap);

	long fetchBuildingIdFromCardId(Long cardId);

	void fillGlobalidIdMap(Map<String, Long> globalid_cmdbId_map, String className);

}
