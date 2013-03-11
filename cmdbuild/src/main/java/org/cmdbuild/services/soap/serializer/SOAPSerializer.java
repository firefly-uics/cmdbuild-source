package org.cmdbuild.services.soap.serializer;

import org.cmdbuild.services.soap.types.Lookup;

public class SOAPSerializer {
	
	/**
	 * Convert Lookup in LookupType
	 * 
	 * @param lookup Lookup to convert
	 * @return
	 */
	public Lookup serializeLookup(org.cmdbuild.elements.Lookup lookup, boolean parentList){
		
		Lookup lookupType = new Lookup();
		
		lookupType.setId(lookup.getId());
		lookupType.setType(lookup.getType());
		lookupType.setDescription(lookup.getDescription());
		lookupType.setPosition(lookup.getNumber());
		lookupType.setNotes(lookup.getNotes());
		lookupType.setCode(lookup.getCode());
		org.cmdbuild.elements.Lookup parent = lookup.getParent();
		if (parent != null)
			lookupType.setParentId(lookup.getParentId());
		if (parentList &&  parent != null){
			lookupType.setParent(serializeLookup(parent, true));
		}
		return lookupType;
	}
		
}
