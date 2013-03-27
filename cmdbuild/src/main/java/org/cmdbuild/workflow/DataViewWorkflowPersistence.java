package org.cmdbuild.workflow;

import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Iterables.isEmpty;
import static org.cmdbuild.dao.driver.postgres.Const.ID_ATTRIBUTE;
import static org.cmdbuild.dao.legacywrappers.ProcessInstanceWrapper.lookupForFlowStatus;
import static org.cmdbuild.dao.query.clause.AnyAttribute.anyAttribute;
import static org.cmdbuild.dao.query.clause.QueryAliasAttribute.attribute;
import static org.cmdbuild.dao.query.clause.alias.EntryTypeAlias.canonicalAlias;
import static org.cmdbuild.dao.query.clause.join.Over.over;
import static org.cmdbuild.dao.query.clause.where.EqualsOperatorAndValue.eq;
import static org.cmdbuild.dao.query.clause.where.InOperatorAndValue.in;
import static org.cmdbuild.dao.query.clause.where.SimpleWhereClause.condition;
import static org.cmdbuild.elements.interfaces.Process.ProcessAttributes.FlowStatus;
import static org.cmdbuild.elements.interfaces.Process.ProcessAttributes.ProcessInstanceId;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.cmdbuild.auth.user.OperationUser;
import org.cmdbuild.common.collect.Mapper;
import org.cmdbuild.common.utils.PaginatedElements;
import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entry.CMCard.CMCardDefinition;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.CMDomain;
import org.cmdbuild.dao.entrytype.CMEntryType;
import org.cmdbuild.dao.query.CMQueryResult;
import org.cmdbuild.dao.query.CMQueryRow;
import org.cmdbuild.dao.query.QuerySpecsBuilder;
import org.cmdbuild.dao.query.clause.OrderByClause;
import org.cmdbuild.dao.query.clause.OrderByClause.Direction;
import org.cmdbuild.dao.query.clause.QueryAliasAttribute;
import org.cmdbuild.dao.query.clause.where.WhereClause;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.logger.Log;
import org.cmdbuild.logic.TemporaryObjectsBeforeSpringDI;
import org.cmdbuild.logic.data.QueryOptions;
import org.cmdbuild.logic.data.access.JsonAttributeSubsetMapper;
import org.cmdbuild.logic.data.lookup.LookupStorableConverter;
import org.cmdbuild.logic.mapping.FilterMapper;
import org.cmdbuild.logic.mapping.SorterMapper;
import org.cmdbuild.logic.mapping.json.JsonFilterMapper;
import org.cmdbuild.logic.mapping.json.JsonSorterMapper;
import org.cmdbuild.workflow.service.WSProcessInstInfo;
import org.cmdbuild.workflow.service.WSProcessInstanceState;
import org.cmdbuild.workflow.user.UserProcessClass;
import org.cmdbuild.workflow.user.UserProcessInstance;
import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class DataViewWorkflowPersistence implements WorkflowPersistence {

	private static final Marker marker = MarkerFactory.getMarker(DataViewWorkflowPersistence.class.getName());
	private static final Logger logger = Log.PERSISTENCE;

	private static final String DEFAULT_SORTING_ATTRIBUTE_NAME = "Description";
	private static final List<UserProcessInstance> EMPTY_PROCESS_INSTANCE_LIST = Collections.emptyList();

	private final OperationUser operationUser;
	private final CMDataView dataView;
	private final ProcessDefinitionManager processDefinitionManager;

	public DataViewWorkflowPersistence(final OperationUser operationUser, final CMDataView dataView,
			final ProcessDefinitionManager processDefinitionManager) {
		this.operationUser = operationUser;
		this.dataView = dataView;
		this.processDefinitionManager = processDefinitionManager;
	}

	@Override
	public Iterable<UserProcessClass> getAllProcessClasses() {
		logger.info(marker, "getting all process classes");
		return from(dataView.findClasses()) //
				.filter(activityClasses()) //
				.transform(toUserProcessClass());
	}

	private Predicate<CMClass> activityClasses() {
		logger.debug(marker, "filtering activity classes");
		return new Predicate<CMClass>() {
			@Override
			public boolean apply(final CMClass input) {
				return dataView.getActivityClass().isAncestorOf(input);
			}
		};
	}

	private Function<CMClass, UserProcessClass> toUserProcessClass() {
		logger.debug(marker, "transforming from '{}' to '{}'", CMClass.class, UserProcessClass.class);
		return new Function<CMClass, UserProcessClass>() {
			@Override
			public UserProcessClass apply(final CMClass input) {
				return wrap(input);
			}
		};
	}

	@Override
	public UserProcessClass findProcessClass(final Long id) {
		logger.info(marker, "getting process class with id '{}'", id);
		return from(getAllProcessClasses()) //
				.filter(processClassWithId(id)) //
				.get(0);
	}

	private Predicate<UserProcessClass> processClassWithId(final Long id) {
		logger.debug(marker, "filtering process classes with id '{}'", id);
		final Predicate<UserProcessClass> predicate = new Predicate<UserProcessClass>() {
			@Override
			public boolean apply(final UserProcessClass input) {
				return input.getId().equals(id);
			}
		};
		return predicate;
	}

	@Override
	public UserProcessClass findProcessClass(final String name) {
		logger.info(marker, "getting process class with name '{}'", name);
		return from(getAllProcessClasses()) //
				.filter(processClassWithName(name)) //
				.get(0);
	}

	private Predicate<UserProcessClass> processClassWithName(final String name) {
		logger.debug(marker, "filtering process classes with name '{}'", name);
		return new Predicate<UserProcessClass>() {
			@Override
			public boolean apply(final UserProcessClass input) {
				return input.getName().equals(name);
			}
		};
	}

	@Override
	public UserProcessInstance createProcessInstance(final WSProcessInstInfo processInstInfo,
			final ProcessCreation processCreation) throws CMWorkflowException {
		logger.info(marker, "creating process instance of '{}' '{}'", //
				processInstInfo.getPackageId(), //
				processInstInfo.getProcessDefinitionId());
		final String processClassName = processDefinitionManager.getProcessClassName(processInstInfo
				.getProcessDefinitionId());
		final CMProcessClass processClass = wrap(dataView.findClass(processClassName));
		final CMCardDefinition cardDefinition = dataView.createCardFor(processClass);
		final CMCard updatedCard = WorkflowUpdateHelper.newInstance(cardDefinition) //
				.withProcessInstInfo(processInstInfo) //
				.build() //
				.initialize() //
				.fillForCreation(processCreation) //
				.save();
		return wrap(updatedCard);
	}

	@Override
	public UserProcessInstance createProcessInstance(final CMProcessClass processClass,
			final WSProcessInstInfo processInstInfo, final ProcessCreation processCreation) throws CMWorkflowException {
		logger.info(marker, "creating process instance for class '{}'", processClass);
		final CMCardDefinition cardDefinition = dataView.createCardFor(processClass);
		final CMCard updatedCard = WorkflowUpdateHelper.newInstance(cardDefinition) //
				.withProcessInstInfo(processInstInfo) //
				.build() //
				.initialize() //
				.fillForCreation(processCreation) //
				.save();
		return wrap(updatedCard);
	}

	@Override
	public UserProcessInstance updateProcessInstance(final CMProcessInstance processInstance,
			final ProcessUpdate processUpdate) throws CMWorkflowException {
		logger.info(marker, "updating process instance for class '{}' and id '{}'", //
				processInstance.getType().getName(), processInstance.getCardId());
		final CMCard card = findProcessCard(processInstance);
		final CMCardDefinition cardDefinition = dataView.update(card);
		final CMCard updatedCard = WorkflowUpdateHelper.newInstance(cardDefinition) //
				.withCard(card) //
				.withProcessInstance(processInstance) //
				.withProcessDefinitionManager(processDefinitionManager) //
				.build() //
				.fillForModification(processUpdate) //
				.save();
		return wrap(updatedCard);
	}

	@Override
	public UserProcessInstance findProcessInstance(final CMProcessInstance processInstance) {
		logger.info(marker, "getting process instance for class '{}' and card id '{}'", //
				processInstance.getType(), processInstance.getCardId());
		return wrap(findProcessCard(processInstance));
	}

	@Override
	public UserProcessInstance findProcessInstance(final WSProcessInstInfo processInstInfo) throws CMWorkflowException {
		final String processClassName = processDefinitionManager.getProcessClassName(processInstInfo
				.getProcessDefinitionId());
		final CMProcessClass processClass = wrap(dataView.findClass(processClassName));
		return wrap(findProcessCard(processClass, processInstInfo.getProcessInstanceId()));
	}

	@Override
	public UserProcessInstance findProcessInstance(final CMProcessClass processClass, final Long cardId) {
		logger.info(marker, "getting process instance for class '{}' and card id '{}'", processClass, cardId);
		return wrap(findProcessCard(processClass, cardId));
	}

	@Override
	public Iterable<? extends UserProcessInstance> queryOpenAndSuspended(final UserProcessClass processClass) {
		logger.info(marker, "getting all opened and suspended process instances for class '{}'", processClass);
		final int[] ids = new int[] { lookupForFlowStatus(WSProcessInstanceState.OPEN).getId(),
				lookupForFlowStatus(WSProcessInstanceState.SUSPENDED).getId() };
		logger.debug(marker, "lookup ids are '{}'", ids);
		return from(dataView.select(anyAttribute(processClass)) //
				.from(processClass) //
				.where(condition(attribute(processClass, FlowStatus.dbColumnName()), in(ids))) //
				.run() //
		).transform(toProcessInstanceOf(processClass));
	}

	private Function<CMQueryRow, UserProcessInstance> toProcessInstanceOf(final CMProcessClass processClass) {
		logger.debug(marker, "transforming from '{}' to '{}'", CMQueryRow.class, UserProcessInstance.class);
		return new Function<CMQueryRow, UserProcessInstance>() {
			@Override
			public UserProcessInstance apply(final CMQueryRow input) {
				final CMCard card = input.getCard(processClass);
				return wrap(card);
			}
		};
	}

	@Override
	public PaginatedElements<UserProcessInstance> query(final String className, final QueryOptions queryOptions) {

		final CMClass fetchedClass = dataView.findClass(className);
		if (fetchedClass == null) {
			return new PaginatedElements<UserProcessInstance>(EMPTY_PROCESS_INSTANCE_LIST,
					EMPTY_PROCESS_INSTANCE_LIST.size());
		}

		final QuerySpecsBuilder querySpecsBuilder = fetchCardQueryBuilder(queryOptions, fetchedClass);

		final CMQueryResult result = querySpecsBuilder.run();
		final List<CMCard> filteredCards = Lists.newArrayList();
		for (final CMQueryRow row : result) {
			final CMCard card = row.getCard(fetchedClass);
			filteredCards.add(card);
		}

		final Iterable<UserProcessInstance> upi = Iterables.transform(filteredCards,
				new Function<CMCard, UserProcessInstance>() {
					@Override
					public UserProcessInstance apply(final CMCard arg0) {
						return wrap(arg0);
					}
				});
		return new PaginatedElements<UserProcessInstance>(upi, result.totalSize());
	}

	public QuerySpecsBuilder fetchCardQueryBuilder( //
			final QueryOptions queryOptions, //
			final CMClass fetchedClass //
	) {

		final FilterMapper filterMapper = new JsonFilterMapper(fetchedClass, queryOptions.getFilter(), dataView);
		final WhereClause whereClause = filterMapper.whereClause();
		final Iterable<FilterMapper.JoinElement> joinElements = filterMapper.joinElements();
		final Mapper<JSONArray, List<QueryAliasAttribute>> attributeSubsetMapper = new JsonAttributeSubsetMapper(
				fetchedClass);
		final List<QueryAliasAttribute> attributeSubsetForSelect = attributeSubsetMapper.map(queryOptions
				.getAttributes());
		final QuerySpecsBuilder querySpecsBuilder = newQuerySpecsBuilder(attributeSubsetForSelect, fetchedClass);
		querySpecsBuilder.from(fetchedClass) //
				.where(whereClause) //
				.limit(queryOptions.getLimit()) //
				.offset(queryOptions.getOffset());

		addJoinOptions(querySpecsBuilder, queryOptions, joinElements);
		addSortingOptions(querySpecsBuilder, queryOptions, fetchedClass);
		return querySpecsBuilder;
	}

	private void addJoinOptions(final QuerySpecsBuilder querySpecsBuilder, final QueryOptions options,
			final Iterable<FilterMapper.JoinElement> joinElements) {
		if (!isEmpty(joinElements)) {
			querySpecsBuilder.distinct();
		}
		for (final FilterMapper.JoinElement joinElement : joinElements) {
			final CMDomain domain = dataView.findDomain(joinElement.domain);
			final CMClass clazz = dataView.findClass(joinElement.destination);
			if (joinElement.left) {
				querySpecsBuilder.leftJoin(clazz, canonicalAlias(clazz), over(domain));
			} else {
				querySpecsBuilder.join(clazz, canonicalAlias(clazz), over(domain));
			}
		}
	}

	private void addSortingOptions( //
			final QuerySpecsBuilder querySpecsBuilder, //
			final QueryOptions options, //
			final CMClass clazz //
	) {

		final SorterMapper sorterMapper = new JsonSorterMapper(clazz, options.getSorters());
		final List<OrderByClause> clauses = sorterMapper.deserialize();

		// If no sorting rules are defined
		// sort by description (if the class has a description)
		if (clauses.isEmpty()) {
			if (clazz.getAttribute(DEFAULT_SORTING_ATTRIBUTE_NAME) != null) {
				querySpecsBuilder.orderBy(attribute(clazz, DEFAULT_SORTING_ATTRIBUTE_NAME), Direction.ASC);
			}
		} else {
			addSortingOptions(querySpecsBuilder, clauses);
		}
	}

	private void addSortingOptions(final QuerySpecsBuilder querySpecsBuilder, final List<OrderByClause> clauses) {
		for (final OrderByClause clause : clauses) {
			querySpecsBuilder.orderBy(clause.getAttribute(), clause.getDirection());
		}
	}

	private QuerySpecsBuilder newQuerySpecsBuilder(final List<QueryAliasAttribute> attributeSubsetForSelect,
			final CMEntryType entryType) {
		if (attributeSubsetForSelect.isEmpty()) {
			return dataView.select(anyAttribute(entryType));
		}
		final Object[] attributesArray = new QueryAliasAttribute[attributeSubsetForSelect.size()];
		attributeSubsetForSelect.toArray(attributesArray);
		return dataView.select(attributesArray);
	}

	private UserProcessClass wrap(final CMClass clazz) {
		logger.debug(marker, "wrapping '{}' into '{}'", CMClass.class, UserProcessClass.class);
		return new ProcessClassImpl(operationUser, clazz, processDefinitionManager);
	}

	private UserProcessInstance wrap(final CMCard card) {
		logger.debug(marker, "wrapping '{}' into '{}'", CMCard.class, UserProcessInstance.class);

		final CMDataView dataView = TemporaryObjectsBeforeSpringDI.getSystemView();
		final CMClass lookupClass = dataView.findClass(LookupStorableConverter.LOOKUP_TABLE_NAME);
		final Iterable<CMQueryRow> rows = dataView.select(anyAttribute(lookupClass)) //
				.from(lookupClass) //
				.where(condition(attribute(lookupClass, "Type"), eq("FlowStatus"))) //
				.run();
		final Map<Long, String> flowStatuses = Maps.newHashMap();
		for (final CMQueryRow row : rows) {
			final CMCard lookupCard = row.getCard(lookupClass);
			flowStatuses.put(lookupCard.getId(), flowStatuses.get(lookupClass.getCodeAttributeName()));
		}

		return ProcessInstanceImpl.newInstance() //
				.withOperationUser(operationUser) //
				.withProcessDefinitionManager(processDefinitionManager) //
				.withCard(card) //
				.withFlowStatusesCodesById(flowStatuses) //
				.build();
	}

	private CMCard findProcessCard(final CMProcessInstance processInstance) {
		return findProcessCard(processInstance.getType(), processInstance.getCardId());
	}

	private CMCard findProcessCard(final CMProcessClass processClass, final Long cardId) {
		logger.debug(marker, "getting process card for class '{}' and card id '{}'", processClass, cardId);
		return dataView.select(anyAttribute(processClass)) //
				.from(processClass) //
				.where(condition(attribute(processClass, ID_ATTRIBUTE), eq(cardId))) //
				.run() //
				.getOnlyRow() //
				.getCard(processClass);
	}

	private CMCard findProcessCard(final CMProcessClass processClass, final String processInstanceId) {
		logger.debug(marker, "getting process card for class '{}' and process instance id '{}'", processClass,
				processInstanceId);
		return dataView.select(anyAttribute(processClass)) //
				.from(processClass) //
				.where(condition(attribute(processClass, ProcessInstanceId.dbColumnName()), eq(processInstanceId))) //
				.run() //
				.getOnlyRow() //
				.getCard(processClass);
	}

}
