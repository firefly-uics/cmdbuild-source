package org.cmdbuild.operation.management;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.cmdbuild.dao.backend.CMBackend;
import org.cmdbuild.elements.Lookup;
import org.cmdbuild.elements.interfaces.IAbstractElement.ElementStatus;
import org.cmdbuild.exception.AuthException;
import org.cmdbuild.exception.NotFoundException;
import org.cmdbuild.exception.ORMException;
import org.cmdbuild.services.auth.UserContext;
import org.springframework.beans.factory.annotation.Autowired;

public class LookupOperation {

	private UserContext userCtx;

	@Autowired
	private CMBackend backend = CMBackend.INSTANCE;

	public LookupOperation(UserContext userCtx) {
		this.userCtx = userCtx;
	}

	public Lookup getLookupById(int lookupId){
		return backend.getLookup(lookupId);
	}

	public Lookup createLookup(String type, String code, String description, String notes,  
			int parentId, int number, boolean isDefault, boolean isActive) throws NotFoundException, ORMException {
		userCtx.privileges().assureAdminPrivilege();
		Lookup lookup = new Lookup();
		if (type != null) 
			lookup.setType(type);
		if (description != null)
			lookup.setDescription(description);
		if (notes != null) 
			lookup.setNotes(notes);
		if (parentId > 0)
			lookup.setParentId(parentId);
		if (code != null)
			lookup.setCode(code);
		lookup.setIsDefault(isDefault);
		if(number<=0){
			number=((List<Lookup>)getLookupList(type)).size()+1;
		}
		lookup.setNumber(number);
		if (isActive) {
			lookup.setStatus(ElementStatus.ACTIVE);
		} else {
			lookup.setStatus(ElementStatus.INACTIVE);
		}
		lookup.save();
		
		return lookup;
	}
	
	public void disableLookup(int id) {
		userCtx.privileges().assureAdminPrivilege();
		Lookup lookup = backend.getLookup(id);
		lookup.setStatus(ElementStatus.INACTIVE);
		lookup.save();
	}
	
	public void enableLookup(int id) {
		userCtx.privileges().assureAdminPrivilege();
		Lookup lookup = backend.getLookup(id);
		lookup.setStatus(ElementStatus.ACTIVE);
		lookup.save();
	}
	
	public Lookup updateLookup(int id, String type, String code, String description,
			int parentId, int position) {
		userCtx.privileges().assureAdminPrivilege();
		return updateLookup(id, type, code, description, "", parentId, position, false, true);
	}
	
	public Lookup updateLookup(int id, String type, String code, String description, String notes,
			int parentId, int position, boolean isDefault, boolean isActive) {
		userCtx.privileges().assureAdminPrivilege();
		Lookup lookup = backend.getLookup(id);
		if (type != null) 
			lookup.setType(type);
		if (code != null)
			lookup.setCode(code);
		if (description != null)
			lookup.setDescription(description);
		if (notes != null) 
			lookup.setNotes(notes);
		if (notes != null) 
			lookup.setNotes(notes);
		if (parentId > 0)
			lookup.setParentId(parentId);
		if (isDefault)
			lookup.setNumber(position);
		if (isActive)
			lookup.setStatus(ElementStatus.ACTIVE);
		else
			lookup.setStatus(ElementStatus.INACTIVE);
		lookup.save();
		return lookup;
	}

	public List<Lookup> getLookupList(String lookupType)
		throws NotFoundException, ORMException {
		return backend.getLookupList(lookupType, null);
	}
	
	public Iterable<Lookup> getLookupListActive(String lookupType, String lookupValue) throws NotFoundException, ORMException {
		Iterable<Lookup> lookups = backend.getLookupList(lookupType, lookupValue);
		List<Lookup> result = new LinkedList<Lookup>();
		for (Lookup l : lookups){
			if (l.getStatus().isActive()){
				result.add(l);
			}
		}
		return result;
	}

	public void reorderLookup(String type, Map<Integer, Integer> lookupPositions) throws ORMException, AuthException, NotFoundException {
		userCtx.privileges().assureAdminPrivilege();
		Iterable<Lookup> lookupList = getLookupList(type);
		for (Lookup lookup : lookupList ) {
			if(lookupPositions.containsKey(lookup.getId())){
				int index = lookupPositions.get(lookup.getId());
				lookup.setNumber(index);
				lookup.save();
			}
		}
	}

	public Lookup getLookup(String type, String description) {
		return backend.getLookup(type, description);
	}
	
}
