package org.cmdbuild.services.bim.connector;

import java.util.Iterator;

import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.dao.entrytype.CMDomain;
import org.cmdbuild.dao.entrytype.attributetype.ReferenceAttributeType;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.data.store.lookup.Lookup;
import org.cmdbuild.data.store.lookup.LookupType;
import org.cmdbuild.logic.data.lookup.LookupLogic;

public abstract class DefaultMapperRules implements MapperRules {

	@Override
	public String findReferencedClassNameFromReferenceAttribute(
			CMAttribute attribute, CMDataView dataView) {
		String domainName = ((ReferenceAttributeType) attribute.getType())
				.getDomainName();
		CMDomain domain = dataView.findDomain(domainName);
		String referencedClass = "";
		String ownerClassName = attribute.getOwner().getName();
		if (domain.getClass1().getName().equals(ownerClassName)) {
			referencedClass = domain.getClass2().getName();
		} else {
			referencedClass = domain.getClass1().getName();
		}
		return referencedClass;
	}

	@Override
	public Long findLookupIdFromDescription(String lookupValue,
			String lookupType, LookupLogic lookupLogic) {
		Long lookupId = null;
		Iterable<LookupType> allLookupTypes = lookupLogic.getAllTypes();
		LookupType theType = null;
		for (Iterator<LookupType> it = allLookupTypes.iterator(); it.hasNext();) {
			LookupType lt = it.next();
			if (lt.name.equals(lookupType)) {
				theType = lt;
				break;
			}
		}
		Iterable<Lookup> allLookusOfType = lookupLogic.getAllLookup(theType,
				true, 0, 0);

		for (Iterator<Lookup> it = allLookusOfType.iterator(); it.hasNext();) {
			Lookup l = it.next();
			if (l.getDescription() != null
					&& l.getDescription().equals(lookupValue)) {
				lookupId = l.getId();
				break;
			}
		}
		return lookupId;
	}

	@Override
	public abstract CMCard fetchCardWithKey(String key, String className,
			CMDataView dataView);

	@Override
	public abstract Long findIdFromKey(String value, String className,
			CMDataView dataView);

}
