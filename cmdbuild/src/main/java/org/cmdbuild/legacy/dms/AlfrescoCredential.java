package org.cmdbuild.legacy.dms;

import org.cmdbuild.config.LegacydmsProperties;

public class AlfrescoCredential {
	private String user;
	private String password;
	
	public AlfrescoCredential(){
		user = null;
		password=null;
	}
	public AlfrescoCredential(LegacydmsProperties settings){
		user = settings.getAlfrescoUser();
		password = settings.getAlfrescoPassword();
	}
	
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getUser() {
		return user;
	}
	public void setUser(String user) {
		this.user = user;
	}	
	
}
