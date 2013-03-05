package org.cmdbuild.services.soap;

import static java.lang.String.format;
import static org.apache.commons.lang.StringUtils.EMPTY;
import static org.cmdbuild.dao.query.clause.FunctionCall.call;
import static org.cmdbuild.logic.DashboardLogic.fakeAnyAttribute;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.activation.DataHandler;
import javax.annotation.Resource;
import javax.jws.WebService;
import javax.xml.ws.WebServiceContext;

import org.cmdbuild.auth.DefaultAuthenticationService;
import org.cmdbuild.auth.user.OperationUser;
import org.cmdbuild.common.digest.Digester;
import org.cmdbuild.common.digest.DigesterFactory;
import org.cmdbuild.dao.entry.CMValueSet;
import org.cmdbuild.dao.entrytype.attributetype.CMAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.EntryTypeAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.LookupAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.ReferenceAttributeType;
import org.cmdbuild.dao.function.CMFunction;
import org.cmdbuild.dao.function.CMFunction.CMFunctionParameter;
import org.cmdbuild.dao.query.CMQueryResult;
import org.cmdbuild.dao.query.CMQueryRow;
import org.cmdbuild.dao.query.clause.alias.Alias;
import org.cmdbuild.dao.query.clause.alias.NameAlias;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.dms.MetadataGroup;
import org.cmdbuild.dms.StoredDocument;
import org.cmdbuild.logger.Log;
import org.cmdbuild.logic.DmsLogic;
import org.cmdbuild.logic.TemporaryObjectsBeforeSpringDI;
import org.cmdbuild.logic.data.lookup.LookupLogic;
import org.cmdbuild.services.auth.OperationUserWrapper;
import org.cmdbuild.services.auth.UserContext;
import org.cmdbuild.services.auth.UserContextToUserInfo;
import org.cmdbuild.services.auth.UserInfo;
import org.cmdbuild.services.soap.operation.EAdministration;
import org.cmdbuild.services.soap.operation.ECard;
import org.cmdbuild.services.soap.operation.ELegacySync;
import org.cmdbuild.services.soap.operation.ELookup;
import org.cmdbuild.services.soap.operation.ERelation;
import org.cmdbuild.services.soap.operation.EReport;
import org.cmdbuild.services.soap.operation.WorkflowLogicHelper;
import org.cmdbuild.services.soap.serializer.AttributeSchemaSerializer;
import org.cmdbuild.services.soap.structure.ActivitySchema;
import org.cmdbuild.services.soap.structure.AttributeSchema;
import org.cmdbuild.services.soap.structure.ClassSchema;
import org.cmdbuild.services.soap.structure.FunctionSchema;
import org.cmdbuild.services.soap.structure.MenuSchema;
import org.cmdbuild.services.soap.structure.WorkflowWidgetSubmission;
import org.cmdbuild.services.soap.types.Attachment;
import org.cmdbuild.services.soap.types.Attribute;
import org.cmdbuild.services.soap.types.CQLQuery;
import org.cmdbuild.services.soap.types.Card;
import org.cmdbuild.services.soap.types.CardExt;
import org.cmdbuild.services.soap.types.CardList;
import org.cmdbuild.services.soap.types.CardListExt;
import org.cmdbuild.services.soap.types.Lookup;
import org.cmdbuild.services.soap.types.Order;
import org.cmdbuild.services.soap.types.Query;
import org.cmdbuild.services.soap.types.Reference;
import org.cmdbuild.services.soap.types.Relation;
import org.cmdbuild.services.soap.types.Report;
import org.cmdbuild.services.soap.types.ReportParams;
import org.cmdbuild.services.soap.types.WSEvent;
import org.cmdbuild.services.soap.types.WSProcessStartEvent;
import org.cmdbuild.services.soap.types.WSProcessUpdateEvent;
import org.cmdbuild.services.soap.types.Workflow;
import org.cmdbuild.servlets.json.serializers.AbstractAttributeValueVisitor;
import org.cmdbuild.workflow.event.WorkflowEvent;
import org.cmdbuild.workflow.event.WorkflowEventManager;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

@WebService(endpointInterface = "org.cmdbuild.services.soap.Private", targetNamespace = "http://soap.services.cmdbuild.org")
public class PrivateImpl implements Private, ApplicationContextAware {

	private static final List<MetadataGroup> METADATA_NOT_SUPPORTED = Collections.emptyList();

