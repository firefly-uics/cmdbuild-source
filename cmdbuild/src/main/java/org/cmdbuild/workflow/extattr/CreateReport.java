package org.cmdbuild.workflow.extattr;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Map;

import org.cmdbuild.elements.filters.AttributeFilter.AttributeFilterType;
import org.cmdbuild.elements.report.ReportFactoryDB;
import org.cmdbuild.elements.report.ReportParameter;
import org.cmdbuild.elements.report.ReportFactory.ReportExtension;
import org.cmdbuild.elements.wrappers.ReportCard;
import org.cmdbuild.exception.CMDBWorkflowException.WorkflowExceptionType;
import org.cmdbuild.logger.Log;
import org.cmdbuild.services.SessionVars;
import org.cmdbuild.services.auth.UserContext;
import org.cmdbuild.workflow.SharkWSFacade;
import org.cmdbuild.workflow.operation.ActivityDO;
import org.enhydra.shark.api.client.wfmc.wapi.WAPI;
import org.enhydra.shark.api.client.wfmc.wapi.WMSessionHandle;
import org.enhydra.shark.api.client.wfmc.wapi.WMWorkItem;
import org.enhydra.shark.client.utilities.SharkWSFactory;
import org.json.JSONException;
import org.json.JSONObject;

public class CreateReport extends AbstractCmdbuildExtendedAttribute {
	
	static final String ReportType = "ReportType";
	static final String ReportCode = "ReportCode";
	
	static final String SaveToAlfresco = "StoreInAlfresco";
	static final String FileCategory = "FileCategory";
	static final String FileDescription = "FileDescription";

	static final String StoreInProcess = "StoreInProcess";
	
	static final String ForcePDF = "ForcePDF";
	static final String ForceCSV = "ForceCSV";
	
	static final String REPORT_CODE = "Code";

	@Override
	protected void doConfigure(WMSessionHandle handle, UserContext userCtx, SharkWSFactory factory,
			WMWorkItem workItem, ActivityDO activityDO, Map<String, Object> processVars,
			Map<String, Object> currentOutValues) {
	}

	@Override
	protected void doReact(WMSessionHandle handle, UserContext userCtx,
			SharkWSFactory factory, SharkWSFacade facade, WMWorkItem workItem,
			ActivityDO activityDO, Map<String, String[]> submissionParameters,
			ExtendedAttributeConfigParams oldConfig,
			Map<String, Object> outputParameters, boolean advance) {
		Map<String,Object> prm = oldConfig.getParameters();
		
		boolean saveAlfresco, storeProcess;
		
		saveAlfresco = prm.containsKey(SaveToAlfresco) && prm.containsKey(FileCategory);
		storeProcess = prm.containsKey(StoreInProcess);
		
		if(!saveAlfresco && !storeProcess){
			//nothing to do..
			Log.WORKFLOW.debug("report not to be stored in process or alfresco");
			new SessionVars().removeReportFactory();
			return;
		}

		try {
			ReportFactoryDB reportFactory = getReportFactory(submissionParameters, oldConfig);
			
			if (saveAlfresco) {
				Log.WORKFLOW.debug("report to be stored in alfresco");
				String fname = getFilenameForAlfresco(prm, reportFactory);
				String category = getCategoryForAlfresco(prm);
				String description = getDescriptionForAlfresco(prm);

				// FIXME make it work later
//				saveToAlfresco(reportFactory, userCtx, activityDO, fname, category, description);
			}
			if (storeProcess) {
				Log.WORKFLOW.debug("report to be stored in process");
				String fname = getFilenameForProcess(prm);
				storeInProcess(reportFactory,handle,factory,workItem,fname);
			}
		} catch(Exception e) {
			Log.WORKFLOW.error("cannot get/save report", e);
			throw WorkflowExceptionType.WF_CANNOT_REACT_CMDBEXTATTR.createException(extAttrName);
		} finally {
			new SessionVars().removeReportFactory();
		}

	}

	private String getFilenameForProcess(Map<String, Object> prm) {
		String fname = (String) prm.get(StoreInProcess);
		if ("1".equals(fname)) {
			return null;
		} else {
			return fname;
		}
	}

	private String getDescriptionForAlfresco(Map<String, Object> prm) {
		return (String) prm.get(FileDescription);
	}

	private String getCategoryForAlfresco(Map<String, Object> prm) {
		return (String) prm.get(FileCategory);
	}

	private String getFilenameForAlfresco(Map<String, Object> prm, ReportFactoryDB reportFactory) {
		String fname = (String)prm.get(SaveToAlfresco);
		if (fname.equals("1")) {
			fname =  generateFileName(reportFactory, false);
		}
		return fname;
	}
	
