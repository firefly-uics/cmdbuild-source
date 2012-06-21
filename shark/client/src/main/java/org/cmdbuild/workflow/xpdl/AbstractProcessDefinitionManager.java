package org.cmdbuild.workflow.xpdl;

import java.io.IOException;
import java.util.List;

import javax.activation.DataSource;
import javax.mail.util.ByteArrayDataSource;

import org.apache.commons.io.IOUtils;
import org.cmdbuild.common.annotations.Legacy;
import org.cmdbuild.workflow.ActivityPerformer;
import org.cmdbuild.workflow.CMActivity;
import org.cmdbuild.workflow.CMProcessClass;
import org.cmdbuild.workflow.CMProcessInstance;
import org.cmdbuild.workflow.CMWorkflowException;
import org.cmdbuild.workflow.ProcessDefinitionManager;

/**
 * Process Definition Manager that uses XPDL definitions.
 */
public abstract class AbstractProcessDefinitionManager implements ProcessDefinitionManager {

	private final ProcessDefinitionStore store;

	public AbstractProcessDefinitionManager(final ProcessDefinitionStore store) {
		this.store = store;
	}

	@Override
	public final String[] getVersions(final CMProcessClass process) throws CMWorkflowException {
		return store.getPackageVersions(process.getName());
	}

	@Override
	public DataSource getDefinition(final CMProcessClass process, final String version) throws CMWorkflowException {
		final byte[] pkgDef = store.downloadPackage(process.getName(), version);
		final ByteArrayDataSource ds = new ByteArrayDataSource(pkgDef, getMimeType());
		ds.setName(String.format("%s_%s.%s", process.getName(), version, getFileExtension()));
		return ds;
	}

	@Override
	public void updateDefinition(CMProcessClass process, DataSource pkgDefData) throws CMWorkflowException {
		try {
			byte[] binaryData = IOUtils.toByteArray(pkgDefData.getInputStream());
			synchronized (this) {
				store.uploadPackage(process.getName(), binaryData);
			}
		} catch (IOException e) {
			throw new CMWorkflowException(e);
		}		
	}

	@Override
	public final CMActivity getStartActivity(final CMProcessClass process, final String groupName) throws CMWorkflowException {
		final List<CMActivity> startActivities = store.getStartActivities(process.getName());
		if (groupName == null) {
			return getStartActivityForAdmin(startActivities);
		} else {
			return getStartActivityForNonAdmin(startActivities, groupName);
		}
	}

	private CMActivity getStartActivityForNonAdmin(final List<CMActivity> startActivities, final String groupName) {
		for (CMActivity a : startActivities) {
			for (ActivityPerformer p : a.getPerformers()) {
				if (p.isRole(groupName)) {
					return a;
				}
			}
		}
		return null;
	}

	private CMActivity getStartActivityForAdmin(final List<CMActivity> startActivities) {
		if (startActivities.size() == 1) {
			return startActivities.get(0);
		} else {
			for (CMActivity a : startActivities) {
				for (ActivityPerformer p : a.getPerformers()) {
					if (p.isAdmin()) {
						return a;
					}
				}
			}
			return null;
		}
	}

	@Override
	public CMActivity getActivity(final CMProcessInstance processInstance, final String activityInstanceId) throws CMWorkflowException {
		return store.getActivity(processInstance.getUniqueProcessDefinition(), activityInstanceId);
	}

	public final String getPackageId(final CMProcessClass process) throws CMWorkflowException {
		return store.getPackageId(process.getName());
	}

	@Legacy("As in 1.x")
	protected final String getStandardPackageId(final CMProcessClass process) {
		return "Package_" + process.getName().toLowerCase();
	}

	public final String getProcessDefinitionId(final CMProcessClass process) throws CMWorkflowException {
		return store.getProcessDefinitionId(process.getName());
	}

	@Legacy("As in 1.x")
	protected final String getStandardProcessDefinitionId(final CMProcessClass process) {
		return "Process_" + process.getName().toLowerCase();
	}

	protected abstract String getMimeType();
	protected abstract String getFileExtension();

}
