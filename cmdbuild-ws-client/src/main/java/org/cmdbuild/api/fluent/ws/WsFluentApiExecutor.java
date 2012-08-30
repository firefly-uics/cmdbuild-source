package org.cmdbuild.api.fluent.ws;

import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableMap;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.cmdbuild.api.fluent.ws.ClassAttribute.classAttribute;
import static org.cmdbuild.api.fluent.ws.FunctionInput.functionInput;
import static org.cmdbuild.api.fluent.ws.FunctionOutput.functionOutput;
import static org.cmdbuild.api.fluent.ws.ReportHelper.DEFAULT_TYPE;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.activation.DataHandler;

import org.apache.commons.lang.StringUtils;
import org.cmdbuild.api.fluent.Card;
import org.cmdbuild.api.fluent.CardDescriptor;
import org.cmdbuild.api.fluent.DownloadedReport;
import org.cmdbuild.api.fluent.ExistingCard;
import org.cmdbuild.api.fluent.FluentApi;
import org.cmdbuild.api.fluent.FluentApiExecutor;
import org.cmdbuild.api.fluent.Function;
import org.cmdbuild.api.fluent.ProcessInstanceDescriptor;
import org.cmdbuild.api.fluent.Relation;
import org.cmdbuild.api.fluent.RelationsQuery;
import org.cmdbuild.api.fluent.Report;
import org.cmdbuild.services.soap.Attribute;
import org.cmdbuild.services.soap.CardList;
import org.cmdbuild.services.soap.CqlQuery;
import org.cmdbuild.services.soap.Filter;
import org.cmdbuild.services.soap.FilterOperator;
import org.cmdbuild.services.soap.Order;
import org.cmdbuild.services.soap.Private;
import org.cmdbuild.services.soap.Query;
import org.cmdbuild.services.soap.ReportParams;
import org.cmdbuild.services.soap.WorkflowWidgetSubmission;

public class WsFluentApiExecutor implements FluentApiExecutor {

	public enum WsType {
		UNKNOWN;

		public static WsType from(final String type) {
			return UNKNOWN;
		}
	}

	public interface EntryTypeConverter {

		String toClientType(EntryTypeAttribute entryTypeAttribute, String wsValue);

		String toWsType(EntryTypeAttribute entryTypeAttribute, Object clientValue);

	}

	public interface RawTypeConverter {

		String toWsType(WsType wsType, Object value);

	}

	private static final FluentApiExecutor NULL_NEVER_USED_EXECUTOR = null;

	private static final String OPERATOR_EQUALS = "EQUALS";
	private static final String OPERATOR_AND = "AND";

	private static final List<Attribute> ALL_ATTRIBUTES = null;
	private static final Query NO_QUERY = null;
	private static final List<Order> NO_ORDERING = null;
	private static final int NO_LIMIT = 0;
	private static final int OFFSET_BEGINNING = 0;
	private static final String NO_FULLTEXT = null;
	private static final CqlQuery NO_CQL = null;

	private static final EntryTypeConverter IDENTITY_ENTRY_TYPE_CONVERTER = new EntryTypeConverter() {

		public String toClientType(final EntryTypeAttribute entityAttribute, final String wsValue) {
			return wsValue;
		}

		public String toWsType(final EntryTypeAttribute entityAttribute, final Object value) {
			return IDENTITY_RAW_TYPE_CONVERTER.toWsType(WsType.UNKNOWN, value);
		}

	};

	private static final RawTypeConverter IDENTITY_RAW_TYPE_CONVERTER = new RawTypeConverter() {

		public String toWsType(final WsType wsType, final Object value) {
			return (value != null) ? value.toString() : StringUtils.EMPTY;
		}

	};

	private final Private proxy;

	private interface EntityAttributeCreator {
		EntryTypeAttribute attributeFor(String entryTypeName, String attributeName);
	}

