package org.cmdbuild.servlets.json.schema;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JRQuery;
import net.sf.jasperreports.engine.JRSubreport;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.design.JRDesignImage;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.xml.JRXmlLoader;

import org.apache.commons.fileupload.FileItem;
import org.cmdbuild.elements.interfaces.ITable;
import org.cmdbuild.elements.interfaces.IAbstractElement.ElementStatus;
import org.cmdbuild.elements.report.ReportFacade;
import org.cmdbuild.elements.report.ReportFactory;
import org.cmdbuild.elements.report.ReportFactoryTemplateSchema;
import org.cmdbuild.elements.report.ReportParameter;
import org.cmdbuild.elements.report.ReportFactory.ReportExtension;
import org.cmdbuild.elements.report.ReportFactory.ReportType;
import org.cmdbuild.elements.wrappers.ReportCard;
import org.cmdbuild.exception.AuthException;
import org.cmdbuild.exception.NotFoundException;
import org.cmdbuild.exception.ReportException.ReportExceptionType;
import org.cmdbuild.logger.Log;
import org.cmdbuild.services.SessionVars;
import org.cmdbuild.servlets.json.JSONBase;
import org.cmdbuild.servlets.utils.MethodParameterResolver;
import org.cmdbuild.servlets.utils.Parameter;
import org.cmdbuild.servlets.utils.Request;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ModReport extends JSONBase {
	private static final long serialVersionUID = 1L;

	private static ReportFacade reportFacade = new ReportFacade();

	@JSONExported
	public String menuTree(Map<String, String> params) throws JSONException, AuthException {
		JSONArray serializer = new JSONArray();
		JSONObject item;
		
		item = new JSONObject();
		item.put("id", "Jasper");
		item.put("text", getTraslation("administration.modreport.importJRFormStep1.menuTitle"));
		item.put("leaf", true);
		item.put("cls", "file");
		item.put("type", "report");
		item.put("selectable", true);
		serializer.put(item);

		return serializer.toString();
	}
	
	@JSONExported
	public JSONObject printSchema(
			JSONObject serializer,
			@Parameter("format") String format
	) throws Exception {
		ReportFactoryTemplateSchema rfts = new ReportFactoryTemplateSchema(ReportExtension.valueOf(format.toUpperCase()));
		rfts.fillReport();
		new SessionVars().setReportFactory(rfts);
		return serializer;
	}
	
	@JSONExported
	public JSONObject printClassSchema(
			JSONObject serializer,
			ITable iTable,
			@Parameter("format") String format
	) throws Exception {
		ReportFactoryTemplateSchema rfts = new ReportFactoryTemplateSchema(ReportExtension.valueOf(format.toUpperCase()), iTable);
		rfts.fillReport();
		new SessionVars().setReportFactory(rfts);
		return serializer;		
	}
	
	@JSONExported
	public JSONObject analyzeJasperReport(
			JSONObject serializer,
			@Parameter("name") String name,
			@Parameter("description") String description,
			@Parameter("groups") String groups, 
			@Parameter("reportId") int reportId,
			@Parameter(value="jrxml", required=false) FileItem file) throws JSONException, NotFoundException {
		
		resetSession();
		ReportCard newReport = new ReportCard();
		setReportSimpleAttributes(name, description, groups, reportId, newReport);
		if (file.getSize() > 0) {
			setReportImagesAndSubReports(serializer, file, newReport);
		} else {
			// to say at the interface to not display the second step
			serializer.put("skipSecondStep", true);
		}
		new SessionVars().setNewReport(newReport);
		return serializer;
	}

	private void setReportImagesAndSubReports(JSONObject serializer,
			FileItem file, ReportCard newReport) throws JSONException {
		String[] imagesNames = null;
		int subreportsNumber=0;
		
		JasperDesign jd = loadJasperDesign(file);
		checkJasperDesignParameters(jd);
		List<JRDesignImage> designImages = ReportFactory.getImages(jd);
		
		if(ReportFactory.checkDuplicateImages(designImages)) { // check duplicates
			serializer.put("duplicateimages", true);
			serializer.put("images", "");
			serializer.put("subreports", "");
		} else {
			imagesNames = manageImages(serializer, designImages);
			subreportsNumber = manageSubReports(serializer, jd);
		}
		
		newReport.setImagesName(imagesNames);
		newReport.setSubreportsNumber(subreportsNumber);
		newReport.setJd(jd);
	}

	private void setReportSimpleAttributes(String name, String description,
			String groups, int reportId, ReportCard newReport) {
		int[] selectedGroups = parseSelectedGroup(groups);
		newReport.setOriginalId(reportId);
		newReport.setCode(name);
		newReport.setDescription(description);
		newReport.setSelectedGroups(selectedGroups);
	}

	private int manageSubReports(JSONObject serializer, JasperDesign jd)
			throws JSONException {
		JSONArray jsonArray;
		JSONObject jsonObject;
		int subreportsNumber=0;
		List<JRSubreport> subreports = ReportFactory.getSubreports(jd);
		jsonArray = new JSONArray();
		for(JRSubreport subreport : subreports) {
			String subreportName = ReportFactory.getSubreportName(subreport);
			subreportsNumber++;
			
			// client
			jsonObject = new JSONObject();
			jsonObject.put("name", subreportName);
			jsonArray.put(jsonObject);
		}
		serializer.put("subreports", jsonArray);
		ReportFactory.prepareDesignSubreportsForUpload(subreports); // update expressions in design
		return subreportsNumber;
	}

	private String[] manageImages(JSONObject serializer,
			List<JRDesignImage> designImages) throws JSONException {
		JSONArray jsonArray;
		JSONObject jsonObject;
		String[] imagesNames;
		jsonArray = new JSONArray();
		imagesNames = new String[designImages.size()];
		for(int i=0; i<designImages.size(); i++) {			
			String imageFilename = ReportFactory.getImageFileName(designImages.get(i));
			imagesNames[i]=imageFilename;
			
			// client
			jsonObject = new JSONObject();
			jsonObject.put("name", imageFilename);
			jsonArray.put(jsonObject);			
		}
		serializer.put("images", jsonArray);
		ReportFactory.prepareDesignImagesForUpload(designImages); // update expressions in design
		return imagesNames;
	}

	private int[] parseSelectedGroup(String groups) {
		int[] selectedGroups = {};
		if(groups!=null && !groups.equals("")) {
			int numGroups = groups.split(",").length;
			selectedGroups = new int[numGroups];
			for(int i=0; i<numGroups;i++) {
				selectedGroups[i] = Integer.parseInt(groups.split(",")[i]);
			}
		}
		return selectedGroups;
	}

	private void checkJasperDesignParameters(JasperDesign jd) {
		JRParameter[] parameters = jd.getParameters();
		for(JRParameter parameter : parameters) {
			ReportParameter.parseJrParameter(parameter);
		}
	}

	private JasperDesign loadJasperDesign(FileItem file) {
		JasperDesign jd = null;
		try {
			jd = JRXmlLoader.load(file.getInputStream());
		} catch (Exception e) {
			Log.REPORT.error("Error loading report", e);
			throw ReportExceptionType.REPORT_INVALID_FILE.createException();
		}
		return jd;
	}		

	@Admin
	@JSONExported
	public JSONObject importJasperReport(
			JSONObject serializer,
			@Request(MethodParameterResolver.MultipartRequest) List<FileItem> files)
			throws JSONException, AuthException {
		ReportCard newReport = new SessionVars().getNewReport();

		if (newReport.getJd() != null) {
			importSubreportsAndImages(files, newReport);
		}
		saveReport(newReport);
		resetSession();
		return serializer;
	}
	
	
	@Admin
	@JSONExported
	public JSONObject saveJasperReport(JSONObject serializer) {
		ReportCard newReport = new SessionVars().getNewReport();
		saveReport(newReport);
		
		return serializer;
	}
	
	private void saveReport(ReportCard newReport) {
		try {
			if (newReport.getOriginalId() < 0) {
				reportFacade.insertReport(newReport);
			} else {
				reportFacade.updateReport(newReport);
			}
		} catch (SQLException e) {
			Log.REPORT.error("Error saving report");
		} catch (IOException e) {
			Log.REPORT.error("Error saving report");
			e.printStackTrace();
		}
	}

	private void importSubreportsAndImages(List<FileItem> files,
			ReportCard newReport) {
		try {
			
			/*
			 * TODO check images and subreport files
			 * - check all elements of "files" param (ie: files.get(i).isFormField() )
			 * - compare filename requested and filename uploaded
			 *  
			 */					
			
			// get IMAGES
			int nImages = newReport.getImagesName().length;

			// imageByte contains the stream of imagesFiles[]
			byte[][] imageByte = new byte[nImages][];
			// lengthImageByte contains the lengths of all imageByte[]
			int lengthImagesByte[] = new int[nImages];						
			
			for(int i=0; i<nImages; i++) {				
				//loading the image file and putting it in imageByte			
				imageByte[i] = files.get(i).get();				
			}

			// get REPORTS
			int nReports= newReport.getSubreportsNumber()+1; // subreports + 1 master report

			// imageByte contains the stream of imagesFiles[]
			byte[][] reportByte = new byte[nReports][];
			// lengthImageByte contains the lengths of all imageByte[]
			int lengthReportByte[] = new int[nReports];

			for(int i=0; i<nReports-1; i++){
				// load the subreport .jasper file and put it in reportByte
				reportByte[i+1] = files.get(i+nImages).get(); //i+1 because of the master report with index 0
			}

			//check if all files have been uploaded
			boolean fileNotUploaded=false;

			for(int i=0; i<nImages; i++){
				if(imageByte[i]==null)
					fileNotUploaded = true;
			}
			
			for(int i=1; i<nReports; i++){ // must start at 1 because 0 is master report
				if(reportByte[i]==null)
					fileNotUploaded = true;
			}

			if(!fileNotUploaded) {
								
				// IMAGES
				for (int i = 0; i < nImages; i++) {
					lengthImagesByte[i] = imageByte[i].length;
				}

				int totByte = 0; // total n. of bytes needed to store all images 
				for (int i = 0; i < nImages; i++) {
					totByte += lengthImagesByte[i];
				}

				// array of bytes to store into db all reports
				byte[] imagesByte = new byte[totByte];

				int startAt = 0; // determinate position in which starts a new image

				// puts in imageByte all the reports
				for (int i = 0; i < nImages; i++) {
					for (int j = 0; j < lengthImagesByte[i]; j++) {
						imagesByte[startAt + j] = imageByte[i][j];
					}
					startAt += lengthImagesByte[i];
				}


				// REPORTS
				ByteArrayOutputStream os = new ByteArrayOutputStream();
				JasperCompileManager.compileReportToStream(newReport.getJd(), os);
				reportByte[0]=os.toByteArray(); // master report in bytes

				for (int i=0; i < nReports; i++) {
					lengthReportByte[i] = reportByte[i].length;
				}

				totByte = 0; // total n. of bytes needed to store all reports (master and subreports)
				for (int i = 0; i < nReports; i++) {
					totByte += lengthReportByte[i];
				}

				// array of bytes to store into db
				byte[] reportsByte = new byte[totByte];

				startAt = 0; // determinate position in which starts a new report

				// puts in reportByte all the reports
				for (int i = 0; i < nReports; i++) {
					for (int j = 0; j < lengthReportByte[i]; j++) {
						reportsByte[startAt + j] = reportByte[i][j];
					}
					startAt += lengthReportByte[i];
				}
							
				// update query
				JRQuery jrQuery = newReport.getJd().getQuery();
				String query=jrQuery.getText();
				query=query.replaceAll("select", "SELECT");
				query=query.replaceAll("from", "FROM");
				query=query.replaceAll("where", "WHERE");
				query=query.replaceAll("\"", "\\\"");
				
				// update report data
				newReport.setType(ReportType.CUSTOM);
				newReport.setStatus(ElementStatus.ACTIVE);
				newReport.setQuery(query);
				newReport.setBeginDate(new Date());
				newReport.setRichReport(reportsByte);
				newReport.setSimpleReport(reportsByte);
				newReport.setReportLength(lengthReportByte);
				
				if(imageByte!=null){
					newReport.setImages(imagesByte);
					newReport.setImagesLength(lengthImagesByte);
				}
			}
			else {
				throw ReportExceptionType.REPORT_UPLOAD_ERROR.createException();
			}
		} catch (JRException e) {
			Log.REPORT.error("Error compiling report", e);
			throw ReportExceptionType.REPORT_COMPILE_ERROR.createException();
		} catch (NoClassDefFoundError e) {
			Log.REPORT.error("Class not found error", e);
			throw ReportExceptionType.REPORT_NOCLASS_ERROR.createException(e.getMessage());
		}
	}
	
	/**
	 * Reset session, last "import report" operation
	 * 
	 * @param serializer
	 * @return
	 * @throws JSONException
	 */
	@JSONExported
	public void resetSession() throws JSONException {
		new SessionVars().removeNewReport();
	}
}
