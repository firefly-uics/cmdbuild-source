package org.cmdbuild.services.soap;

import java.util.ArrayList;
import java.util.List;

import javax.activation.DataHandler;
import javax.annotation.Resource;
import javax.jws.WebService;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;

import org.cmdbuild.dms.documents.StoredDocument;
import org.cmdbuild.logger.Log;
import org.cmdbuild.logic.DmsLogic;
import org.cmdbuild.services.auth.AuthenticationService;
import org.cmdbuild.services.auth.UserContext;
import org.cmdbuild.services.auth.UserContextToUserInfo;
import org.cmdbuild.services.auth.UserInfo;
import org.cmdbuild.services.soap.operation.EAdministration;
import org.cmdbuild.services.soap.operation.ECard;
import org.cmdbuild.services.soap.operation.ELegacySync;
import org.cmdbuild.services.soap.operation.ELookup;
import org.cmdbuild.services.soap.operation.ERelation;
import org.cmdbuild.services.soap.operation.EReport;
import org.cmdbuild.services.soap.operation.EWorkflow;
import org.cmdbuild.services.soap.operation.PrivateWorkflow;
import org.cmdbuild.services.soap.structure.ActivitySchema;
import org.cmdbuild.services.soap.structure.AttributeSchema;
import org.cmdbuild.services.soap.structure.MenuSchema;
import org.cmdbuild.services.soap.structure.WorkflowWidgetSubmission;
import org.cmdbuild.services.soap.types.Attachment;
import org.cmdbuild.services.soap.types.Attribute;
import org.cmdbuild.services.soap.types.CQLQuery;
import org.cmdbuild.services.soap.types.Card;
import org.cmdbuild.services.soap.types.CardList;
import org.cmdbuild.services.soap.types.Lookup;
import org.cmdbuild.services.soap.types.Order;
import org.cmdbuild.services.soap.types.Query;
import org.cmdbuild.services.soap.types.Reference;
import org.cmdbuild.services.soap.types.Relation;
import org.cmdbuild.services.soap.types.Report;
import org.cmdbuild.services.soap.types.ReportParams;
import org.cmdbuild.services.soap.types.Workflow;
import org.cmdbuild.services.soap.utils.WebserviceUtils;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

@WebService(endpointInterface = "org.cmdbuild.services.soap.Private", targetNamespace = "http://soap.services.cmdbuild.org")
public class PrivateImpl implements Private, ApplicationContextAware {

	private ApplicationContext applicationContext;

	@Resource
	WebServiceContext wsc;

	private UserContext getUserCtx() {
		MessageContext msgCtx = wsc.getMessageContext();
		AuthenticationService as = new AuthenticationService();
		WebserviceUtils utils = new WebserviceUtils();
		return as.getWSUserContext(utils.getAuthData(msgCtx));
	}

