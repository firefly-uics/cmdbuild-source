package org.cmdbuild.logic;

import static com.google.common.collect.FluentIterable.from;
import static java.lang.String.format;
import static org.cmdbuild.logic.PrivilegeUtils.assure;
import static org.cmdbuild.spring.SpringIntegrationUtils.applicationContext;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.activation.DataSource;

import org.cmdbuild.auth.acl.PrivilegeContext;
import org.cmdbuild.common.utils.PagedElements;
import org.cmdbuild.config.WorkflowConfiguration;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.data.store.lookup.LookupStore;
import org.cmdbuild.exception.CMDBWorkflowException;
import org.cmdbuild.exception.CMDBWorkflowException.WorkflowExceptionType;
import org.cmdbuild.exception.ConsistencyException.ConsistencyExceptionType;
import org.cmdbuild.logic.data.QueryOptions;
import org.cmdbuild.logic.data.access.ProcessEntryFiller;
import org.cmdbuild.logic.data.access.resolver.ForeignReferenceResolver;
import org.cmdbuild.logic.data.access.resolver.ReferenceAndLookupSerializer;
import org.cmdbuild.services.FilesStore;
import org.cmdbuild.workflow.CMActivity;
import org.cmdbuild.workflow.CMProcessClass;
import org.cmdbuild.workflow.CMProcessInstance;
import org.cmdbuild.workflow.CMWorkflowException;
import org.cmdbuild.workflow.QueryableUserWorkflowEngine;
import org.cmdbuild.workflow.user.UserActivityInstance;
import org.cmdbuild.workflow.user.UserProcessClass;
import org.cmdbuild.workflow.user.UserProcessInstance;
import org.joda.time.DateTime;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;

/**
 * Business Logic Layer for Workflow Operations.
 */
public class WorkflowLogic implements Logic {

	private static final Iterable<UserProcessClass> EMPTY_USER_PROCESS_CLASS = Collections.emptyList();

	private static final UserActivityInstance NULL_ACTIVITY_INSTANCE = null;

	private static final String BEGIN_DATE_ATTRIBUTE = "beginDate";
	private static final String SKETCH_PATH = "images" + File.separator + "workflow" + File.separator;

	private final PrivilegeContext privilegeContext;
	private final QueryableUserWorkflowEngine wfEngine;
	private final CMDataView dataView;
	private final WorkflowConfiguration configuration;
	private final FilesStore filesStore;

	public WorkflowLogic( //
			final PrivilegeContext privilegeContext, //
			final QueryableUserWorkflowEngine wfEngine, //
			final CMDataView dataView, //
			final WorkflowConfiguration configuration, //
			final FilesStore filesStore //
	) {
		this.privilegeContext = privilegeContext;
		this.wfEngine = wfEngine;
		this.dataView = dataView;
		this.configuration = configuration;
		this.filesStore = filesStore;
	}

	/*
	 * Ungliness to be used in old code
	 */

	public boolean isProcessUsable(final String className) {
		return isWorkflowEnabled() && wfEngine.findProcessClassByName(className).isUsable();
	}

	public boolean isWorkflowEnabled() {
		return configuration.isEnabled();
	}

	public PagedElements<UserProcessInstance> query(final String className, final QueryOptions queryOptions) {
		final PagedElements<UserProcessInstance> fetchedProcesses = wfEngine.query(className, queryOptions);
		final CMClass processClass = dataView.findClass(className);
		final Iterable<UserProcessInstance> processes = ForeignReferenceResolver.<UserProcessInstance> newInstance() //
				.withSystemDataView(TemporaryObjectsBeforeSpringDI.getSystemView()) //
				.withEntryType(processClass) //
				.withEntries(fetchedProcesses) //
				.withEntryFiller(new ProcessEntryFiller()) //
				.withLookupStore(applicationContext().getBean(LookupStore.class)) //
				.withSerializer(new ReferenceAndLookupSerializer<UserProcessInstance>()) //
				.build() //
				.resolve();
		return new PagedElements<UserProcessInstance>(processes, fetchedProcesses.totalSize());
	}

	public Iterable<UserProcessClass> findAllProcessClasses() {
		return configuration.isEnabled() ? wfEngine.findAllProcessClasses() : EMPTY_USER_PROCESS_CLASS;
	}

	public Iterable<? extends UserProcessClass> findActiveProcessClasses() {
		return configuration.isEnabled() ? wfEngine.findProcessClasses() : EMPTY_USER_PROCESS_CLASS;
	}

