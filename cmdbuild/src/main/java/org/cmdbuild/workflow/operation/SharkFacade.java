package org.cmdbuild.workflow.operation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;

import org.cmdbuild.elements.interfaces.IAttribute;
import org.cmdbuild.elements.interfaces.ICard;
import org.cmdbuild.elements.interfaces.ITable;
import org.cmdbuild.exception.CMDBWorkflowException;
import org.cmdbuild.exception.CMDBWorkflowException.WorkflowExceptionType;
import org.cmdbuild.logger.Log;
import org.cmdbuild.services.WorkflowService;
import org.cmdbuild.services.WorkflowService.WorkflowOperation;
import org.cmdbuild.services.auth.UserContext;
import org.cmdbuild.workflow.ActivityVariable;
import org.cmdbuild.workflow.CmdbuildActivityInfo;
import org.cmdbuild.workflow.CmdbuildProcessInfo;
import org.cmdbuild.workflow.SharkWSFacade;
import org.cmdbuild.workflow.WorkflowCache;
import org.cmdbuild.workflow.WorkflowConstants;
import org.enhydra.shark.api.client.wfmc.wapi.WMSessionHandle;
import org.enhydra.shark.api.client.wfmc.wapi.WMWorkItem;
import org.enhydra.shark.api.client.wfservice.WMEntity;
import org.enhydra.shark.client.utilities.SharkWSFactory;

public class SharkFacade {

	SharkWSFacade facade;
	UserContext userCtx;

	public SharkFacade(final UserContext userCtx) {
		this.userCtx = userCtx;
		this.facade = new SharkWSFacade();
	}

	protected <T> T executeAdmin(final WorkflowService.WorkflowOperation<T> operation) {
		return execute(operation, true);
	}

	protected <T> T execute(final WorkflowService.WorkflowOperation<T> operation) {
		return execute(operation, false);
	}

	protected <T> T execute(final WorkflowService.WorkflowOperation<T> operation, final boolean adminConnection) {
		try {
			return WorkflowService.getInstance().execute(operation, userCtx, adminConnection);
		} catch (final CMDBWorkflowException e) {
			Log.WORKFLOW.error("error executing an action (cmdb) - " + e.getExceptionTypeText(), e);
			throw e;
		} catch (final Exception e) {
			Log.WORKFLOW.error("error executing an action", e);
			throw WorkflowExceptionType.WF_GENERIC_ERROR.createException();
		}
	}

	private abstract class AbstractUpdateActivityOperation implements WorkflowOperation<Boolean> {
		private final String processInstanceId;
		private final String workItemId;
		private final boolean complete;
		private final Map<String, String> params;

		protected AbstractUpdateActivityOperation(final String processInstanceId, final String workItemId,
				final Map<String, String> params, final boolean complete) {
			this.processInstanceId = processInstanceId;
			this.workItemId = workItemId;
			this.complete = complete;
			this.params = params;
		}

		@Override
		public Boolean execute(final WMSessionHandle handle, final SharkWSFactory factory, final UserContext userCtx)
				throws Exception {
			Log.WORKFLOW.info("Updating workitem " + workItemId);
			final WMWorkItem item = facade.getWorkItem(handle, factory, processInstanceId, workItemId);
			if (item == null) {
				Log.WORKFLOW.error("Can't get workitem " + workItemId);
				throw WorkflowExceptionType.WF_CANNOT_GET_WORKITEM.createException(workItemId);
			}

			facade.checkPrivilegesOnWorkItem(item, userCtx, handle);

			// NdPaolo: WHY?! you should not change suspended processes!
			facade.resumeWorkItemIfSuspended(handle, userCtx, factory, item);

			final Entry<WMEntity, CmdbuildProcessInfo> procInfo = facade.getProcessInfo(handle, factory, item);
			final CmdbuildActivityInfo actInfo = facade.getActivityInfo(handle, factory, item, procInfo.getKey())
					.getValue();
			final List<ActivityVariable> variables = getVariables(handle, userCtx, factory, item);
			final int cardId = facade.getCmdbuildCardId(handle, userCtx, factory, item);
			final ActivityDO activity = new ActivityDO(procInfo.getValue(), actInfo, variables,
					item.getProcessInstanceId(), item.getId(), cardId, true);

			final ICard card = userCtx.tables().get(procInfo.getValue().getCmdbuildBindedClass()).cards().get(cardId);
			updateWFCard(params, card, actInfo);

			if (!this.params.isEmpty()) {
				activity.updateVariables(params);
				facade.updateWorkItemValues(handle, userCtx, factory, item, activity.variables);
			}
			if (complete) {
				facade.checkIfRequiredVariablesCompleted(activity.variables);
				facade.setWorkItemToRunning(handle, userCtx, factory, item);
				facade.completeWorkItem(handle, userCtx, factory, item);
			}
			return true;
		}

		protected abstract List<ActivityVariable> getVariables(WMSessionHandle handle, UserContext userCtx,
				SharkWSFactory factory, WMWorkItem item);

