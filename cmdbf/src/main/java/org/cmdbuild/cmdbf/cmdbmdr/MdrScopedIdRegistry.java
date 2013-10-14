package org.cmdbuild.cmdbf.cmdbmdr;

import static org.cmdbuild.dao.query.clause.AnyAttribute.anyAttribute;
import static org.cmdbuild.dao.query.clause.QueryAliasAttribute.attribute;
import static org.cmdbuild.dao.query.clause.where.EqualsOperatorAndValue.eq;
import static org.cmdbuild.dao.query.clause.where.SimpleWhereClause.condition;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.cmdbuild.cmdbf.CMDBfId;
import org.cmdbuild.config.CmdbfConfiguration;
import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entry.CMCard.CMCardDefinition;
import org.cmdbuild.dao.entry.CMEntry;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.query.CMQueryRow;
import org.cmdbuild.dao.query.QuerySpecsBuilder;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.logic.data.access.DataAccessLogic;
import org.dmtf.schemas.cmdbf._1.tns.servicedata.MdrScopedIdType;

public class MdrScopedIdRegistry {
	static private final String REGISTRY = "_MdrScopedId";
	static private final String ITEM_ID = "IdItem";
	static private final String ALIAS = "MdrScopedId";

	private CMDataView view;
	private CmdbfConfiguration cmdbfConfiguration;
	
	public MdrScopedIdRegistry(DataAccessLogic dataAccessLogic, CmdbfConfiguration cmdbfConfiguration) {
		this.view = dataAccessLogic.getView();
		this.cmdbfConfiguration = cmdbfConfiguration;
	}
	
	public boolean isLocal(MdrScopedIdType id){
		return cmdbfConfiguration.getMdrId().equals(id.getMdrId());
	}
	
	public CMDBfId resolveAlias(MdrScopedIdType alias) {
		CMDBfId id = null;
		if(isLocal(alias)){
			if(alias instanceof CMDBfId)
				id = (CMDBfId)alias;
			else
				id = new CMDBfId(alias);
		}
		else {
			CMClass registry = view.findClass(REGISTRY);			
			QuerySpecsBuilder queryBuilder = view.select(anyAttribute(registry)).
				from(registry).where(condition(attribute(registry, ALIAS), eq(CMDBfId.toString(alias))));
			for(CMQueryRow row : queryBuilder.run()) {
				CMCard card = row.getCard(registry);
				Long idItem = ((Number)card.get(ITEM_ID)).longValue();
				id = getCMDBfId(idItem, null);
			}
		}
		return id;
	}
	
	public Set<CMDBfId> getAlias(MdrScopedIdType id) {
		Set<CMDBfId> aliasList = new HashSet<CMDBfId>();
		aliasList.add(id instanceof CMDBfId ? (CMDBfId)id : new CMDBfId(id));
		if(isLocal(id)) {
			CMClass registry = view.findClass(REGISTRY);			
			QuerySpecsBuilder queryBuilder = view.select(anyAttribute(registry)).from(registry).
				where(condition(attribute(registry, ITEM_ID), eq(getInstanceId(id))));
			for(CMQueryRow row : queryBuilder.run()) {
				CMCard card = row.getCard(registry);
				String mdrScopedId = (String)card.get("MdrScopedId");
				aliasList.add(CMDBfId.valueOf(mdrScopedId));
			}
		}
		return aliasList;
	}
	
	public synchronized void addAlias(Long instanceId, Collection<CMDBfId> alias) {
		CMClass registry = view.findClass(REGISTRY);			
		for(MdrScopedIdType id : alias) {
			if(!isLocal(id)){
				QuerySpecsBuilder queryBuilder = view.select(anyAttribute(registry)).
					from(registry).where(condition(attribute(registry, ALIAS), eq(CMDBfId.toString(id))));
				for(CMQueryRow row : queryBuilder.run()) {
					CMCard card = row.getCard(registry);
					view.delete(card);
				}
				CMCardDefinition card = view.createCardFor(registry);
				card.set(ALIAS, CMDBfId.toString(id));
				card.set(ITEM_ID, instanceId);
				card.save();
			}
		}
	}
	
	public synchronized void addAlias(CMEntry element, Collection<CMDBfId> alias) {
		addAlias(element.getId(), alias);
	}
	
	public synchronized void removeAlias(CMEntry element) {
		CMClass registry = view.findClass(REGISTRY);			
		QuerySpecsBuilder queryBuilder = view.select(anyAttribute(registry)).from(registry).
			where(condition(attribute(registry, ITEM_ID), eq(element.getId())));
		for(CMQueryRow row : queryBuilder.run()) {
			CMCard card = row.getCard(registry);
			view.delete(card);
		}
	}

    public CMDBfId getCMDBfId(Long instanceId, String recordId) {
		StringBuffer localId = new StringBuffer();
		localId.append(Long.toString(instanceId));
		if(recordId != null) {
			localId.append('#');
			localId.append(recordId);
		}
		return new CMDBfId(cmdbfConfiguration.getMdrId(), localId.toString());
	}
    
	public CMDBfId getCMDBfId(Long id) {
		return getCMDBfId(id, null);
	}
		
	public CMDBfId getCMDBfId(CMEntry element) {
		return getCMDBfId(element, null);
	}
	
	public CMDBfId getCMDBfId(CMEntry element, String recordId) {
		return getCMDBfId(element.getId(), recordId);
	}
	
	public CMDBfId getCMDBfId(MdrScopedIdType id, String recordId) {
		return getCMDBfId(getInstanceId(id), recordId);
	}
    
    public Long getInstanceId(MdrScopedIdType id) { 
    	Long instanceId = null;
    	int pos = id.getLocalId().indexOf('#');
    	if(pos < 0)
    		pos = id.getLocalId().length();
		instanceId = Long.parseLong(id.getLocalId().substring(0, pos));
    	return instanceId;
    }
    
    public String getRecordId(MdrScopedIdType id) { 
    	String recordId = null;
    	int pos = id.getLocalId().indexOf('#');
    	if(pos > 0 && pos < id.getLocalId().length()-1)
        	recordId = id.getLocalId().substring(pos + 1);
    	return recordId;
    }
}