	public UserProcessClass findProcessClass(final String className) {
		final Optional<UserProcessClass> optional = from(findAllProcessClasses()) //
				.filter(new Predicate<UserProcessClass>() {
					@Override
					public boolean apply(final UserProcessClass input) {
						return input.getName().equals(className);
					}
				}).first();
		return optional.isPresent() ? optional.get() : null;
	}

	/*
	 * Management
	 */

	/**
	 * Returns the process start activity for the current user.
	 * 
	 * @param process
	 *            class name
	 * @return the start activity definition
	 * @throws CMWorkflowException
	 */
	public CMActivity getStartActivity(final String processClassName) throws CMWorkflowException {
		return wfEngine.findProcessClassByName(processClassName).getStartActivity();
	}

	public CMActivity getStartActivityOrDie( //
			final String processClassName //
	) throws CMWorkflowException, CMDBWorkflowException {

		final UserProcessClass theProess = wfEngine.findProcessClassByName(processClassName);
		final CMActivity theActivity = theProess.getStartActivity();
		if (theActivity == null) {
			throw WorkflowExceptionType.WF_START_ACTIVITY_NOT_FOUND.createException(theProess.getDescription());
		}

		return theActivity;
	}

	public CMActivity getStartActivityOrDie( //
			final Long processClassId //
	) throws CMWorkflowException, CMDBWorkflowException {

		final UserProcessClass theProess = wfEngine.findProcessClassById(processClassId);
		final CMActivity theActivity = theProess.getStartActivity();
		if (theActivity == null) {
			throw WorkflowExceptionType.WF_START_ACTIVITY_NOT_FOUND.createException(theProess.getDescription());
		}

		return theActivity;
	}

	/**
	 * Returns the process start activity for the current user.
	 * 
	 * @param process
	 *            class id
	 * @return the start activity definition
	 * @throws CMWorkflowException
	 */
	public CMActivity getStartActivity(final Long processClassId) throws CMWorkflowException {
		logger.debug("getting starting activity for process with class id '{}'", processClassId);
		return wfEngine.findProcessClassById(processClassId).getStartActivity();
	}

	public UserProcessInstance getProcessInstance(final String processClassName, final Long cardId) {
		logger.debug("getting process instance for class name '{}' and card id '{}'", processClassName, cardId);
		final CMProcessClass processClass = wfEngine.findProcessClassByName(processClassName);
		return wfEngine.findProcessInstance(processClass, cardId);
	}

	public UserProcessInstance getProcessInstance(final Long processClassId, final Long cardId) {
		logger.debug("getting process instance for class id '{}' and card id '{}'", processClassId, cardId);
		final CMProcessClass processClass = wfEngine.findProcessClassById(processClassId);
		return wfEngine.findProcessInstance(processClass, cardId);
	}

	public UserActivityInstance getActivityInstance(final String processClassName, final Long processCardId,
			final String activityInstanceId) {
		logger.debug("getting activity instance '{}' for process '{}'", activityInstanceId, processClassName);
		final UserProcessInstance processInstance = getProcessInstance(processClassName, processCardId);
		return getActivityInstance(processInstance, activityInstanceId);
	}

	public UserActivityInstance getActivityInstance(final Long processClassId, final Long processCardId,
			final String activityInstanceId) {
		logger.debug("getting activity instance '{}' for process '{}'", activityInstanceId, processClassId);
		final UserProcessInstance processInstance = getProcessInstance(processClassId, processCardId);
		return getActivityInstance(processInstance, activityInstanceId);
	}

	private UserActivityInstance getActivityInstance(final UserProcessInstance processInstance,
			final String activityInstanceId) {
		for (final UserActivityInstance activityInstance : processInstance.getActivities()) {
			if (activityInstance.getId().equals(activityInstanceId)) {
				return activityInstance;
			}
		}
		logger.error("activity instance '{}' not found", activityInstanceId);
		return NULL_ACTIVITY_INSTANCE;
	}

	/**
	 * Retrieve the processInstance and check if the given date is the same of
	 * the process begin date in this case, we assume that the process is
	 * updated
	 * 
	 * @param processClassName
	 * @param processInstanceId
	 * @param givenBeginDate
	 * @return
	 */
	public boolean isProcessUpdated( //
			final String processClassName, //
			final Long processInstanceId, //
			final DateTime givenBeginDate //
	) {

		final CMProcessInstance processInstance = getProcessInstance(processClassName, processInstanceId);
		return isProcessUpdated(processInstance, givenBeginDate);
	}