		/*
		 * when called on save only, advance process was not updating the notes
		 */
		private void updateWFCard(final Map<String, String> params, final ICard card,
				final CmdbuildActivityInfo activity) {
			// save attributes in cmdbuild also
			for (final IAttribute attribute : card.getSchema().getAttributes().values()) {
				if (!attribute.isDisplayable())
					continue;
				final String attrName = attribute.getName();
				final String attrNewValue = params.get(attrName);
				if (null != attrNewValue) {
					card.getAttributeValue(attrName).setValue(attrNewValue);
				}
			}
			card.getAttributeValue(ICard.CardAttributes.Code.toString()).setValue(activity.getActivityName());
			if (complete) {
				card.forceSave();
			} else {
				card.save();
			}
		}
	}

	public ActivityDO startActivityTemplate(final String cmdbuildBindedClass) {
		ActivityDO out = null;
		final Entry<WMEntity, CmdbuildProcessInfo> proc = WorkflowCache.getInstance().getProcessInfoFromBindedClass(
				cmdbuildBindedClass);

		final String actId = proc.getValue().getInitialActivityIdFor(userCtx);
		if (actId == null) {
			throw WorkflowExceptionType.WF_CANNOT_START.createException();
		}
		final Entry<WMEntity, CmdbuildActivityInfo> act = WorkflowCache.getInstance().getActivityInfo(actId,
				proc.getKey());

		final CmdbuildActivityInfo actInfo = act.getValue();
		final ITable schema = UserContext.systemContext().tables().get(cmdbuildBindedClass);

		final List<ActivityVariable> emptyVars = actInfo.getVariableInstances(schema);

		out = new ActivityDO(proc.getValue(), actInfo, emptyVars, WorkflowConstants.ProcessToStartId, null, -1, true);
		out.setPerformer(actInfo.getParticipantIdOrExpression());
		out.configureExtendedAttributes(null, userCtx, null, null);

		return out;
	}

	/**
	 * Start the latest version of the process associated with the given
	 * cmdbuildbindedClass
	 * 
	 * @param cmdbuildBindedClass
	 * @return
	 * @throws Exception
	 */
	public ActivityDO startProcess(final String cmdbuildBindedClass) {
		final WorkflowOperation<ActivityDO> operation = new WorkflowOperation<ActivityDO>() {
			@Override
			public ActivityDO execute(final WMSessionHandle handle, final SharkWSFactory factory,
					final UserContext userCtx) throws Exception {

				final String procInstId = facade.createAndStartProcess(handle, userCtx, factory, cmdbuildBindedClass);
				final List<WMWorkItem> items = facade.getAllWorkItems(handle, userCtx, factory, procInstId);
				final WMWorkItem item = facade.filterForCurrentGroup(handle, factory, userCtx, procInstId,
						cmdbuildBindedClass, items);

				final List<ActivityVariable> variables = facade.getWorkItemVariables(handle, userCtx, factory, item);
				final Entry<WMEntity, CmdbuildProcessInfo> procInfo = facade.getProcessInfo(handle, factory, item);
				final Entry<WMEntity, CmdbuildActivityInfo> actInfo = facade.getActivityInfo(handle, factory, item,
						procInfo.getKey());

				final ActivityDO out = new ActivityDO(procInfo.getValue(), actInfo.getValue(), variables, procInstId,
						item.getId(), facade.getCmdbuildCardId(handle, userCtx, factory, item), facade.isEditable(
								userCtx, item, handle));
				out.configureExtendedAttributes(handle, userCtx, factory, item);
				return out;
			}
		};

		return execute(operation);
	}

	public ActivityDO getActivityList(final ICard card) {
		final List<ICard> cardList = new LinkedList<ICard>();
		cardList.add(card);
		final List<ActivityDO> activityList = getActivityList(card.getSchema().getName(), cardList, false);
		try {
			return activityList.iterator().next();
		} catch (final NoSuchElementException e) {
			throw WorkflowExceptionType.WF_CANNOT_GET_WORKITEM.createException();
		}
	}

	public Map<Integer, ActivityDO> getActivityMap(final ITable table, final List<ICard> cards) {
		// The web service does not know what flow status was requested
		return getActivityMap(table, cards, null);
	}

	public Map<Integer, ActivityDO> getActivityMap(final ITable table, final List<ICard> cards, final String flowStatus) {
		final Map<Integer, ActivityDO> activityMap = new HashMap<Integer, ActivityDO>();

		if (table.isActivity()) {
			List<ActivityDO> acts;
			if (flowStatus == null || flowStatus.startsWith(WorkflowConstants.StateOpen)
					|| WorkflowConstants.AllState.equals(flowStatus)) {
				acts = getActivityList(table.getName(), cards, false);
			} else {
				acts = new ArrayList<ActivityDO>();
			}

			for (final ActivityDO activity : acts) {
				if (activity == null) {
					Log.WORKFLOW.warn("a process was not found!");
				} else {
					activityMap.put(activity.getCmdbuildCardId(), activity);
				}
			}
		}

		return activityMap;
	}

