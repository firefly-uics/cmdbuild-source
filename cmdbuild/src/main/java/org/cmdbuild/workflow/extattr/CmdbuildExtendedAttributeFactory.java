package org.cmdbuild.workflow.extattr;

import java.util.HashMap;
import java.util.Map;

import org.cmdbuild.logger.Log;

public class CmdbuildExtendedAttributeFactory {

	// Initialization on Demand Holder design pattern
	private static class InstanceHolder {
       public static CmdbuildExtendedAttributeFactory INSTANCE = new CmdbuildExtendedAttributeFactory();
    }
 
    public static CmdbuildExtendedAttributeFactory getInstance() {
        return InstanceHolder.INSTANCE;
    }

	Map<String, Class<? extends CmdbuildExtendedAttribute>> mapping;

	private CmdbuildExtendedAttributeFactory() {
		mapping = new HashMap<String, Class<? extends CmdbuildExtendedAttribute>>();
		configure();
	}

	private void configure() {
		register(org.cmdbuild.workflow.extattr.ManageRelations.class);
		register(org.cmdbuild.workflow.extattr.CreateModifyCard.class);
		register(org.cmdbuild.workflow.extattr.LinkCards.class);
		register(org.cmdbuild.workflow.extattr.OpenNote.class);
		register(org.cmdbuild.workflow.extattr.OpenAttachment.class);
		register(org.cmdbuild.workflow.extattr.CreateReport.class);
		register(org.cmdbuild.workflow.extattr.ManageEmail.class);
		register(org.cmdbuild.workflow.extattr.Calendar.class);
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
