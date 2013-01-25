package org.cmdbuild.services.soap.operation;

import static org.apache.commons.lang.StringUtils.*;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.cmdbuild.exception.NotFoundException.NotFoundExceptionType;
import org.cmdbuild.logger.Log;
import org.cmdbuild.operation.management.LookupOperation;
import org.cmdbuild.services.auth.UserContext;
import org.cmdbuild.services.soap.serializer.SOAPSerializer;
import org.cmdbuild.services.soap.types.Lookup;

public class ELookup {
	
	private UserContext userCtx;

	public ELookup(UserContext userCtx) {
		this.userCtx = userCtx;
	}
	
	private SOAPSerializer serializer = new SOAPSerializer();
	
	public int createLookup(Lookup lookup) {
		final String type = lookup.getType();
		final String code = defaultIfEmpty(lookup.getCode(), EMPTY);
		final String description = lookup.getDescription();
		final String notes = lookup.getNotes();
		int parentId = lookup.getParentId();
		int position = lookup.getPosition();

		LookupOperation operation = new LookupOperation(userCtx);
		org.cmdbuild.elements.Lookup l = operation.createLookup( //
				type, //
				code, //
				description, //
				notes, //
				parentId, //
				position, //
				false, //
				true);
		return l.getId();
	}
	
	public boolean deleteLookup(int lookupId)  {
		LookupOperation operation = new LookupOperation(userCtx);
		operation.disableLookup(lookupId);
		return true;
	}

	public boolean updateLookup(Lookup lookup)  {
		int id = lookup.getId();
		int parentId = lookup.getParentId();
		int position = lookup.getPosition();
		String code = lookup.getCode();
		String description = lookup.getDescription();
		String type = lookup.getType();
		LookupOperation operation = new LookupOperation(userCtx);
		operation.updateLookup(id, type, code, description, parentId, position);
		return true;
	}

	public Lookup getLookupById(int id)  {
		Log.SOAP.debug("Getting lookup with id " + id);
		LookupOperation operation = new LookupOperation(userCtx);
		org.cmdbuild.elements.Lookup l = operation.getLookupById(id);
		return serializer.serializeLookup(l, true);
	}

	public Lookup[] getLookupList(String type, String value, boolean parentList)  {
		List<Lookup> lookupTypeList = new ArrayList<Lookup>();
		LookupOperation operation = new LookupOperation(userCtx);
		Log.SOAP.debug("Getting all lookup with type " + type + " and value " + value);
		Iterable<org.cmdbuild.elements.Lookup> list = operation.getLookupListActive(type, value);
		if (list != null) {
			for (org.cmdbuild.elements.Lookup l : list) {
				Lookup lookup = serializer.serializeLookup(l, parentList);
				lookupTypeList.add(lookup);
			}
			Lookup[] lookupList = new Lookup[lookupTypeList.size()];
			lookupList = lookupTypeList.toArray(lookupList);
			return lookupList;
		} else {
			Log.SOAP.debug("The request didn't produce active lookup");
			throw NotFoundExceptionType.LOOKUP_NOTFOUND.createException(type);
		}
	}

	public Lookup[] getLookupListByCode(String type, String code, boolean parentList)  {
		LookupOperation operation = new LookupOperation(userCtx);
		Iterable<org.cmdbuild.elements.Lookup> lookupList = operation.getLookupList(type);
		List<Lookup> list = new LinkedList<Lookup>();
		for (org.cmdbuild.elements.Lookup lookup : lookupList) {
			if (lookup.getStatus().isActive() && code.equals(lookup.getCode())){
				list.add(serializer.serializeLookup(lookup, parentList));
			}
		}
		Lookup[] lookupArray = new Lookup[list.size()];
		lookupArray = list.toArray(lookupArray);
		return lookupArray;
	}
}
