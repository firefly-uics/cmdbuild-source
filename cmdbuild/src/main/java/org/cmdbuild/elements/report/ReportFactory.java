package org.cmdbuild.elements.report;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import net.sf.jasperreports.engine.JRBand;
import net.sf.jasperreports.engine.JRElementGroup;
import net.sf.jasperreports.engine.JRExporter;
import net.sf.jasperreports.engine.JRExporterParameter;
import net.sf.jasperreports.engine.JRGroup;
import net.sf.jasperreports.engine.JRImage;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JRSubreport;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.base.JRBaseReport;
import net.sf.jasperreports.engine.design.JRDesignExpression;
import net.sf.jasperreports.engine.design.JRDesignImage;
import net.sf.jasperreports.engine.design.JRDesignParameter;
import net.sf.jasperreports.engine.design.JRDesignSubreport;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.export.JRCsvExporter;
import net.sf.jasperreports.engine.export.JRCsvExporterParameter;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.engine.export.JRRtfExporter;
import net.sf.jasperreports.engine.export.oasis.JROdtExporter;
import net.sf.jasperreports.engine.xml.JRXmlLoader;
import net.sf.jasperreports.engine.xml.JRXmlWriter;

import org.apache.commons.lang.ArrayUtils;
import org.apache.log4j.Level;
import org.cmdbuild.config.CmdbuildProperties;
import org.cmdbuild.logger.Log;
import org.cmdbuild.services.DBService;

public abstract class ReportFactory {

	private JasperReport jasperReport;
	protected JasperPrint jasperPrint;
	
	/** Log level for jrxml backup (if error occurs) */
	private final Level logLevelForJRXMLBackup = Level.DEBUG;
	
	/** Parameter name (replacement) for images name */
	public static final String PARAM_IMAGE = "IMAGE";
	
	/** Parameter name (replacement) for subreports name */
	public static final String PARAM_SUBREPORT = "SUBREPORT";
		
	/** report types enum */
	public enum ReportType {CUSTOM};
	
	/** report extensions enum */
	public enum ReportExtension {PDF,CSV,ODT,ZIP,RTF};
	
	/** get report extension */
	public abstract ReportExtension getReportExtension();
	
	public abstract JasperPrint fillReport() throws Exception;

	protected JasperPrint fillReport(JasperReport report, Map<String, Object> jasperFillManagerParameters) throws Exception { 
		jasperFillManagerParameters.put(JRParameter.REPORT_LOCALE, getSystemLocale());
		jasperReport = report;
		long start = System.currentTimeMillis();
		try {
			jasperPrint = JasperFillManager.fillReport(report, jasperFillManagerParameters, DBService.getConnection());
		} catch (Exception exception) {
			if(Log.REPORT.getLevel() == logLevelForJRXMLBackup)
				saveJRXMLTmpFile(report);
			throw exception;
		}
		Log.REPORT.debug("REPORT fill time: "+( System.currentTimeMillis()-start)+ " ms" );
		
		return jasperPrint;
	}
	
	private Locale getSystemLocale() {
		return CmdbuildProperties.getInstance().getLocale();
	}

