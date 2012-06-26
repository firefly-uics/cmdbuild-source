package org.cmdbuild.elements.report;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipOutputStream;

import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.design.JRDesignImage;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.xml.JRXmlWriter;

import org.cmdbuild.elements.wrappers.ReportCard;
import org.cmdbuild.logger.Log;

public class ReportFactoryDB extends ReportFactory {
	
	private ReportCard reportCard; // report bean
	private ReportExtension reportExtension; // pdf,csv ...	
	private List<ReportParameter> reportParameters; // launch parameters
		
	public ReportFactoryDB(int reportId, ReportExtension reportExtension) throws SQLException, IOException, ClassNotFoundException  {
		reportCard = ReportCard.findReportById(reportId);
		this.reportExtension = reportExtension;
	}
		
	public JasperPrint fillReport() throws Exception {
		
		JasperReport[] jra = reportCard.getRichReportJRA();
		JasperReport masterReport = jra[0];

		// init jasper "rendering map"
		Map<String, Object> jasperFillManagerParameters = new LinkedHashMap<String, Object>();
		
		// add parameters to "rendering map"
		for(ReportParameter rp : reportParameters) {
			jasperFillManagerParameters.put(rp.getFullName(), rp.getValue());
		}
		
		// add subreports to "rendering map"
		for(int k=1; k<jra.length; k++) {
			if(jra[k]!=null) {								
				jasperFillManagerParameters.put(ReportFactory.PARAM_SUBREPORT+k, jra[k]);
			}
		}

		// add images to "rendering map"
		InputStream[] isa = reportCard.getImagesISA();
		for(int i=0; i<isa.length; i++) {
			jasperFillManagerParameters.put(ReportFactory.PARAM_IMAGE+i, isa[i]);
		}				
		
		// launch report rendering
		super.fillReport(masterReport,jasperFillManagerParameters);
				
		return jasperPrint;
	}
	
	public ReportExtension getReportExtension() {
		return reportExtension;
	}
	
	public void sendReportToStream(OutputStream outStream) throws Exception {
		if(getReportExtension() == ReportExtension.ZIP)
			writeZip(outStream);
		else
			super.sendReportToStream(outStream);
	}
	
	public ReportCard getReportCard() {
		return reportCard;
	}	

	public void setReportExtension(ReportExtension reportExtension) {
		this.reportExtension = reportExtension;
	}
	
	public List<ReportParameter> getReportParameters() throws ClassNotFoundException, IOException {
		if(reportParameters==null) {
			JasperReport[] jra = reportCard.getRichReportJRA();
			JasperReport masterReport = jra[0];
			reportParameters = new LinkedList<ReportParameter>();
			
			JRParameter[] jrParameters = masterReport.getParameters();
			for(int i=0; i<jrParameters.length; i++) {
				if(jrParameters[i].isForPrompting() && !jrParameters[i].isSystemDefined()) {
					JRParameter jrParameter = jrParameters[i];
					ReportParameter rp = ReportParameter.parseJrParameter(jrParameter);
					reportParameters.add(rp);
				}
			}	
		}
		return reportParameters;
	}
	
	/**
	 * Create a zip file containing all the files (masterreport, subreports, images)
	 * 
	 */
	private void writeZip(OutputStream outStream) throws Exception {
		File zipfile = null;
		List<String> newImagesFilenames = new LinkedList<String>();
		JasperReport[] jra = reportCard.getRichReportJRA();
		
		try {
			// create zip file
			zipfile = File.createTempFile("cmdbuild_report_export", ".zip");
			ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipfile));
			
			// get jasperdesign for master report
			JasperReport masterReport = jra[0];
			JasperDesign jd = jasperReportToJasperDesign(masterReport);
			
			// add images to zip file
			List<JRDesignImage> designImages = getImages(jd);			
			InputStream[] isa = reportCard.getImagesISA();
			String[] origImagesName = reportCard.getImagesName()!=null?reportCard.getImagesName():new String[isa.length]; // original names with check for backward compatibility
			for(int i=0; i<isa.length;i++) {
				
				String imageFilename;
				if(origImagesName[i]!=null) {
					imageFilename = origImagesName[i];
				}
				else { // backward compatibility
					imageFilename = "image"+(i+1)+"."+getImageFormatName(isa[i]);
					origImagesName[i] = imageFilename;
				}
				
				// update image filename
				setImageFilename(designImages.get(i), imageFilename);
								
				// write image
				InputStream is = isa[i];
				newImagesFilenames.add(imageFilename);
				zos.putNextEntry(new ZipEntry(imageFilename));
				for (int b = is.read(); b != -1; b = is.read()) {
	            	zos.write(b);
	            }
	            zos.closeEntry();
			}
			
			// add reports (jrxml) to zip file
			for(int i=0; i<jra.length;i++) {
				
				// add ZIP entry to output stream.
				String filename = jra[i].getName()+".jrxml";
				try {
					zos.putNextEntry(new ZipEntry(filename));
				} catch (ZipException e) {
					Log.REPORT.warn(e);
					continue;
				}
				
				// if master report, update images and subreports paths
				if(i==0) {
					// update images
					prepareDesignImagesForZipExport(designImages,origImagesName);
				    
				    //update subreports					
					prepareDesignSubreportsForZipExport(getSubreports(jd), jra);
				    
				    // compile updated jasperdesign
				    JasperReport newjr = JasperCompileManager.compileReport(jd);
				    
				    // replace jasperreport obj with the new one					    
				    jra[0]=newjr;					    						
				}
				
				// write to zip
				JRXmlWriter.writeReport(jra[i], zos, "UTF-8");
				
				// complete the entry
				zos.closeEntry();
			}
			// close zip stream
			zos.close();
			
			// send final zip to output stream			
			FileInputStream fis = new FileInputStream(zipfile);
			for (int b = fis.read(); b != -1; b = fis.read()) {
				outStream.write(b);
			}
			fis.close();
			
		} finally {
			// delete zip file
			if (zipfile!=null && zipfile.exists())				
				zipfile.delete();
		}
	}		
}