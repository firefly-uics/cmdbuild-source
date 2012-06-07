package org.cmdbuild.workflow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.cmdbuild.exception.CMDBWorkflowException.WorkflowExceptionType;
import org.cmdbuild.logger.Log;
import org.cmdbuild.services.WorkflowService;
import org.cmdbuild.services.auth.UserContext;
import org.enhydra.shark.api.client.wfmc.wapi.WMSessionHandle;
import org.enhydra.shark.api.client.wfservice.WMEntity;
import org.enhydra.shark.client.utilities.SharkWSFactory;
import org.enhydra.shark.utilities.WMEntityUtilities;

public class WorkflowCache {

	Map<WMEntity, CmdbuildProcessInfo> processes;
	Map<WMEntity, CmdbuildActivityInfo> activities;
	Set<String> bindedClasses;

	Set<WMEntity> loadedProcesses;

	private static volatile WorkflowCache instance = null;
	private static Object instanceSyncObject = new Object();

	public static WorkflowCache getInstance() {
		if (instance == null) {
			synchronized (instanceSyncObject) {
				if (instance == null) {
					instance = create();
				}
			}
		}
		return instance;
	}

	private static WorkflowCache create() {
		try {
			return new WorkflowCache();
		} catch (Exception e) {
			throw WorkflowExceptionType.WF_WAPI_CONNECTION_ERROR.createException();
		}
	}

	public static void reload() {
		synchronized (instanceSyncObject) {
			instance = create();
		}
	}

	private WorkflowCache() {
		configure();
	}

	public void configure() {
		WorkflowService service = WorkflowService.getInstance();
		activities = new HashMap<WMEntity, CmdbuildActivityInfo>();
		bindedClasses = new HashSet<String>();
		loadedProcesses = new HashSet<WMEntity>();
		try {
			processes = service.executeAdmin(new GetAllProcesses());
		} catch (Exception e) {
			throw WorkflowExceptionType.WF_CANNOT_LOAD_PROCESSES.createException();
		}
		for (CmdbuildProcessInfo procInfo : processes.values()) {
			bindedClasses.add(procInfo.getCmdbuildBindedClass());
		}
	}

	private class GetAllProcesses implements WorkflowService.WorkflowOperation<Map<WMEntity, CmdbuildProcessInfo>> {
		@SuppressWarnings("unchecked")
		public Map<WMEntity, CmdbuildProcessInfo> execute(WMSessionHandle handle, SharkWSFactory factory,
				UserContext userCtx) throws Exception {
			Map<WMEntity, CmdbuildProcessInfo> out = new HashMap();

			String[] packIds = factory.getPackageAdministration().getOpenedPackageIds(handle);
			for (String packId : packIds) {
				String[] versions = factory.getPackageAdministration().getPackageVersions(handle, packId);
				for (String version : versions) {
					WMEntity packEnt = factory.getPackageAdministration().getPackageEntity(handle, packId, version);

					for (WMEntity procEnt : WMEntityUtilities.getAllWorkflowProcesses(handle, factory.getXPDLBrowser(),
							packEnt)) {
						CmdbuildProcessInfo procInfo = new CmdbuildProcessInfo(procEnt, handle, factory);
						Log.WORKFLOW.debug("cache process info  -  class: " + procInfo.cmdbuildBindedClass
								+ ", pack.id: " + packEnt.getId() + ", pack.ver: " + version + ", id: "
								+ procEnt.getId());
						out.put(procEnt, procInfo);
					}
				}
			}
			return out;
		}
	}

	private class WMEntProc {
		WMEntity procEntity;
		CmdbuildProcessInfo procInfo;
	}

	private class GetActivities implements WorkflowService.WorkflowOperation<Map<WMEntity, CmdbuildActivityInfo>> {
		WMEntProc wmEntProc;

		public Map<WMEntity, CmdbuildActivityInfo> execute(WMSessionHandle handle, SharkWSFactory factory,
				UserContext userCtx) throws Exception {
			Map<WMEntity, CmdbuildActivityInfo> out = new HashMap<WMEntity, CmdbuildActivityInfo>();
			for (WMEntity act : WMEntityUtilities.getAllActivities(handle, factory.getXPDLBrowser(),
					wmEntProc.procEntity)) {
				putActivity(wmEntProc.procInfo, act, handle, factory, out);
			}

			for (WMEntity actSet : WMEntityUtilities.getAllActivitySets(handle, factory.getXPDLBrowser(),
					wmEntProc.procEntity)) {
				for (WMEntity act : WMEntityUtilities.getAllActivities(handle, factory.getXPDLBrowser(), actSet)) {
					putActivity(wmEntProc.procInfo, act, handle, factory, out);
				}
			}
			return out;
		}

		private void putActivity(CmdbuildProcessInfo procInfo, WMEntity act, WMSessionHandle handle,
				SharkWSFactory factory, Map<WMEntity, CmdbuildActivityInfo> out) throws Exception {
			out.put(act, new CmdbuildActivityInfo(procInfo, act, handle, factory));
		}
	}

	public Set<String> getBindedClasses() {
		return bindedClasses;
	}

