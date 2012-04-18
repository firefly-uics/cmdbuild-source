package org.cmdbuild.logic;

import java.io.File;
import java.io.IOException;

import javax.activation.DataSource;

import org.cmdbuild.common.annotations.Legacy;
import org.cmdbuild.services.CustomFilesStore;
import org.cmdbuild.services.auth.UserContext;
import org.cmdbuild.workflow.CMProcessClass;
import org.cmdbuild.workflow.CMWorkflowEngine;
import org.cmdbuild.workflow.CMWorkflowException;

/**
 * Business Logic Layer for Workflow Operations
 */
public class WorkflowLogic {

	private static final String SKETCH_PATH = "images" + File.separator + "workflow" + File.separator;
	private static final CustomFilesStore customFileStore = new CustomFilesStore();

	private final CMWorkflowEngine wfEngine;

	@Legacy("Temporary constructor before switching to Spring DI")
	public WorkflowLogic(final UserContext userCtx) {
		wfEngine = TemporaryObjectsBeforeSpringDI.getWorkflowEngine(userCtx);
	}

	public WorkflowLogic(final CMWorkflowEngine wfEngine) {
		this.wfEngine = wfEngine;
	}

	public DataSource getProcessDefinitionTemplate(final Object processClassNameOrId) throws CMWorkflowException {
		return wfEngine.findProcessClass(processClassNameOrId).getDefinitionTemplate();
	}

	public String[] getProcessDefinitionVersions(final Object processClassNameOrId) throws CMWorkflowException {
		return wfEngine.findProcessClass(processClassNameOrId).getDefinitionVersions();
	}

	public DataSource getProcessDefinition(final Object processClassNameOrId, final String version)
			throws CMWorkflowException {
		return wfEngine.findProcessClass(processClassNameOrId).getDefinition(version);
	}

	public void updateProcessDefinition(final Object processClassNameOrId, final DataSource xpdlFile)
			throws CMWorkflowException {
		wfEngine.findProcessClass(processClassNameOrId).updateDefinition(xpdlFile);
	}

	/*
	 * It's WRONG to display the latest sketch for every process
	 */

	public void removeSketch(final Object processClassNameOrId) {
		final CMProcessClass process = wfEngine.findProcessClass(processClassNameOrId);
		final String filterPattern = process.getName() + ".*";
		final String[] processImages = customFileStore.list(SKETCH_PATH, filterPattern);
		if (processImages.length > 0) {
			customFileStore.remove(SKETCH_PATH + processImages[0]);
		}
	}

	public void addSketch(final Object processClassNameOrId, DataSource ds) throws IOException {
		final CMProcessClass process = wfEngine.findProcessClass(processClassNameOrId);
		final String relativeUploadPath = SKETCH_PATH+process.getName()+customFileStore.getExtension(ds.getName());
		customFileStore.save(ds.getInputStream(), relativeUploadPath);
	}
}