	private boolean isProcessUpdated(final CMProcessInstance processInstance, final DateTime givenBeginDate) {
		final DateTime currentBeginDate = processInstance.getBeginDate();
		return givenBeginDate.equals(currentBeginDate);
	}

	/**
	 * Starts the process, kills every activity except for the one that this
	 * user wanted to start, advances it if requested.
	 * 
	 * @param processClassName
	 *            process class name
	 * @param vars
	 *            values
	 * @param widgetSubmission
	 * @param advance
	 * 
	 * @return the created process instance
	 * 
	 * @throws CMWorkflowException
	 */
	public UserProcessInstance startProcess(final String processClassName, final Map<String, ?> vars,
			final Map<String, Object> widgetSubmission, final boolean advance) throws CMWorkflowException {
		final CMProcessClass processClass = wfEngine.findProcessClassByName(processClassName);
		return startProcess(processClass, vars, widgetSubmission, advance);
	}

	/**
	 * Starts the process, kills every activity except for the one that this
	 * user wanted to start, advances it if requested.
	 * 
	 * @param processClassId
	 *            process class id
	 * @param vars
	 *            values
	 * @param widgetSubmission
	 * @param advance
	 * 
	 * @return the created process instance
	 * 
	 * @throws CMWorkflowException
	 */
	public UserProcessInstance startProcess(final Long processClassId, final Map<String, ?> vars,
			final Map<String, Object> widgetSubmission, final boolean advance) throws CMWorkflowException {
		final CMProcessClass proc = wfEngine.findProcessClassById(processClassId);
		return startProcess(proc, vars, widgetSubmission, advance);
	}

	private UserProcessInstance startProcess(final CMProcessClass process, final Map<String, ?> vars,
			final Map<String, Object> widgetSubmission, final boolean advance) throws CMWorkflowException {
		final UserProcessInstance procInst = wfEngine.startProcess(process);
		final Map<String, Object> mergedVars = mergeVars(procInst.getValues(), vars);
		return updateOnlyActivity(procInst, mergedVars, widgetSubmission, advance);
	}

	/**
	 * This awful hack is needed because SOMEONE decided that it was a good idea
	 * to specify default attributes in the database, so old clients did it and
	 * now we have to deal with it.
	 * 
	 * @param databaseValues
	 *            values as they are in the newly created database row
	 * @param entrySet
	 *            values submitted in the form
	 * @return database values overridden by the submitted ones
	 */
	private Map<String, Object> mergeVars(final Iterable<Entry<String, Object>> databaseValues,
			final Map<String, ?> submittedValues) {
		final Map<String, Object> mergedValues = new HashMap<String, Object>();
		for (final Entry<String, ?> e : databaseValues) {
			mergedValues.put(e.getKey(), e.getValue());
		}
		for (final Entry<String, ?> e : submittedValues.entrySet()) {
			mergedValues.put(e.getKey(), e.getValue());
		}
		return mergedValues;
	}

	public UserProcessInstance updateProcess(final String processClassName, final Long processCardId,
			final String activityInstanceId, final Map<String, ?> vars, final Map<String, Object> widgetSubmission,
			final boolean advance) throws CMWorkflowException {
		final CMProcessClass processClass = wfEngine.findProcessClassByName(processClassName);
		final UserProcessInstance processInstance = wfEngine.findProcessInstance(processClass, processCardId);
		return updateProcess( //
				processInstance, //
				activityInstanceId, //
				vars, //
				widgetSubmission, //
				advance);
	}

	public UserProcessInstance updateProcess(final Long processClassId, final Long processCardId,
			final String activityInstanceId, final Map<String, ?> vars, final Map<String, Object> widgetSubmission,
			final boolean advance) throws CMWorkflowException {

		final CMProcessClass processClass = wfEngine.findProcessClassById(processClassId);
		final UserProcessInstance processInstance = wfEngine.findProcessInstance(processClass, processCardId);

		/*
		 * check if the given begin date is the same of the stored process, to
		 * be sure to deny the update of old versions
		 */
		if (vars.containsKey(BEGIN_DATE_ATTRIBUTE)) {
			final Long givenBeginDateAsLong = (Long) vars.get(BEGIN_DATE_ATTRIBUTE);
			final DateTime givenBeginDate = new DateTime(givenBeginDateAsLong);
			if (!isProcessUpdated(processInstance, givenBeginDate)) {
				throw ConsistencyExceptionType.OUT_OF_DATE_PROCESS.createException();
			}

			/*
			 * must be removed to not use it as a custom attribute
			 */
			vars.remove(BEGIN_DATE_ATTRIBUTE);
		}

		updateProcess( //
				processInstance, //
				activityInstanceId, //
				vars, //
				widgetSubmission, //
				advance);

		/*
		 * retrieve again the processInstance because the updateProcess return
		 * the old processInstance, not the updated.
		 */
		return wfEngine.findProcessInstance(processClass, processCardId);
	}

