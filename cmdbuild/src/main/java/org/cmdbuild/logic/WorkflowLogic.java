package org.cmdbuild.logic;

import javax.activation.DataSource;
import javax.mail.util.ByteArrayDataSource;

import org.cmdbuild.common.annotations.Legacy;
import org.cmdbuild.elements.wrappers.GroupCard;
import org.cmdbuild.services.auth.UserContext;
import org.cmdbuild.workflow.CMProcessClass;
import org.cmdbuild.workflow.CMWorkflowEngine;
import org.cmdbuild.workflow.xpdl.XPDLDocument;
import org.cmdbuild.workflow.xpdl.XPDLException;
import org.cmdbuild.workflow.xpdl.XPDLPackageFactory;

/**
 * Business Logic Layer for Workflow Operations
 */
public class WorkflowLogic {

	public static final String XPDL_MIME_TYPE = "application/x-xpdl";
	public static final String XPDL_EXTENSION = "xpdl";
	private static final String DEFAULT_SYSTEM_PARTICIPANT = "System";

	private final CMWorkflowEngine wfEngine;

	@Legacy("Temporary constructor before switching to Spring DI")
	public WorkflowLogic(final UserContext userCtx) {
		wfEngine = TemporaryObjectsBeforeSpringDI.getWorkflowEngine(userCtx);
	}

	public WorkflowLogic(final CMWorkflowEngine wfEngine) {
		this.wfEngine = wfEngine;
	}

	public DataSource getXpdlTemplate(final Object processClassNameOrId) throws XPDLException {
		final CMProcessClass pc = wfEngine.findProcessClass(processClassNameOrId);
		final XPDLDocument doc = pc.getXpdlTemplate();
		doc.addSystemParticipant(DEFAULT_SYSTEM_PARTICIPANT);
		addAllGroupsToTemplate(doc);
		// Extended attribute on process userStoppable NOT NEEDED
		// Applications NOT NEEDED?
		byte[] xpdl = XPDLPackageFactory.xpdlByteArray(doc.getPkg());
		final ByteArrayDataSource ds = new ByteArrayDataSource(xpdl, XPDL_MIME_TYPE);
		ds.setName(String.format("%s.%s", pc.getDescription(), XPDL_EXTENSION));
		return ds;
	}

	@Legacy("Should use the new authentication framework")
	private void addAllGroupsToTemplate(XPDLDocument doc) {
		for (GroupCard groupCard : GroupCard.all()) {
			doc.addRoleParticipant(groupCard.getName());
		}
	}
}
