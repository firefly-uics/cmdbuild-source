package org.cmdbuild.workflow.xpdl;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.activation.DataSource;
import javax.mail.util.ByteArrayDataSource;

import org.apache.commons.io.IOUtils;
import org.cmdbuild.common.annotations.Legacy;
import org.cmdbuild.workflow.ActivityPerformer;
import org.cmdbuild.workflow.CMActivity;
import org.cmdbuild.workflow.CMProcessClass;
import org.cmdbuild.workflow.CMWorkflowException;
import org.cmdbuild.workflow.ProcessDefinitionManager;
import org.cmdbuild.workflow.service.CMWorkflowService;

/**
 * Process Definition Manager that uses XPDL definitions.
 */
public abstract class AbstractProcessDefinitionManager implements ProcessDefinitionManager {

	/**
	 * Process information for caching.
	 */
	protected static class ProcessInfo {
		String packageId;
		String lastProcessVersionId;
		List<CMActivity> startActivities;
	}

	private volatile Map<String, ProcessInfo> processInfoLazyCacheDontUseDirectly; // = new HashMap<Object, ProcessInfo>();

	private Map<String, ProcessInfo> processInfoCache() throws CMWorkflowException {
		if (processInfoLazyCacheDontUseDirectly == null) {
			synchronized (this) {
				if (processInfoLazyCacheDontUseDirectly == null) {
					processInfoLazyCacheDontUseDirectly = AbstractProcessDefinitionManager.this.fetchProcessDefinitionInfo();
				}
			}
		}
		return processInfoLazyCacheDontUseDirectly;
	}

	private final CMWorkflowService workflowService;

	public AbstractProcessDefinitionManager(final CMWorkflowService workflowService) {
		this.workflowService = workflowService;
	}

	@Override
	public final String[] getVersions(final CMProcessClass process) throws CMWorkflowException {
		return workflowService.getPackageVersions(getPackageId(process));
	}

	@Override
	public DataSource getDefinition(final CMProcessClass process, final String version) throws CMWorkflowException {
		final byte[] pkgDef = workflowService.downloadPackage(getPackageId(process), version);
		final ByteArrayDataSource ds = new ByteArrayDataSource(pkgDef, getMimeType());
		ds.setName(String.format("%s_%s.%s", process.getName(), version, getFileExtension()));
		return ds;
	}

	@Override
	public void updateDefinition(CMProcessClass process, DataSource pkgDefData) throws CMWorkflowException {
		try {
			byte[] binaryData = IOUtils.toByteArray(pkgDefData.getInputStream());
			synchronized (this) {
				workflowService.uploadPackage(getPackageId(process), binaryData);
				addPackage(binaryData, processInfoCache()); // TODO Test cache updated
			}
		} catch (IOException e) {
			throw new CMWorkflowException(e);
		}		
	}

	@Override
	public final CMActivity getStartActivity(final CMProcessClass process, final String groupName) throws CMWorkflowException {
		ProcessInfo pi = processInfoCache().get(process.getName());
		if (groupName == null) {
			return getStartActivityForAdmin(pi);
		} else {
			return getStartActivityForNonAdmin(pi, groupName);
		}
	}

	private CMActivity getStartActivityForNonAdmin(ProcessInfo pi, final String groupName) {
		for (CMActivity a : pi.startActivities) {
			for (ActivityPerformer p : a.getPerformers()) {
				if (p.isRole(groupName)) {
					return a;
				}
			}
		}
		return null;
	}

	private CMActivity getStartActivityForAdmin(ProcessInfo pi) {
		if (pi.startActivities.size() == 1) {
			return pi.startActivities.get(0);
		} else {
			for (CMActivity a : pi.startActivities) {
				for (ActivityPerformer p : a.getPerformers()) {
					if (p.isAdmin()) {
						return a;
					}
				}
			}
			return null;
		}
	}

	private Map<String, ProcessInfo> fetchProcessDefinitionInfo() throws CMWorkflowException {
		final Map<String, ProcessInfo> out = new HashMap<String, ProcessInfo>();
		for (byte[] pkgDef : workflowService.downloadAllPackages()) {
			addPackage(pkgDef, out);
		}
		return out;
	}

	protected abstract void addPackage(byte[] pkgDef, Map<String, ProcessInfo> processInfoMap);

	@Legacy("As in 1.x")
	protected final String getPackageId(final CMProcessClass process) {
		return "Package_" + process.getName().toLowerCase();
	}

	@Legacy("As in 1.x")
	protected final String getProcessId(final CMProcessClass process) {
		return "Process_" + process.getName().toLowerCase();
	}

	protected abstract String getMimeType();
	protected abstract String getFileExtension();

}
