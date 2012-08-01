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
import org.cmdbuild.workflow.CMProcessClass;
import org.cmdbuild.workflow.CMWorkflowException;
import org.cmdbuild.workflow.ContaminatedWorkflowEngine;
import org.cmdbuild.workflow.user.UserActivityInstance;
import org.cmdbuild.workflow.user.UserProcessInstance;

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
	public Iterable<UserProcessInstance> query(final CardQuery cardQuery) {
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
	public CMActivity getStartActivity(final Long processClassId) throws CMWorkflowException {
		return wfEngine.findProcessClassById(processClassId).getStartActivity();
	}

	public UserProcessInstance getProcessInstance(
		final Long processClassId, final Long cardId) {
		final CMProcessClass proc = wfEngine.findProcessClassById(processClassId);

		return wfEngine.findProcessInstance(proc, cardId);
	}

	public UserActivityInstance getActivityInstance(final Long processClassId, final Long processCardId, final Object activityInstanceId) {
		UserProcessInstance pi = getProcessInstance(processClassId, processCardId);
		for (UserActivityInstance a:pi.getActivities()) {
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
	 * @param process class id
	 * @param variable values
	 * @return the created process instance
	 * @throws CMWorkflowException 
	 */
	public UserProcessInstance startProcess(
			final Long processClassId,
			final Map<String, Object> vars,
			final Map<String, Object> widgetSubmission,
			final boolean advance) throws CMWorkflowException {
		final CMProcessClass proc = wfEngine.findProcessClassById(processClassId);
		final UserProcessInstance procInst = wfEngine.startProcess(proc);
		return updateOnlyActivity(procInst, vars, widgetSubmission, advance);
	}

	public UserProcessInstance updateProcess(Long processClassId,
			final Long processCardId,
			final String activityInstanceId,
			final Map<String, Object> vars,
			final Map<String, Object> widgetSubmission,
			boolean advance) throws CMWorkflowException {

		final CMProcessClass proc = wfEngine.findProcessClassById(processClassId);
		final UserProcessInstance procInst = wfEngine.findProcessInstance(proc, processCardId);
		final UserActivityInstance activityInstance = procInst.getActivityInstance(activityInstanceId);

		return updateActivity(activityInstance, vars, widgetSubmission, advance);
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
	private UserProcessInstance updateOnlyActivity(
			final UserProcessInstance procInst,
			final Map<String, Object> vars,
			final Map<String, Object> widgetSubmission,
			final boolean advance) throws CMWorkflowException {
		final List<UserActivityInstance> activities = procInst.getActivities();
		if (activities.size() != 1) {
			throw new UnsupportedOperationException(String.format("Not just one activity to advance! (%d activities)", activities.size()));
		}
		final UserActivityInstance firstActInst = activities.get(0);
		return updateActivity(firstActInst, vars, widgetSubmission, advance);
	}

	private UserProcessInstance updateActivity(
			final UserActivityInstance activityInstance,
			final Map<String, Object> vars,
			final Map<String, Object> widgetSubmission,
			final boolean advance) throws CMWorkflowException {
		wfEngine.updateActivity(activityInstance, vars, widgetSubmission);
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

	public DataSource getProcessDefinitionTemplate(final Long processClassId) throws CMWorkflowException {
		return wfEngine.findProcessClassById(processClassId).getDefinitionTemplate();
	}

	public String[] getProcessDefinitionVersions(final Long processClassId) throws CMWorkflowException {
		return wfEngine.findProcessClassById(processClassId).getDefinitionVersions();
	}

	public DataSource getProcessDefinition(final Long processClassId, final String version)
			throws CMWorkflowException {
		return wfEngine.findProcessClassById(processClassId).getDefinition(version);
	}

	public void updateProcessDefinition(final Long processClassId, final DataSource xpdlFile)
			throws CMWorkflowException {
		wfEngine.findProcessClassById(processClassId).updateDefinition(xpdlFile);
	}

	/*
	 * It's WRONG to display the latest sketch for every process
	 */

	public void removeSketch(final Long processClassId) {
		final CMProcessClass process = wfEngine.findProcessClassById(processClassId);
		final String filterPattern = process.getName() + ".*";
		final String[] processImages = customFileStore.list(SKETCH_PATH, filterPattern);
		if (processImages.length > 0) {
			customFileStore.remove(SKETCH_PATH + processImages[0]);
		}
	}

	public void addSketch(final Long processClassId, DataSource ds) throws IOException {
		final CMProcessClass process = wfEngine.findProcessClassById(processClassId);
		final String relativeUploadPath = SKETCH_PATH+process.getName()+customFileStore.getExtension(ds.getName());
		customFileStore.save(ds.getInputStream(), relativeUploadPath);
	}

	public void abortProcess(Long processClassId, long processCardId) throws CMWorkflowException {
		final CMProcessClass process = wfEngine.findProcessClassById(processClassId);
		final UserProcessInstance pi = wfEngine.findProcessInstance(process, processCardId);

		wfEngine.abortProcessInstance(pi);
	}
}
