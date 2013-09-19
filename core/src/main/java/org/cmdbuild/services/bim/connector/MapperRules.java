package org.cmdbuild.services.bim.connector;

import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.view.CMDataView;

public interface MapperRules {
	
	CMCard fetchCardWithKey(String key, String className, CMDataView dataView); 
	
	Long findIdFromKey(String value, String className, CMDataView dataView);

}