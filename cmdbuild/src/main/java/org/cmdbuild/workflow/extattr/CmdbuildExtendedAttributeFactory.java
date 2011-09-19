package org.cmdbuild.workflow.extattr;

import java.util.HashMap;
import java.util.Map;

import org.cmdbuild.config.WorkflowProperties;
import org.cmdbuild.exception.CMDBWorkflowException.WorkflowExceptionType;
import org.cmdbuild.logger.Log;

public class CmdbuildExtendedAttributeFactory {

	private static CmdbuildExtendedAttributeFactory instance = null;
	private static Object instanceSyncObject = new Object();

	public static CmdbuildExtendedAttributeFactory getInstance() {
		if (instance == null) {
			synchronized (instanceSyncObject) {
				if (instance == null) {
					instance = new CmdbuildExtendedAttributeFactory();
				}
			}
		}
		return instance;
	}

	Map<String, Class<? extends CmdbuildExtendedAttribute>> mapping;

	@SuppressWarnings("unchecked")
	private CmdbuildExtendedAttributeFactory() {
		mapping = new HashMap();
		configure();
	}

	@SuppressWarnings("unchecked")
	private void configure() {
		WorkflowProperties props = WorkflowProperties.getInstance();
		String pack = props.getExtendedAttributePackage();
		for (String clsName : props.getExtendedAttributeClasses()) {
			try {
				String fullClassName = pack + "." + clsName;
				Class<? extends CmdbuildExtendedAttribute> cls = (Class<? extends CmdbuildExtendedAttribute>) Class.forName(fullClassName);
				this.register(cls);
			} catch (ClassNotFoundException e) {
				throw WorkflowExceptionType.WF_CANNOT_CONFIGURE_CMDBEXTATTR.createException(clsName);
			}
		}
	}

	public void register(Class<? extends CmdbuildExtendedAttribute> extAttrCls) {
		try {
			String extAttrName = (extAttrCls.newInstance()).extendedAttributeName();
			Log.WORKFLOW.debug("Registering extended attribute: " + extAttrName);
			mapping.put(extAttrName, extAttrCls);
		} catch (Exception e) {
			Log.WORKFLOW.error("Failed registering extended attribute for class: " + extAttrCls.getCanonicalName());
		}
	}

	public boolean hasMapping(String name) {
		return mapping.containsKey(name);
	}

	public CmdbuildExtendedAttribute getExtAttr(String name) throws Exception {
		if (mapping.containsKey(name)) {
			return mapping.get(name).newInstance();
		}
		throw new Exception("mapped extended attribute " + name + " not found!");
	}
}
