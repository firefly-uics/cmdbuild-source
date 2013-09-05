package org.cmdbuild.model.bim;

import org.cmdbuild.data.store.Store.Storable;

public class BimLayer implements Storable {

	private String className; 
	private boolean active,root;
	
	public BimLayer(String className) {
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

	public boolean isRoot() {
		return root;
	}

	public void setRoot(boolean bimRoot) {
		this.root = bimRoot;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean enabledBim) {
		this.active = enabledBim;
	}
	

}
