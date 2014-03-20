package org.cmdbuild.cmdbf;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.cmdbuild.logger.Log;
import org.dmtf.schemas.cmdbf._1.tns.query.QueryErrorFault;
import org.dmtf.schemas.cmdbf._1.tns.servicedata.ContentSelectorType;
import org.dmtf.schemas.cmdbf._1.tns.servicedata.EdgesType;
import org.dmtf.schemas.cmdbf._1.tns.servicedata.ItemTemplateType;
import org.dmtf.schemas.cmdbf._1.tns.servicedata.ItemType;
import org.dmtf.schemas.cmdbf._1.tns.servicedata.MdrScopedIdType;
import org.dmtf.schemas.cmdbf._1.tns.servicedata.NodesType;
import org.dmtf.schemas.cmdbf._1.tns.servicedata.QueryResultType;
import org.dmtf.schemas.cmdbf._1.tns.servicedata.QueryType;
import org.dmtf.schemas.cmdbf._1.tns.servicedata.RecordConstraintType;
import org.dmtf.schemas.cmdbf._1.tns.servicedata.RelationshipTemplateType;
import org.dmtf.schemas.cmdbf._1.tns.servicedata.RelationshipType;

import com.google.common.collect.Iterables;

public abstract class CMDBfQueryResult extends QueryResultType {
	private QueryType body;
	
	public CMDBfQueryResult(QueryType body) throws QueryErrorFault {
		this.body = body;
	}
	
