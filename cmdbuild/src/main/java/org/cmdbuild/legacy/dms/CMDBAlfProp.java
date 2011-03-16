package org.cmdbuild.legacy.dms;

import org.alfresco.webservice.types.NamedValue;

public enum CMDBAlfProp {
	ID("{it.cmdbuild.alfresco}objid"){
		@Override
		public void setInMeta(CMDBAlfrescoMeta meta, NamedValue nv) {
			try{
				int id = Integer.parseInt(nv.getValue());
				meta.setObjid(id);
			}catch(Exception e){meta.setObjid(-1);}
		}
	},
	CLASSNAME("{it.cmdbuild.alfresco}classname"){
		@Override
		public void setInMeta(CMDBAlfrescoMeta meta, NamedValue nv) {
			meta.setClassname(nv.getValue());
		}
	},
	CODE("{it.cmdbuild.alfresco}code"){
		@Override
		public void setInMeta(CMDBAlfrescoMeta meta, NamedValue nv) {
			meta.setCode(nv.getValue());
		}
	},
	DESCR("{it.cmdbuild.alfresco}description"){
		@Override
		public void setInMeta(CMDBAlfrescoMeta meta, NamedValue nv) {
			meta.setDescription(nv.getValue());
		}
	},
	NOTES("{it.cmdbuild.alfresco}notes"){
		@Override
		public void setInMeta(CMDBAlfrescoMeta meta, NamedValue nv) {
			meta.setNotes(nv.getValue());
		}
	}
	;
	
	private String name;
	private CMDBAlfProp(String name){
		this.name=name;
	}
	public boolean isProp(NamedValue nv){return this.name.equals(nv.getName());}
	public abstract void setInMeta(CMDBAlfrescoMeta meta, NamedValue nv);
	
	public static CMDBAlfProp fromNV(NamedValue nv){
		for(CMDBAlfProp cap : CMDBAlfProp.values()){
			if(cap.isProp(nv)) return cap;
		}
		return null;
	}
}