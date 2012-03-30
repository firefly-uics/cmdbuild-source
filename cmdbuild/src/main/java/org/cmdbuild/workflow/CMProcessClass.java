package org.cmdbuild.workflow;

import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.workflow.xpdl.XPDLDocument;
import org.cmdbuild.workflow.xpdl.XPDLException;

public interface CMProcessClass extends CMClass {

	XPDLDocument getXpdlTemplate() throws XPDLException;
}
