package org.cmdbuild.elements;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import org.cmdbuild.elements.interfaces.IAttribute;
import org.cmdbuild.elements.interfaces.ITable;
import org.cmdbuild.elements.interfaces.ProcessFactory;
import org.cmdbuild.elements.interfaces.ProcessType;
import org.cmdbuild.elements.proxy.TableProxy;
import org.cmdbuild.elements.wrappers.GroupCard;
import org.cmdbuild.exception.CMDBWorkflowException;
import org.cmdbuild.exception.CMDBWorkflowException.WorkflowExceptionType;
import org.cmdbuild.services.auth.UserContext;
import org.cmdbuild.workflow.utils.SimpleXMLDoc;
import org.cmdbuild.workflow.CmdbuildProcessInfo;
import org.cmdbuild.workflow.WorkflowCache;
import org.cmdbuild.workflow.CmdbuildProcessInfo.PerformerType;
import org.cmdbuild.workflow.operation.ActivityDO;
import org.cmdbuild.workflow.operation.SharkFacade;
import org.cmdbuild.workflow.xpdl.XPDLAttribute;
import org.cmdbuild.workflow.xpdl.XPDLPackageDescriptor;
import org.cmdbuild.workflow.xpdl.XPDLPackageEncoder;
import org.cmdbuild.workflow.xpdl.XPDLParticipant;
import org.enhydra.shark.api.client.wfservice.WMEntity;

public class ProcessTypeImpl extends TableProxy implements ProcessType {

	private class XPDLManagerImpl implements XPDLManager {
		
		public SimpleXMLDoc template(String[] users, String[] roles) {
			checkNotSuperclass();
			//just include all roles if no role is passed
			if (roles == null) {
				List<String> tmp = new LinkedList<String>();
				for(GroupCard role : GroupCard.allActive()) {
					tmp.add(role.getName());
				}
				roles = tmp.toArray(new String[]{});
			}
			
			List<XPDLParticipant> participants = new ArrayList<XPDLParticipant>();
			if (users != null){
				for (String user : users) {
					participants.add(new XPDLParticipant(user, PerformerType.HUMAN));
				}
			}
			if (roles != null){
				for (String role : roles) {
					participants.add(new XPDLParticipant(role, PerformerType.ROLE));
				}
			}

			List<XPDLAttribute> attributes = new LinkedList<XPDLAttribute>();
			for (IAttribute attr : ProcessTypeImpl.this.getAttributes().values() ) {
				if(Mode.RESERVED != attr.getMode() && Mode.NOACTIVE != attr.getMode()){
					attributes.add( new XPDLAttribute(attr) );
				}
			}
			
			XPDLPackageDescriptor descriptor = new XPDLPackageDescriptor();
			descriptor.setProcessClassName(ProcessTypeImpl.this.getName());
			descriptor.setUserStoppable(false);
			descriptor.setParticipants(participants);
			descriptor.setAttributes(attributes);
			return XPDLPackageEncoder.getInstance().create(descriptor);
		}

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

	public Integer[] getPackageVersions() {
		String className = getName();
		return WorkflowCache.getInstance().getPackageVersionsForClass(className);
	}

	public ActivityDO startActivityTemplate() {
		ActivityDO template = sharkFacade.startActivityTemplate(getName());
		template.setCmdbuildClassId(getId());
		return template;
	}
}