	private ApplicationContext applicationContext;

	@Resource
	WebServiceContext wsc;

	private UserContext getUserCtx() {
		// FIXME
		final DefaultAuthenticationService as = new DefaultAuthenticationService();
		return new OperationUserWrapper(as.getOperationUser());
	}

	private OperationUser getOperationUser() {
		final DefaultAuthenticationService as = new DefaultAuthenticationService();
		return as.getOperationUser();
	}

	private LookupLogic lookupLogic() {
		return TemporaryObjectsBeforeSpringDI.getLookupLogic();
	}

	@Override
	public void setApplicationContext(final ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

	@Override
	public CardList getCardList(final String className, final Attribute[] attributeList, final Query queryType,
			final Order[] orderType, final Integer limit, final Integer offset, final String fullTextQuery,
			final CQLQuery cqlQuery) {
		return getCardList(className, attributeList, queryType, orderType, limit, offset, fullTextQuery, cqlQuery,
				false);
	}

	@Override
	public Card getCard(final String className, final Integer cardId, final Attribute[] attributeList) {
		return getCard(className, cardId, attributeList, false);
	}

	@Override
	public CardList getCardHistory(final String className, final int cardId, final Integer limit, final Integer offset) {
		final ECard ecard = new ECard(getUserCtx());
		return ecard.getCardHistory(className, cardId, limit, offset);
	}

	@Override
	public int createCard(final Card cardType) {
		final ECard ecard = new ECard(getUserCtx());
		return ecard.createCard(cardType);
	}

	@Override
	public boolean updateCard(final Card card) {
		final ECard ecard = new ECard(getUserCtx());
		return ecard.updateCard(card);
	}

	@Override
	public boolean deleteCard(final String className, final int cardId) {
		final ECard ecard = new ECard(getUserCtx());
		return ecard.deleteCard(className, cardId);
	}

	@Override
	public int createLookup(final Lookup lookup) {
		final ELookup elookup = new ELookup(getUserCtx());
		return elookup.createLookup(lookup);
	}

	@Override
	public boolean deleteLookup(final int lookupId) {
		lookupLogic().disableLookup(Long.valueOf(lookupId));
		return true;
	}

	@Override
	public boolean updateLookup(final Lookup lookup) {
		final ELookup elookup = new ELookup(getUserCtx());
		return elookup.updateLookup(lookup);
	}

	@Override
	public Lookup getLookupById(final int id) {
		final ELookup elookup = new ELookup(getUserCtx());
		return elookup.getLookupById(id);
	}

	@Override
	public Lookup[] getLookupList(final String type, final String value, final boolean parentList) {
		final ELookup elookup = new ELookup(getUserCtx());
		return elookup.getLookupList(type, value, parentList);
	}

	@Override
	public Lookup[] getLookupListByCode(final String type, final String code, final boolean parentList) {
		final ELookup elookup = new ELookup(getUserCtx());
		return elookup.getLookupListByCode(type, code, parentList);
	}

	@Override
	public boolean createRelation(final Relation relation) {
		final ERelation erelation = new ERelation(getUserCtx());
		return erelation.createRelation(relation);
	}

	@Override
	public boolean deleteRelation(final Relation relation) {
		final ERelation erelation = new ERelation(getUserCtx());
		return erelation.deleteRelation(relation);
	}

	@Override
	public List<Relation> getRelationList(final String domain, final String className, final int cardId) {
		final ERelation erelation = new ERelation(getUserCtx());
		return erelation.getRelationList(domain, className, cardId);
	}

	@Override
	public Relation[] getRelationHistory(final Relation relation) {
		final ERelation erelation = new ERelation(getUserCtx());
		return erelation.getRelationHistory(relation);
	}

	@Override
	public Attachment[] getAttachmentList(final String className, final int cardId) {
		final DmsLogic dmsLogic = applicationContext.getBean(DmsLogic.class);
		final List<StoredDocument> storedDocuments = dmsLogic.search(className, cardId);
		final List<Attachment> attachments = new ArrayList<Attachment>();
		for (final StoredDocument storedDocument : storedDocuments) {
			final Attachment attachment = new Attachment(storedDocument);
			attachments.add(attachment);
		}
		return attachments.toArray(new Attachment[attachments.size()]);
	}

	@Override
	public boolean uploadAttachment(final String className, final int objectid, final DataHandler file,
			final String filename, final String category, final String description) {
		final DmsLogic dmsLogic = applicationContext.getBean(DmsLogic.class);
		try {
			dmsLogic.upload(getUserCtx().getUsername(), className, objectid, file.getInputStream(), filename, category,
					description, METADATA_NOT_SUPPORTED);
		} catch (final Exception e) {
			final String message = String.format("error uploading file '%s' in '%s'", filename, className);
			Log.SOAP.error(message, e);
		}
		return false;
	}

	@Override
	public DataHandler downloadAttachment(final String className, final int objectid, final String filename) {
		final DmsLogic dmsLogic = applicationContext.getBean(DmsLogic.class);
		return dmsLogic.download(className, objectid, filename);
	}

	@Override
	public boolean deleteAttachment(final String className, final int cardId, final String filename) {
		final DmsLogic dmsLogic = applicationContext.getBean(DmsLogic.class);
		dmsLogic.delete(className, cardId, filename);
		return true;
	}

	@Override
	public boolean updateAttachmentDescription(final String className, final int cardId, final String filename,
			final String description) {
		try {
			final DmsLogic dmsLogic = applicationContext.getBean(DmsLogic.class);
			dmsLogic.updateDescriptionAndMetadata(className, cardId, filename, description, METADATA_NOT_SUPPORTED);
			return true;
		} catch (final Exception e) {
			return false;
		}
	}

	@Override
	public Workflow updateWorkflow(final Card card, final boolean completeTask, final WorkflowWidgetSubmission[] widgets) {
		final WorkflowLogicHelper helper = new WorkflowLogicHelper(getUserCtx());
		return helper.updateProcess(card, widgets, completeTask);
	}

	@Override
	public String getProcessHelp(final String classname, final Integer cardid) {
		final WorkflowLogicHelper helper = new WorkflowLogicHelper(getUserCtx());
		return helper.getInstructions(classname, cardid);
	}

	@Override
	public AttributeSchema[] getAttributeList(final String className) {
		Log.SOAP.info(format("getting attributes schema for class '%s'", className));
		final ECard op = new ECard(getUserCtx());
		final AttributeSchema[] attributes = op.getAttributeList(className);
		return attributes;
	}

	@Override
	public ActivitySchema getActivityObjects(final String className, final Integer cardid) {
		final WorkflowLogicHelper helper = new WorkflowLogicHelper(getUserCtx());
		return helper.getActivitySchema(className, cardid);
	}

	@Override
	public MenuSchema getActivityMenuSchema() {
		final EAdministration op = new EAdministration(getUserCtx());
		return op.getProcessMenuSchema();
	}

	@Override
	public Reference[] getReference(final String className, final Query query, final Order[] orderType,
			final Integer limit, final Integer offset, final String fullTextQuery, final CQLQuery cqlQuery) {
		final ECard op = new ECard(getUserCtx());
		return op.getReference(className, query, orderType, limit, offset, fullTextQuery, cqlQuery);
	}

	@Override
	public MenuSchema getCardMenuSchema() {
		final EAdministration op = new EAdministration(getUserCtx());
		return op.getClassMenuSchema();
	}

	@Override
	public MenuSchema getMenuSchema() {
		final EAdministration op = new EAdministration(getUserCtx());
		return op.getMenuSchema();
	}

	@Override
	public Report[] getReportList(final String type, final int limit, final int offset) {
		final EReport op = new EReport(getUserCtx());
		return op.getReportList(type, limit, offset);
	}

	@Override
	public AttributeSchema[] getReportParameters(final int id, final String extension) {
		final EReport op = new EReport(getUserCtx());
		return op.getReportParameters(id, extension);
	}

	@Override
	public DataHandler getReport(final int id, final String extension, final ReportParams[] params) {
		final EReport op = new EReport(getUserCtx());
		return op.getReport(id, extension, params);
	}

	@Override
	public String sync(final String xml) {
		Log.SOAP.info("Calling webservice ExternalSync.sync");
		Log.SOAP.debug("xml message:" + xml);
		final ELegacySync op = new ELegacySync(getUserCtx());
		return op.sync(xml);
	}

	@Override
	public UserInfo getUserInfo() {
		return UserContextToUserInfo.newInstance(getUserCtx()).build();
	}

	/*
	 * r2.1
	 */

	@Override
	public CardList getCardListWithLongDateFormat(final String className, final Attribute[] attributeList,
			final Query queryType, final Order[] orderType, final Integer limit, final Integer offset,
			final String fullTextQuery, final CQLQuery cqlQuery) {
		return getCardList(className, attributeList, queryType, orderType, limit, offset, fullTextQuery, cqlQuery, true);
	}

	private CardList getCardList(final String className, final Attribute[] attributeList, final Query queryType,
			final Order[] orderType, final Integer limit, final Integer offset, final String fullTextQuery,
			final CQLQuery cqlQuery, final boolean enableLongDateFormat) {
		final ECard ecard = new ECard(getUserCtx());
		return ecard.getCardList(className, attributeList, queryType, orderType, limit, offset, fullTextQuery,
				cqlQuery, enableLongDateFormat);
	}

	/*
	 * r2.2
	 */

	@Override
	public ClassSchema getClassSchema(final String className) {
		Log.SOAP.info(format("getting schema for class '%s'", className));
		final ECard op = new ECard(getUserCtx());
		final ClassSchema classSchema = op.getClassSchema(className);
		return classSchema;
	}

	@Override
	public CardListExt getCardListExt(final String className, final Attribute[] attributeList, final Query queryType,
			final Order[] orderType, final Integer limit, final Integer offset, final String fullTextQuery,
			final CQLQuery cqlQuery) {
		final ECard ecard = new ECard(getUserCtx());
		return ecard.getCardListExt(className, attributeList, queryType, orderType, limit, offset, fullTextQuery,
				cqlQuery, false);
	}

	/*
	 * r2.3
	 */

	@Override
	public Attribute[] callFunction(final String functionName, final Attribute[] params) {
		Log.SOAP.info(format("calling function '%s' with parameters: %s", functionName, params));
		final CMDataView view = TemporaryObjectsBeforeSpringDI.getUserDataView();
		final CMFunction function = view.findFunctionByName(functionName);
		final Object[] actualParams = convertFunctionInput(function, params);

		final Alias f = NameAlias.as("f");
		final CMQueryResult queryResult = view.select(fakeAnyAttribute(function, f))
				.from(call(function, actualParams), f).run();

		if (queryResult.isEmpty()) {
			return new Attribute[0];
		} else {
			final CMQueryRow row = queryResult.iterator().next();
			return convertFunctionOutput(function, row.getValueSet(f));
		}
	}

	/**
	 * Converts the web service parameters to objects suited for the persistence
	 * layer.
	 * 
	 * Actually it does not need to convert from String to the native Object
	 * because the persistence layer does it automatically!
	 * 
	 * @param function
	 * @param params
	 *            received from the web services
	 * @return params for the persistence layer
	 */
	private Object[] convertFunctionInput(final CMFunction function, final Attribute[] wsParams) {
		final Map<String, String> paramsMap = new HashMap<String, String>();
		if (wsParams != null) {
			for (final Attribute p : wsParams) {
				paramsMap.put(p.getName(), p.getValue());
			}
		}
		final List<CMFunctionParameter> functionParams = function.getInputParameters();
		final List<String> params = new ArrayList<String>(functionParams.size());
		for (final CMFunctionParameter fp : functionParams) {
			final String functionParamName = fp.getName();
			final String stringValue = paramsMap.get(functionParamName);
			params.add(stringValue);
		}
		return params.toArray();
	}

	private Attribute[] convertFunctionOutput(final CMFunction function, final CMValueSet valueSet) {
		final List<CMFunctionParameter> outputParams = function.getOutputParameters();
		final Attribute[] output = new Attribute[outputParams.size()];
		int i = 0;
		for (final CMFunctionParameter p : outputParams) {
			final Attribute a = nativeValueToWsAttribute(p, valueSet);
			output[i] = a;
			++i;
		}
		return output;
	}

	private Attribute nativeValueToWsAttribute(final CMFunctionParameter functionParam, final CMValueSet valueSet) {
		final Attribute a = new Attribute();
		final String paramName = functionParam.getName();
		a.setName(paramName);
		final CMAttributeType<?> type = functionParam.getType();
		final Object value = valueSet.get(paramName);
		a.setValue(nativeValueToWsString(type, value));
		return a;
	}

	private String nativeValueToWsString(final CMAttributeType<?> type, final Object value) {
		return (value == null) ? EMPTY : new AbstractAttributeValueVisitor(type, value) {

			@Override
			public void visit(final EntryTypeAttributeType attributeType) {
				throw new UnsupportedOperationException("regclasses not supported");
			}

			@Override
			public void visit(final LookupAttributeType attributeType) {
				throw new UnsupportedOperationException("lookups not supported");
			}

			@Override
			public void visit(final ReferenceAttributeType attributeType) {
				throw new UnsupportedOperationException("references not supported");
			}

		}.convertValue().toString();
	}

	@Override
	public void notify(final WSEvent wsEvent) {
		Log.SOAP.info("event received");
		final WorkflowEventManager eventManager = TemporaryObjectsBeforeSpringDI.getWorkflowEventManager();
		wsEvent.accept(new WSEvent.Visitor() {

			@Override
			public void visit(final WSProcessStartEvent wsEvent) {
				Log.SOAP.info(format("event for process start: %d / %s / %s", //
						wsEvent.getSessionId(), wsEvent.getProcessDefinitionId(), wsEvent.getProcessInstanceId()));
				final WorkflowEvent event = WorkflowEvent.newProcessStartEvent(wsEvent.getProcessDefinitionId(),
						wsEvent.getProcessInstanceId());
				eventManager.pushEvent(wsEvent.getSessionId(), event);
			}

			@Override
			public void visit(final WSProcessUpdateEvent wsEvent) {
				Log.SOAP.info(format("event for process update: %d / %s / %s", //
						wsEvent.getSessionId(), wsEvent.getProcessDefinitionId(), wsEvent.getProcessInstanceId()));
				final WorkflowEvent event = WorkflowEvent.newProcessUpdateEvent(wsEvent.getProcessDefinitionId(),
						wsEvent.getProcessInstanceId());
				eventManager.pushEvent(wsEvent.getSessionId(), event);
			}

		});
	}

	@Override
	public List<FunctionSchema> getFunctionList() {
		final List<FunctionSchema> functionSchemas = new ArrayList<FunctionSchema>();
		final CMDataView view = TemporaryObjectsBeforeSpringDI.getUserDataView();
		for (final CMFunction function : view.findAllFunctions()) {
			functionSchemas.add(functionSchemaFor(function));
		}
		return functionSchemas;
	}

	private FunctionSchema functionSchemaFor(final CMFunction function) {
		final FunctionSchema functionSchema = new FunctionSchema();
		functionSchema.setName(function.getIdentifier().getLocalName());
		functionSchema.setInput(attributeSchemasFrom(function.getInputParameters()));
		functionSchema.setOutput(attributeSchemasFrom(function.getOutputParameters()));
		return functionSchema;
	}

	private List<AttributeSchema> attributeSchemasFrom(final List<CMFunctionParameter> parameters) {
		final List<AttributeSchema> attributeSchemas = new ArrayList<AttributeSchema>();
		for (final CMFunction.CMFunctionParameter parameter : parameters) {
			attributeSchemas.add(AttributeSchemaSerializer.serialize(parameter));
		}
		return attributeSchemas;
	}

	@Override
	public String generateDigest(final String plainText, final String digestAlgorithm) throws NoSuchAlgorithmException {
		if (digestAlgorithm == null) {
			Log.SOAP.error("The digest algorithm is null");
			throw new IllegalArgumentException(
					"Both the argument must not be null. Specify the text to be encrypted and a valid digest algorithm");
		}
		if (plainText == null) {
			return null;
		}
		final Digester digester = DigesterFactory.createDigester(digestAlgorithm);
		Log.SOAP.info("Generating digest with algorithm " + digester + " ("
				+ (digester.isReversible() ? "reversible" : "irreversible") + ")");
		return digester.encrypt(plainText);
	}

	@Override
	public CardExt getCardWithLongDateFormat(final String className, final Integer cardId,
			final Attribute[] attributeList) {
		return getCard(className, cardId, attributeList, true);
	}

	public CardExt getCard(final String className, final Integer cardId, final Attribute[] attributeList,
			final boolean enableLongDateFormat) {
		final ECard ecard = new ECard(getUserCtx());
		return ecard.getCardExt(className, cardId, attributeList, enableLongDateFormat);
	}

	@Override
	public DataHandler getBuiltInReport(final String reportId, final String extension, final ReportParams[] params) {
		final EReport report = new EReport(getUserCtx());
		return report.getReport(reportId, extension, params);
	}

}