	@Override
	public void setApplicationContext(final ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

	public CardList getCardList(String className, Attribute[] attributeList, Query queryType, Order[] orderType,
			Integer limit, Integer offset, String fullTextQuery, CQLQuery cqlQuery) {
		return getCardList(className, attributeList, queryType, orderType, limit, offset, fullTextQuery, cqlQuery, false);
	}

	public CardList getCardListWithLongDateFormat(String className, Attribute[] attributeList, Query queryType,
			Order[] orderType, Integer limit, Integer offset, String fullTextQuery, CQLQuery cqlQuery) {
		return getCardList(className, attributeList, queryType, orderType, limit, offset, fullTextQuery, cqlQuery, true);
	}

	private CardList getCardList(String className, Attribute[] attributeList, Query queryType, Order[] orderType,
			Integer limit, Integer offset, String fullTextQuery, CQLQuery cqlQuery, boolean enableLongDateFormat) {
		ECard ecard = new ECard(getUserCtx());
		return ecard.getCardList(className, attributeList, queryType, orderType, limit, offset, fullTextQuery, cqlQuery, enableLongDateFormat);
	}

	public Card getCard(String className, Integer cardId, Attribute[] attributeList) {
		ECard ecard = new ECard(getUserCtx());
		return ecard.getCard(className, cardId, attributeList);
	}

	public CardList getCardHistory(String className, int cardId, Integer limit, Integer offset) {
		ECard ecard = new ECard(getUserCtx());
		return ecard.getCardHistory(className, cardId, limit, offset);
	}

	public int createCard(Card cardType) {
		ECard ecard = new ECard(getUserCtx());
		return ecard.createCard(cardType);
	}

	public boolean updateCard(Card card) {
		ECard ecard = new ECard(getUserCtx());
		return ecard.updateCard(card);
	}

	public boolean deleteCard(String className, int cardId) {
		ECard ecard = new ECard(getUserCtx());
		return ecard.deleteCard(className, cardId);
	}

	public int createLookup(Lookup lookup) {
		ELookup elookup = new ELookup(getUserCtx());
		return elookup.createLookup(lookup);
	}

	public boolean deleteLookup(int lookupId) {
		ELookup elookup = new ELookup(getUserCtx());
		return elookup.deleteLookup(lookupId);
	}

	public boolean updateLookup(Lookup lookup) {
		ELookup elookup = new ELookup(getUserCtx());
		return elookup.updateLookup(lookup);
	}

	public Lookup getLookupById(int id) {
		ELookup elookup = new ELookup(getUserCtx());
		return elookup.getLookupById(id);
	}

	public Lookup[] getLookupList(String type, String value, boolean parentList) {
		ELookup elookup = new ELookup(getUserCtx());
		return elookup.getLookupList(type, value, parentList);
	}

	public Lookup[] getLookupListByCode(String type, String code, boolean parentList) {
		ELookup elookup = new ELookup(getUserCtx());
		return elookup.getLookupListByCode(type, code, parentList);
	}

	public boolean createRelation(Relation relation) {
		ERelation erelation = new ERelation(getUserCtx());
		return erelation.createRelation(relation);
	}

	public boolean deleteRelation(Relation relation) {
		ERelation erelation = new ERelation(getUserCtx());
		return erelation.deleteRelation(relation);
	}

	public List<Relation> getRelationList(String domain, String className, int cardId) {
		ERelation erelation = new ERelation(getUserCtx());
		return erelation.getRelationList(domain, className, cardId);
	}

	public Relation[] getRelationHistory(Relation relation) {
		ERelation erelation = new ERelation(getUserCtx());
		return erelation.getRelationHistory(relation);
	}

	public Attachment[] getAttachmentList(String className, int cardId) {
		final DmsLogic dmsLogic = applicationContext.getBean(DmsLogic.class);
		dmsLogic.setUserContext(getUserCtx());
		final List<StoredDocument> storedDocuments = dmsLogic.search(className, cardId);
		final List<Attachment> attachments = new ArrayList<Attachment>();
		for (StoredDocument storedDocument : storedDocuments) {
			final Attachment attachment = new Attachment(storedDocument);
			attachments.add(attachment);
		}
		return attachments.toArray(new Attachment[attachments.size()]);
	}

	public boolean uploadAttachment(String className, int objectid, DataHandler file, String filename, String category,
			String description) {
		final DmsLogic dmsLogic = applicationContext.getBean(DmsLogic.class);
		dmsLogic.setUserContext(getUserCtx());
		try {
			dmsLogic.upload(getUserCtx().getUsername(), className, objectid, file.getInputStream(), filename, category,
					description);
		} catch (Exception e) {
			final String message = String.format("error uploading file '%s' in '%s'", filename, className);
			Log.SOAP.error(message, e);
		}
		return false;
	}

	public DataHandler downloadAttachment(String className, int objectid, String filename) {
		final DmsLogic dmsLogic = applicationContext.getBean(DmsLogic.class);
		dmsLogic.setUserContext(getUserCtx());
		return dmsLogic.download(className, objectid, filename);
	}

	public boolean deleteAttachment(String className, int cardId, String filename) {
		final DmsLogic dmsLogic = applicationContext.getBean(DmsLogic.class);
		dmsLogic.setUserContext(getUserCtx());
		dmsLogic.delete(className, cardId, filename);
		return true;
	}

	public boolean updateAttachmentDescription(String className, int cardId, String filename, String description) {
		try {
			final DmsLogic dmsLogic = applicationContext.getBean(DmsLogic.class);
			dmsLogic.setUserContext(getUserCtx());
			dmsLogic.updateDescription(className, cardId, filename, description);
			return true;
		} catch (final Exception e) {
			return false;
		}
	}

	public Workflow updateWorkflow(Card card, boolean completeTask, WorkflowWidgetSubmission[] widgets) {
		PrivateWorkflow ws = new PrivateWorkflow(getUserCtx());
		return ws.updateWorkflow(card, completeTask, widgets);
	}

	public String getProcessHelp(String classname, Integer cardid) {
		EWorkflow workflow = new EWorkflow(getUserCtx());
		return workflow.getProcessHelp(classname, cardid);
	}

	public AttributeSchema[] getAttributeList(String className) {
		ECard op = new ECard(getUserCtx());
		return op.getAttributeList(className);
	}

	public ActivitySchema getActivityObjects(String className, Integer cardid) {
		PrivateWorkflow wf = new PrivateWorkflow(getUserCtx());
		return wf.getActivityObjects(className, cardid);
	}

	public MenuSchema getActivityMenuSchema() {
		EAdministration op = new EAdministration(getUserCtx());
		return op.getProcessMenuSchema();
	}

	public Reference[] getReference(String className, Query query, Order[] orderType, Integer limit, Integer offset,
			String fullTextQuery, CQLQuery cqlQuery) {
		ECard op = new ECard(getUserCtx());
		return op.getReference(className, query, orderType, limit, offset, fullTextQuery, cqlQuery);
	}

	public MenuSchema getCardMenuSchema() {
		EAdministration op = new EAdministration(getUserCtx());
		return op.getClassMenuSchema();
	}

	public MenuSchema getMenuSchema() {
		EAdministration op = new EAdministration(getUserCtx());
		return op.getMenuSchema();
	}

	public Report[] getReportList(String type, int limit, int offset) {
		EReport op = new EReport(getUserCtx());
		return op.getReportList(type, limit, offset);
	}

	public AttributeSchema[] getReportParameters(int id, String extension) {
		EReport op = new EReport(getUserCtx());
		return op.getReportParameters(id, extension);
	}

	public DataHandler getReport(int id, String extension, ReportParams[] params) {
		EReport op = new EReport(getUserCtx());
		return op.getReport(id, extension, params);
	}

	public String sync(String xml) {
		Log.SOAP.info("Calling webservice ExternalSync.sync");
		Log.SOAP.debug("xml message:" + xml);
		ELegacySync op = new ELegacySync(getUserCtx());
		return op.sync(xml);
	}

	public UserInfo getUserInfo() {
		return UserContextToUserInfo.newInstance(getUserCtx()).build();
	}
}
