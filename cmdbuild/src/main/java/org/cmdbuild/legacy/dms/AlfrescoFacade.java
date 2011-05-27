package org.cmdbuild.legacy.dms;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.activation.DataHandler;

import org.cmdbuild.config.LegacydmsProperties;
import org.cmdbuild.elements.TableImpl;
import org.cmdbuild.elements.interfaces.ICard;
import org.cmdbuild.elements.interfaces.ITable;
import org.cmdbuild.exception.AuthException;
import org.cmdbuild.exception.CMDBException;
import org.cmdbuild.exception.NotFoundException;
import org.cmdbuild.exception.ORMException;
import org.cmdbuild.exception.NotFoundException.NotFoundExceptionType;
import org.cmdbuild.logger.Log;
import org.cmdbuild.services.Settings;
import org.cmdbuild.services.auth.UserContext;
import org.cmdbuild.utils.tree.CTree;

@SuppressWarnings("restriction")
public class AlfrescoFacade {
	
	private String classname;
	private int objid;	
	private String author;
	private String code="";
	private String notes="";
	private String description="";
	/**
	 * DMS Constructor. Get necessary settings based on card
	 * 
	 * @param classname Card classname
	 * @param objid Card id
	 * 
	 * @throws AuthException
	 * @throws ORMException
	 * @throws NotFoundException
	 */

	public AlfrescoFacade(UserContext userCtx, String className, int cardId) throws AuthException, ORMException, NotFoundException {
		LegacydmsProperties properties = (LegacydmsProperties) Settings.getInstance().getModule("legacydms");
		if (properties.isEnabled()){
			this.classname = className;
			this.objid = cardId;
			ICard card = userCtx.tables().get(className).cards().get(cardId);
			this.author = userCtx.getUsername();
			if (card.getCode() != null)
				this.code = card.getCode();
			if (card.getNotes() != null)
				this.notes = card.getNotes();
		} else {
			Log.DMS.debug("Legacy server alfresco is not configured");
			throw NotFoundExceptionType.SERVICE_UNAVAILABLE.createException();
		}
	}
	
	public boolean upload(InputStream file, String filename, String category, String description) throws IOException, CMDBException{
		
		LegacydmsProperties properties = (LegacydmsProperties) Settings.getInstance().getModule("legacydms");
		AlfrescoConnectionFacade alfresco = new AlfrescoConnectionFacade(properties);
		
		CMDBAlfrescoMeta meta = new CMDBAlfrescoMeta(classname, code, notes, description, objid, category);
		CTree<ITable> tree = TableImpl.tree();
		
		if (alfresco.upload(author, description, filename, file, meta, tree))
			return true;
		else
			throw ORMException.ORMExceptionType.ORM_ATTACHMENT_UPLOAD_FAILED.createException();
	}
	
	public DataHandler download (String filename) {
		LegacydmsProperties properties = (LegacydmsProperties) Settings.getInstance().getModule("legacydms");
		AlfrescoConnectionFacade alfresco = new AlfrescoConnectionFacade(properties);
		String category = properties.getCmdbuildCategory();		

		CMDBAlfrescoMeta meta = new CMDBAlfrescoMeta(classname, code, notes, description, objid, category);

		DataHandler fileDataHandler = alfresco.download(filename, meta);
		if (fileDataHandler == null)
			throw NotFoundExceptionType.ATTACHMENT_NOTFOUND.createException(filename, this.classname, String.valueOf(this.objid));
		return fileDataHandler;
	}
	
	public boolean updateDescription(String filename, String newdescription) {
		LegacydmsProperties properties = (LegacydmsProperties) Settings.getInstance().getModule("legacydms");
		AlfrescoConnectionFacade alfresco = new AlfrescoConnectionFacade(properties);
		
		String category= properties.getCmdbuildCategory();		
		//CMDBAlfrescoMeta meta = new CMDBAlfrescoMeta(classname, null, null, null, objid, category);
		CMDBAlfrescoMeta meta = new CMDBAlfrescoMeta(classname, code, notes, description, objid, category);
		CTree<ITable> tree = TableImpl.tree();
		
		return alfresco.updateDescription(meta, tree, filename, newdescription);
	}
	
	public boolean delete(String filename) throws NotFoundException{
		LegacydmsProperties properties = (LegacydmsProperties) Settings.getInstance().getModule("legacydms");
		AlfrescoConnectionFacade alfresco = new AlfrescoConnectionFacade(properties);
		
		String category= properties.getCmdbuildCategory();		
		//CMDBAlfrescoMeta meta = new CMDBAlfrescoMeta(classname, null, null, null, objid, category);
		CMDBAlfrescoMeta meta = new CMDBAlfrescoMeta(classname, code, notes, description, objid, category);
		CTree<ITable> tree = TableImpl.tree();
		
		return alfresco.delete(filename, meta, tree);
	}
	
	public List<AttachmentBean> search() {
		LegacydmsProperties properties = (LegacydmsProperties) Settings.getInstance().getModule("legacydms");
		AlfrescoConnectionFacade alfresco = new AlfrescoConnectionFacade(properties);
		
		String category= properties.getCmdbuildCategory();		
		CMDBAlfrescoMeta meta = new CMDBAlfrescoMeta(classname, null, null, null, objid, category);
		//CMDBAlfrescoMeta meta = new CMDBAlfrescoMeta(classname, code, notes, description, objid, category);
		CTree<ITable> tree = TableImpl.tree();
		
		return (List<AttachmentBean>) alfresco.search(meta, tree, null);
	}

}
