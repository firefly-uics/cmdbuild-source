package org.cmdbuild.logic.email;

import javax.activation.DataHandler;

import org.cmdbuild.exception.CMDBException;
import org.cmdbuild.logic.Logic;
import org.cmdbuild.logic.email.EmailLogic.Email;

import com.google.common.base.Optional;
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

	void upload(Email email, DataHandler dataHandler) throws CMDBException;

	void copy(Email email, Attachment attachment) throws CMDBException;

	void copyAll(Email source, Email destination) throws CMDBException;

	Iterable<Attachment> readAll(Email email) throws CMDBException;

	Optional<DataHandler> read(Email email, Attachment attachment) throws CMDBException;

	void delete(Email email, Attachment attachment) throws CMDBException;

}
