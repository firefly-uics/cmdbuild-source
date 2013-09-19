package org.cmdbuild.services.bim.connector;

import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.logic.data.lookup.LookupLogic;

public interface MapperRules {

	String findReferencedClassNameFromReferenceAttribute(CMAttribute attribute,
			CMDataView dataView);

	Long findLookupIdFromDescription(String lookupValue, String lookupType,
			LookupLogic lookupLogic);
	
	CMCard fetchCardWithKey(String key, String className, CMDataView dataView); 
	
	Long findIdFromKey(String value, String className, CMDataView dataView);

}