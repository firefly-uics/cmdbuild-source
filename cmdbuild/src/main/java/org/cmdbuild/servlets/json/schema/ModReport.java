package org.cmdbuild.servlets.json.schema;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JRQuery;
import net.sf.jasperreports.engine.JRSubreport;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.design.JRDesignImage;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.xml.JRXmlLoader;

import org.apache.commons.fileupload.FileItem;
import org.cmdbuild.common.annotations.OldDao;
import org.cmdbuild.elements.interfaces.IAbstractElement.ElementStatus;
import org.cmdbuild.elements.report.ReportFactory;
import org.cmdbuild.elements.report.ReportFactory.ReportExtension;
import org.cmdbuild.elements.report.ReportFactory.ReportType;
import org.cmdbuild.elements.report.ReportFactoryTemplateSchema;
import org.cmdbuild.elements.report.ReportParameter;
import org.cmdbuild.exception.AuthException;
import org.cmdbuild.exception.NotFoundException;
import org.cmdbuild.exception.ReportException.ReportExceptionType;
import org.cmdbuild.logger.Log;
import org.cmdbuild.logic.TemporaryObjectsBeforeSpringDI;
import org.cmdbuild.model.Report;
import org.cmdbuild.services.SessionVars;
import org.cmdbuild.services.store.report.ReportStore;
import org.cmdbuild.servlets.json.JSONBase;
import org.cmdbuild.servlets.utils.MethodParameterResolver;
import org.cmdbuild.servlets.utils.Parameter;
import org.cmdbuild.servlets.utils.Request;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ModReport extends JSONBase {

	@JSONExported
	public JSONArray menuTree() throws JSONException, AuthException {
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

		return serializer;
	}

	/**
	 * Print a report that
	 * lists all the classes
	 * 
	 * @param format
	 * @throws Exception
	 */
	@JSONExported
	public void printSchema( //
			@Parameter(PARAMETER_FORMAT) String format //
	) throws Exception {
		ReportFactoryTemplateSchema rfts = new ReportFactoryTemplateSchema(ReportExtension.valueOf(format.toUpperCase()));
		rfts.fillReport();
		new SessionVars().setReportFactory(rfts);
	}

	/**
	 * Print a report with the
	 * detail of a class
	 * 
	 * @param format
	 * @throws Exception
	 */
	@JSONExported
	public void printClassSchema(
			@Parameter(PARAMETER_CLASS_NAME) final String className,
			@Parameter(PARAMETER_FORMAT) final String format
	) throws Exception {
		ReportFactoryTemplateSchema rfts = new ReportFactoryTemplateSchema(ReportExtension.valueOf(format.toUpperCase()), className);
		rfts.fillReport();
		new SessionVars().setReportFactory(rfts);
	}

	/**
	 * 
	 * Is the first step of the report upload
	 * Analyzes the JRXML and eventually return
	 * the configuration of the second step 
	 */
	
	@Admin
	@JSONExported
	public JSONObject analyzeJasperReport ( //
			@Parameter(PARAMETER_NAME) String name, //
			@Parameter(PARAMETER_DESCRIPTION) String description, //
			@Parameter(PARAMETER_GROUS) String groups, //
			@Parameter(PARAMETER_REPORT_ID) int reportId, //
			@Parameter(value=PARAMETER_JRXML, required=false) FileItem file //
			) throws JSONException, NotFoundException {

		resetSession();
		Report newReport = new Report();
		setReportSimpleAttributes(name, description, groups, reportId, newReport);

		final JSONObject out = new JSONObject(); 
		if (file.getSize() > 0) {
			setReportImagesAndSubReports(out, file, newReport);
		} else {
			// there is no second step
			out.put("skipSecondStep", true);
		}

		new SessionVars().setNewReport(newReport);
		return out;
	}

	private void setReportImagesAndSubReports(JSONObject serializer,
			FileItem file, Report newReport) throws JSONException {
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
			String groups, int reportId, Report newReport) {
		newReport.setOriginalId(reportId);
		newReport.setCode(name);
		newReport.setDescription(description);
		newReport.setGroups(parseSelectedGroup(groups));
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

	private String[] parseSelectedGroup(String groups) {
		final String[] stringGroups;
		if (groups!=null && !groups.equals("")) {
			stringGroups = groups.split(",");
		} else {
			stringGroups = new String[0];
		}

		return stringGroups;
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
	/**
	 * Is the second step of the report
	 * import. Manage the sub reports and
	 * the images
	 * 
	 * @param files
	 * @throws JSONException
	 * @throws AuthException
	 */
	public void importJasperReport(
			@Request(MethodParameterResolver.MultipartRequest) List<FileItem> files)
			throws JSONException, AuthException {
		Report newReport = new SessionVars().getNewReport();

		if (newReport.getJd() != null) {
			importSubreportsAndImages(files, newReport);
		}

		saveReport(newReport);
		resetSession();
	}

	@OldDao
	@Admin
	@JSONExported
	public void saveJasperReport() {
		Report newReport = new SessionVars().getNewReport();
		saveReport(newReport);
	}

	private void saveReport(final Report newReport) {
		final ReportStore reportStore = TemporaryObjectsBeforeSpringDI.getReportStore();
		try {
			if (newReport.getOriginalId() < 0) {
				reportStore.insertReport(newReport);
			} else {
				reportStore.updateReport(newReport);
			}
		} catch (SQLException e) {
			Log.REPORT.error("Error saving report");
		} catch (IOException e) {
			Log.REPORT.error("Error saving report");
			e.printStackTrace();
		}
	}

	private void importSubreportsAndImages(final List<FileItem> files,
			Report newReport) {
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
			Integer lengthImagesByte[] = new Integer[nImages];

			for(int i=0; i<nImages; i++) {
				//loading the image file and putting it in imageByte
				imageByte[i] = files.get(i).get();
			}

			// get REPORTS
			int nReports= newReport.getSubreportsNumber()+1; // subreports + 1 master report

			// imageByte contains the stream of imagesFiles[]
			byte[][] reportByte = new byte[nReports][];
			// lengthImageByte contains the lengths of all imageByte[]
			Integer lengthReportByte[] = new Integer[nReports];

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

				// update report data
				newReport.setType(ReportType.CUSTOM);
				newReport.setStatus(ElementStatus.ACTIVE.toString());
				newReport.setRichReport(reportsByte);
				newReport.setSimpleReport(reportsByte);
				newReport.setReportLength(lengthReportByte);
				newReport.setBeginDate(new Date());

				// update query
				JRQuery jrQuery = newReport.getJd().getQuery();
				if (jrQuery != null) {
					final String query = jrQuery.getText();
					query.replaceAll("\"", "\\\"");
					newReport.setQuery(query);
				}

				if (imageByte!=null) {
					newReport.setImages(imagesByte);
					newReport.setImagesLength(lengthImagesByte);
				}
			} else {
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

	@OldDao
	@JSONExported
	public void deleteReport(
			@Parameter(PARAMETER_ID) final int id) throws JSONException {
		final ReportStore reportStore = TemporaryObjectsBeforeSpringDI.getReportStore();
		reportStore.deleteReport(id);
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