	public void execute() throws QueryErrorFault {
		Map<String, ItemSet<CMDBfItem>> itemMap = new HashMap<String, ItemSet<CMDBfItem>>();
		Map<String, PathSet> relationshipMap = new HashMap<String, PathSet>();
		
		for(ItemTemplateType itemTemplate : body.getItemTemplate()) {
			ItemSet<CMDBfItem> templateItems = new ItemSet<CMDBfItem>();
			itemMap.put(itemTemplate.getId(), templateItems);
			templateItems.addAll(getItems(itemTemplate));
		}				
		
		for(RelationshipTemplateType relationshipTemplate : body.getRelationshipTemplate()){
			PathSet pathSet = new PathSet();
			relationshipMap.put(relationshipTemplate.getId(), pathSet);
			
			ItemSet<CMDBfItem> sourceSet = null;			
			if(relationshipTemplate.getSourceTemplate() != null) {
				ItemTemplateType sourceTemplate = (ItemTemplateType) relationshipTemplate.getSourceTemplate().getRef();
				if(sourceTemplate != null)
					sourceSet = itemMap.get(sourceTemplate.getId());				
			}
			
			ItemSet<CMDBfItem> targetSet = null;
			if(relationshipTemplate.getTargetTemplate() != null) {
				ItemTemplateType targetTemplate = (ItemTemplateType) relationshipTemplate.getTargetTemplate().getRef();
				if(targetTemplate != null)
					targetSet = itemMap.get(targetTemplate.getId());				
			}
			
			if(relationshipTemplate.getDepthLimit() != null){
				ItemSet<CMDBfItem> intermediateSet;
				ItemTemplateType intermediateTemplate = (ItemTemplateType) relationshipTemplate.getDepthLimit().getIntermediateItemTemplate();				
				if(intermediateTemplate != null)
					intermediateSet = itemMap.get(intermediateTemplate.getId());
				else
					intermediateSet = new ItemSet<CMDBfItem>();
				ItemSet<CMDBfItem> visitedSet = new ItemSet<CMDBfItem>();
				visitedSet.addAll(sourceSet);
				boolean loop = true;
				for(long i=0;  loop; i++) {
					for(CMDBfRelationship relationship : getRelationships(relationshipTemplate, sourceSet, targetSet))
						pathSet.add(relationship);
					ItemSet<CMDBfItem> nextSourceSet = new ItemSet<CMDBfItem>();
					for(CMDBfRelationship relationship : getRelationships(relationshipTemplate, sourceSet, intermediateSet)) {
						pathSet.add(relationship);
						CMDBfItem target = intermediateSet.get(relationship.getTarget());
						if(visitedSet.add(target))
							nextSourceSet.add(target);
					}
					sourceSet = nextSourceSet;					
					loop = !sourceSet.isEmpty() &&  (relationshipTemplate.getDepthLimit().getMaxIntermediateItems() == null || i<relationshipTemplate.getDepthLimit().getMaxIntermediateItems().longValue());
				}
			}
			else
				pathSet.addAll(getRelationships(relationshipTemplate, sourceSet, targetSet));
		}
		
		boolean loop;
		do {
			loop = false;
			for(RelationshipTemplateType relationshipTemplate : body.getRelationshipTemplate()) {
				ItemTemplateType sourceTemplate = null;
				ItemSet<CMDBfItem> sourceSet = null;
				Integer sourceMin = null;
				Integer sourceMax = null;			
				if(relationshipTemplate.getSourceTemplate() != null) {					
					sourceTemplate = (ItemTemplateType) relationshipTemplate.getSourceTemplate().getRef();
					if(sourceTemplate != null)
						sourceSet = itemMap.get(sourceTemplate.getId());
					 sourceMin = relationshipTemplate.getSourceTemplate().getMinimum();
					 sourceMax = relationshipTemplate.getSourceTemplate().getMaximum();						
				}
				
				ItemTemplateType targetTemplate = null;
				ItemSet<CMDBfItem> targetSet = null;
				Integer targetMin = null;
				Integer targetMax = null;								
				if(relationshipTemplate.getTargetTemplate() != null) {
					targetTemplate = (ItemTemplateType) relationshipTemplate.getTargetTemplate().getRef();
					if(targetTemplate != null)
						targetSet = itemMap.get(targetTemplate.getId());
					targetMin = relationshipTemplate.getTargetTemplate().getMinimum();
					targetMax = relationshipTemplate.getTargetTemplate().getMaximum();								
				}
				
				ItemTemplateType intermediateTemplate = null;
				ItemSet<CMDBfItem> intermediateSet = new ItemSet<CMDBfItem>();
				if(relationshipTemplate.getDepthLimit() != null) {
					intermediateTemplate = (ItemTemplateType) relationshipTemplate.getDepthLimit().getIntermediateItemTemplate();
					if(intermediateTemplate != null)
						intermediateSet = itemMap.get(intermediateTemplate.getId());						
				}
							
				PathSet pathSet = relationshipMap.get(relationshipTemplate.getId());
				removeInvalidPaths(pathSet, sourceSet, intermediateSet, targetSet, sourceMin, sourceMax, targetMin, targetMax);			
			}
			
			for(ItemTemplateType itemTemplate : body.getItemTemplate()) {
				ItemSet<CMDBfItem> nodeSet = itemMap.get(itemTemplate.getId());
				for(RelationshipTemplateType relationshipTemplate : body.getRelationshipTemplate()) {
										
					String sourceTemplateId = null;
					if(relationshipTemplate.getSourceTemplate() != null) {					
						ItemTemplateType sourceTemplate = (ItemTemplateType) relationshipTemplate.getSourceTemplate().getRef();
						if(sourceTemplate != null)
							sourceTemplateId = sourceTemplate.getId();
					}
					
					String targetTemplateId = null;								
					if(relationshipTemplate.getTargetTemplate() != null) {
						ItemTemplateType targetTemplate = (ItemTemplateType) relationshipTemplate.getTargetTemplate().getRef();
						if(targetTemplate != null)
							targetTemplateId = targetTemplate.getId();
					}
					
					String intermediateTemplateId = null;
					if(relationshipTemplate.getDepthLimit() != null) {
						ItemTemplateType intermediateTemplate = (ItemTemplateType) relationshipTemplate.getDepthLimit().getIntermediateItemTemplate();
						if(intermediateTemplate != null)
							intermediateTemplateId = intermediateTemplate.getId();
					}
					
					PathSet pathSet = relationshipMap.get(relationshipTemplate.getId());					
					
					if(itemTemplate.getId().equals(sourceTemplateId) || itemTemplate.getId().equals(intermediateTemplateId)) {
						Set<CMDBfId> sourceSet = new HashSet<CMDBfId>();
						for(CMDBfId id : pathSet.sourceSet()) {
							Collection<CMDBfRelationship> relationSet = pathSet.relationSetBySource(id);
							if(relationSet!=null && !relationSet.isEmpty())
								sourceSet.add(id);
						}							
						loop |= nodeSet.retainAll(sourceSet);							
					}
					
					if(itemTemplate.getId().equals(targetTemplateId) || itemTemplate.getId().equals(intermediateTemplateId)){
						Set<CMDBfId> targetSet = new HashSet<CMDBfId>();
		
						for(CMDBfId id : pathSet.targetSet()) {
							Collection<CMDBfRelationship> relationSet = pathSet.relationSetByTarget(id);
							if(relationSet!=null && !relationSet.isEmpty())
								targetSet.add(id);
						}
						loop |= nodeSet.retainAll(targetSet);
					}
				}
			}
			
		} while(loop);
		
		try {
			for(ItemTemplateType itemTemplate : body.getItemTemplate()) {
				if(!itemTemplate.isSuppressFromResult()){
					NodesType nodesResult = new NodesType();
					nodesResult.setTemplateId(itemTemplate.getId());
					getNodes().add(nodesResult);
					
					ItemSet<CMDBfItem> itemSet = itemMap.get(itemTemplate.getId());
					fetchItemRecords(itemTemplate.getId(), itemSet, itemTemplate.getContentSelector());
					for(CMDBfItem item : itemSet){
						fetchAlias(item);
						ItemType resultItem = new ItemType();
						resultItem.getInstanceId().addAll(item.instanceIds());
						nodesResult.getItem().add(resultItem);						
						resultItem.getRecord().addAll(item.records());
					}
				}
			}
			
			for(RelationshipTemplateType relationshipTemplate : body.getRelationshipTemplate()) {
				if(!relationshipTemplate.isSuppressFromResult()) {
					EdgesType edgesResult = new EdgesType();
					edgesResult.setTemplateId(relationshipTemplate.getId());
					getEdges().add(edgesResult);							
					
					PathSet relationshipSet = relationshipMap.get(relationshipTemplate.getId());
					fetchRelationshipRecords(relationshipTemplate.getId(), relationshipSet, relationshipTemplate.getContentSelector());					
					for(CMDBfRelationship relationship : relationshipSet){
						fetchAlias(relationship);						
						RelationshipType resultRelationship = new RelationshipType();
						resultRelationship.getInstanceId().addAll(relationship.instanceIds());
						resultRelationship.setSource(relationship.getSource());
						resultRelationship.setTarget(relationship.getTarget());						
						edgesResult.getRelationship().add(resultRelationship);
						resultRelationship.getRecord().addAll(relationship.records());
					}
				}
			}
		} catch (Throwable e) {
			Log.CMDBUILD.error("CMDBf graphQuery", e);
			throw new QueryErrorFault(e.getMessage(), e);
		}				
	}
	
