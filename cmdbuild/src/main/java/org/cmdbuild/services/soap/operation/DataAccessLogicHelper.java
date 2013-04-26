package org.cmdbuild.services.soap.operation;

import static com.google.common.collect.FluentIterable.from;
import static org.apache.commons.lang.StringUtils.EMPTY;
import static org.cmdbuild.services.soap.operation.SerializationStuff.serialize;
import static org.cmdbuild.services.soap.operation.SerializationStuff.Functions.toAttributeSchema;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.activation.DataHandler;
import javax.activation.DataSource;

import org.apache.commons.lang.StringUtils;
import org.cmdbuild.auth.user.OperationUser;
import org.cmdbuild.common.utils.TempDataSource;
import org.cmdbuild.dao.CardStatus;
import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.CMDomain;
import org.cmdbuild.dao.query.clause.QueryDomain.Source;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.logger.Log;
import org.cmdbuild.logic.LogicDTO.DomainWithSource;
import org.cmdbuild.logic.WorkflowLogic;
import org.cmdbuild.logic.commands.AbstractGetRelation.RelationInfo;
import org.cmdbuild.logic.commands.GetCardHistory.GetCardHistoryResponse;
import org.cmdbuild.logic.commands.GetRelationHistory.GetRelationHistoryResponse;
import org.cmdbuild.logic.commands.GetRelationList.DomainInfo;
import org.cmdbuild.logic.commands.GetRelationList.GetRelationListResponse;
import org.cmdbuild.logic.data.QueryOptions;
import org.cmdbuild.logic.data.access.DataAccessLogic;
import org.cmdbuild.logic.data.access.FetchCardListResponse;
import org.cmdbuild.logic.data.access.RelationDTO;
import org.cmdbuild.model.data.Card;
import org.cmdbuild.report.ReportFactory;
import org.cmdbuild.report.ReportFactory.ReportExtension;
import org.cmdbuild.report.ReportFactory.ReportType;
import org.cmdbuild.report.ReportFactoryDB;
import org.cmdbuild.report.ReportParameter;
import org.cmdbuild.services.auth.PrivilegeManager.PrivilegeType;
import org.cmdbuild.services.auth.UserContext;
import org.cmdbuild.services.meta.MetadataService;
import org.cmdbuild.services.soap.serializer.MenuSchemaSerializer;
import org.cmdbuild.services.soap.structure.AttributeSchema;
import org.cmdbuild.services.soap.structure.ClassSchema;
import org.cmdbuild.services.soap.structure.MenuSchema;
import org.cmdbuild.services.soap.types.Attribute;
import org.cmdbuild.services.soap.types.CQLParameter;
import org.cmdbuild.services.soap.types.CQLQuery;
import org.cmdbuild.services.soap.types.Card.ValueSerializer;
import org.cmdbuild.services.soap.types.CardExt;
import org.cmdbuild.services.soap.types.CardList;
import org.cmdbuild.services.soap.types.CardListExt;
import org.cmdbuild.services.soap.types.Metadata;
import org.cmdbuild.services.soap.types.Order;
import org.cmdbuild.services.soap.types.Query;
import org.cmdbuild.services.soap.types.Reference;
import org.cmdbuild.services.soap.types.Relation;
import org.cmdbuild.services.soap.types.Report;
import org.cmdbuild.services.soap.types.ReportParams;
import org.cmdbuild.services.soap.utils.SoapToJsonUtils;
import org.cmdbuild.services.store.menu.MenuStore;
import org.cmdbuild.services.store.menu.MenuStore.MenuItem;
import org.cmdbuild.services.store.report.ReportStore;
import org.cmdbuild.workflow.CMWorkflowException;
import org.cmdbuild.workflow.user.UserActivityInstance;
import org.cmdbuild.workflow.user.UserProcessInstance;
import org.joda.time.DateTime;
import org.json.JSONArray;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class DataAccessLogicHelper implements SoapLogicHelper {

	private static final Marker marker = MarkerFactory.getMarker(DataAccessLogicHelper.class.getName());

	private final String ACTIVITY_DESCRIPTION_ATTRIBUTE = "ActivityDescription";
	private final String INVALID_ACTIVITY_DESCRIPTION = StringUtils.EMPTY;

	private final CMDataView dataView;
	private final DataAccessLogic dataAccessLogic;
	private final WorkflowLogic workflowLogic;
	private final OperationUser operationUser;
	private final javax.sql.DataSource dataSource;

	private MenuStore menuStore;
	private ReportStore reportStore;

	public DataAccessLogicHelper(final CMDataView dataView, final DataAccessLogic datAccessLogic,
			final WorkflowLogic workflowLogic, final OperationUser operationUser, final javax.sql.DataSource dataSource) {
		this.dataView = dataView;
		this.dataAccessLogic = datAccessLogic;
		this.workflowLogic = workflowLogic;
		this.operationUser = operationUser;
		this.dataSource = dataSource;
	}

	public void setMenuStore(final MenuStore menuStore) {
		this.menuStore = menuStore;
	}

	public void setReportStore(final ReportStore reportStore) {
		this.reportStore = reportStore;
	}

	public AttributeSchema[] getAttributeList(final String className) {
		logger.info(marker, "getting attributes schema for class '{}'", className);
		return from(dataAccessLogic.findClass(className).getActiveAttributes()) //
				.transform(toAttributeSchema()) //
				.toArray(AttributeSchema.class);
	}

	public int createCard(final org.cmdbuild.services.soap.types.Card card) {
		return dataAccessLogic.createCard(transform(card)).intValue();
	}

	public boolean updateCard(final org.cmdbuild.services.soap.types.Card card) {
		dataAccessLogic.updateCard(transform(card));
		return true;
	}

	public boolean deleteCard(final String className, final int cardId) {
		dataAccessLogic.deleteCard(className, Long.valueOf(cardId));
		return true;
	}

	public boolean createRelation(final Relation relation) {
		dataAccessLogic.createRelations(transform(relation));
		return true;
	}

	public boolean deleteRelation(final Relation relation) {
		final CMDomain domain = dataView.findDomain(relation.getDomainName());
		final DomainWithSource dom = DomainWithSource.create(domain.getId(), Source._1.toString());
		final Card srcCard = Card.newInstance() //
				.withClassName(relation.getClass1Name()) //
				.withId(Long.valueOf(relation.getCard1Id())) //
				.build();
		final GetRelationListResponse response = dataAccessLogic.getRelationList(srcCard, dom);
		for (final DomainInfo domainInfo : response) {
			for (final RelationInfo relationInfo : domainInfo) {
				if (relationInfo.getTargetId().equals(Long.valueOf(relation.getCard2Id()))) {
					final RelationDTO relationToDelete = transform(relation);
					relationToDelete.relationId = relationInfo.getRelationId();
					dataAccessLogic.deleteRelation(relationToDelete.domainName, relationToDelete.relationId);
				}
			}
		}
		return true;
	}

	private Card transform(final org.cmdbuild.services.soap.types.Card card) {
		final Card _card = Card.newInstance() //
				.withClassName(card.getClassName()) //
				.withId(Long.valueOf(card.getId())) //
				.withAllAttributes(transform(card.getAttributeList())) //
				.build();
		return _card;
	}

	private static Map<String, Object> transform(final List<Attribute> attributes) {
		final Map<String, Object> keysAndValues = Maps.newHashMap();
		for (final Attribute attribute : attributes) {
			final String name = attribute.getName();
			final String value = attribute.getValue();
			if (value != null) {
				keysAndValues.put(name, value);
			}
		}
		return keysAndValues;
	}

	private RelationDTO transform(final Relation relation) {
		final RelationDTO relationDTO = new RelationDTO();
		relationDTO.domainName = relation.getDomainName();
		relationDTO.master = Source._1.toString();
		relationDTO.addSourceCard(Long.valueOf(relation.getCard1Id()), relation.getClass1Name());
		relationDTO.addDestinationCard(Long.valueOf(relation.getCard2Id()), relation.getClass2Name());
		return relationDTO;
	}

	private Relation transform(final RelationInfo relationInfo, final CMDomain domain, final Source source) {
		final Relation relation = new Relation();
		relation.setBeginDate(relationInfo.getRelationBeginDate().toGregorianCalendar());
		final DateTime endDate = relationInfo.getRelationEndDate();
		relation.setEndDate(endDate != null ? endDate.toGregorianCalendar() : null);
		relation.setStatus(CardStatus.ACTIVE.value());
		relation.setDomainName(domain.getIdentifier().getLocalName());
		if (source.equals(Source._1.toString())) {
			relation.setClass1Name(domain.getClass1().getIdentifier().getLocalName());
			relation.setClass2Name(domain.getClass2().getIdentifier().getLocalName());
		} else {
			relation.setClass1Name(domain.getClass2().getIdentifier().getLocalName());
			relation.setClass2Name(domain.getClass1().getIdentifier().getLocalName());
		}
		relation.setCard1Id(relationInfo.getRelation().getCard1Id().intValue());
		relation.setCard2Id(relationInfo.getRelation().getCard2Id().intValue());
		return relation;
	}

	public List<Relation> getRelations(final String className, final String domainName, final Long cardId) {
		final CMDomain domain = dataView.findDomain(domainName);
		final DomainWithSource dom;
		if (domain.getClass1().getIdentifier().getLocalName().equals(className)) {
			dom = DomainWithSource.create(domain.getId(), Source._1.toString());
		} else {
			dom = DomainWithSource.create(domain.getId(), Source._2.toString());
		}
		final Card srcCard = buildCard(cardId, className);
		final GetRelationListResponse response = dataAccessLogic.getRelationList(srcCard, dom);
		final List<Relation> relations = Lists.newArrayList();
		for (final DomainInfo domainInfo : response) {
			for (final RelationInfo relationInfo : domainInfo) {
				relations.add(transform(relationInfo, domain, Source.valueOf(dom.querySource)));
			}
		}
		return relations;
	}

	private Card buildCard(final Long cardId, final String className) {
		final Card card = Card.newInstance() //
				.withClassName(className) //
				.withId(cardId) //
				.build();
		return card;
	}

	public Relation[] getRelationHistory(final Relation relation) {
		final List<Relation> historicRelations = Lists.newArrayList();
		final CMDomain domain = dataView.findDomain(relation.getDomainName());
		final Card srcCard = buildCard(Long.valueOf(relation.getCard1Id()), relation.getClass1Name());
		final GetRelationHistoryResponse response = dataAccessLogic.getRelationHistory(srcCard, domain);
		for (final RelationInfo relationInfo : response) {
			if (relationInfo.getRelation().getCard1Id().equals(Long.valueOf(relation.getCard1Id()))
					&& relationInfo.getRelation().getCard2Id().equals(Long.valueOf(relation.getCard2Id()))) {
				historicRelations.add(transform(relationInfo, domain, Source._1));
			}
		}
		return historicRelations.toArray(new Relation[historicRelations.size()]);
	}

	public CardExt getCardExt(final String className, final Long cardId, final Attribute[] attributeList) {
		final Card fetchedCard;
		if (attributeList == null || attributeList.length == 0) {
			fetchedCard = dataAccessLogic.fetchCard(className, cardId);
		} else {
			final QueryOptions queryOptions = QueryOptions.newQueryOption() //
					.onlyAttributes(displayableAttributes(attributeList)) //
					.build();
			fetchedCard = dataAccessLogic.fetchCardShort(className, cardId, queryOptions);
		}
		return transformToCardExt(fetchedCard, attributeList);
	}

	private JSONArray displayableAttributes(final Attribute[] attributeList) {
		final JSONArray array = new JSONArray();
		for (final Attribute attribute : attributeList) {
			array.put(attribute.getName());
		}
		return array;
	}

	private CardExt transformToCardExt(final Card card, final Attribute[] attributeList) {
		final CardExt cardExt;
		final ValueSerializer valueSerializer = new ValueSerializer(card);
		if (attributeList == null || attributeList.length == 0) {
			cardExt = new CardExt(card, valueSerializer);
		} else {
			cardExt = new CardExt(card, attributeList, valueSerializer);
		}
		addExtras(card, cardExt);
		return cardExt;
	}

	private void addExtras(final Card card, final CardExt cardExt) {
		final CMClass activityClass = dataAccessLogic.findClass("Activity");
		if (activityClass.isAncestorOf(card.getType())) {
			final UserProcessInstance processInstance = workflowLogic.getProcessInstance(card.getClassName(),
					card.getId());
			final WorkflowLogicHelper workflowLogicHelper = new WorkflowLogicHelper(workflowLogic);
			UserActivityInstance activityInstance = null;
			try {
				activityInstance = workflowLogicHelper.selectActivityInstanceFor(processInstance);
			} catch (final CMWorkflowException e) {
				activityInstance = null;
			}
			addActivityExtras(activityInstance, cardExt);
			addActivityMetadata(activityInstance, cardExt);
		} else {
			addMetadata(card, cardExt);
		}
	}

	private void addActivityExtras(final UserActivityInstance actInst, final CardExt cardExt) {
		String activityDescription = INVALID_ACTIVITY_DESCRIPTION;
		if (actInst != null) {
			try {
				activityDescription = actInst.getDefinition().getDescription();
			} catch (final CMWorkflowException e) {
				// keep the placeholder description
			}
		}
		cardExt.getAttributeList().add(newAttribute(ACTIVITY_DESCRIPTION_ATTRIBUTE, activityDescription));
	}

	private Attribute newAttribute(final String name, final String value) {
		final Attribute attribute = new Attribute();
		attribute.setName(name);
		attribute.setValue(value);
		return attribute;
	}

	private void addActivityMetadata(final UserActivityInstance actInst, final CardExt cardExt) {
		final PrivilegeType privileges;
		if (actInst != null) {
			privileges = actInst.isWritable() ? PrivilegeType.WRITE : PrivilegeType.READ;
		} else {
			privileges = PrivilegeType.NONE;
		}
		addPrivilege(privileges, cardExt);
	}

	private void addPrivilege(final PrivilegeType privilege, final CardExt cardExt) {
		cardExt.setMetadata(Arrays.asList(newPrivilegeMetadata(privilege)));
	}

	private Metadata newPrivilegeMetadata(final PrivilegeType privilege) {
		final Metadata meta = new Metadata();
		meta.setKey(MetadataService.RUNTIME_PRIVILEGES_KEY);
		meta.setValue(privilegeSerialization(privilege));
		return meta;
	}

	private String privilegeSerialization(final PrivilegeType privileges) {
		return privileges.toString().toLowerCase();
	}

	// TODO: fetch privileges for table outside...
	private void addMetadata(final Card card, final CardExt cardExt) {
		final CMClass type = card.getType();
		final PrivilegeType privilege;
		if (operationUser.hasWriteAccess(type)) {
			privilege = PrivilegeType.WRITE;
		} else if (operationUser.hasReadAccess(type)) {
			privilege = PrivilegeType.READ;
		} else {
			privilege = PrivilegeType.NONE;
		}
		addPrivilege(privilege, cardExt);
	}

	public CardList getCardList(final String className, final Attribute[] attributeList, final Query queryType,
			final Order[] orderType, final Integer limit, final Integer offset, final String fullTextQuery,
			final CQLQuery cqlQuery) {
		final FetchCardListResponse response = cardList(className, attributeList, queryType, orderType, limit, offset,
				fullTextQuery, cqlQuery);
		return toCardList(response);
	}

	public CardListExt getCardListExt(final String className, final Attribute[] attributeList, final Query queryType,
			final Order[] orderType, final Integer limit, final Integer offset, final String fullTextQuery,
			final CQLQuery cqlQuery) {
		final FetchCardListResponse response = cardList(className, attributeList, queryType, orderType, limit, offset,
				fullTextQuery, cqlQuery);
		return toCardListExt(response);
	}

	private FetchCardListResponse cardList(final String className, final Attribute[] attributeList,
			final Query queryType, final Order[] orderType, final Integer limit, final Integer offset,
			final String fullTextQuery, final CQLQuery cqlQuery) {
		// TODO: manage guest filter
		final QueryOptions queryOptions = QueryOptions.newQueryOption() //
				.limit(limit != null ? limit : Integer.MAX_VALUE) //
				.offset(offset != null ? offset : 0) //
				.filter(SoapToJsonUtils.createJsonFilterFrom(queryType, fullTextQuery, cqlQuery)) //
				.orderBy(SoapToJsonUtils.toJsonArray(orderType)) //
				.onlyAttributes(SoapToJsonUtils.toJsonArray(attributeList)) //
				.parameters(cqlQuery != null ? toMap(cqlQuery.getParameters()) : new HashMap<String, Object>()) //
				.build();
		return dataAccessLogic.fetchCards(className, queryOptions);
	}

	private CardList toCardList(final FetchCardListResponse response) {
		final CardList cardList = new CardList();
		final int totalNumberOfCards = response.getTotalNumberOfCards();
		cardList.setTotalRows(totalNumberOfCards);
		for (final Card card : response.getPaginatedCards()) {
			cardList.addCard(new org.cmdbuild.services.soap.types.CardExt(card));
		}
		return cardList;
	}

	private CardListExt toCardListExt(final FetchCardListResponse response) {
		final CardListExt cardListExt = new CardListExt();
		final int totalNumberOfCards = response.getTotalNumberOfCards();
		cardListExt.setTotalRows(totalNumberOfCards);
		for (final Card card : response.getPaginatedCards()) {
			cardListExt.addCard(new org.cmdbuild.services.soap.types.CardExt(card));
		}
		return cardListExt;
	}

	private Map<String, Object> toMap(final List<CQLParameter> cqlParameters) {
		final Map<String, Object> parameters = Maps.newHashMap();
		for (final CQLParameter cqlParameter : cqlParameters) {
			parameters.put(cqlParameter.getKey(), cqlParameter.getValue());
		}
		return parameters;
	}

	public Reference[] getReference(final String classname, final Query query, final Order[] order,
			final Integer limit, final Integer offset, final String fullText, final CQLQuery cqlQuery) {
		final CardListExt cardList = getCardListExt(classname, null, query, order, limit, offset, fullText, cqlQuery);
		return from(cardList.getCards()) //
				.transform(new Function<CardExt, Reference>() {
					@Override
					public Reference apply(final CardExt input) {
						final Reference reference = new Reference();
						reference.setId(input.getId());
						reference.setClassname(classname);
						reference.setDescription(descriptionOf(input));
						reference.setTotalRows(cardList.getTotalRows());
						return reference;
					}
				}) //
				.toArray(Reference.class);
	}

	private String descriptionOf(final CardExt card) {
		for (final Attribute attribute : card.getAttributeList()) {
			if ("Description".equals(attribute.getName())) {
				return attribute.getValue();
			}
		}
		return EMPTY;
	}

	public CardList getCardHistory(final String className, final int cardId, final Integer limit, final Integer offset) {
		logger.info(marker, "getting history for '{}' card with id '{}'", className, cardId);
		final CardList cardList = new CardList();
		final Card card = Card.newInstance() //
				.withClassName(className) //
				.withId(Long.valueOf(cardId)) //
				.build();
		final GetCardHistoryResponse response = dataAccessLogic.getCardHistory(card);
		int size = 0;
		final int start = (offset != null) ? offset : 0;
		final int end = (limit != null) ? start + limit : Integer.MAX_VALUE;
		for (final Card historyCard : response) {
			if (start <= size && size < end) {
				cardList.addCard(new org.cmdbuild.services.soap.types.Card(historyCard));
			}
			size++;
		}
		cardList.setTotalRows(size);
		return cardList;
	}

	public ClassSchema getClassSchema(final String className) {
		logger.info(marker, "getting schema for class '{}'");
		final CMClass clazz = dataAccessLogic.findClass(className);

		final ClassSchema classSchema = new ClassSchema();
		classSchema.setName(clazz.getIdentifier().getLocalName());
		classSchema.setDescription(clazz.getDescription());
		classSchema.setSuperClass(clazz.isSuperclass());

		final List<AttributeSchema> attributes = Lists.newArrayList();
		for (final CMAttribute attribute : clazz.getAttributes()) {
			if (attribute.isSystem() || !attribute.isActive()) {
				logger.debug(marker, "skipping attribute '{}'", attribute.getName());
				continue;
			}
			logger.debug(marker, "keeping attribute '{}'", attribute.getName());
			attributes.add(serialize(attribute));
		}
		classSchema.setAttributes(attributes);

		return classSchema;
	}

	public MenuSchema getVisibleClassesTree() {
		final CMClass rootClass = dataView.findClass("Class");
		final MenuSchemaSerializer serializer = new MenuSchemaSerializer(operationUser, dataAccessLogic, workflowLogic);
		return serializer.serializeVisibleClassesFromRoot(rootClass);
	}

	public MenuSchema getVisibleProcessesTree() {
		final CMClass rootClass = dataView.findClass("Activity");
		final MenuSchemaSerializer serializer = new MenuSchemaSerializer(operationUser, dataAccessLogic, workflowLogic);
		return serializer.serializeVisibleClassesFromRoot(rootClass);
	}

	public MenuSchema getMenuSchemaForPreferredGroup() {
		final MenuItem rootMenuItem = menuStore.getMenuToUseForGroup(operationUser.getPreferredGroup().getName());
		final MenuSchemaSerializer serializer = new MenuSchemaSerializer(operationUser, dataAccessLogic, workflowLogic);
		return serializer.serializeMenuTree(rootMenuItem);
	}

	public Report[] getReportsByType(final String type, final int limit, final int offset) {
		final List<Report> pagedReports = new ArrayList<Report>();
		final ReportType reportType = ReportType.valueOf(type.toUpperCase());
		int numRecords = 0;
		final List<org.cmdbuild.model.Report> fetchedReports = reportStore.findReportsByType(reportType);
		for (final org.cmdbuild.model.Report report : fetchedReports) {
			if (report.isUserAllowed()) {
				++numRecords;
				if (limit > 0 && numRecords > offset && numRecords <= offset + limit) {
					pagedReports.add(transform(report));
				}
			}
		}
		return pagedReports.toArray(new Report[pagedReports.size()]);
	}

	private Report transform(final org.cmdbuild.model.Report reportModel) {
		final Report report = new Report();
		report.setDescription(reportModel.getDescription());
		report.setId(reportModel.getId());
		report.setTitle(reportModel.getCode());
		report.setType(reportModel.getType().toString());
		return report;
	}

	public AttributeSchema[] getReportParameters(final int id, final String extension) {
		ReportFactoryDB reportFactory;
		try {
			reportFactory = new ReportFactoryDB(dataSource, reportStore, id, ReportExtension.valueOf(extension.toUpperCase()));
			final List<AttributeSchema> reportParameterList = new ArrayList<AttributeSchema>();
			for (final ReportParameter reportParameter : reportFactory.getReportParameters()) {
				final CMAttribute reportAttribute = reportParameter.createCMDBuildAttribute();
				final AttributeSchema attribute = serialize(reportAttribute);
				reportParameterList.add(attribute);
			}
			return reportParameterList.toArray(new AttributeSchema[reportParameterList.size()]);
		} catch (final SQLException e) {
			Log.SOAP.error("SQL error in report", e);
		} catch (final IOException e) {
			Log.SOAP.error("Error reading report", e);
		} catch (final ClassNotFoundException e) {
			Log.SOAP.error("Cannot find class in report", e);
		}
		return null;
	}

	public DataHandler getReport(final int id, final String extension, final ReportParams[] params) {
		final ReportExtension reportExtension = ReportExtension.valueOf(extension.toUpperCase());
		try {
			final ReportFactoryDB reportFactory = new ReportFactoryDB(dataSource,reportStore, id, reportExtension);
			if (params != null) {
				for (final ReportParameter reportParameter : reportFactory.getReportParameters()) {
					for (final ReportParams param : params) {
						if (param.getKey().equals(reportParameter.getName())) {
							// update parameter
							reportParameter.parseValue(param.getValue());
						}
					}
				}
			}
			reportFactory.fillReport();
			String filename = reportFactory.getReportCard().getCode().replaceAll(" ", "");
			// add extension
			filename += "." + reportFactory.getReportExtension().toString().toLowerCase();
			// send to stream
			final DataSource dataSource = TempDataSource.create(filename, reportFactory.getContentType());
			final OutputStream outputStream = dataSource.getOutputStream();
			reportFactory.sendReportToStream(outputStream);
			return new DataHandler(dataSource);
		} catch (final SQLException e) {
			Log.SOAP.error("SQL error in report", e);
		} catch (final IOException e) {
			Log.SOAP.error("Error reading report", e);
		} catch (final ClassNotFoundException e) {
			Log.SOAP.error("Cannot find class in report", e);
		} catch (final Exception e) {
			Log.SOAP.error("Error getting report", e);
		}
		return null;
	}

	public DataHandler getReport(final String reportId, final String extension, final ReportParams[] params,
			final UserContext userCtx) {
		try {
			final BuiltInReport builtInReport = BuiltInReport.from(reportId);
			final ReportFactory reportFactory = builtInReport.newBuilder(userCtx) //
					.withExtension(extension) //
					.withProperties(propertiesFrom(params)) //
					.build();
			reportFactory.fillReport();
			final DataSource dataSource = TempDataSource.create(null, reportFactory.getContentType());
			final OutputStream outputStream = dataSource.getOutputStream();
			reportFactory.sendReportToStream(outputStream);
			return new DataHandler(dataSource);
		} catch (final Throwable e) {
			throw new Error(e);
		}
	}

	private Map<String, String> propertiesFrom(final ReportParams[] params) {
		final Map<String, String> properties = Maps.newHashMap();
		for (final ReportParams param : params) {
			properties.put(param.getKey(), param.getValue());
		}
		return properties;
	}
}
