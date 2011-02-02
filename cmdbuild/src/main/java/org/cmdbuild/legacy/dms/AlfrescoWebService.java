package org.cmdbuild.legacy.dms;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.webservice.authentication.AuthenticationFault;
import org.alfresco.webservice.classification.AppliedCategory;
import org.alfresco.webservice.classification.ClassificationFault;
import org.alfresco.webservice.classification.ClassificationServiceSoapBindingStub;
import org.alfresco.webservice.repository.QueryResult;
import org.alfresco.webservice.repository.RepositoryFault;
import org.alfresco.webservice.repository.RepositoryServiceSoapBindingStub;
import org.alfresco.webservice.types.CML;
import org.alfresco.webservice.types.CMLAddAspect;
import org.alfresco.webservice.types.CMLCreate;
import org.alfresco.webservice.types.CMLUpdate;
import org.alfresco.webservice.types.NamedValue;
import org.alfresco.webservice.types.Node;
import org.alfresco.webservice.types.ParentReference;
import org.alfresco.webservice.types.Predicate;
import org.alfresco.webservice.types.Query;
import org.alfresco.webservice.types.Reference;
import org.alfresco.webservice.types.ResultSet;
import org.alfresco.webservice.types.ResultSetRow;
import org.alfresco.webservice.types.ResultSetRowNode;
import org.alfresco.webservice.types.Store;
import org.alfresco.webservice.util.AuthenticationUtils;
import org.alfresco.webservice.util.Constants;
import org.alfresco.webservice.util.Utils;
import org.alfresco.webservice.util.WebServiceFactory;
import org.cmdbuild.config.LegacydmsProperties;
import org.cmdbuild.exception.AuthException.AuthExceptionType;
import org.cmdbuild.exception.NotFoundException.NotFoundExceptionType;
import org.cmdbuild.logger.Log;


/*
 * modifica metadati oggetto
 * ricerca oggetti
 * ->non è banale ottenere la lista degli oggetti con la lista delle categorie associate!
 */

public class AlfrescoWebService {
	String baseSearchPath;
	String categoryRoot;
	boolean connected;
	AlfrescoCredential credential;
	LegacydmsProperties settings;
	Store spaces;
	
	public AlfrescoWebService(LegacydmsProperties settings, AlfrescoCredential credential){
		this.settings = settings;
		this.credential = credential;
		this.baseSearchPath = settings.getRepositoryWSPath() + settings.getRepositoryApp();
		this.categoryRoot = settings.getCmdbuildCategory();
		this.spaces = new Store(Constants.WORKSPACE_STORE, "SpacesStore");
		String endPoint = settings.getServerURL();
		WebServiceFactory.setEndpointAddress(endPoint);
	}

	public void initialize(){
		connected = AuthenticationUtils.getTicket() != null;
		if( !connected ){
			try {
				AuthenticationUtils.startSession(settings.getAlfrescoUser(), settings.getAlfrescoPassword());
				connected = true;
			} catch (AuthenticationFault e) {
				connected = false;
				Log.DMS.info("Failed to connect to Alfresco WebService", e);
				e.printStackTrace();
			}
		}
	}

	public void exit(){
		if( connected ){
			AuthenticationUtils.endSession();
			connected = false;
		}
	}