	public Entry<WMEntity, CmdbuildProcessInfo> getProcessInfoFromBindedClass(String cmdbuildBindedClass) {
		WMEntity procDef = null;
		for (Entry<WMEntity, CmdbuildProcessInfo> entry : this.processes.entrySet()) {
			if (entry.getValue().cmdbuildBindedClass.equals(cmdbuildBindedClass)) {
				procDef = entry.getKey();
				break;
			}
		}
		if (procDef != null) {
			return getProcessInfo(procDef.getPkgId(), procDef.getId());
		}
		throw WorkflowExceptionType.WF_PROCESSINFO_NOT_FOUND.createException();
	}

	public Entry<WMEntity, CmdbuildProcessInfo> getProcessInfo(String cmdbuildClass, int version) {

		for (Entry<WMEntity, CmdbuildProcessInfo> entry : this.processes.entrySet()) {
			if (entry.getValue().cmdbuildBindedClass.equals(cmdbuildClass)
					&& Integer.parseInt(entry.getValue().getPackageVersion()) == version) {
				return entry;
			}
		}
		throw WorkflowExceptionType.WF_CANNOT_FIND_XPDL.createException(cmdbuildClass, String.valueOf(version));
	}

	public Integer[] getPackageVersionsForClass(String cmdbuildClassName) {

		List<Integer> tmp = new ArrayList<Integer>();
		for (CmdbuildProcessInfo procInfo : this.processes.values()) {
			if (procInfo.cmdbuildBindedClass.equals(cmdbuildClassName)) {
				tmp.add(Integer.parseInt(procInfo.getPackageVersion()));
			}
		}
		return tmp.toArray(new Integer[] {});
	}

	public boolean hasProcessClass(String cmdbuildBindedClass) {
		for (Entry<WMEntity, CmdbuildProcessInfo> entry : this.processes.entrySet()) {
			if (entry.getValue().cmdbuildBindedClass.equals(cmdbuildBindedClass)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Get latest version of process
	 * 
	 * @param packId
	 * @param procId
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public Entry<WMEntity, CmdbuildProcessInfo> getProcessInfo(String packId, String procId) {
		int max = -1;
		Map<Integer, Entry<WMEntity, CmdbuildProcessInfo>> versions = new HashMap();
		for (Entry<WMEntity, CmdbuildProcessInfo> entry : this.processes.entrySet()) {
			WMEntity proc = entry.getKey();
			if (proc.getPkgId().equals(packId) && proc.getId().equals(procId)) {
				int cur = Integer.parseInt(proc.getPkgVer());
				if (max < cur) {
					max = cur;
				}
				versions.put(cur, entry);
			}
		}
		if (versions.size() == 1) {
			// return the only entry
			return versions.values().iterator().next(); // MA!!
		} else {
			return versions.get(max);
		}
	}

	public Entry<WMEntity, CmdbuildProcessInfo> getProcessInfo(String packId, String version, String procId) {
		for (Entry<WMEntity, CmdbuildProcessInfo> entry : this.processes.entrySet()) {
			WMEntity proc = entry.getKey();
			if (proc.getPkgId().equals(packId) && proc.getPkgVer().equals(version) && proc.getId().equals(procId)) {
				return entry;
			}
		}
		return null;
	}

	/**
	 * input string must be in the format:
	 * {packageId}#{packageVersion}#{processId}
	 * 
	 * @param mngrName
	 * @return
	 */
	public Entry<WMEntity, CmdbuildProcessInfo> getProcessInfo(String mngrName) {
		String[] toks = mngrName.split("#");
		if (toks.length != 3) {
			return null;
		}
		return getProcessInfo(toks[0], toks[1], toks[2]);
	}

	public Entry<WMEntity, CmdbuildActivityInfo> getActivityInfo(String actId, WMEntity proc) {
		loadActivitiesIfNotLoaded(proc);
		for (Entry<WMEntity, CmdbuildActivityInfo> entry : this.activities.entrySet()) {
			WMEntity act = entry.getKey();
			if (proc.getPkgId().equals(act.getPkgId()) && proc.getPkgVer().equals(act.getPkgVer())
					&& proc.getId().equals(act.getWpId()) && act.getActId().equals(actId)) {
				return entry;
			}
		}

		return null;
	}

	private void loadActivitiesIfNotLoaded(WMEntity proc) {
		if (loadedProcesses.contains(proc)) {
			Log.WORKFLOW.debug("activities of process " + proc.getId() + " already loaded");
			return;
		}
		Log.WORKFLOW.debug("lazy load activities of process: " + proc.getId());
		WMEntProc wmEntProc = new WMEntProc();
		wmEntProc.procEntity = proc;
		wmEntProc.procInfo = this.processes.get(proc);
		GetActivities op = new GetActivities();
		op.wmEntProc = wmEntProc;

		try {
			Map<WMEntity, CmdbuildActivityInfo> infos = WorkflowService.getInstance().executeAdmin(op);
			this.activities.putAll(infos);
			loadedProcesses.add(proc);
		} catch (Exception e) {
			Log.WORKFLOW.error("Cannot find activities for process " + proc.getId());
			Log.WORKFLOW.debug(e);
			throw WorkflowExceptionType.WF_CANNOT_LOAD_ACTIVITIES.createException(proc.getId());
		}
	}
}
