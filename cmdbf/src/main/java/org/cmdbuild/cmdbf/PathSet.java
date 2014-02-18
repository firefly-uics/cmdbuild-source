package org.cmdbuild.cmdbf;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class PathSet extends ItemSet<CMDBfRelationship> {
	private Map<CMDBfId, Set<CMDBfRelationship>> sourceIdMap;
	private Map<CMDBfId, Set<CMDBfRelationship>> targetIdMap;
	
	public PathSet() {
		sourceIdMap = new HashMap<CMDBfId, Set<CMDBfRelationship>>();
		targetIdMap = new HashMap<CMDBfId, Set<CMDBfRelationship>>();
	}
	
	public Collection<CMDBfId> sourceSet() {
		return sourceIdMap.keySet();
	}
	
	public Collection<CMDBfId> targetSet() {
		return targetIdMap.keySet();
	}
	
	public Collection<CMDBfRelationship> relationSetBySource(CMDBfId id) {
		return sourceIdMap.get(id);
	}
	
	public Collection<CMDBfRelationship> relationSetByTarget(CMDBfId id) {
		return targetIdMap.get(id);
	}
	
	@Override
	public boolean add(CMDBfRelationship relationship) {
		boolean modified = super.add(relationship);
		if(modified) {
			CMDBfRelationship setRel = get(relationship);
			Set<CMDBfRelationship> sourceSet = sourceIdMap.get(setRel.getSource());
			if(sourceSet == null) {
				sourceSet = new HashSet<CMDBfRelationship>();
				sourceIdMap.put(setRel.getSource(), sourceSet);
			}
			sourceSet.add(setRel);
			
			Set<CMDBfRelationship> targetSet = targetIdMap.get(setRel.getTarget());
			if(targetSet == null) {
				targetSet = new HashSet<CMDBfRelationship>();
				targetIdMap.put(setRel.getTarget(), targetSet);
			}
			targetSet.add(setRel);
		}
		return modified;
	}

	@Override
	public boolean remove(Object o) {
		boolean modified = false;
		CMDBfRelationship setRel = get(o);
		if(setRel != null)
			modified = super.remove(setRel);
		if(modified) {
			Set<CMDBfRelationship> sourceSet = sourceIdMap.get(setRel.getSource());
			if(sourceSet != null)
				sourceSet.remove(setRel);
			
			Set<CMDBfRelationship> targetSet = targetIdMap.get(setRel.getTarget());
			if(targetSet != null)
				targetSet.remove(setRel);
		}
		return modified;
	}
		
	public boolean removeAllBySource(Collection<CMDBfId> idSet) {
		Collection<CMDBfRelationship> relationList = new ArrayList<CMDBfRelationship>();
		for(CMDBfId id : idSet) {
			for(CMDBfRelationship relation : relationSetBySource(id))
				relationList.add(relation);
		}		
		return removeAll(relationList);
	}
	
	public boolean removeAllByTarget(Collection<CMDBfId> idSet) {
		Collection<CMDBfRelationship> relationList = new ArrayList<CMDBfRelationship>();
		for(CMDBfId id : idSet) {
			for(CMDBfRelationship relation : relationSetByTarget(id))
				relationList.add(relation);
		}		
		return removeAll(relationList);
	}
}