	public boolean isConnected(){
		return connected;
	}

	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		if(connected) exit();
	}
	
	protected boolean makeMeta(String author, String fname, String fdescription, CMDBAlfrescoMeta meta, String[] path) {
		initialize();
		if(!connected) {
			throw AuthExceptionType.AUTH_LOGIN_WRONG.createException();
		}

		RepositoryServiceSoapBindingStub repo = WebServiceFactory.getRepositoryService();
		ClassificationServiceSoapBindingStub clazz = WebServiceFactory.getClassificationService();
		
		String refstr = baseSearchPath;
		for(String p : path){
			refstr += "/cm:"+p;
		}
		Reference reference = new Reference(spaces, null, refstr); 

		ResultSetRow[] rows = searchFile(fname,reference,spaces,repo);
		
		if(rows==null){
			Log.DMS.info(String.format("File %s was not found in repository", fname));
			return false;
		}

		ResultSetRow row = rows[0];
		ResultSetRowNode node = row.getNode();
		String id = node.getId();

		CMLUpdate update = new CMLUpdate();
		Predicate updPred = new Predicate();
		updPred.setStore(spaces);

		Reference updRef = new Reference();
		updRef.setStore(spaces);
		updRef.setUuid(id);

		updPred.setNodes(new Reference[]{updRef});


		NamedValue[] titledProps = new NamedValue[3];
		titledProps[0] = Utils.createNamedValue(Constants.PROP_TITLE, fname);
		titledProps[1] = Utils.createNamedValue(Constants.PROP_DESCRIPTION, fdescription);
		titledProps[2] = Utils.createNamedValue(AlfrescoConstant.AUTHOR.name, author);

		update.setWhere(updPred);
		update.setWhere_id(id);
		update.setProperty(titledProps);

		CML cml = new CML();

		CMLAddAspect cmdbAsp = createCMDBAspect(updPred,"1",meta);
		CMLAddAspect versionAsp = createVersionable(updPred,"1");
		cml.setUpdate(new CMLUpdate[]{update});
		cml.setAddAspect(new CMLAddAspect[]{versionAsp,cmdbAsp});
		try {
			/*UpdateResult[] updRes = */
			repo.update(cml);
			makeClassification(updPred,meta.getCategory(),clazz,repo);
			return true;
		} catch (RepositoryFault e) {
			e.printStackTrace();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		return false;	}

	
	private CMLAddAspect createVersionable(Predicate pred,String id){
		return new CMLAddAspect(Constants.ASPECT_VERSIONABLE,null,pred,id);
	}
	private CMLAddAspect createCMDBAspect(Predicate pred,String id, CMDBAlfrescoMeta meta){
		NamedValue[] values = new NamedValue[5];
		values[0] = Utils.createNamedValue("{it.cmdbuild.alfresco}classname", meta.getClassname());
		values[1] = Utils.createNamedValue("{it.cmdbuild.alfresco}objid", "" + meta.getObjid());
		values[2] = Utils.createNamedValue("{it.cmdbuild.alfresco}code", meta.getCode());
		values[3] = Utils.createNamedValue("{it.cmdbuild.alfresco}description", meta.getDescription());
		values[4] = Utils.createNamedValue("{it.cmdbuild.alfresco}notes", meta.getNotes());

		return new CMLAddAspect("{it.cmdbuild.alfresco}cmdbuildMeta",values,pred,id);
	}
	
	
	private void makeClassification(Predicate pred,String applied, ClassificationServiceSoapBindingStub clazz, RepositoryServiceSoapBindingStub repo){
		Reference catRef = findCategory(applied,repo);
		if (null == catRef) {
			createCategory(applied,repo);
			catRef = findCategory(applied,repo);
			if (catRef == null)
				throw NotFoundExceptionType.ATTACHMENT_NOTFOUND.createException("pippo");
		}
		AppliedCategory ap = new AppliedCategory();
		ap.setClassification("{http://www.alfresco.org/model/content/1.0}generalclassifiable");
		ap.setCategories(new Reference[]{catRef});
		try {
			clazz.setCategories(pred, new AppliedCategory[]{ap});
		} catch (ClassificationFault e) {
			Log.DMS.info("ClassificationFault while applying a category", e);
			e.printStackTrace();
		} catch (RemoteException e) {
			Log.DMS.info("RemoteException while applying a category", e);
			e.printStackTrace();
		}
	}
	
	private Reference findCategory(String category,RepositoryServiceSoapBindingStub repo)
	{
		Query query = new Query(Constants.QUERY_LANG_LUCENE, "PATH:\"/cm:generalclassifiable//cm:"+escapeQuery(category)+"\"");
		Log.DMS.debug("Lucene query to find category: " + query.getStatement());

		QueryResult result;
		try {
			result = repo.query(spaces, query, true);
			ResultSetRow row = result.getResultSet().getRows(0);
			NamedValue[] nvs = row.getColumns();
			Reference out = null;
			for(NamedValue nv : nvs){
				if(AlfrescoConstant.UUID.isName(nv.getName())){
					out = new Reference();
					out.setUuid(nv.getValue());
					out.setStore(spaces);
				}
			}
			return out;
		} catch (RepositoryFault e) {
			Log.DMS.info("Repository fault", e);
			e.printStackTrace();
		} catch (RemoteException e) {
			Log.DMS.info("Remote exception", e);
			e.printStackTrace();
		} catch( NullPointerException e ) {
			e.printStackTrace();
		}
		return null;
	}

	
	private ResultSetRow[] searchFile(String filename,Reference rootSpaceReference,Store spaces,RepositoryServiceSoapBindingStub repo){
		try{

			Predicate predicate = new Predicate(new Reference[]{rootSpaceReference}, null, null);        
			Node[] nodes = repo.get(predicate);

			Query query = new Query();
			query.setLanguage(Constants.QUERY_LANG_LUCENE);

			String stmt = "+PARENT:\"workspace://SpacesStore/"+ nodes[0].getReference().getUuid() + "\" ";
			query.setStatement(stmt);

			// Execute the query
			QueryResult res = repo.query(spaces, query, false);

			ResultSet rs = res.getResultSet();
			ResultSetRow[] rows = rs.getRows();
			for(ResultSetRow row : rows){
				NamedValue[] nvs = row.getColumns();
				for(NamedValue nv : nvs){
					if(AlfrescoConstant.NAME.isName(nv.getName())){
						if(nv.getValue().equals(filename)){
							return new ResultSetRow[]{row};
						}
					}
				}
			}
			return null;
		}catch(Exception e){
			Log.DMS.info("Cannot search file", e);
			e.printStackTrace();
			return null;
		}
	}
	
	private Reference getRootReference(RepositoryServiceSoapBindingStub repo)
	{
		Query query = new Query(Constants.QUERY_LANG_LUCENE, "PATH:\"/cm:generalclassifiable//cm:" + escapeQuery(categoryRoot) + "\"");
		Log.DMS.debug("Lucene query to find root category: " + query.getStatement());
		try
		{
			QueryResult result= repo.query(this.spaces, query, true);
			ResultSet rs = result.getResultSet();
			ResultSetRow[] rows = rs.getRows();
			String uuid = rows[0].getNode().getId();
			return new Reference(spaces, uuid, null);
		}
		catch (Exception e){
			e.printStackTrace();
			return null;
		}
	}
	
	private void createCategory(String category,RepositoryServiceSoapBindingStub repo){
		Reference spaceRef = getRootReference(repo);
		final String SUBCATEGORIES = "subcategories";
		final String CATEGORY = "category";
		String path = category;

		//		either UUID or the strict path works"
		ParentReference parentRef = new ParentReference(spaces, spaceRef.getUuid(),null ,
				Constants.createQNameString(Constants.NAMESPACE_CONTENT_MODEL,SUBCATEGORIES),
				Constants.createQNameString(Constants.NAMESPACE_CONTENT_MODEL, path));

		NamedValue[] properties = new NamedValue[]{
				Utils.createNamedValue(Constants.PROP_NAME, path)
		};



		CMLCreate create = new CMLCreate("1", parentRef, null, null, null, Constants.createQNameString(Constants.NAMESPACE_CONTENT_MODEL,CATEGORY), properties);
		CML cml = new CML();
		cml.setCreate(new CMLCreate[]{create});
		
		try {
			WebServiceFactory.getRepositoryService().update(cml);
		}catch (Exception e) {
			Log.DMS.info("Exception while creating categories", e);
			e.printStackTrace();
		}
	}
	
	Map<String,Boolean> knownCategoryPaths = new HashMap<String,Boolean>();
	
	/**
	 * this method DO NOT initialize a connection, NOR close the connection.
	 * To be called from the AlfrescoConstant.CATEGORY.set
	 * @param uuid
	 * @return
	 */
	
	public boolean isCMDBCategory(String path){
		if(knownCategoryPaths.containsKey(path)){ return knownCategoryPaths.get(path); }
		initialize();
		if(!connected) return false;
		Reference ref = new Reference(spaces,path,null);
		RepositoryServiceSoapBindingStub repo = WebServiceFactory.getRepositoryService();
		try{
			QueryResult res = repo.queryParents(ref);
			ResultSetRow[] rows = res.getResultSet().getRows();
			for(ResultSetRow row : rows){
				NamedValue[] nvs = row.getColumns();
				for(NamedValue nv : nvs){
					if(nv.getName().endsWith("name")){
						if(nv.getValue().equals(this.categoryRoot)){
							knownCategoryPaths.put(path,true);
							return true;
						}
					}
				}
			}
			knownCategoryPaths.put(path, false);
			return false;
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}
	}
	
	
	public ResultSetRow searchRow(String path,boolean ispath){
		initialize();
		if(!connected) return null;

		RepositoryServiceSoapBindingStub repo = WebServiceFactory.getRepositoryService();
		
		Reference ref = new Reference();
		ref.setStore(spaces);
		ref.setUuid(path);
		
		Predicate pred = new Predicate(new Reference[]{ref},spaces,null);
		
		try {
			Node node = repo.get(pred)[0];
			ResultSetRow out = new ResultSetRow();
			out.setColumns(node.getProperties());
			return out;
		} catch (RepositoryFault e) {
			Log.DMS.info("Exception while searchRow", e);
			e.printStackTrace();
		} catch (RemoteException e) {
			Log.DMS.info("Exception while searchRow", e);
			e.printStackTrace();
		} catch (Exception e){
			Log.DMS.info("Exception while searchRow", e);
			e.printStackTrace();
		}
		return null;
	}
	
	protected ResultSetRow[] searchFiles(String[] path, CMDBAlfrescoMeta filter, String text){
		initialize();
		if(!connected) return null;
		
		RepositoryServiceSoapBindingStub repo = WebServiceFactory.getRepositoryService();

		try{
			String refstr = null;
			if(path != null){
				refstr = baseSearchPath;
				for(String p : path){
					refstr += "/cm:"+p;
				}
			}

			Query query = new Query();
			query.setLanguage(Constants.QUERY_LANG_LUCENE);

			String stmt = "";
			if(refstr != null){
				stmt += "+PATH:\"" + refstr + "//*\" ";
			}
			
			//sto cercando files
			//stmt += " +TYPE:\"{http://www.alfresco.org/model/content/1.0}content\" ";
			//che sono stati classificati in qualche modo
			stmt += " +ASPECT:\"{http://www.alfresco.org/model/content/1.0}generalclassifiable\" ";
			//e che sn collegati ad un oggetti di cmdbuild
			if(text != null && text.length()>0){
				stmt += " +TEXT:\"" + text + "\" ";
			}
			if( -1 != filter.getObjid()){
				stmt += " +@\\{it.cmdbuild.alfresco\\}objid:\"" + filter.getObjid() + "\"";
			}
			if( filter.getCode() != null && (!filter.getCode().equals("")) ){
				stmt += " +@\\{it.cmdbuild.alfresco\\}code:\"" + filter.getCode() + "\"";
			}
			if( filter.getDescription() != null && (!filter.getDescription().equals("")) ){
				stmt += " +@\\{it.cmdbuild.alfresco\\}description:\"" + filter.getDescription() + "\"";
			}
			if( filter.getNotes() != null && (!filter.getNotes().equals(""))){
				stmt += " +@\\{it.cmdbuild.alfresco\\}notes:\"" + filter.getNotes() + "\"";
			}

			query.setStatement(stmt);

			QueryResult res = repo.query(spaces, query, false);

			ResultSet rs = res.getResultSet();
			return rs.getRows();
		}catch(Exception e){
			Log.DMS.info("Exception while search for files", e);
			e.printStackTrace();
			return null;
		}
	}
	
	protected boolean updateDescription(String description, String UUID){
		initialize();
		
		if(!connected) 
			return false;
		
		RepositoryServiceSoapBindingStub repo = WebServiceFactory.getRepositoryService();
		
		Reference reference = new Reference(spaces, UUID, null);
		
		Predicate updPred = new Predicate();
		updPred.setStore(spaces);
		updPred.setNodes(new Reference[]{reference});
		
		NamedValue[] titledProps = new NamedValue[1];
		titledProps[0] = Utils.createNamedValue(Constants.PROP_DESCRIPTION, description);
		
		CMLUpdate update = new CMLUpdate();
		update.setWhere(updPred);
		update.setProperty(titledProps);
		
		CML cml = new CML();
		cml.setUpdate(new CMLUpdate[]{update});
		try {
			repo.update(cml);
			return true;
		} catch (RepositoryFault e) {
			Log.DMS.info("Repository fault while updating file description", e);
			e.printStackTrace();
		} catch (RemoteException e) {
			Log.DMS.info("Remote exception while updating file description", e);
			e.printStackTrace();
		}
		return false;
	}

	/*
	 * Quick fix because I don't know how to escape the names and
     * this should be completely rewritten for Alfresco 3.0
	 */
	private String escapeQuery(String query) {
		return query.replaceAll(" ", "_x0020_");
	}
}
