package org.cmdbuild.services.soap.operation;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.StringTokenizer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.lang.StringUtils;
import org.cmdbuild.logger.Log;
import org.cmdbuild.services.auth.UserContext;
import org.cmdbuild.services.soap.syncscheduler.ConnectorJob;
import org.cmdbuild.services.soap.syncscheduler.ConnectorJob.Action;
import org.cmdbuild.services.soap.syncscheduler.ConnectorJob.DomainDirected;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

public class ELegacySync {

	UserContext userCtx;
	private int masterCardId; // id of the master card
	private String masterClassName; //name of the class of the master class
	static ExecutorService jobQueue = Executors.newSingleThreadExecutor(); 

	public enum SyncAttributeNode {
		EXTERNALSYNC("ExternalSync"), 
		ACTIONLIST("actionList"),
		ACTION("action"),
		CARDLIST("cardList"),
		MASTER("cardMaster"),
		MASTER_CARDID("masterCardId"),
		MASTER_CLASSNAME("masterClassName"),
		DETAIL_CARDID("objid"),
		DOMAIN("domain"), 
		DOMAINDIRECTION("domaindirection"),
		IDENTIFIERS("identifiers"), 
		ISSHARED("isshared");

		private final String attributeName;
		
		SyncAttributeNode(String attributeName) { 
			this.attributeName = attributeName; 
		}
	    public String getAttribute() { 
	    	return this.attributeName; 
	    }   
	}
	
	public ELegacySync(UserContext userCtx){
		this.userCtx = userCtx;
	}
	
	/**
	 * 
	 * @param xml list of card to create/update/delete from external connector.  
	 * @return String
	 * @throws Exception 
	 * @throws AxisFault
	 */
	@SuppressWarnings(value={"unchecked"})
	public String sync(String xml) {
		 try {
			Document document = DocumentHelper.parseText(xml);
			Element element  = document.getRootElement();

			ConnectorJob masterJob = new ConnectorJob();

			LinkedList<ConnectorJob> detailJobList = new LinkedList<ConnectorJob>();
			
			Iterator iterCardList =  element.elementIterator(SyncAttributeNode.CARDLIST.getAttribute());
			if(iterCardList.hasNext()){

				
				Element elementCardList = (Element)iterCardList.next();
				setMasterInfo(masterJob, element); 
				
				//iterate over cardlist
				Iterator iterElement =  elementCardList.elementIterator();
				while (iterElement.hasNext()) {
					/** Card/s to update **/
					Element elementCard = (Element)iterElement.next();

					//is card master
					if (elementCard.attribute("key")!=null) {
						/** Infos about the master card **/
						setMasterJob(masterJob, elementCard);
						setAction(masterJob, element);
						jobQueue.submit(masterJob); //execute immediately
					} else {
						ConnectorJob detailJob = new ConnectorJob();
						/** Infos about the current detail card **/
						setDetailJob(detailJob, elementCard);
						setAction(detailJob, element);
						detailJobList.add(detailJob); 
					}
				}
				for (ConnectorJob job: detailJobList) {
					jobQueue.submit(job); 
				}
			}
		} catch (DocumentException e) {
			Log.SOAP.error("Cannot parse xml document");
			Log.SOAP.debug(xml);
		}catch (Exception e){
			Log.SOAP.error(e.getMessage(), e);
		}
		return "success";
	}
	

	@SuppressWarnings(value={"unchecked"})
	private void setAction(ConnectorJob job, Element element) {		
		/** Action to execute **/
		String action = new String();
		Iterator<Element> iterAction =  element.elementIterator(SyncAttributeNode.ACTION.getAttribute());
		if(iterAction.hasNext()){
			action= ((Element)iterAction.next()).getText();
			try {
				job.setAction(Action.getAction(action));
			} catch (Exception e) {
				Log.SOAP.error("error setting action", e);
			} 
				
		}
	}
	
