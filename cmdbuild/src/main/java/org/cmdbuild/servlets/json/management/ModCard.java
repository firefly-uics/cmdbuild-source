package org.cmdbuild.servlets.json.management;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import javax.activation.DataHandler;

import org.apache.commons.fileupload.FileItem;
import org.cmdbuild.elements.DirectedDomain;
import org.cmdbuild.elements.DirectedDomain.DomainDirection;
import org.cmdbuild.elements.Lookup;
import org.cmdbuild.elements.TableTree;
import org.cmdbuild.elements.filters.AbstractFilter;
import org.cmdbuild.elements.filters.AttributeFilter;
import org.cmdbuild.elements.filters.AttributeFilter.AttributeFilterType;
import org.cmdbuild.elements.filters.CompositeFilter;
import org.cmdbuild.elements.filters.CompositeFilter.CompositeFilterItem;
import org.cmdbuild.elements.filters.FilterOperator;
import org.cmdbuild.elements.filters.FilterOperator.OperatorType;
import org.cmdbuild.elements.filters.OrderFilter;
import org.cmdbuild.elements.filters.OrderFilter.OrderFilterType;
import org.cmdbuild.elements.interfaces.BaseSchema.Mode;
import org.cmdbuild.elements.interfaces.CardQuery;
import org.cmdbuild.elements.interfaces.DomainFactory;
import org.cmdbuild.elements.interfaces.IAttribute;
import org.cmdbuild.elements.interfaces.ICard;
import org.cmdbuild.elements.interfaces.IDomain;
import org.cmdbuild.elements.interfaces.IRelation;
import org.cmdbuild.elements.interfaces.ITable;
import org.cmdbuild.elements.interfaces.ITableFactory;
import org.cmdbuild.elements.interfaces.Process.ProcessAttributes;
import org.cmdbuild.elements.interfaces.RelationFactory;
import org.cmdbuild.elements.wrappers.PrivilegeCard.PrivilegeType;
import org.cmdbuild.exception.CMDBException;
import org.cmdbuild.legacy.dms.AlfrescoFacade;
import org.cmdbuild.legacy.dms.AttachmentBean;
import org.cmdbuild.logic.LogicDTO.Card;
import org.cmdbuild.logic.LogicDTO.DomainWithSource;
import org.cmdbuild.logic.DataAccessLogic;
import org.cmdbuild.logic.commands.GetRelationList.GetRelationListResponse;
import org.cmdbuild.services.FilterService;
import org.cmdbuild.services.auth.UserContext;
import org.cmdbuild.services.gis.GeoCard;
import org.cmdbuild.services.meta.MetadataService;
import org.cmdbuild.servlets.json.JSONBase;
import org.cmdbuild.servlets.json.serializers.JsonGetRelationListResponse;
import org.cmdbuild.servlets.json.serializers.Serializer;
import org.cmdbuild.servlets.utils.OverrideKeys;
import org.cmdbuild.servlets.utils.Parameter;
import org.cmdbuild.servlets.utils.builder.CardQueryParameter;
import org.cmdbuild.utils.CQLFacadeCompiler;
import org.cmdbuild.workflow.WorkflowConstants;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ModCard extends JSONBase {

	@JSONExported
	public JSONObject getCardList(
			JSONObject serializer,
			@Parameter("limit") int limit,
			@Parameter("start") int offset,
			@Parameter(value="sort",required=false) String sortField,
			@Parameter(value="dir",required=false) String sortDirection,
			@Parameter(value="query",required=false) String fullTextQuery,
			@Parameter(value="writeonly",required=false) boolean writeonly,
			CardQuery cardQuery,
			UserContext userContext) throws JSONException, CMDBException {
		temporaryPatchToFakePrivilegeCheckOnCQL(cardQuery, userContext);
		JSONArray rows = new JSONArray();
		if (writeonly) {
			removeReadOnlySubclasses(cardQuery, userContext);
		}
		if (fullTextQuery != null)
		    cardQuery.fullText(fullTextQuery.trim());
		if (sortField != null || sortDirection != null) {
			if (sortField.endsWith("_value"))
				sortField = sortField.substring(0, sortField.length()-6);
			cardQuery.clearOrder().order(sortField, OrderFilterType.valueOf(sortDirection));
		}
		for(ICard card : cardQuery.subset(offset, limit).count()) {
			rows.put(Serializer.serializeCardWithPrivileges(card, false));
		}
		serializer.put("rows", rows);
		serializer.put("results", cardQuery.getTotalRows());
		return serializer;
	}

	private void temporaryPatchToFakePrivilegeCheckOnCQL(CardQuery cardQuery, UserContext userContext) {
		ITable fromTable = cardQuery.getTable();
		if (fromTable.getMode() == Mode.RESERVED) {
			userContext.privileges().assureReadPrivilege(fromTable);
		}
	}
	
	@JSONExported
	public JSONObject getDetailList(
			JSONObject serializer,
			@Parameter("IdClass") int masterIdClass,
			@Parameter("Id") int masterIdCard,
			@Parameter("limit") int limit,
			@Parameter("start") int offset,
			@Parameter(value="sort",required=false) String sortField,
			@Parameter(value="dir",required=false) String sortDirection,
			@Parameter(value="query",required=false) String fullTextQuery,
			@Parameter(value="DirectedDomain", required=false) String directedDomainParameter,
			ITableFactory tf,
			RelationFactory rf,
			DomainFactory df) throws JSONException, CMDBException {
		JSONArray rows = new JSONArray();
		
		//define the inverse domain
		DirectedDomain directedDomain = stringToDirectedDomain(df, directedDomainParameter);
		DirectedDomain invertedDomain = DirectedDomain.create(directedDomain.getDomain(), !directedDomain.getDirectionValue());

		CardQuery masterQuery = tf.get(masterIdClass).cards().list().id(masterIdCard);

		CardQuery detailQuery = directedDomain.getDestTable().cards().list().cardInRelation(invertedDomain, masterQuery);
		
		if (fullTextQuery != null)
			detailQuery.fullText(fullTextQuery.trim());
		if (sortField != null || sortDirection != null) {
			if (sortField.endsWith("_value"))
				sortField = sortField.substring(0, sortField.length()-6);
			detailQuery.clearOrder().order(sortField, OrderFilterType.valueOf(sortDirection));
		}
		for(ICard card : detailQuery.subset(offset, limit).count()) {
			rows.put(Serializer.serializeCardWithPrivileges(card, false));
		}
		serializer.put("rows", rows);
		serializer.put("results", detailQuery.getTotalRows());
		return serializer;
	}
	
	
	/*
	 * TODO: Find a way to fix this somewhere else
	 */
	private void removeReadOnlySubclasses(CardQuery cardQuery, UserContext userContext) {
		List<String> readOnlyTables = new LinkedList<String>();
		TableTree wholeTree = userContext.tables().fullTree();
		for (ITable table : wholeTree) {
			if (PrivilegeType.READ.equals(table.getMetadata().get(MetadataService.RUNTIME_PRIVILEGES_KEY)))
				readOnlyTables.add(String.valueOf(table.getId()));
		}
		if (!readOnlyTables.isEmpty()) {
			String[] readOnlyTablesArray = readOnlyTables.toArray(new String[0]);
			cardQuery.filter(ICard.CardAttributes.ClassId.toString(), AttributeFilterType.DIFFERENT, (Object[])readOnlyTablesArray);
		}
	}

    @JSONExported 
    public JSONObject getCard( 
    		JSONObject serializer, 
    		ICard card ) throws JSONException, CMDBException { 
    	serializer.put("card", Serializer.serializeCardWithPrivileges(card, false)); 
    	serializer.put("attributes", Serializer.serializeAttributeList(card.getSchema(), true)); 
    	return serializer; 
    }

	@JSONExported
	public void resetCardFilter(
			@Parameter(value=CardQueryParameter.FILTER_CATEGORY_PARAMETER, required=false) String categoryOrNull,
			@Parameter(value=CardQueryParameter.FILTER_SUBCATEGORY_PARAMETER, required=false) String subcategoryOrNull			
		) throws JSONException, CMDBException {
		FilterService.clearFilters(categoryOrNull, subcategoryOrNull);
	}

	@JSONExported
	public JSONObject setCardFilter(JSONObject serializer,
			Map<String,String> requestParams,
			@Parameter(CardQueryParameter.FILTER_CLASSID) int classId,
			@Parameter(value="cql",required=false) String cqlQuery,
			@Parameter(value=CardQueryParameter.FILTER_CATEGORY_PARAMETER, required=false) String categoryOrNull,
			@Parameter(value=CardQueryParameter.FILTER_SUBCATEGORY_PARAMETER, required=false) String subcategoryOrNull,
			@Parameter(value="checkedRecords", required=false) JSONObject cardInRelation,
			ITableFactory tf,
			DomainFactory df ) throws JSONException, CMDBException {
		CardQuery cardFilter = FilterService.getFilter(classId, categoryOrNull, subcategoryOrNull);
		cardFilter.reset();
		if(cqlQuery != null && cqlQuery.trim().length() > 0) {
			Map<String,Object> reqPrms = new HashMap<String,Object>();
			reqPrms.putAll(requestParams);
			CQLFacadeCompiler.naiveCmbuildCompileSystemUser(cardFilter, cqlQuery, -1, -1, reqPrms);
		} else {
			addAttributeFilter(cardFilter, requestParams);
			addRelationFilter(cardFilter, cardInRelation, tf, df);
		}
		return serializer;
	}

	private void addAttributeFilter(CardQuery cardFilter,
			Map<String,String> requestParams) {
		// Builds a map of attribute name, suffix list
		Map<String, List<String>> attributeMap = buildFilterAttributesMap(requestParams);
		// attribute filter
		for (String attributeName : attributeMap.keySet()) {
			IAttribute attribute = cardFilter.getTable().getAttribute(attributeName);
			List<AbstractFilter> filterList = new LinkedList<AbstractFilter>();
			for (String suffix : attributeMap.get(attributeName)) {
				filterList.add(buildFilterForAttribute(attribute, suffix, requestParams));
			}
			if (filterList.size() == 0)
				cardFilter.filter(filterList.iterator().next());
			else
				cardFilter.filter(new FilterOperator(OperatorType.OR, filterList));
		}
	}

	private Map<String, List<String>> buildFilterAttributesMap(Map<String,String> requestParams) {
		Map<String, List<String>> attributeMap = new HashMap<String, List<String>>();
		for (String requestParamName : requestParams.keySet()) {
			if (requestParamName.endsWith("_ftype")) {
				if (requestParams.get(requestParamName).length() == 0)
					continue;
				//requestAttributeName is the requestParams without the "_ftype" substring
				String requestAttributeName = requestParamName.substring(0,requestParamName.length()-6);
				//attributeName is the requestAttributeNames without the generatedId
				int suffixPosition = requestAttributeName.lastIndexOf("_");
				String attributeName = requestAttributeName.substring(0, suffixPosition);
				String attributeSuffix = requestAttributeName.substring(suffixPosition);
				if (attributeMap.containsKey(attributeName)) {
					attributeMap.get(attributeName).add(attributeSuffix);
				} else {
					List<String> newList = new LinkedList<String>();
					newList.add(attributeSuffix);
					attributeMap.put(attributeName, newList);
				}
			}
		}
		return attributeMap;
	}

	private AbstractFilter buildFilterForAttribute(IAttribute attribute, String suffix, Map<String,String> requestParams) {
		String attributeName = attribute.getName();
		String fullAttributeName = attributeName + suffix;
		String ftype = requestParams.get(fullAttributeName+"_ftype");
		if (ftype.equals("null"))
			return new AttributeFilter(attribute, AttributeFilterType.NULL);
		if (ftype.equals("between")) {
			List<AbstractFilter> subFilters = new LinkedList<AbstractFilter>();
			subFilters.add(new AttributeFilter(attribute, AttributeFilterType.MAJOR, requestParams.get(fullAttributeName)));
			subFilters.add(new AttributeFilter(attribute, AttributeFilterType.MINOR, requestParams.get(fullAttributeName+"_end")));
			return new FilterOperator(OperatorType.AND, subFilters);
		}
		return new AttributeFilter(attribute, AttributeFilterType.valueOf(ftype.toUpperCase()), requestParams.get(fullAttributeName));
	}

	private void addRelationFilter(CardQuery cardFilter,
			JSONObject cardInRelation,
			ITableFactory tf,
			DomainFactory df ) throws JSONException {
		if (cardInRelation != null) {
			JSONArray domains = cardInRelation.names();
			if (domains != null){
				for (int i = 0; i < domains.length(); ++i) {
					String domainDirectionString = domains.getString(i);
					DirectedDomain directedDomain = stringToDirectedDomain(df, domainDirectionString);
					try {
						JSONObject cardObject = cardInRelation.getJSONObject(domainDirectionString);
						setFileterForCard(cardObject, directedDomain, cardFilter, tf);
						
					} catch(JSONException e) {
						int destClassId = cardInRelation.getInt(domainDirectionString);
						cardFilter.cardNotInRelation(directedDomain, tf.get(destClassId));
					}
				}
			}
		}		
	}

	private void setFileterForCard(JSONObject cardObject, 
		DirectedDomain directedDomain, 
		CardQuery cardQuery,
		ITableFactory tf) throws JSONException{
        	// THIS IS AWFUL       	
        	String type = cardObject.getString("type");
        	if (type.equals("cards")){
        	    List<ICard> destCards = new LinkedList<ICard>();
        	    JSONArray cardArray = cardObject.getJSONArray("cards");
        	    for (int j = 0; j < cardArray.length(); ++j) {
        		ICard destCard = stringToCard(tf, cardArray.getString(j));
        		destCards.add(destCard);
        	    }
        	    CardQuery destQuery = directedDomain.getDestTable().cards().list();
        	    destQuery.cards(destCards);
        	    cardQuery.cardInRelation(directedDomain, destQuery);  
        	} else if (type.equals("notRel")) {
        	    int destinationClass = cardObject.getInt("destinationClass");
        	    ITable destination = tf.get(destinationClass);
        	    cardQuery.cardNotInRelation(directedDomain, destination);
        	} else {
        	    int destinationClass = cardObject.getInt("destinationClass");
        	    CardQuery relationFilter = FilterService.getFilter(destinationClass, "domaincardlistfilter", directedDomain.toString());
        	    cardQuery.cardInRelation(directedDomain, relationFilter);
        	}
	}
	@JSONExported
	public JSONObject getCardListShort(
			JSONObject serializer,
			@Parameter("limit") int limit,
			@Parameter("Id") int cardId,
			CardQuery cardQueryTemplate) throws JSONException, CMDBException {
		final String[] shortAttrList = {"Id","Description"};
		CardQuery cardQuery = ((CardQuery) cardQueryTemplate.clone())
			.attributes(shortAttrList)
			.order(ICard.CardAttributes.Description.toString(), OrderFilterType.ASC);
		if (limit > 0)
			cardQuery.limit(limit).count();
		if (cardId > 0)
			cardQuery.id(cardId);
		JSONArray rows = new JSONArray();
		for(ICard card: cardQuery) {
			rows.put(Serializer.serializeCardNormalized(card));
		}
		serializer.put("rows", rows);
		if (cardQuery.needsCount())
			serializer.put("results", cardQuery.getTotalRows());
		return serializer;
	}

	@JSONExported
	public JSONObject getCardPosition(
			JSONObject serializer,
			ICard card,
			UserContext userCtx,
			@Parameter("withflowstatus") boolean withFlowStatus,
			@Parameter(value="sort",required=false) String sortField,
			@Parameter(value="dir",required=false) String sortDirection,
			CardQuery currentCardFilter ) throws JSONException, CMDBException {
		CardQuery cardFilter = (CardQuery) currentCardFilter.clone();

		removeAttributesNotNeededForPositionQuery(cardFilter);
		if (withFlowStatus) {
			Lookup stateLookup = (Lookup)card.getValue(ProcessAttributes.FlowStatus.toString());
			serializer.put("flowstatus", stateLookup.getCode());
			String lookupId = String.valueOf(stateLookup.getId());
			cardFilter.filterUpdate(ProcessAttributes.FlowStatus.toString(), AttributeFilterType.EQUALS, lookupId);
			
			if (stateLookup.getCode().startsWith(WorkflowConstants.StateOpen)) {
				cardFilter.setNextExecutorFilter(userCtx);
			}
		}
		if (sortField != null || sortDirection != null) {
			if (sortField.endsWith("_value"))
				sortField = sortField.substring(0, sortField.length()-6);
			cardFilter.clearOrder().order(sortField, OrderFilterType.valueOf(sortDirection));
		}
	
		serializer.put("position", cardFilter.position(card.getId()));
		
		return serializer;
	}

	private void removeAttributesNotNeededForPositionQuery(CardQuery cardQuery) {
		String fullTextQuery = cardQuery.getFullTextQuery();
		// fulltext query needs every field
		if ((fullTextQuery == null) || (fullTextQuery.trim().length() == 0)) {
			Set<String> attrList = new HashSet<String>();
			addStandardAttributes(attrList);
			addFilterAttributes(cardQuery.getFilter(), attrList);
			addOrderingAttributes(cardQuery, attrList);
			cardQuery.attributes(attrList.toArray(new String[attrList.size()]));
		}
	}

	private void addStandardAttributes(Set<String> attrList) {
		attrList.add(ICard.CardAttributes.Id.toString());
		attrList.add(ICard.CardAttributes.Status.toString());
		attrList.add(ProcessAttributes.FlowStatus.toString());
	}

	private void addFilterAttributes(AbstractFilter abstractFilter, Set<String> attrList) {
		if (abstractFilter instanceof CompositeFilter) {
			CompositeFilter compositeFilter = (CompositeFilter) abstractFilter;
			for (CompositeFilterItem item : compositeFilter.getItems()) {
				addFilterAttributes(item.getFilter(), attrList);
			}
		} else if (abstractFilter instanceof FilterOperator) {
			FilterOperator filterOperator = (FilterOperator) abstractFilter;
			for (AbstractFilter filter : filterOperator.getExpressions()) {
				addFilterAttributes(filter, attrList);
			}
		} else if (abstractFilter instanceof AttributeFilter) {
			AttributeFilter attributeFilter = (AttributeFilter) abstractFilter;
			attrList.add(attributeFilter.getAttributeName());
		}
	}

	private void addOrderingAttributes(CardQuery cardFilter,
			Set<String> attrList) {
		for (OrderFilter f : cardFilter.getOrdering()) {
			attrList.add(f.getAttributeName());
		}
	}

	@JSONExported
	public JSONObject updateCard(
			JSONObject serializer,
			Map<String,String> attributes,
			ICard card
	) throws JSONException, CMDBException {
		setCardAttributes(card, attributes, false);
		boolean created = card.isNew();
		card.save();
		if (created) {
			serializer.put("id", card.getId());
		}
		setCardGeoFeatures(card, attributes);
		return serializer;
	}

	private void setCardGeoFeatures(ICard card, Map<String, String> attributes) throws JSONException {
		String geoAttributesJsonString = attributes.get("geoAttributes");
		if (geoAttributesJsonString != null) {
			GeoCard geoCard = new GeoCard(card);
			final JSONObject geoAttributesObject = new JSONObject(geoAttributesJsonString);
			final String[] geoAttributesName = JSONObject.getNames(geoAttributesObject);
			if (geoAttributesName != null) {
				for (String name : geoAttributesName) {
					final String value = geoAttributesObject.getString(name);
					geoCard.setGeoFeatureValue(name, value);
				}
			}
		}
	}

	@JSONExported
	public void updateBulkCards(
			Map<String,String> attributes,
			@Parameter(value="selections", required=false) String[] cardsToUpdate,
			@Parameter(value="fullTextQuery", required=false) String fullTextQuery,
			@Parameter("isInverted") boolean isInverted,
			CardQuery cardQuery,
			ITableFactory tf
	) throws JSONException, CMDBException {

		if (fullTextQuery != null) {
		    cardQuery.fullText(fullTextQuery.trim());
		}
		
		List<ICard> cardsList = buildCardListToBulkUpdate(cardsToUpdate, tf);
		if (isInverted) {
			if (!cardsList.isEmpty()) {
				cardQuery.excludeCards(cardsList);
			}
		} else {
	    	cardQuery.cards(cardsList);
		}
		
		ICard card = cardQuery.getTable().cards().create(); // Unprivileged card as a template
    	setCardAttributes(card, attributes, true);
		cardQuery.clearOrder().subset(0, 0).update(card);
	}

	private List<ICard> buildCardListToBulkUpdate(String[] cardsToUpdate,
			ITableFactory tf) {
		List<ICard> cardsList = new LinkedList<ICard>();
		if (cardsToUpdate[0] != "") { //if the first element is an empty string the array is empty
	    	for (String cardIdAndClass: cardsToUpdate) {
	    	    ICard cardToUpdate = stringToCard(tf, cardIdAndClass);
	    	    cardsList.add(cardToUpdate);
	    	}
		}
		return cardsList;
	}
	
	static public void setCardAttributes(ICard card, Map<String, String> attributes, Boolean forceChange){
		for (IAttribute attribute : card.getSchema().getAttributes().values()) {
			if (!attribute.isDisplayable())
				continue;
			String attrName = attribute.getName();
			String attrNewValue = attributes.get(attrName);
			if (null != attrNewValue) {
				if (forceChange) {
					card.getAttributeValue(attrName).setValueForceChange(attrNewValue);
				} else {
					card.getAttributeValue(attrName).setValue(attrNewValue);
				}
			}
		}
	}

	@JSONExported
	public void deleteCard(
			ICard card) throws JSONException, CMDBException {
		card.delete();
	}
	
	@JSONExported
	public JSONObject deleteDetailCard(
			JSONObject serializer,
			IRelation relation,
			@OverrideKeys(key={"Id","IdClass"},newKey={"CardId","ClassId"})
			ICard detailCard) {
		
		relation.delete();
		detailCard.delete();
		return serializer;
	}

	@JSONExported
	public JSONObject getCardHistory(
			JSONObject serializer,
			ICard card,
			ITableFactory tf,
			RelationFactory rf,
			@Parameter("IsProcess") boolean isProcess) throws JSONException, CMDBException {
		if (isProcess)
			return getProcessHistory(serializer, card, tf);
		JSONArray rows = new JSONArray();
		CardQuery cardQuery = tf.get(card.getIdClass()).cards().list().history(card.getId());
		rows.put(Serializer.serializeCard(card, true));
		for (ICard cardHistory: cardQuery) {
			JSONObject sc = Serializer.serializeCard(cardHistory, true);
			sc.put("_AttrHist", true);
			rows.put(sc);
		}
		for(IRelation relationHistory: rf.list(card).straightened().history()){
			JSONObject sr = Serializer.serializeRelation(relationHistory);
			sr.put("_RelHist", true);
			rows.put(sr);
		}
		serializer.put("rows", rows);
		return serializer;
	}

	private JSONObject getProcessHistory(
			JSONObject serializer,
			ICard card,
			ITableFactory tf) throws JSONException, CMDBException {
		JSONArray rows = new JSONArray();
		CardQuery cardQuery = tf.get(card.getIdClass()).cards().list().history(card.getId())
			.filter("User", AttributeFilterType.DONTCONTAINS, "RemoteApi")
			.filter("User", AttributeFilterType.DONTCONTAINS, "System")
			.order(ICard.CardAttributes.Code.toString(), OrderFilterType.ASC)
			.order(ICard.CardAttributes.BeginDate.toString(), OrderFilterType.ASC);
		for(ICard process: cardQuery) {
			String processCode = process.getCode();
			if (processCode != null && processCode.length() != 0) {
				JSONObject sc = Serializer.serializeCard(process, true);
				sc.put("_AttrHist", true);
				rows.put(sc);
			}
		}
		serializer.put("rows", rows);
		return serializer;
	}

	/*
	 * Relations
	 */

	@JSONExported
	public JSONObject getRelationList(
			ICard card,
			UserContext userCtx,
			@Parameter(value = "domainlimit", required = false) int domainlimit,
			@Parameter(value = "domainId", required = false) Long domainId,
			@Parameter(value = "src", required = false) String querySource) throws JSONException {

		final DataAccessLogic dataAccesslogic = new DataAccessLogic();
		final Card src = new Card(card.getSchema().getId(), card.getId());
		final DomainWithSource dom = DomainWithSource.create(domainId, querySource);
		final GetRelationListResponse out = dataAccesslogic.getRelationList(src, dom);
		return new JsonGetRelationListResponse(out, domainlimit).toJson();
	}

	@JSONExported
	@Transacted
	public void createRelations(
			ICard card,
			@Parameter("Relations") JSONObject relations,
			ITableFactory tf,
			RelationFactory rf,
			DomainFactory df ) throws JSONException, CMDBException {
		String domainDirectionString = relations.names().getString(0);
		DirectedDomain directedDomain = stringToDirectedDomain(df, domainDirectionString);
		JSONArray cardArray = relations.getJSONArray(domainDirectionString);
		for (int i=0; i<cardArray.length(); ++i) {
			String cardString = cardArray.getString(i);
			ICard destCard = stringToCard(tf, cardString);
			IRelation relation;
			IDomain domain = directedDomain.getDomain();
			if (directedDomain.getDirectionValue()) {
				relation = rf.create(domain, card, destCard);
			} else {
				relation = rf.create(domain, destCard, card);
			}
			relation.save();
		}
	}

	@JSONExported
	public void modifyRelation(
			IRelation relation,
			@Parameter("DomainDirection") boolean direction,
			ICard newCard ) throws JSONException, CMDBException {
		if (direction) {
			relation.setCard2(newCard);
		} else {
			relation.setCard1(newCard);
		}
		relation.save();
	}

	@JSONExported
	public void deleteRelation(
			IRelation relation ) throws JSONException, CMDBException {
		relation.delete();
	}

	/*
	 * Attachments
	 */

	@JSONExported
	public JSONObject getAttachmentList(
			JSONObject serializer,
			UserContext userCtx,
			ICard card ) throws JSONException, CMDBException {
		AlfrescoFacade alfrescoOperation = new AlfrescoFacade(userCtx, card.getSchema().getName(), card.getId());
		JSONArray rows = new JSONArray();
		for(AttachmentBean attachment : alfrescoOperation.search()) {
			rows.put(Serializer.serializeAttachment(attachment));
		}
		serializer.put("rows", rows);
		return serializer;
	}

	@JSONExported
	public DataHandler downloadAttachment(
			UserContext userCtx,
			@Parameter("Filename") String filename,
			ICard card ) throws JSONException, CMDBException {
		AlfrescoFacade alfrescoOperation = new AlfrescoFacade(userCtx, card.getSchema().getName(), card.getId());
		return alfrescoOperation.download(filename);
	}

	@JSONExported
	public void uploadAttachment(
			UserContext userCtx,
			@Parameter("File") FileItem file,
			@Parameter("Category") String category,
			@Parameter("Description") String description,
			ICard card ) throws JSONException, CMDBException, IOException {
		AlfrescoFacade alfrescoOperation = new AlfrescoFacade(userCtx, card.getSchema().getName(), card.getId());
		alfrescoOperation.upload(file.getInputStream(), removeFilePath(file.getName()), category, description);
	}

	// Needed by Internet Explorer that uploads the file with full path
	private String removeFilePath(String name) {
		int backslashIndex = name.lastIndexOf("\\");
		int slashIndex = name.lastIndexOf("/");
		int fileNameIndex = Math.max(slashIndex, backslashIndex) + 1;
		return name.substring(fileNameIndex);
	}

	@JSONExported
	public JSONObject deleteAttachment(
			JSONObject serializer,
			UserContext userCtx,
			@Parameter("Filename") String filename,
			ICard card ) throws JSONException, CMDBException, IOException {
		AlfrescoFacade alfrescoOperation = new AlfrescoFacade(userCtx, card.getSchema().getName(), card.getId());
		alfrescoOperation.delete(filename);
		return serializer;
	}

	@JSONExported
	public JSONObject modifyAttachment(
			JSONObject serializer,
			UserContext userCtx,
			@Parameter("Filename") String filename,
			@Parameter("Description") String description,
			ICard card ) throws JSONException, CMDBException, IOException {
		AlfrescoFacade alfrescoOperation = new AlfrescoFacade(userCtx, card.getSchema().getName(), card.getId());
		alfrescoOperation.updateDescription(filename, description);
		return serializer;
	}

	static private DirectedDomain stringToDirectedDomain(DomainFactory df, String string) {
		StringTokenizer st = new StringTokenizer(string, "_");
		int domainId = Integer.parseInt(st.nextToken());
		IDomain domain = df.get(domainId);
		DomainDirection direction = DomainDirection.valueOf(st.nextToken());
		return DirectedDomain.create(domain, direction);
	}

	static private ICard stringToCard(ITableFactory tf, String string) {
		StringTokenizer st = new StringTokenizer(string, "_");
		int classId = Integer.parseInt(st.nextToken());
		int cardId = Integer.parseInt(st.nextToken());
		return tf.get(classId).cards().get(cardId);
	}
}