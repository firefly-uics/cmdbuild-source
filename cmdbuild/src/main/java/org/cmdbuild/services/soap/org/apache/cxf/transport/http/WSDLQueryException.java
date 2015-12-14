package org.cmdbuild.services.soap.org.apache.cxf.transport.http;

import org.apache.cxf.common.i18n.Message;
import org.apache.cxf.common.i18n.UncheckedException;

public class WSDLQueryException extends UncheckedException {

	public WSDLQueryException(final Message msg, final Throwable t) {
		super(msg, t);
	}

}
