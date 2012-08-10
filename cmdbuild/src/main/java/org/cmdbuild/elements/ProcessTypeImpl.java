package org.cmdbuild.elements;

import java.util.Map.Entry;

import org.cmdbuild.elements.interfaces.ITable;
import org.cmdbuild.elements.interfaces.ProcessFactory;
import org.cmdbuild.elements.interfaces.ProcessType;
import org.cmdbuild.elements.proxy.TableProxy;
import org.cmdbuild.services.auth.UserContext;
import org.cmdbuild.workflow.CmdbuildProcessInfo;
import org.cmdbuild.workflow.WorkflowCache;
import org.cmdbuild.workflow.operation.ActivityDO;
import org.cmdbuild.workflow.operation.SharkFacade;
import org.enhydra.shark.api.client.wfservice.WMEntity;

public class ProcessTypeImpl extends TableProxy implements ProcessType {

	private SharkFacade sharkFacade;

	public ProcessTypeImpl(ITable table, UserContext userCtx) {
		super(table, userCtx);
		this.sharkFacade = new SharkFacade(userCtx);
	}

	public ProcessFactory cards() {
		return new ProcessFactoryImpl(this, userCtx);
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
