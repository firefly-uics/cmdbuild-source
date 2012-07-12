package org.cmdbuild.logic;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.activation.DataSource;

import org.cmdbuild.common.annotations.Legacy;
import org.cmdbuild.config.WorkflowProperties;
import org.cmdbuild.elements.interfaces.CardQuery;
import org.cmdbuild.services.CustomFilesStore;
import org.cmdbuild.workflow.CMActivity;
import org.cmdbuild.workflow.CMActivityInstance;
import org.cmdbuild.workflow.CMProcessClass;
import org.cmdbuild.workflow.CMProcessInstance;
import org.cmdbuild.workflow.CMWorkflowException;
import org.cmdbuild.workflow.ContaminatedWorkflowEngine;

/**
 * Business Logic Layer for Workflow Operations.
 */
public class WorkflowLogic {

	private static final String SKETCH_PATH = "images" + File.separator + "workflow" + File.separator;
	private static final CustomFilesStore customFileStore = new CustomFilesStore();

	private final ContaminatedWorkflowEngine wfEngine;

	public WorkflowLogic(final ContaminatedWorkflowEngine wfEngine) {
		this.wfEngine = wfEngine;
	}

	/*
	 * Ungliness to be used in old code
	 */

	public boolean isProcessUsable(String className) {
		return isWorkflowEnabled() && wfEngine.findProcessClassByName(className).isUsable();
	}

	@Legacy("Old DAO")
	public boolean isWorkflowEnabled() {
		return WorkflowProperties.getInstance().isEnabled();
	}

	/**
	 * Queries the data store for process instances. It should accept an
	 * object representing the filter, and not the query object itself.
	 * 
	 * @param cardQuery
	 * @return
	 */
	@Legacy("Old DAO")
	public Iterable<CMProcessInstance> query(final CardQuery cardQuery) {
		return wfEngine.query(cardQuery);
	}

	/*
	 * Management
	 */

	/**
	 * Returns the process start activity for the current user.
	 * 
	 * @param process class name or id
	 * @return the start activity definition
	 * @throws CMWorkflowException
	 */
	public CMActivity getStartActivity(final Object processClassNameOrId) throws CMWorkflowException {
		return wfEngine.findProcessClass(processClassNameOrId).getStartActivity();
	}

	public CMProcessInstance getProcessInstance(
		final Object processClassNameOrId, final Long cardId) {
		final CMProcessClass proc = wfEngine.findProcessClass(processClassNameOrId);

		return wfEngine.findProcessInstance(proc, cardId);
	}

	public CMActivityInstance getActivityInstance(final Object processClassNameOrId, final Long processCardId, final Object activityInstanceId) {
		CMProcessInstance pi = getProcessInstance(processClassNameOrId, processCardId);
		for (CMActivityInstance a:pi.getActivities()) {
			if (a.getId().equals(activityInstanceId)) {
				return a;
			}
		}
		return null;
	}

	/**
	 * Starts the process, kills every activity except for the one that this
	 * user wanted to start, advances it if requested.
	 * 
	 * @param process class name or id
	 * @param variable values
	 * @return the created process instance
	 * @throws CMWorkflowException 
	 */
	public CMProcessInstance startProcess(
			final Object processClassNameOrId,
			final Map<String, Object> vars,
			final boolean advance) throws CMWorkflowException {
		final CMProcessClass proc = wfEngine.findProcessClass(processClassNameOrId);
		final CMProcessInstance procInst = wfEngine.startProcess(proc);
		return updateOnlyActivity(procInst, vars, advance);
	}

	/**
	 * Updates and (optionally) advances the only activity of a process
	 * instance.
	 * 
	 * @param procInst process instance
	 * @param vars variables to update
	 * @param advance
	 * @return the updated process instance
	 * @throws CMWorkflowException
	 */
	private CMProcessInstance updateOnlyActivity(final CMProcessInstance procInst, final Map<String, Object> vars,
			final boolean advance) throws CMWorkflowException {
		final List<CMActivityInstance> activities = procInst.getActivities();
		if (activities.size() != 1) {
			throw new UnsupportedOperationException(String.format("Not just one activity to advance! (%d activities)", activities.size()));
		}
		final CMActivityInstance firstActInst = activities.get(0);
		return updateActivity(firstActInst, vars, advance);
	}

	private CMProcessInstance updateActivity(final CMActivityInstance activityInstance, final Map<String, Object> vars,
			final boolean advance) throws CMWorkflowException {
		wfEngine.updateActivity(activityInstance, vars);
		if (advance) {
			return wfEngine.advanceActivity(activityInstance);
		} else {
			return activityInstance.getProcessInstance();
		}
	}

	/*
	 * Administration
	 */

	public void sync() throws CMWorkflowException {
		wfEngine.sync();
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