	private Set<CMDBfItem> getItems(ItemTemplateType itemTemplate) {
		ItemSet<CMDBfItem> itemTemplateResultSet = null;
		Set<CMDBfId> instanceId = null;
		if(itemTemplate.getInstanceIdConstraint() != null) {
			instanceId = new HashSet<CMDBfId>();
			for(MdrScopedIdType alias : itemTemplate.getInstanceIdConstraint().getInstanceId()){
				CMDBfId id = resolveAlias(alias);
				if(id != null)
					instanceId.add(id);
			}
		}
		if(itemTemplate.getRecordConstraint() != null && !itemTemplate.getRecordConstraint().isEmpty()) {
			Iterator<RecordConstraintType> recordIterator = itemTemplate.getRecordConstraint().iterator();
			while(recordIterator.hasNext() && (itemTemplateResultSet==null || !itemTemplateResultSet.isEmpty())) {
				RecordConstraintType recordConstraint = recordIterator.next();
				ItemSet<CMDBfItem> recordConstraintResultSet = new ItemSet<CMDBfItem>();
				recordConstraintResultSet.addAll(getItems(itemTemplate.getId(), instanceId, recordConstraint));
				if(itemTemplateResultSet == null)
					itemTemplateResultSet = recordConstraintResultSet;
				else
					itemTemplateResultSet.retainAll(recordConstraintResultSet);
			}
		}
		else {
			itemTemplateResultSet = new ItemSet<CMDBfItem>();
			itemTemplateResultSet.addAll(getItems(itemTemplate.getId(), instanceId, null));			
		}
		return itemTemplateResultSet;
	}
	
	private Set<CMDBfRelationship> getRelationships(RelationshipTemplateType relationshipTemplate, ItemSet<CMDBfItem> sources, ItemSet<CMDBfItem> targets) {
		ItemSet<CMDBfRelationship> relationshipTemplateResultSet = null;
		Set<CMDBfId> instanceId = null;
		if(relationshipTemplate.getInstanceIdConstraint() != null) {
			instanceId = new HashSet<CMDBfId>();
			for(MdrScopedIdType alias : relationshipTemplate.getInstanceIdConstraint().getInstanceId()){
				CMDBfId id = resolveAlias(alias);
				if(id != null)
					instanceId.add(id);
			}
		}
		if(relationshipTemplate.getRecordConstraint() != null && !relationshipTemplate.getRecordConstraint().isEmpty()) {
			Iterator<RecordConstraintType> recordIterator = relationshipTemplate.getRecordConstraint().iterator();
			while(recordIterator.hasNext() && (relationshipTemplateResultSet==null || !relationshipTemplateResultSet.isEmpty())) {
				RecordConstraintType recordConstraint = recordIterator.next();
				ItemSet<CMDBfRelationship> recordConstraintResultSet = new ItemSet<CMDBfRelationship>();
				recordConstraintResultSet.addAll(getRelationships(relationshipTemplate.getId(), instanceId, sources!=null ? sources.idSet() : null, targets!=null ? targets.idSet() : null, recordConstraint));
				if(relationshipTemplateResultSet == null)
					relationshipTemplateResultSet = recordConstraintResultSet;
				else
					relationshipTemplateResultSet.retainAll(recordConstraintResultSet);
			}
		}
		else {
			relationshipTemplateResultSet = new ItemSet<CMDBfRelationship>();
			relationshipTemplateResultSet.addAll(getRelationships(relationshipTemplate.getId(), instanceId, sources!=null ? sources.idSet() : null, targets!=null ? targets.idSet() : null, null));			
		}
		return relationshipTemplateResultSet;		
	}

