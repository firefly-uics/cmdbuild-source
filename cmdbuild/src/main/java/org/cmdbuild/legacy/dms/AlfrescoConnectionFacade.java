package org.cmdbuild.legacy.dms;

import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.activation.DataHandler;

import org.alfresco.webservice.types.NamedValue;
import org.alfresco.webservice.types.ResultSetRow;
import org.cmdbuild.config.LegacydmsProperties;
import org.cmdbuild.elements.TableImpl;
import org.cmdbuild.elements.interfaces.ITable;
import org.cmdbuild.exception.NotFoundException;
import org.cmdbuild.logger.Log;
import org.cmdbuild.utils.tree.CTree;

@SuppressWarnings({"unchecked", "restriction"})
public class AlfrescoConnectionFacade {
	
	private AlfrescoCredential credential = new AlfrescoCredential();

	private AbstractAlfrescoFileServer fileserver = null;
	private AlfrescoWebService ws;

	public AbstractAlfrescoFileServer getFileserver() {
		return fileserver;
	}
	
	public AlfrescoWebService getWs() {
		return ws;
	}

	public AlfrescoConnectionFacade(LegacydmsProperties settings){
		
		String fsname = settings.getFtpType();
		fsname = "org.cmdbuild.legacy.dms.fileserver." + fsname;

		Class<? extends AbstractAlfrescoFileServer> fsclass = null;
		try {
			credential.setUser(settings.getAlfrescoUser());
			credential.setPassword(settings.getAlfrescoPassword());
			ws = new AlfrescoWebService(settings, credential);
			fsclass = (Class<? extends AbstractAlfrescoFileServer>)Class.forName(fsname);
			fileserver = fsclass.newInstance();
			fileserver.init(settings, credential);
		} catch (ClassNotFoundException e) {
			Log.DMS.info("Failed to load fileserver concrete class", e);
			e.printStackTrace();
		} catch (InstantiationException e) {
			Log.DMS.info("Failed to instance fileserver concrete class", e);
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			Log.DMS.info("Failed to instance fileserver concrete class", e);
			e.printStackTrace();
		}
	}

	/**
	 * Upload a file to alfresco server,
	 * "path" element will contains the classes path, ie :
	 * path = new String[]{"asset","computer","desktop","" + 1520};
	 * the last element is the Id of the object related to the document.
	 * @param filename
	 * @param is
	 * @param meta
	 * @param path
	 * @return
	 */
	public synchronized boolean upload( String author, String filedescription, String filename, InputStream is, CMDBAlfrescoMeta meta, CTree<ITable> tree) {
		String[] path = getPath(meta.getClassname(),meta.getObjid());
		if( !fileserver.upload(filename, is, path) ){
			Log.DMS.warn("Failed to upload with fileserver, return false");
			return false;
		}
		//hack..
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			Log.DMS.debug("Error uploading file", e.fillInStackTrace());
			Log.DMS.warn(e.getLocalizedMessage(), e);
			e.printStackTrace();
		}
		//
		if( !ws.makeMeta(author, filename, filedescription, meta, path) ){
			Log.DMS.debug("Error making metadata");
			Log.DMS.warn("Failed to make metadata, return false");
			return false;
		}
		return true;
	}
	
	public synchronized boolean delete (String filename, CMDBAlfrescoMeta meta, CTree<ITable> tree) throws NotFoundException{
		String[] path = getPath(meta.getClassname(), meta.getObjid());
		//hack..
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			Log.DMS.debug("Error making metadata", e.fillInStackTrace());
			return fileserver.delete(filename, path);
		}
		//
		return fileserver.delete(filename, path);
	}
	
	private String[] getPath(String classname, int objid) throws NotFoundException{

		Collection<String> classWithAncestors = TableImpl.tree().path(classname);

		String[] path = new String[classWithAncestors.size()+1];
		classWithAncestors.toArray(path);
		path[classWithAncestors.size()]="Id"+objid;
		//Log.DMS.debug("Requested path " + path);
		return path;
	}
	
	public synchronized DataHandler download( String filename, CMDBAlfrescoMeta meta) throws NotFoundException{
		String[] path = getPath(meta.getClassname(),meta.getObjid());
		Log.DMS.debug("Getting file in " + path);
		DataHandler dataSource  = fileserver.download(filename, path); 
		return dataSource;
	}
	
	public synchronized boolean updateDescription( CMDBAlfrescoMeta meta,CTree<ITable> tree, String filename, String newDescription ) {
		Collection<AttachmentBean> attach = search(meta, tree, null);
		
		for (AttachmentBean attachment : attach){
			if (attachment.getName().equals(filename)){
				String uuid = attachment.getUuid();
				return ws.updateDescription(newDescription, uuid);
			} 
		}
		return false;
	}
		
	public synchronized Collection<AttachmentBean> search( CMDBAlfrescoMeta meta,CTree<ITable> tree,String text ) {
		
		String concrete = meta.getClassname();
		String[] path = getPath(concrete, meta.getObjid());

		ResultSetRow[] rows = ws.searchFiles(path, meta,text);
		List<AttachmentBean> out = new LinkedList<AttachmentBean>();
		if(rows != null){
			for(ResultSetRow r : rows){
				AttachmentBean ab = convertRow(r,meta);
				ab.setFspath(path);
				out.add(ab);
			}
			Collections.sort(out, new AttachmentBeanComparator());
		}
		return out;
	}
	
	private AttachmentBean convertRow(ResultSetRow row, CMDBAlfrescoMeta meta){
		
		AttachmentBean out = new AttachmentBean();
		out.setMetadata(meta);
		NamedValue[] nvs = row.getColumns();
		AlfrescoConstant ac;
		ws.initialize();
		for( NamedValue nv : nvs ){
			ac = AlfrescoConstant.fromName(nv.getName());
			if(ac != null)
				ac.setInBean(out, nv, this);
		}
		return out;
	}
}