	private static EntityAttributeCreator cardAttributeCreator = new EntityAttributeCreator() {

		public EntryTypeAttribute attributeFor(String entryTypeName, String attributeName) {
			return classAttribute(entryTypeName, attributeName);
		}

	};

	private static EntityAttributeCreator functionInputCreator = new EntityAttributeCreator() {

		public EntryTypeAttribute attributeFor(String entryTypeName, String attributeName) {
			return functionInput(entryTypeName, attributeName);
		}

	};

	private static EntityAttributeCreator functionOutputCreator = new EntityAttributeCreator() {

		public EntryTypeAttribute attributeFor(String entryTypeName, String attributeName) {
			return functionOutput(entryTypeName, attributeName);
		}

	};

	private EntryTypeConverter entryTypeConverter;
	private RawTypeConverter rawTypeConverter;

	public WsFluentApiExecutor(final Private proxy) {
		this.proxy = proxy;
		this.entryTypeConverter = IDENTITY_ENTRY_TYPE_CONVERTER;
		this.rawTypeConverter = IDENTITY_RAW_TYPE_CONVERTER;
	}

	public void setEntryTypeConverter(final EntryTypeConverter entryTypeConverter) {
		this.entryTypeConverter = (entryTypeConverter == null) ? IDENTITY_ENTRY_TYPE_CONVERTER : entryTypeConverter;
	}

	protected void setRawTypeConverter(final RawTypeConverter rawTypeConverter) {
		this.rawTypeConverter = (rawTypeConverter == null) ? IDENTITY_RAW_TYPE_CONVERTER : rawTypeConverter;
	}

	public CardDescriptor create(final Card card) {
		final org.cmdbuild.services.soap.Card soapCard = soapCardFor(card);
		final int id = proxy.createCard(soapCard);
		return new CardDescriptor(card.getClassName(), id);
	}

	public void update(final Card card) {
		final org.cmdbuild.services.soap.Card soapCard = soapCardFor(card);
		soapCard.setId(card.getId());
		proxy.updateCard(soapCard);
	}

	public void delete(final Card card) {
		proxy.deleteCard(card.getClassName(), card.getId());
	}

	public Card fetch(final Card card) {
		final org.cmdbuild.services.soap.Card soapCard = proxy.getCard( //
				card.getClassName(), //
				card.getId(), //
				hasAttributes(card) ? attributesNameFor(card) : ALL_ATTRIBUTES);
		return cardFor(soapCard);
	}

	private List<Attribute> attributesNameFor(final Card card) {
		final List<Attribute> attributeNames = new ArrayList<Attribute>();
		for (final String attributeName : card.getAttributeNames()) {
			attributeNames.add(new Attribute() {
				{
					setName(attributeName);
				}
			});
		}
		return attributeNames;
	}

	public List<Card> fetchCards(final Card card) {
		final CardList cardList = proxy.getCardList( //
				card.getClassName(), //
				ALL_ATTRIBUTES, //
				hasAttributes(card) ? queriedAttributesFor(card) : NO_QUERY, //
				NO_ORDERING, //
				NO_LIMIT, //
				OFFSET_BEGINNING, //
				NO_FULLTEXT, //
				NO_CQL);
		return cardsFor(cardList);
	}

	private Query queriedAttributesFor(final Card card) {
		final FilterOperator filterOperator = new FilterOperator();
		filterOperator.setOperator(OPERATOR_AND);
		filterOperator.getSubquery().addAll(queriesFor(card));
		return queryFor(filterOperator);
	}

	private List<Query> queriesFor(final Card card) {
		final List<Query> queries = new ArrayList<Query>();
		for (final String name : card.getAttributeNames()) {
			final String wsValue = entryTypeConverter.toWsType(classAttribute(card.getClassName(), name),
					card.get(name));
			final Query attributeQuery = new Query() {
				{
					setFilter(wsEqualsFilter(name, wsValue));
				}
			};
			queries.add(attributeQuery);
		}
		return queries;
	}

