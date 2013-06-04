package org.cmdbuild.cmdbf.federation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import org.cmdbuild.cmdbf.CMDBfQueryResult;
import org.cmdbuild.cmdbf.CMDBfId;
import org.cmdbuild.cmdbf.CMDBfItem;
import org.cmdbuild.cmdbf.CMDBfRelationship;
import org.cmdbuild.cmdbf.ContentSelectorFunction;
import org.cmdbuild.cmdbf.ItemSet;
import org.cmdbuild.cmdbf.ManagementDataRepository;
import org.cmdbuild.cmdbf.PathSet;
import org.dmtf.schemas.cmdbf._1.tns.query.ExpensiveQueryErrorFault;
import org.dmtf.schemas.cmdbf._1.tns.query.InvalidPropertyTypeFault;
import org.dmtf.schemas.cmdbf._1.tns.query.QueryErrorFault;
import org.dmtf.schemas.cmdbf._1.tns.query.UnknownTemplateIDFault;
import org.dmtf.schemas.cmdbf._1.tns.query.UnsupportedConstraintFault;
import org.dmtf.schemas.cmdbf._1.tns.query.UnsupportedSelectorFault;
import org.dmtf.schemas.cmdbf._1.tns.query.XPathErrorFault;
import org.dmtf.schemas.cmdbf._1.tns.servicedata.ContentSelectorType;
import org.dmtf.schemas.cmdbf._1.tns.servicedata.EdgesType;
import org.dmtf.schemas.cmdbf._1.tns.servicedata.ItemType;
import org.dmtf.schemas.cmdbf._1.tns.servicedata.MdrScopedIdType;
import org.dmtf.schemas.cmdbf._1.tns.servicedata.NodesType;
import org.dmtf.schemas.cmdbf._1.tns.servicedata.QueryResultType;
import org.dmtf.schemas.cmdbf._1.tns.servicedata.QueryType;
import org.dmtf.schemas.cmdbf._1.tns.servicedata.RecordConstraintType;
import org.dmtf.schemas.cmdbf._1.tns.servicedata.RelationshipType;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

public class FederationQueryResult extends CMDBfQueryResult {
	
	private Collection<QueryResultType> mdrQueryResults;

	public FederationQueryResult(QueryType body, Collection<ManagementDataRepository> mdrCollection) throws QueryErrorFault, InvalidPropertyTypeFault, UnknownTemplateIDFault, ExpensiveQueryErrorFault, XPathErrorFault, UnsupportedSelectorFault, UnsupportedConstraintFault {
		super(body);
		mdrQueryResults = new ArrayList<QueryResultType>();
		for(ManagementDataRepository mdr : mdrCollection) {
			//TODO preprocess query for reconciliation
			mdrQueryResults.add(mdr.graphQuery(body));
		}
		execute();
	}

	@Override
	protected Collection<CMDBfItem> getItems(final String templateId, Set<CMDBfId> instanceId, RecordConstraintType recordConstraint) {
		Collection<CMDBfItem> items = new ArrayList<CMDBfItem>();
		for(QueryResultType result : mdrQueryResults) {	
			if(result.getNodes() != null) {
				NodesType templateResult = Iterables.find(result.getNodes(), new Predicate<NodesType>(){
					public boolean apply(NodesType input){
						return templateId.equals(input.getTemplateId());
					}
				});
				if(templateResult!=null) {
					for(ItemType item : templateResult.getItem()) {
						//TODO items reconciliation
						CMDBfItem cmdbfItem = new CMDBfItem(item);
						if(filter(cmdbfItem, instanceId, recordConstraint))
							items.add(cmdbfItem);
					}
				}
			}
		}		
		return items;
	}

	@Override
	protected Collection<CMDBfRelationship> getRelationships(final String templateId, Set<CMDBfId> instanceId, Set<CMDBfId> source, Set<CMDBfId> target, RecordConstraintType recordConstraint) {
		Collection<CMDBfRelationship> relationships = new ArrayList<CMDBfRelationship>();
		for(QueryResultType result : mdrQueryResults) {
			if(result.getEdges() != null) {
				EdgesType templateResult = Iterables.find(result.getEdges(), new Predicate<EdgesType>(){
					public boolean apply(EdgesType input){
						return templateId.equals(input.getTemplateId());
					}
				});
				if(templateResult!=null) {
					//TODO items reconciliation				
					for(RelationshipType relationship : templateResult.getRelationship()) {
						CMDBfRelationship cmdbfRelationship = new CMDBfRelationship(relationship);
						if(source.contains(cmdbfRelationship.getSource()) && target.contains(cmdbfRelationship.getTarget())){
							if(filter(cmdbfRelationship, instanceId, recordConstraint))
								relationships.add(cmdbfRelationship);
						}
					}
				}
			}
		}		
		return relationships;
	}

	@Override
	protected void fetchItemRecords(String templateId, ItemSet<CMDBfItem> items, ContentSelectorType contentSelector) {
		for(CMDBfItem item : items)
			Iterables.transform(item.records(), new ContentSelectorFunction(contentSelector));
		
	}

	@Override
	protected void fetchRelationshipRecords(String templateId, PathSet relationships, ContentSelectorType contentSelector) {
		for(CMDBfRelationship relationship : relationships)
			Iterables.transform(relationship.records(), new ContentSelectorFunction(contentSelector));		
		
	}
	
	@Override
	protected CMDBfId resolveAlias(MdrScopedIdType alias) {
		return alias instanceof CMDBfId ? (CMDBfId)alias : new CMDBfId(alias);
	}
	
	@Override
	protected void fetchAlias(CMDBfItem item) {	}
}
