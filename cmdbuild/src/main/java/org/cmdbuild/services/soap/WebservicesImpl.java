package org.cmdbuild.services.soap;

import static java.lang.String.format;

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
import org.cmdbuild.services.soap.operation.EAdministration;
import org.cmdbuild.services.soap.operation.ECard;
import org.cmdbuild.services.soap.operation.ELookup;
import org.cmdbuild.services.soap.operation.ERelation;
import org.cmdbuild.services.soap.operation.WorkflowLogicHelper;
import org.cmdbuild.services.soap.structure.AttributeSchema;
import org.cmdbuild.services.soap.structure.MenuSchema;
import org.cmdbuild.services.soap.types.Attachment;
import org.cmdbuild.services.soap.types.Attribute;
import org.cmdbuild.services.soap.types.Card;
import org.cmdbuild.services.soap.types.CardList;
import org.cmdbuild.services.soap.types.Lookup;
import org.cmdbuild.services.soap.types.Order;
import org.cmdbuild.services.soap.types.Query;
import org.cmdbuild.services.soap.types.Reference;
import org.cmdbuild.services.soap.types.Relation;
import org.cmdbuild.services.soap.types.Workflow;
import org.cmdbuild.services.soap.utils.WebserviceUtils;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

@WebService(targetNamespace = "http://soap.services.cmdbuild.org", endpointInterface = "org.cmdbuild.services.soap.Webservices")
public class WebservicesImpl implements Webservices, ApplicationContextAware {

	private ApplicationContext applicationContext;

	@Resource
	WebServiceContext wsc;

	private UserContext getUserCtx() {
		final MessageContext msgCtx = wsc.getMessageContext();
		final AuthenticationService as = new AuthenticationService();
		final WebserviceUtils utils = new WebserviceUtils();
		return as.getWSUserContext(utils.getAuthData(msgCtx));
	}

	@Override
	public void setApplicationContext(final ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

	@Override
	public CardList getCardList(final String className, final Attribute[] attributeList, final Query queryType,
			final Order[] orderType, final Integer limit, final Integer offset, final String fullTextQuery) {
		final ECard ecard = new ECard(getUserCtx());
		return ecard.getCardList(className, attributeList, queryType, orderType, limit, offset, fullTextQuery, null,
				false);
	}

	@Override
	public Card getCard(final String className, final Integer cardId, final Attribute[] attributeList) {
		final ECard ecard = new ECard(getUserCtx());
		return ecard.getCard(className, cardId, attributeList);
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
		final ELookup elookup = new ELookup(getUserCtx());
		return elookup.deleteLookup(lookupId);
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
		dmsLogic.setUserContext(getUserCtx());
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
		dmsLogic.setUserContext(getUserCtx());
		try {
			dmsLogic.upload(getUserCtx().getUsername(), className, objectid, file.getInputStream(), filename, category,
					description);
		} catch (final Exception e) {
			final String message = String.format("error uploading file '%s' in '%s'", filename, className);
			Log.SOAP.error(message, e);
		}
		return false;
	}

	@Override
	public DataHandler downloadAttachment(final String className, final int objectid, final String filename) {
		final DmsLogic dmsLogic = applicationContext.getBean(DmsLogic.class);
		dmsLogic.setUserContext(getUserCtx());
		return dmsLogic.download(className, objectid, filename);
	}

	@Override
	public boolean deleteAttachment(final String className, final int cardId, final String filename) {
		final DmsLogic dmsLogic = applicationContext.getBean(DmsLogic.class);
		dmsLogic.setUserContext(getUserCtx());
		dmsLogic.delete(className, cardId, filename);
		return true;
	}

	@Override
	public boolean updateAttachmentDescription(final String className, final int cardId, final String filename,
			final String description) {
		try {
			final DmsLogic dmsLogic = applicationContext.getBean(DmsLogic.class);
			dmsLogic.setUserContext(getUserCtx());
			dmsLogic.updateDescription(className, cardId, filename, description);
			return true;
		} catch (final Exception e) {
			return false;
		}
	}

	@Override
	public Workflow startWorkflow(final Card card, final boolean completeTask) {
		final WorkflowLogicHelper helper = new WorkflowLogicHelper(getUserCtx());
		return helper.updateProcess(card, completeTask);
	}

	@Override
	public boolean updateWorkflow(final Card card, final boolean completeTask) {
		final WorkflowLogicHelper helper = new WorkflowLogicHelper(getUserCtx());
		helper.updateProcess(card, completeTask);
		return true;
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
	public AttributeSchema[] getActivityObjects(final String className, final Integer cardid) {
		final WorkflowLogicHelper helper = new WorkflowLogicHelper(getUserCtx());
		final List<AttributeSchema> attributeSchemaList = helper.getAttributeSchemaList(className, cardid);
		return attributeSchemaList.toArray(new AttributeSchema[attributeSchemaList.size()]);
	}

	@Override
	public MenuSchema getActivityMenuSchema() {
		final EAdministration op = new EAdministration(getUserCtx());
		return op.getProcessMenuSchema();
	}

	@Override
	public Reference[] getReference(final String className, final Query query, final Order[] orderType,
			final Integer limit, final Integer offset, final String fullTextQuery) {
		final ECard op = new ECard(getUserCtx());
		return op.getReference(className, query, orderType, limit, offset, fullTextQuery, null);
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
	public boolean resumeWorkflow(final Card card, final boolean completeTask) {
		if (completeTask) {
			Log.SOAP.warn("ignoring completeTask parameter because it does not make any sense");
		}
		try {
			final WorkflowLogicHelper helper = new WorkflowLogicHelper(getUserCtx());
			helper.resumeProcess(card);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

}