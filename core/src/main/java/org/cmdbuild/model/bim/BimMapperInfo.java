package org.cmdbuild.model.bim;

import org.cmdbuild.data.store.Store.Storable;

public class BimMapperInfo implements Storable {

	private String className; 
	private boolean active,bimRoot;
	
	public BimMapperInfo(String className) {
		this.className = className;
	}

	@Override
	public String getIdentifier() {
		return getClassName();
	}
	
	public String getClassName() {
		return className;
	}
	
	public void setClassName(String className) {
		this.className = className;
	}

	public boolean isBimRoot() {
		return bimRoot;
	}

	public void setBimRoot(boolean bimRoot) {
		this.bimRoot = bimRoot;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean enabledBim) {
		this.active = enabledBim;
	}

}