	public void sendReportToStream(OutputStream outStream) throws Exception {
		if(isReportFilled()) {
			JRExporter exporter = null;
			
			switch(getReportExtension()) {
				case PDF:
					exporter = new JRPdfExporter();				
					break;
				
				case CSV:
					exporter = new JRCsvExporter();
					exporter.setParameter(JRCsvExporterParameter.FIELD_DELIMITER, ";");
					break;
					
				case ODT:
					exporter = new JROdtExporter();
					break;
					
				case RTF:
					exporter = new JRRtfExporter();
					break;
			}

			if(exporter!=null) {
				exporter.setParameter(JRExporterParameter.JASPER_PRINT, jasperPrint);
				exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, outStream);
				try {
					exporter.exportReport();
				} catch (Exception exception) {
					if(Log.REPORT.getLevel() == logLevelForJRXMLBackup)
						saveJRXMLTmpFile(jasperReport);
					throw exception;
				}				
			}
		}
	}
	
	
	public String getContentType() {
		switch(getReportExtension()) {
			case PDF:
				return "application/pdf";
			
			case CSV:
				return "text/plain";
				
			case ODT:
				return "application/vnd";
				
			case RTF:
				return "application/rtf";
				
			case ZIP:
				return "application/zip";
				
			default:
				return "";
		}
  	}
	
	public boolean isReportFilled() {
		return jasperPrint!=null;
	}
	
	public JasperPrint getJasperPrint() {
		return jasperPrint;
	}
	
	/**
	 * JasperReport to JasperDesign converter
	 */
	public static JasperDesign jasperReportToJasperDesign(JasperReport masterReport) throws Exception {
		JasperDesign jd = null;
		File masterReportFile = null;
		FileOutputStream fos = null;
		FileInputStream fis = null;
		try {
			masterReportFile = File.createTempFile("cmdbuild_jasper_to_design", ".tmp");
			fos = new FileOutputStream(masterReportFile);
			JRXmlWriter.writeReport(masterReport, fos, "UTF-8");
			fos.flush();
			fos.close();
			fis = new FileInputStream(masterReportFile);
			jd = JRXmlLoader.load(fis);
			fis.close(); 
		} finally {
			if(fos!=null)
				fos.close();
			if(fis!=null)
				fis.close();
			if (masterReportFile!=null && masterReportFile.exists())				
				masterReportFile.delete();
		}
		return jd;
	}	
	
	/**
	 * Search for subreport expression
	 */
	@SuppressWarnings("unchecked")
	public static List<JRSubreport> getSubreports(JasperDesign jd) {		
		// Search parameter indicating IReport subreport's directory
		String subreportDir = new String();
		JRDesignParameter subreportDirPar;
		@SuppressWarnings("rawtypes")
		Map jdMapParameters = jd.getParametersMap();
		if (jdMapParameters.containsKey("SUBREPORT_DIR")) {
			subreportDirPar = (JRDesignParameter) jdMapParameters.get("SUBREPORT_DIR");
			subreportDir = subreportDirPar.getDefaultValueExpression().getText();
		}
		subreportDir = subreportDir.replace("\"", ""); // deleting quotes
		if (!subreportDir.trim().equals(""))
			Log.REPORT.debug("The directory of subreport is: " + subreportDir);
							
		// Expressions
		List<JRSubreport> subreportsList = new LinkedList<JRSubreport>();
		for(JRBand band : getBands(jd)) {
			if(band!=null && band.getChildren()!=null) {
				searchSubreports(band.getChildren(),subreportsList); // adds in subreportList the JRSubreport found
			}
		}
		Log.REPORT.debug("In the report there are " + subreportsList.size() +" subreports");
		
		return subreportsList;
	}
	
	/**
	 * Search for images expression
	 */
	@SuppressWarnings("unchecked")
	public static List<JRDesignImage> getImages(JRBaseReport report) {
		List<JRDesignImage> designImagesList = new LinkedList<JRDesignImage>();
		
		for(JRBand band : getBands(report)) {
			if(band!=null && band.getChildren()!=null) {
				searchImages(band.getChildren(),designImagesList); // adds in imagesList the JRImages founded
			}
		}
		Log.REPORT.debug("In the report there are " + designImagesList.size() +" images");
		
		return designImagesList;
	}

	public static String getImageFileName(JRImage jrImage){
		String filename = "";
		String path = jrImage.getExpression().getText().replaceAll("\"", "");
		if(!path.trim().equals("")){
			path=path.replaceAll("[\\\\]", "/");
			StringTokenizer tokenizer = new StringTokenizer(path, "/");
			int totToken=tokenizer.countTokens();
			for(int i=0; i<totToken;i++)
				filename=tokenizer.nextToken();
		}
		return filename;
	}

	public static void setImageFilename(JRImage jrImage, String newValue) {
		JRDesignExpression newImageExpr = new JRDesignExpression();						
		newImageExpr.setText(newValue);
		newImageExpr.setValueClassName(jrImage.getExpression().getValueClassName());
		((JRDesignImage)jrImage).setExpression(newImageExpr);
	}
	
	public static String getSubreportName(JRSubreport jrSubreport) {
		String srExpr = jrSubreport.getExpression().getText();
		String subreportPath;
		subreportPath = srExpr.replaceAll("\\$P\\{SUBREPORT_DIR\\}", "");
		subreportPath = subreportPath.replaceAll("\\+", ""); // substituting plus with separator
		subreportPath = subreportPath.replaceAll("[ \"]", ""); // removing spaces, quotes
		
		return subreportPath;
	}
	
	/**
	 * Update images expressions before uploading to DB; set names like "IMAGE1", "IMAGE2" etc
	 * 
	 */	
	public static void prepareDesignImagesForUpload(List<JRDesignImage> designImagesList) {
		for(int i=0; i<designImagesList.size(); i++) {
			JRDesignImage jrImage = designImagesList.get(i);
			
			// set expression
			JRDesignExpression newImageExpr = new JRDesignExpression();
			String newImageName = PARAM_IMAGE+i;
			newImageExpr.setText("$P{REPORT_PARAMETERS_MAP}.get(\"" + newImageName + "\")");
			newImageExpr.setValueClassName("java.io.InputStream");
			((JRDesignImage)jrImage).setExpression(newImageExpr);
			
			// set options
			((JRDesignImage)jrImage).setUsingCache(true);
			((JRDesignImage)jrImage).setOnErrorType(JRImage.ON_ERROR_TYPE_BLANK);
		}
	}
	
	/**
	 * Update images expressions before downloading (zip export); set original images name
	 * 
	 */	
	public static void prepareDesignImagesForZipExport(List<JRDesignImage> designImagesList, String[] origImagesName) {
		for(int i=0; i<designImagesList.size(); i++) {
			JRDesignImage jrImage = designImagesList.get(i);
			JRDesignExpression newImageExpr = new JRDesignExpression();
			newImageExpr.setText("\""+origImagesName[i]+"\"");
			newImageExpr.setValueClassName("java.lang.String");
			((JRDesignImage)jrImage).setExpression(newImageExpr);
		}
	}	
		
	/**
	 * Update subreport expressions before uploadng to DB; set names like "SUBREPORT1", "SUBREPORT2" etc
	 * 
	 */
	public static void prepareDesignSubreportsForUpload(List<JRSubreport> subreportsList) {
		for(int i=0; i<subreportsList.size(); i++) {
			JRDesignSubreport jrSubreport = (JRDesignSubreport) subreportsList.get(i);
			JRDesignExpression newExpr = new JRDesignExpression();
			String newSubreportName = PARAM_SUBREPORT + (i+1);
			newExpr.setText("$P{REPORT_PARAMETERS_MAP}.get(\"" + newSubreportName + "\")");
			newExpr.setValueClassName("net.sf.jasperreports.engine.JasperReport");
			jrSubreport.setExpression(newExpr);
		}
	}
	
	/**
	 * Update subreport expressions before downloading (zip export); set original names
	 * 
	 */
	public static void prepareDesignSubreportsForZipExport(List<JRSubreport> designSubreports, JasperReport[] jasperSubreports) {
		for(int i=0; i<designSubreports.size(); i++) {
			JRDesignSubreport jrSubreport = (JRDesignSubreport) designSubreports.get(i);
			String subreportName = jasperSubreports[i+1].getName()+".jasper"; // 0 = master report
			JRDesignExpression newExpr = new JRDesignExpression();
			newExpr.setText("\""+ subreportName +"\"");
			newExpr.setValueClassName("java.lang.String");
			jrSubreport.setExpression(newExpr);
		}
	}
	
	/**
	 * Check duplicate images
	 */
	public static boolean checkDuplicateImages(List<JRDesignImage> designImages) {
		for(int i=0; i<designImages.size(); i++) {
			JRDesignImage image1 = designImages.get(i);
			String filename1 = getImageFileName(image1);
			for(int j=(i+1); j<designImages.size(); j++) {
				JRDesignImage image2 = designImages.get(j);
				String filename2 = getImageFileName(image2);
				if(filename1.equalsIgnoreCase(filename2)) {
					return true;
				}				
			}
		}
		return false;
	}
	
	/**
	 * Read image and get format name (png,gif,jpg) 
	 */
	public static String getImageFormatName(InputStream is) throws IOException {
		String format = "";
		ImageInputStream iis = null;
		try {
			iis = ImageIO.createImageInputStream(is);
			Iterator<ImageReader> readerIterator = ImageIO.getImageReaders(iis);
			if (readerIterator.hasNext()) {
				ImageReader reader = (ImageReader) readerIterator.next();
				format = reader.getFormatName();
			}
			iis.flush();
		} finally {
			iis.close();
		}
		is.reset();
		return format;
	}
	
	/**
	 * Get all the bands of the report
	 */
	public static List<JRBand> getBands(JRBaseReport jasperDesign) { 
		List<JRBand> bands = new LinkedList<JRBand>();
		bands.add(jasperDesign.getTitle());
		bands.add(jasperDesign.getPageHeader());
		bands.add(jasperDesign.getColumnHeader());
		for(JRBand detail : jasperDesign.getDetailSection().getBands()) {
			bands.add(detail);	
		}
		bands.add(jasperDesign.getColumnFooter());
		bands.add(jasperDesign.getPageFooter());
		bands.add(jasperDesign.getLastPageFooter());
		bands.add(jasperDesign.getSummary());		
		for(JRGroup group : jasperDesign.getGroups()) {
			for(JRBand band : (JRBand[]) ArrayUtils.addAll(group.getGroupFooterSection().getBands(), group.getGroupHeaderSection().getBands())) {
				bands.add(band);
			}
		}
		return bands;
	}

	/**
	 * Search for a JRImage in all the children of the input list
	 * and put it into a List.
	 * 
	 * @param elements: the children of a JRElementGroup considered
	 */
	@SuppressWarnings("unchecked")
	private static void searchImages(List<Object> elements, List<JRDesignImage> imagesList) {
		Iterator<Object> i = elements.listIterator();
		while (i.hasNext()) {
			Object jreg = i.next();
			if (jreg instanceof JRDesignImage)
				imagesList.add((JRDesignImage) jreg);
			else if (jreg instanceof JRElementGroup)
				searchImages(((JRElementGroup) jreg).getChildren(),imagesList);
		}
	}

	/**
	 * Search for a JRSubreport in all the children of the input list
	 * and put it into a List.
	 * 
	 * @param elements: the children of a JRElementGroup considered
	 */
	@SuppressWarnings("unchecked")
	private static void searchSubreports(List<Object> elements,List<JRSubreport> subreportsList) {
		Iterator<Object> i = elements.listIterator();
		while (i.hasNext()) {
			Object jreg = i.next();
			if (jreg instanceof JRSubreport)
				subreportsList.add((JRSubreport) jreg);
			else if (jreg instanceof JRElementGroup)
				searchSubreports(((JRElementGroup) jreg).getChildren(), subreportsList);
		}
	}
	
	/**
	 * Save .jrxml file
	 */
	private File saveJRXMLTmpFile(JasperReport report) throws Exception {
		File tmpFile = File.createTempFile("cmdbuild_report_debug", ".jrxml");
		FileOutputStream fos = new FileOutputStream(tmpFile);
		JRXmlWriter.writeReport(report, fos, "UTF-8");
		fos.flush();
		fos.close();
		Log.REPORT.debug("REPORT jrxml file: " + tmpFile.getAbsolutePath());
		return tmpFile;
	}

}