	private Query queryFor(final FilterOperator filterOperator) {
		final Query query = new Query();
		query.setFilterOperator(filterOperator);
		return query;
	}

	private List<Card> cardsFor(final CardList cardList) {
		final List<Card> cards = new ArrayList<Card>();
		for (final org.cmdbuild.services.soap.Card soapCard : cardList.getCards()) {
			cards.add(cardFor(soapCard));
		}
		return unmodifiableList(cards);
	}

	private Card cardFor(final org.cmdbuild.services.soap.Card soapCard) {
		final ExistingCard card = existingCardFrom(soapCard);
		for (final Attribute attribute : soapCard.getAttributeList()) {
			final String attributeName = attribute.getName();
			final String wsValue = wsValueFor(attribute);
			card.with( //
					attributeName, //
					entryTypeConverter.toClientType(classAttribute(soapCard.getClassName(), attributeName), wsValue));
		}
		return card;
	}

	private ExistingCard existingCardFrom(final org.cmdbuild.services.soap.Card soapCard) {
		return new FluentApi(NULL_NEVER_USED_EXECUTOR) //
				.existingCard(soapCard.getClassName(), soapCard.getId());
	}

	public void create(final Relation relation) {
		proxy.createRelation(soapRelationFor(relation));
	}

	public void delete(final Relation relation) {
		proxy.deleteRelation(soapRelationFor(relation));
	}

	private org.cmdbuild.services.soap.Relation soapRelationFor(final Relation relation) {
		final org.cmdbuild.services.soap.Relation soapRelation = new org.cmdbuild.services.soap.Relation();
		soapRelation.setDomainName(relation.getDomainName());
		soapRelation.setClass1Name(relation.getClassName1());
		soapRelation.setCard1Id(relation.getCardId1());
		soapRelation.setClass2Name(relation.getClassName2());
		soapRelation.setCard2Id(relation.getCardId2());
		return soapRelation;
	}

	public List<Relation> fetch(final RelationsQuery query) {
		final List<org.cmdbuild.services.soap.Relation> soapRelations = proxy.getRelationList( //
				query.getDomainName(), //
				query.getClassName(), //
				query.getCardId());
		final List<Relation> relations = new ArrayList<Relation>();
		for (final org.cmdbuild.services.soap.Relation soapRelation : soapRelations) {
			relations.add(relationFor(soapRelation));
		}
		return unmodifiableList(relations);
	}

	private Relation relationFor(final org.cmdbuild.services.soap.Relation soapRelation) {
		final Relation relation = new Relation(soapRelation.getDomainName());
		relation.setCard1(soapRelation.getClass1Name(), soapRelation.getCard1Id());
		relation.setCard2(soapRelation.getClass2Name(), soapRelation.getCard2Id());
		return relation;
	}

	public Map<String, String> execute(final Function function) {
		final List<Attribute> outputs = proxy.callFunction( //
				function.getFunctionName(), //
				wsInputAttributesFor(function));
		return unmodifiableMap(clientAttributesFor(function, outputs));
	}

	public DownloadedReport download(final Report report) {
		final ReportHelper helper = new ReportHelper(proxy);
		final org.cmdbuild.services.soap.Report soapReport = helper.getReport(DEFAULT_TYPE, report.getTitle());
		final List<ReportParams> reportParams = compileParams(report.getParameters());
		final DataHandler dataHandler = helper.getDataHandler(soapReport, report.getFormat(), reportParams);
		final File file = helper.temporaryFile();
		helper.saveToFile(dataHandler, file);
		return new DownloadedReport(file);
	}

	private List<ReportParams> compileParams(final Map<String, Object> params) {
		final List<ReportParams> reportParameters = new ArrayList<ReportParams>();
		for (final Entry<String, Object> entry : params.entrySet()) {
			final ReportParams parameter = new ReportParams();
			parameter.setKey(entry.getKey());
			parameter.setValue(rawTypeConverter.toWsType(WsType.UNKNOWN, entry.getValue()));
			reportParameters.add(parameter);
		}
		if (reportParameters.isEmpty()) {
			reportParameters.add(new ReportParams());
		}
		return reportParameters;
	}

