package org.cmdbuild.legacy.dms;

import org.alfresco.webservice.types.ResultSetRow;
import org.alfresco.webservice.types.NamedValue;
import org.alfresco.webservice.util.Constants;

public enum AlfrescoConstant {
	
	NAME(Constants.PROP_NAME){
		@Override
		public void setInBean(AttachmentBean bean, NamedValue nv, AlfrescoConnectionFacade af) {
			bean.setName(nv.getValue());
		}
	},
	CREATED(Constants.PROP_CREATED){
		@Override
		public void setInBean(AttachmentBean bean, NamedValue nv, AlfrescoConnectionFacade af) {
			bean.setCreated(DateUtils.parse(nv.getValue()));
		}
	},
	DESCR(Constants.PROP_DESCRIPTION){
		@Override
		public void setInBean(AttachmentBean bean, NamedValue nv, AlfrescoConnectionFacade af) {
			bean.setDescription(nv.getValue());
		}
	},
	MODIFIED("{http://www.alfresco.org/model/content/1.0}modified"){
		@Override
		public void setInBean(AttachmentBean bean, NamedValue nv, AlfrescoConnectionFacade af) {
			bean.setModified(DateUtils.parse(nv.getValue()));
		}
	},
	CATEGORIES("{http://www.alfresco.org/model/content/1.0}categories"){
		@Override
		public void setInBean(AttachmentBean bean, NamedValue nv, AlfrescoConnectionFacade af) {
			String[] paths = nv.getValues();
			String strip = "workspace://SpacesStore/";
			for(String path : paths){
				int idx = path.indexOf(strip);
				path = path.substring(idx + strip.length());

				if( af.getWs().isCMDBCategory(path) ){
					ResultSetRow row = af.getWs().searchRow(path, true);
					if(row != null){
						NamedValue[] nvs = row.getColumns();
						for(NamedValue nvb : nvs){
							if(NAME.isName(nvb.getName())){
								bean.setCategory(nvb.getValue());
							}
						}
					}
				}
			}
		}
	},
	VERSION("{http://www.alfresco.org/model/content/1.0}versionLabel"){
		@Override
		public void setInBean(AttachmentBean bean, NamedValue nv, AlfrescoConnectionFacade af) {
			bean.setVersion(nv.getValue());
		}
	},
	PATH("{http://www.alfresco.org/model/content/1.0}path"){
		@Override
		public void setInBean(AttachmentBean bean, NamedValue nv, AlfrescoConnectionFacade af) {
			bean.setPath(nv.getValue());
		}
	},
	AUTHOR("{http://www.alfresco.org/model/content/1.0}author"){
		@Override
		public void setInBean(AttachmentBean bean, NamedValue nv, AlfrescoConnectionFacade af) {
			bean.setAuthor(nv.getValue());
		}
	},
	UUID("{http://www.alfresco.org/model/system/1.0}node-uuid"){
		@Override
		public void setInBean(AttachmentBean bean, NamedValue nv, AlfrescoConnectionFacade af) {
			bean.setUuid(nv.getValue());
		}
	};
	
	String name;
	private AlfrescoConstant(String name){
		this.name = name;
	}
	
	public boolean isName(String name){
		return this.name.equals(name);
	}
	
	public static AlfrescoConstant fromName(String name){
		for(AlfrescoConstant ac : AlfrescoConstant.values()){
			if(ac.isName(name)) return ac;
		}
		return null;
	}
	
	public String getName(){
		return name;
	}
	
	public String getValue(NamedValue nv){
		return nv.getValue();
	}
	public String[] getValues(NamedValue nv){
		return nv.getValues();
	}
	
	public abstract void setInBean(AttachmentBean bean, NamedValue nv, AlfrescoConnectionFacade af);
	
}