	private UserProcessInstance updateProcess(final UserProcessInstance processInstance,
			final String activityInstanceId, final Map<String, ?> vars, final Map<String, Object> widgetSubmission,
			final boolean advance) throws CMWorkflowException {
		final UserActivityInstance activityInstance = processInstance.getActivityInstance(activityInstanceId);
		return updateActivity(activityInstance, vars, widgetSubmission, advance);
	}

	/**
	 * Updates and (optionally) advances the only activity of a process
	 * instance.
	 * 
	 * @param procInst
	 *            process instance
	 * @param vars
	 *            variables to update
	 * @param advance
	 * @return the updated process instance
	 * @throws CMWorkflowException
	 */
	private UserProcessInstance updateOnlyActivity(final UserProcessInstance procInst, final Map<String, ?> vars,
			final Map<String, Object> widgetSubmission, final boolean advance) throws CMWorkflowException {
		final List<UserActivityInstance> activities = procInst.getActivities();
		if (activities.size() != 1) {
			throw new UnsupportedOperationException(format("Not just one activity to advance! (%d activities)",
					activities.size()));
		}
		final UserActivityInstance firstActInst = activities.get(0);
		return updateActivity(firstActInst, vars, widgetSubmission, advance);
	}

	private UserProcessInstance updateActivity(final UserActivityInstance activityInstance, final Map<String, ?> vars,
			final Map<String, Object> widgetSubmission, final boolean advance) throws CMWorkflowException {
		wfEngine.updateActivity(activityInstance, vars, widgetSubmission);
		if (advance) {
			return wfEngine.advanceActivity(activityInstance);
		} else {
			return activityInstance.getProcessInstance();
		}
	}

	public void suspendProcess(final String processClassName, final Long processCardId) throws CMWorkflowException {
		final CMProcessClass processClass = wfEngine.findProcessClassByName(processClassName);
		final UserProcessInstance processInstance = wfEngine.findProcessInstance(processClass, processCardId);
		wfEngine.suspendProcessInstance(processInstance);
	}

	public void resumeProcess(final String processClassName, final Long processCardId) throws CMWorkflowException {
		final CMProcessClass processClass = wfEngine.findProcessClassByName(processClassName);
		final UserProcessInstance processInstance = wfEngine.findProcessInstance(processClass, processCardId);
		wfEngine.resumeProcessInstance(processInstance);
	}

	/*
	 * Administration
	 */

	public void sync() throws CMWorkflowException {
		assure(privilegeContext.hasAdministratorPrivileges());
		wfEngine.sync();
	}

	public DataSource getProcessDefinitionTemplate(final Long processClassId) throws CMWorkflowException {
		return wfEngine.findProcessClassById(processClassId).getDefinitionTemplate();
	}

	public String[] getProcessDefinitionVersions(final Long processClassId) throws CMWorkflowException {
		return wfEngine.findProcessClassById(processClassId).getDefinitionVersions();
	}

	public DataSource getProcessDefinition(final Long processClassId, final String version) throws CMWorkflowException {
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
		final String[] processImages = filesStore.list(SKETCH_PATH, filterPattern);
		if (processImages.length > 0) {
			filesStore.remove(SKETCH_PATH + processImages[0]);
		}
	}

	public void addSketch(final Long processClassId, final DataSource ds) throws IOException {
		final CMProcessClass process = wfEngine.findProcessClassById(processClassId);
		final String relativeUploadPath = SKETCH_PATH + process.getName() + filesStore.getExtension(ds.getName());
		filesStore.save(ds.getInputStream(), relativeUploadPath);
	}

	public void abortProcess(final Long processClassId, final long processCardId) throws CMWorkflowException {
		logger.info("aborting process with id '{}' for class '{}'", processCardId, processClassId);
		if (processCardId < 0) {
			logger.error("invalid card id '{}'", processCardId);
			throw WorkflowExceptionType.WF_CANNOT_ABORT_PROCESS.createException();
		}
		final CMProcessClass process = wfEngine.findProcessClassById(processClassId);
		final UserProcessInstance pi = wfEngine.findProcessInstance(process, processCardId);
		wfEngine.abortProcessInstance(pi);
	}

}
