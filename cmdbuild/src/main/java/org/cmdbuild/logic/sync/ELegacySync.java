package org.cmdbuild.logic.sync;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.StringTokenizer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.axis.AxisFault;
import org.cmdbuild.logic.Logic;
import org.cmdbuild.services.auth.UserContext;
import org.cmdbuild.services.soap.syncscheduler.ConnectorJob;
import org.cmdbuild.services.soap.syncscheduler.ConnectorJob.Action;
import org.cmdbuild.services.soap.syncscheduler.ConnectorJob.DomainDirected;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

public class ELegacySync implements Logic {

	private static final Marker marker = MarkerFactory.getMarker(ELegacySync.class.getName());

	private UserContext userCtx;
	private int masterCardId; // id of the master card
	private String masterClassName; // name of the class of the master class
	private static ExecutorService jobQueue = Executors.newSingleThreadExecutor();

	public enum SyncAttributeNode {
		EXTERNALSYNC("ExternalSync"), //
		ACTIONLIST("actionList"), //
		ACTION("action"), //
		CARDLIST("cardList"), //
		MASTER("cardMaster"), //
		MASTER_CARDID("masterCardId"), //
		MASTER_CLASSNAME("masterClassName"), //
		DETAIL_CARDID("objid"), //
		DOMAIN("domain"), //
		DOMAINDIRECTION("domaindirection"), //
		IDENTIFIERS("identifiers"), //
		ISSHARED("isshared"), //
		;

		private final String attributeName;

		SyncAttributeNode(final String attributeName) {
			this.attributeName = attributeName;
		}

		public String getAttribute() {
			return this.attributeName;
		}
	}

	public ELegacySync(final UserContext userCtx) {
		this.userCtx = userCtx;
	}

	/**
	 * 
	 * @param xml
	 *            list of card to create/update/delete from external connector.
	 * @return String
	 * @throws Exception
	 * @throws AxisFault
	 */
	@SuppressWarnings(value = { "unchecked" })
	public String sync(final String xml) {
		try {
			final Document document = DocumentHelper.parseText(xml);
			final Element element = document.getRootElement();

			final ConnectorJob masterJob = new ConnectorJob();

			final LinkedList<ConnectorJob> detailJobList = new LinkedList<ConnectorJob>();

			final Iterator iterCardList = element.elementIterator(SyncAttributeNode.CARDLIST.getAttribute());
			if (iterCardList.hasNext()) {

				final Element elementCardList = (Element) iterCardList.next();
				setMasterInfo(masterJob, element);

				// iterate over cardlist
				final Iterator iterElement = elementCardList.elementIterator();
				while (iterElement.hasNext()) {
					/** Card/s to update **/
					final Element elementCard = (Element) iterElement.next();

					// is card master
					if (elementCard.attribute("key") != null) {
						/** Infos about the master card **/
						setMasterJob(masterJob, elementCard);
						setAction(masterJob, element);
						jobQueue.submit(masterJob); // execute immediately
					} else {
						final ConnectorJob detailJob = new ConnectorJob();
						/** Infos about the current detail card **/
						setDetailJob(detailJob, elementCard);
						setAction(detailJob, element);
						detailJobList.add(detailJob);
					}
				}
				for (final ConnectorJob job : detailJobList) {
					jobQueue.submit(job);
				}
			}
		} catch (final DocumentException e) {
			logger.error(marker, "Cannot parse xml document");
			logger.debug(marker, xml);
		} catch (final Exception e) {
			logger.error(marker, e.getMessage(), e);
		}
		return "success";
	}

	@SuppressWarnings(value = { "unchecked" })
	private void setAction(final ConnectorJob job, final Element element) {
		/** Action to execute **/
		String action = new String();
		final Iterator<Element> iterAction = element.elementIterator(SyncAttributeNode.ACTION.getAttribute());
		if (iterAction.hasNext()) {
			action = iterAction.next().getText();
			try {
				job.setAction(Action.getAction(action));
			} catch (final Exception e) {
				logger.error(marker, "error setting action", e);
			}

		}
	}