	@SuppressWarnings(value={"unchecked"})
	private void setMasterInfo(ConnectorJob job, Element rootElement){/** Infos about the master card **/
		Iterator<Element> iterMasterCard =  rootElement.elementIterator(SyncAttributeNode.MASTER.getAttribute());
		if(iterMasterCard.hasNext()){
			Element masterCard = (Element) iterMasterCard.next();
			//classname ex. "Computer"
			Iterator<Element> iterClassName =  masterCard.elementIterator(SyncAttributeNode.MASTER_CLASSNAME.getAttribute());
			if(iterClassName.hasNext()){
				this.masterClassName=((Element)iterClassName.next()).getText();
			}
			//id ex. 504256
			Iterator<Element> iterCardId =  masterCard.elementIterator(SyncAttributeNode.MASTER_CARDID.getAttribute());
			if(iterCardId.hasNext()){
				this.masterCardId=Integer.parseInt(((Element)iterCardId.next()).getText());
			}
		}
	}

	private void setMasterJob(ConnectorJob job, Element cardElement){/** Infos about the master card **/
		job.setMasterClassName(this.masterClassName);
		job.setDetailClassName(this.masterClassName);
		job.setMasterCardId(this.masterCardId);
		job.setDetailCardId(this.masterCardId);
		job.setElementCard(cardElement);
		job.setIsMaster(true);
		job.setUserContext(userCtx);
	}
	
	private void setDetailJob(ConnectorJob job, Element element){
		/** Infos about the detail card **/
		job.setMasterClassName(this.masterClassName);
		job.setMasterCardId(this.masterCardId);
		job.setDomainName(getDomainName(element));
		job.setDomainDirection(DomainDirected.getDirection(getDirectionDomain(element)));
		job.setDetailIdentifiers(getDetailIdentifiers(element));
		job.setIsShared(isSharedDetail(element));
		job.setDetailCardId(getDetailId(element));
		job.setDetailClassName(element.getName());
		job.setElementCard(element);
		job.setUserContext(userCtx);
	}
	
	private LinkedList<String> getDetailIdentifiers(Element node){
		LinkedList<String> list = new LinkedList<String>();
		Attribute idsAttribute = (Attribute) node.attribute(SyncAttributeNode.IDENTIFIERS.getAttribute());
		if(idsAttribute!=null){
			String ids=idsAttribute.getStringValue();
			StringTokenizer st= new StringTokenizer(ids, ",");
			while(st.hasMoreTokens())
				list.add(st.nextToken().trim());
		}
		return list;
	}

	private boolean isSharedDetail(Element node){
		Attribute isSharedAttribute = (Attribute) node.attribute(SyncAttributeNode.ISSHARED.getAttribute());
		if(isSharedAttribute!=null){
			return Boolean.valueOf(isSharedAttribute.getStringValue());
		}
		return false;
	}
	
	/**
	 * Return the direction of the domain
	 * Directed if master object is class1 in domain comment,
	 * Inverted otherwise
	 * @param node the node containing the detail infos
	 * @return the enum corresponding to the value found (default directed)
	 */
	private String getDirectionDomain(Element node){
		Attribute directionAttribute = (Attribute) node.attribute(SyncAttributeNode.DOMAINDIRECTION.getAttribute());
		if(directionAttribute!=null){
			return directionAttribute.getStringValue();
		}
		return "";
	}
	
	/** Methods for reading XML **/
	/**
	 * Return the domain name
	 * @param node the node containing the detail infos
	 * @return the domain name
	 */
	private String getDomainName(Element node){
		String domain = new String();
		Attribute domainAttribute = (Attribute) node.attribute(SyncAttributeNode.DOMAIN.getAttribute());
		if(domainAttribute!=null){
			domain=domainAttribute.getStringValue();
		}
		return domain;
	}

	@SuppressWarnings("unchecked")
	private int getDetailId(Element node){
		int detailCardId=0;
		Iterator<Attribute> attributeIterator = (Iterator<Attribute>) node.attributeIterator();
		while(attributeIterator.hasNext()){
			Attribute attribute = attributeIterator.next();
			if(attribute.getName().trim().equals(SyncAttributeNode.DETAIL_CARDID.getAttribute())){
				String sDetailCardId=attribute.getStringValue();
				detailCardId=Integer.parseInt(sDetailCardId);
			}
		}
		return detailCardId;
	}

}
	
