package org.cmdbuild.elements;

import java.io.InputStream;
import java.util.Map.Entry;

import org.cmdbuild.elements.interfaces.ITable;
import org.cmdbuild.elements.interfaces.ProcessFactory;
import org.cmdbuild.elements.interfaces.ProcessType;
import org.cmdbuild.elements.proxy.TableProxy;
import org.cmdbuild.exception.CMDBWorkflowException;
import org.cmdbuild.exception.CMDBWorkflowException.WorkflowExceptionType;
import org.cmdbuild.services.auth.UserContext;
import org.cmdbuild.workflow.CmdbuildProcessInfo;
import org.cmdbuild.workflow.WorkflowCache;
import org.cmdbuild.workflow.operation.ActivityDO;
import org.cmdbuild.workflow.operation.SharkFacade;
import org.enhydra.shark.api.client.wfservice.WMEntity;

public class ProcessTypeImpl extends TableProxy implements ProcessType {

	private class XPDLManagerImpl implements XPDLManager {

		private void checkNotSuperclass() {
			if (ProcessTypeImpl.this.isSuperClass()) {
				throw WorkflowExceptionType.WF_WRONG_SUPERCLASS_OPERATION.createException();
			}
		}
		
		public byte[] download(int version) {
			checkNotSuperclass();
			return sharkFacade.downloadXPDL(getName(), version);
		}

		public void upload(InputStream inputStream, boolean userStoppable) {
			checkNotSuperclass();
			try {
				sharkFacade.uploadUpdateXPDL(inputStream, getName(), userStoppable);
			} catch (CMDBWorkflowException e) {
				throw e;
			} catch (Exception e) {
				throw WorkflowExceptionType.WF_GENERIC_ERROR.createException();
			}
		}
	}

	private SharkFacade sharkFacade;
	private XPDLManager xpdlManager;

	public ProcessTypeImpl(ITable table, UserContext userCtx) {
		super(table, userCtx);
		this.sharkFacade = new SharkFacade(userCtx);
		this.xpdlManager = new XPDLManagerImpl();
	}

	public ProcessFactory cards() {
		return new ProcessFactoryImpl(this, userCtx);
	}

	public XPDLManager getXPDLManager() {
		return xpdlManager;
	}

	public CmdbuildProcessInfo getProcInfo() {
		CmdbuildProcessInfo procInfo = null;
		String className = getName();
		try {
			Entry<WMEntity,CmdbuildProcessInfo> entry = WorkflowCache.getInstance().getProcessInfoFromBindedClass(className);
			return entry.getValue();
		} catch (Exception e) {
		}
		return procInfo;
	}

	public ActivityDO startActivityTemplate() {
		ActivityDO template = sharkFacade.startActivityTemplate(getName());
		template.setCmdbuildClassId(getId());
		return template;
	}
}