	private void removeInvalidPaths(PathSet pathSet, ItemSet<CMDBfItem> sourceSet, ItemSet<CMDBfItem> intermediateSet, ItemSet<CMDBfItem> targetSet, Integer sourceMin, Integer sourceMax, Integer targetMin, Integer targetMax) {
		boolean loop = false;
		do {
			Set<CMDBfRelationship> validated = new HashSet<CMDBfRelationship>();	
			for(CMDBfId source : pathSet.sourceSet()) {
				if(sourceSet==null || sourceSet.contains(source)) {
					Collection<CMDBfRelationship> relationSet = pathSet.relationSetBySource(source);
					if(relationSet != null) {
						for(CMDBfRelationship relationship : relationSet) {
							if(!validated.contains(relationship)) {				
								if(validatePath(relationship.getTarget(), validated, pathSet, intermediateSet, targetSet))
									validated.add(relationship);
							}
						}
					}
				}
			}					
			pathSet.retainAll(validated);
			
			if(sourceMin!=null || sourceMax!=null){
				List<CMDBfId> invalid = new ArrayList<CMDBfId>();
				for(CMDBfId source : pathSet.sourceSet()){
					Collection<CMDBfRelationship> relationSet = pathSet.relationSetBySource(source);
					int count = relationSet!=null ? relationSet.size() : 0;
					if((sourceMin!=null && count<sourceMin) || (sourceMax!=null && count>sourceMax))
						invalid.add(source);
				}
				loop = pathSet.removeAllBySource(invalid);
			}
			
			if(targetMin!=null || targetMax!=null){
				List<CMDBfId> invalid = new ArrayList<CMDBfId>();
				for(CMDBfId target : pathSet.targetSet()){
					Collection<CMDBfRelationship> relationSet = pathSet.relationSetByTarget(target);
					int count = relationSet!=null ? relationSet.size() : 0;
					if((targetMin!=null && count<targetMin) || (targetMax!=null && count>targetMax))
						invalid.add(target);
				}
				loop = pathSet.removeAllByTarget(invalid);
			}
		}
		while(loop);		
	}
	
	private boolean validatePath(CMDBfId target, Set<CMDBfRelationship> validated, PathSet pathSet, Set<CMDBfItem> intermediateSet, Set<CMDBfItem> targetSet) {
		boolean isValid = (targetSet==null || targetSet.contains(target));
		if(!isValid && (intermediateSet==null || intermediateSet.contains(target))) {
			Collection<CMDBfRelationship> relationSet = pathSet.relationSetBySource(target);
			if(relationSet != null) {			
				Iterator<CMDBfRelationship> iterator = relationSet.iterator();
				while(!isValid && iterator.hasNext()) {			
					CMDBfRelationship relationship = iterator.next();
					if(!validated.contains(relationship)) {				
						if(validatePath(relationship.getTarget(), validated, pathSet, intermediateSet, targetSet)) {
							validated.add(relationship);
							isValid = true;
						}
					}
					else
						isValid = true;
				} 
			}
		}
		return isValid;
	}
	
	protected boolean filter(CMDBfItem item, final Set<CMDBfId> idSet, RecordConstraintType recordConstraint) {
		boolean match = true;
		if(idSet != null)
			match = Iterables.any(item.instanceIds(), new IdConstraintPredicate(idSet));
		if(match && recordConstraint != null)
			match = Iterables.any(item.records(), new RecordConstraintPredicate(recordConstraint));		
		return match;
	}

	abstract protected Collection<CMDBfItem> getItems(String templateId, Set<CMDBfId> instanceId, RecordConstraintType recordConstraint);

	abstract protected Collection<CMDBfRelationship> getRelationships(String templateId, Set<CMDBfId> instanceId, Set<CMDBfId> source, Set<CMDBfId> target, RecordConstraintType recordConstraint);
	
	abstract protected void fetchItemRecords(String templateId, ItemSet<CMDBfItem> items, ContentSelectorType contentSelector);
	
	abstract protected void fetchRelationshipRecords(String templateId, PathSet relationships, ContentSelectorType contentSelector);
		
	abstract protected CMDBfId resolveAlias(MdrScopedIdType alias);
	
	abstract protected void fetchAlias(CMDBfItem item);
}