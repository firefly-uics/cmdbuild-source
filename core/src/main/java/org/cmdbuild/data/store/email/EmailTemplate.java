package org.cmdbuild.data.store.email;

import java.util.List;

import org.cmdbuild.data.store.Storable;

public interface EmailTemplate extends Storable {

	Long getId();

	String getName();

	String getDescription();

	String getFrom();

	String getTo();

	/**
	 * Read the "TO" attribute and build a list splitting over the separator of
	 * email addresses {@link EmailConstants.ADDRESSES_SEPARATOR}.
	 */
	List<String> getToAddresses();

	String getCc();

	/**
	 * Read the "CC" attribute and build a list splitting over the separator of
	 * email addresses {@link EmailConstants.ADDRESSES_SEPARATOR}.
	 */
	List<String> getCCAddresses();

	String getBcc();

	/**
	 * Read the "BCC" attribute and build a list splitting over the separator of
	 * email addresses {@link EmailConstants.ADDRESSES_SEPARATOR}.
	 */
	List<String> getBCCAddresses();

	String getSubject();

	String getBody();

	Long getAccount();

}