	@SuppressWarnings(value = { "unchecked" })
	private void setMasterInfo(final ConnectorJob job, final Element rootElement) {
		/** Infos about the master card **/
		final Iterator<Element> iterMasterCard = rootElement.elementIterator(SyncAttributeNode.MASTER.getAttribute());
		if (iterMasterCard.hasNext()) {
			final Element masterCard = iterMasterCard.next();
			// classname ex. "Computer"
			final Iterator<Element> iterClassName = masterCard.elementIterator(SyncAttributeNode.MASTER_CLASSNAME
					.getAttribute());
			if (iterClassName.hasNext()) {
				this.masterClassName = iterClassName.next().getText();
			}
			// id ex. 504256
			final Iterator<Element> iterCardId = masterCard.elementIterator(SyncAttributeNode.MASTER_CARDID
					.getAttribute());
			if (iterCardId.hasNext()) {
				this.masterCardId = Integer.parseInt(iterCardId.next().getText());
			}
		}
	}

	private void setMasterJob(final ConnectorJob job, final Element cardElement) {
		/** Infos about the master card **/
		job.setMasterClassName(this.masterClassName);
		job.setDetailClassName(this.masterClassName);
		job.setMasterCardId(this.masterCardId);
		job.setDetailCardId(this.masterCardId);
		job.setElementCard(cardElement);
		job.setIsMaster(true);
		job.setUserContext(userCtx);
	}

	private void setDetailJob(final ConnectorJob job, final Element element) {
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

	private LinkedList<String> getDetailIdentifiers(final Element node) {
		final LinkedList<String> list = new LinkedList<String>();
		final Attribute idsAttribute = node.attribute(SyncAttributeNode.IDENTIFIERS.getAttribute());
		if (idsAttribute != null) {
			final String ids = idsAttribute.getStringValue();
			final StringTokenizer st = new StringTokenizer(ids, ",");
			while (st.hasMoreTokens()) {
				list.add(st.nextToken().trim());
			}
		}
		return list;
	}

	private boolean isSharedDetail(final Element node) {
		final Attribute isSharedAttribute = node.attribute(SyncAttributeNode.ISSHARED.getAttribute());
		if (isSharedAttribute != null) {
			return Boolean.valueOf(isSharedAttribute.getStringValue());
		}
		return false;
	}

	/**
	 * Return the direction of the domain Directed if master object is class1 in
	 * domain comment, Inverted otherwise
	 * 
	 * @param node
	 *            the node containing the detail infos
	 * @return the enum corresponding to the value found (default directed)
	 */
	private String getDirectionDomain(final Element node) {
		final Attribute directionAttribute = node.attribute(SyncAttributeNode.DOMAINDIRECTION.getAttribute());
		if (directionAttribute != null) {
			return directionAttribute.getStringValue();
		}
		return "";
	}

	/** Methods for reading XML **/
	/**
	 * Return the domain name
	 * 
	 * @param node
	 *            the node containing the detail infos
	 * @return the domain name
	 */
	private String getDomainName(final Element node) {
		String domain = new String();
		final Attribute domainAttribute = node.attribute(SyncAttributeNode.DOMAIN.getAttribute());
		if (domainAttribute != null) {
			domain = domainAttribute.getStringValue();
		}
		return domain;
	}

	@SuppressWarnings("unchecked")
	private int getDetailId(final Element node) {
		int detailCardId = 0;
		final Iterator<Attribute> attributeIterator = node.attributeIterator();
		while (attributeIterator.hasNext()) {
			final Attribute attribute = attributeIterator.next();
			if (attribute.getName().trim().equals(SyncAttributeNode.DETAIL_CARDID.getAttribute())) {
				final String sDetailCardId = attribute.getStringValue();
				detailCardId = Integer.parseInt(sDetailCardId);
			}
		}
		return detailCardId;
	}

}
