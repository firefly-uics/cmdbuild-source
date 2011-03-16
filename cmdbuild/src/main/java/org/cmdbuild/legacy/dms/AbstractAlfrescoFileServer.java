package org.cmdbuild.legacy.dms;

import java.io.InputStream;

import javax.activation.DataHandler;

import org.cmdbuild.config.LegacydmsProperties;

@SuppressWarnings("restriction")
public abstract class AbstractAlfrescoFileServer {
	
	public AbstractAlfrescoFileServer(){}
	
	protected abstract boolean init(LegacydmsProperties properties, AlfrescoCredential credential);
	
	protected abstract boolean upload(String filename,InputStream is,String[] path);
	
	protected abstract boolean delete(String filename,String[] path);
	
	protected abstract DataHandler download(String filename,String[] path);
}