	public List<ActivityDO> getActivityList(final String className, final List<ICard> cmdbuildCards,
			final boolean onlyExecutables) {
		final WorkflowOperation<List<ActivityDO>> operation = new WorkflowOperation<List<ActivityDO>>() {

			@Override
			public List<ActivityDO> execute(final WMSessionHandle handle, final SharkWSFactory factory,
					final UserContext userCtx) throws Exception {

				final List<ActivityDO> out = new ArrayList<ActivityDO>(cmdbuildCards.size());
				for (int i = 0; i < cmdbuildCards.size(); i++) {
					out.add(null);
				}

				final List<String> procInstIds = new ArrayList<String>(cmdbuildCards.size());
				for (final ICard crd : cmdbuildCards) {
					procInstIds.add((String) crd.getValue("ProcessCode"));
				}

				final WorkItemQuery query = new WorkItemQuery();
				try {
					query.setPackageId(WorkflowCache.getInstance().getProcessInfoFromBindedClass(className).getKey()
							.getPkgId());
				} catch (final CMDBWorkflowException e) {
					// TODO: quick fix not to crash for activity superclasses
					// (should be handled differently)
					if (e.getExceptionType() != WorkflowExceptionType.WF_PROCESSINFO_NOT_FOUND)
						throw e;
				}
				query.setSharkFacade(facade);
				query.setProcInstIds(procInstIds);
				final WMWorkItem[] items = query.filter(handle, userCtx, factory);

				for (final WMWorkItem item : items) {
					final int cmdbId = facade.getCmdbuildCardId(handle, userCtx, factory, item);
					ICard crd = null;
					for (final ICard card : cmdbuildCards) {
						if (card.getId() == cmdbId) {
							Log.WORKFLOW.debug("found card for " + cmdbId);
							crd = card;
							break;
						}
					}
					final Entry<WMEntity, CmdbuildProcessInfo> procInfo = facade.getProcessInfo(handle, factory, item);
					final Entry<WMEntity, CmdbuildActivityInfo> actInfo = facade.getActivityInfo(handle, factory, item,
							procInfo.getKey());

					if (crd == null) {
						Log.WORKFLOW.error("Card not found for " + cmdbId);
						continue;
					}
					if (actInfo == null) {
						Log.WORKFLOW.error("ActivityInfo not found for " + item.getActivityDefinitionId());
						continue;
					}
					final int index = cmdbuildCards.indexOf(crd);

					final List<ActivityVariable> variables = actInfo.getValue().getVariableInstances(crd.getSchema());

					// NdPaolo: I don't know what this piece of code did, but it
					// does not work! (value is always null!)
					// Awaiting large worklow refactoring
					// for (ActivityVariable av : variables) {
					// String name = av.getName();
					// Object value = av.getValue();
					// crd.setValue(name, value);
					// }

					final ActivityDO activity = new ActivityDO(procInfo.getValue(), actInfo.getValue(), variables,
							item.getProcessInstanceId(), item.getId(), cmdbId, facade.isEditable(userCtx, item, handle));
					activity.setCmdbuildCardNotes(crd.getNotes());
					activity.setCmdbuildClassId(crd.getIdClass());
					activity.configureExtendedAttributes(handle, userCtx, factory, item);
					activity.setPerformer(facade.getActivityParticipant(handle, userCtx, factory, item));

					out.set(index, activity);
				}
				for (int i = 0; i < cmdbuildCards.size(); i++) {
					if (null == out.get(i)) {
						final ICard crd = cmdbuildCards.get(i);
						Log.WORKFLOW.warn("the process " + crd.getValue("ProcessCode") + " in card " + crd.getId()
								+ " was not found in Shark!");
					}
				}
				return out;
			}
		};
		return execute(operation);
	}

	public boolean updateActivity(final String processInstanceId, final String workItemId,
			final Map<String, String> params, final boolean complete) {
		final WorkflowOperation<Boolean> operation = new AbstractUpdateActivityOperation(processInstanceId, workItemId,
				params, complete) {
			@Override
			protected List<ActivityVariable> getVariables(final WMSessionHandle handle, final UserContext userCtx,
					final SharkWSFactory factory, final WMWorkItem item) {
				return facade.getWorkItemVariables(handle, userCtx, factory, item);
			}
		};
		return execute(operation);
	}

	public boolean resumeProcess(final String processInstanceId) {
		final WorkflowOperation<Boolean> operation = new WorkflowOperation<Boolean>() {
			@Override
			public Boolean execute(final WMSessionHandle handle, final SharkWSFactory factory, final UserContext userCtx)
					throws Exception {
				facade.resumeProcess(handle, userCtx, factory, processInstanceId);
				return true;
			}
		};
		return execute(operation);
	}

}