	public ProcessInstanceDescriptor createProcessInstance(final Card processCard, final AdvanceProcess advance) {
		final org.cmdbuild.services.soap.Card soapCard = soapCardFor(processCard);
		final boolean advanceProcess = (advance == AdvanceProcess.YES);
		final List<WorkflowWidgetSubmission> emptyWidgetsSubmission = emptyList();
		final org.cmdbuild.services.soap.Workflow workflowInfo = proxy.updateWorkflow(soapCard, advanceProcess,
				emptyWidgetsSubmission);
		return new ProcessInstanceDescriptor(processCard.getClassName(), workflowInfo.getProcessid(),
				workflowInfo.getProcessinstanceid());
	}

	/*
	 * Utils
	 */

	private List<Attribute> wsInputAttributesFor(final Function function) {
		return wsAttributesFor(functionInputCreator, function.getFunctionName(), function.getInputs());
	}

	private Map<String, String> clientAttributesFor(final Function function, final List<Attribute> wsAttributes) {
		return clientAttributesFor(functionOutputCreator, function.getFunctionName(), wsAttributes);
	}

	private org.cmdbuild.services.soap.Card soapCardFor(final Card card) {
		final org.cmdbuild.services.soap.Card soapCard = new org.cmdbuild.services.soap.Card();
		soapCard.setClassName(card.getClassName());
		if (card.getId() != null) {
			soapCard.setId(card.getId());
		}
		soapCard.getAttributeList().addAll(wsAttributesFor(card));
		return soapCard;
	}

	private List<Attribute> wsAttributesFor(final Card card) {
		return wsAttributesFor(cardAttributeCreator, card.getClassName(), card.getAttributes());
	}

	private List<Attribute> wsAttributesFor(final EntityAttributeCreator attributeCreator, final String className,
			final Map<String, Object> attributes) {
		final List<Attribute> wsAttributes = new ArrayList<Attribute>(attributes.size());
		for (final Map.Entry<String, Object> e : attributes.entrySet()) {
			final String wsValue = entryTypeConverter.toWsType(attributeCreator.attributeFor(className, e.getKey()),
					e.getValue());
			wsAttributes.add(wsAttribute(e.getKey(), wsValue));
		}
		return wsAttributes;
	}

	private Map<String, String> clientAttributesFor(final EntityAttributeCreator attributeCreator,
			final String entryTypeName, final List<Attribute> wsAttributes) {
		final Map<String, String> clientAttributes = new HashMap<String, String>();
		for (final Attribute attribute : wsAttributes) {
			final String attributeName = attribute.getName();
			final String wsValue = wsValueFor(attribute);
			clientAttributes.put( //
					attributeName, //
					entryTypeConverter.toClientType(attributeCreator.attributeFor(entryTypeName, attributeName),
							wsValue) //
					);
		}
		return clientAttributes;
	}

	private boolean hasAttributes(final Card card) {
		return !card.getAttributes().isEmpty();
	}

	/*
	 * WS object factories
	 */

	private String wsValueFor(final Attribute wsAttribute) {
		return isReferenceOrLookup(wsAttribute) ? wsAttribute.getCode() : wsAttribute.getValue();
	}

	private boolean isReferenceOrLookup(final Attribute wsAttribute) {
		return isNotBlank(wsAttribute.getCode());
	}

	public Filter wsEqualsFilter(final String attributeName, final String attibuteValue) {
		return new Filter() {
			{
				setName(attributeName);
				setOperator(OPERATOR_EQUALS);
				getValue().add(attibuteValue);
			}
		};
	}

	public static Attribute wsAttribute(final String attributeName, final String attributeValue) {
		return new Attribute() {
			{
				setName(attributeName);
				setValue(attributeValue);
			}
		};
	}

}
