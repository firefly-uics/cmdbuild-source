package org.cmdbuild.logic.email;

import javax.activation.DataHandler;

import org.cmdbuild.exception.CMDBException;
import org.cmdbuild.logic.Logic;

import com.google.common.collect.ForwardingObject;

public interface EmailAttachmentsLogic extends Logic {

	static interface Attachment {

		String getClassName();

		Long getCardId();

		String getFileName();

	}

	static abstract class ForwardingAttachment extends ForwardingObject implements Attachment {

		@Override
		protected abstract Attachment delegate();

		@Override
		public String getClassName() {
			return delegate().getClassName();
		}

		@Override
		public Long getCardId() {
			return delegate().getCardId();
		}

		@Override
		public String getFileName() {
			return delegate().getFileName();
		}

	}

	void upload(Long emailId, boolean temporary, DataHandler dataHandler) throws CMDBException;

	void copy(Long emailId, boolean temporary, Attachment attachment) throws CMDBException;

	Iterable<Attachment> readAll(Long emailId, boolean temporary) throws CMDBException;

	void delete(Long emailId, boolean temporary, String fileName) throws CMDBException;

}
