package org.cmdbuild.model.email;

import java.util.Arrays;
import java.util.List;

import org.cmdbuild.data.store.Store.Storable;

public class EmailTemplate implements Storable {

	private String name = "";
	private String description = "";
	private String from = "";
	private String to = "";
	private String cc = "";
	private String bcc = "";
	private String subject = "";
	private String body = "";
	private Long ownerClassId = null;

	/**
	 * Return the name of the template
	 * 
	 * @return templateName
	 */
	public String getName() {
		return name;
	}

	/**
	 * Set a template name
	 * 
	 * @param name
	 */
	public void setName(final String name) {
		this.name = name;
	}

	/**
	 * Return the description of the template
	 * 
	 * @return description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Set a template description
	 * 
	 * @param name
	 */
	public void setDescription(final String description) {
		this.description = description;
	}

	/**
	 * Return the template for the From email field
	 * 
	 * @return from
	 */
	public String getFrom() {
		return from;
	}

	/**
	 * Set a template for the From of the email
	 * 
	 * @param from
	 */
	public void setFrom(final String from) {
		this.from = from;
	}

	/**
	 * Return the template for the To email field
	 * 
	 * @return to
	 */
	public String getTo() {
		return to;
	}

	/**
	 * Read the "TO" attribute and build a list splitting over the separator of
	 * email addresses {@link EmailConstants.ADDRESSES_SEPARATOR}
	 * 
	 * @return
	 */
	public List<String> getToAddresses() {
		return Arrays.asList(getTo().split(EmailConstants.ADDRESSES_SEPARATOR));
	}

	/**
	 * Set a template for the To email field
	 * 
	 * @param to
	 */
	public void setTo(final String to) {
		this.to = to;
	}

	/**
	 * Return the template for the CC email field
	 * 
	 * @return CC
	 */
	public String getCC() {
		return cc;
	}

	/**
	 * Read the "CC" attribute and build a list splitting over the separator of
	 * email addresses {@link EmailConstants.ADDRESSES_SEPARATOR}
	 * 
	 * @return
	 */
	public List<String> getCCAddresses() {
		return Arrays.asList(getCC().split(EmailConstants.ADDRESSES_SEPARATOR));
	}

	/**
	 * Set a template for the CC email field
	 * 
	 * @param CC
	 */
	public void setCC(final String cc) {
		this.cc = cc;
	}

	/**
	 * Return the template for the BCC email field
	 * 
	 * @return bcc
	 */
	public String getBCC() {
		return bcc;
	}

	/**
	 * Read the "BCC" attribute and build a list splitting over the separator of
	 * email addresses {@link EmailConstants.ADDRESSES_SEPARATOR}
	 * 
	 * @return
	 */
	public List<String> getBCCAddresses() {
		return Arrays.asList(getBCC().split(EmailConstants.ADDRESSES_SEPARATOR));
	}

	/**
	 * Set a template for the BCC email field
	 * 
	 * @param bcc
	 */
	public void setBCC(final String bcc) {
		this.bcc = bcc;
	}

	/**
	 * Return the template for the Subject email field
	 * 
	 * @return subject
	 */
	public String getSubject() {
		return subject;
	}

	/**
	 * Set a template for the Subject email field
	 * 
	 * @param subject
	 */
	public void setSubject(final String subject) {
		this.subject = subject;
	}

	/**
	 * Return the template for the Body email field
	 * 
	 * @return body
	 */
	public String getBody() {
		return body;
	}

	/**
	 * Set a template for the Body email field
	 * 
	 * @param body
	 */
	public void setBody(final String body) {
		this.body = body;
	}

	/**
	 * Return the name of the CMDBuild class that owns this template
	 * 
	 * @return ownerClassName
	 */
	public Long getOwnerClassId() {
		return ownerClassId;
	}

	/**
	 * Set the CMDBuild owner class name, it could also be null if the template
	 * is visible for any CMDBuild class
	 * 
	 * @param ownerClassName
	 */
	public void setOwnerId(final Long ownerClassId) {
		this.ownerClassId = ownerClassId;
	}

	@Override
	public String getIdentifier() {
		return this.getName();
	}
}