	private ReportFactoryDB getReportFactory(Map<String, String[]> submissionParameters, ExtendedAttributeConfigParams oldConfig)
	throws Exception {
		ReportFactoryDB factory = (ReportFactoryDB) new SessionVars().getReportFactory();
		if(factory == null) {
			throw WorkflowExceptionType.WF_EXTATTR_CREATEREPORT_FACTORYNOTINSESSION.createException();
		}
		
		if(!factory.isReportFilled()) {
			//configuration can be needed if user didn't asked for the report from js
			if(factory.getReportExtension() == null || factory.getReportExtension().equals("")) {
				String extension = firstStringOrNull(submissionParameters.get("reportExtension"));
				factory.setReportExtension(ReportExtension.valueOf(extension.toUpperCase()));
			}
			for (ReportParameter param : factory.getReportParameters()) {
				if (null == param.getValue() || param.getValue().equals(param.getDefaultValue())) {
					String value = firstStringOrNull(submissionParameters.get(param.getName()));
					if (value != null) {
						param.parseValue(value);
					}
				}
			}
			factory.fillReport();
		}
		
		return factory;
	}
	
	// FIXME make it work later
//	private void saveToAlfresco(ReportFactoryDB factory, UserContext userCtx, ActivityDO activity,
//			String fileName, String category, String description) throws Exception{
//		String className = activity.getProcessInfo().getCmdbuildBindedClass();
//		AlfrescoFacade op = new AlfrescoFacade(userCtx,className,activity.getCmdbuildCardId());
//		op.upload(getInputStream(factory), fileName, category, description);
//	}

	private String generateFileName(ReportFactoryDB factory, boolean addRandom) {
		return getNormalizedCode(factory) + "." + factory.getReportExtension();
	}

	private String getNormalizedCode(ReportFactoryDB factory) {
		return factory.getReportCard().getCode().replace(' ', '_');
	}
	private ByteArrayInputStream getInputStream( ReportFactoryDB factory )
	throws Exception {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		factory.sendReportToStream(baos);
		return new ByteArrayInputStream(baos.toByteArray());
	}
	
	/*
	 * A temporary file is created, then his name is stored in the workflow.
	 * Call the toolagent getReportFullURL to obtain a string which holds the 
	 * report address
	 */
	private void storeInProcess(ReportFactoryDB factory,
			WMSessionHandle handle, SharkWSFactory wsFactory, WMWorkItem workItem,
			String fileName)
	throws Exception{
		File file = null;
		if(fileName != null) {
			String tmpDir = System.getProperty("java.io.tmpdir");
			if(!tmpDir.endsWith(File.separator)){tmpDir += File.separator;}
			file = new File(tmpDir + fileName);
			file.createNewFile();
		} else {
			file = File.createTempFile(getNormalizedCode(factory), "."+ factory.getReportExtension());
			fileName = file.getName();
		}
		Log.WORKFLOW.debug("created file: " + fileName + ", fullpath: " + file.getAbsolutePath());
		FileOutputStream fos = new FileOutputStream(file);
		
		factory.sendReportToStream(fos);
		fos.flush(); fos.close();
		
		WAPI wapi = wsFactory.getWAPIConnection();
		
		String outputName = this.outParameters.get(0);
		
		wapi.assignWorkItemAttribute(handle, workItem.getProcessInstanceId(), workItem.getId(), outputName, fileName);
	}
	

	@Override
	protected void addCustomParams(ActivityDO activityDO, JSONObject object,
			ExtendedAttributeConfigParams eacp) throws JSONException {
		Map<String,Object> prm = eacp.getParameters();
		object.put("Type", prm.get(ReportType));
		object.put("Code", prm.get(ReportCode));
		String code = (String) prm.get(ReportCode);
		object.put("Id", getReportId(code));
		
		if(prm.containsKey(ForcePDF)) {
			object.put("forceextension", "pdf");
		} else if(prm.containsKey(ForceCSV)) {
			object.put("forceextension", "csv");
		}
		
		JSONObject params = new JSONObject();
		for(String key : prm.keySet()) {
			if(!key.equals(ReportCode) && !key.equals(ReportType)) {
				params.put(key, prm.get(key));
			}
		}
		object.put("parameters", params);
	}

	private int getReportId(String code) {
		ReportCard report = new ReportCard(UserContext.systemContext().tables().get(ReportCard.REPORT_CLASS_NAME).cards().list().filterUpdate(REPORT_CODE, AttributeFilterType.EQUALS, code).get());
		return report.getId();
	}

	public String extendedAttributeName() {
		return "createReport";
	}

}
