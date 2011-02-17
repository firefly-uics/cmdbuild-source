package org.cmdbuild.elements.wrappers;

import static org.cmdbuild.utils.BinaryUtils.fromByte;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.design.JasperDesign;

import org.cmdbuild.dao.type.ByteArray;
import org.cmdbuild.dao.type.IntArray;
import org.cmdbuild.dao.type.StringArray;
import org.cmdbuild.elements.filters.AttributeFilter.AttributeFilterType;
import org.cmdbuild.elements.filters.OrderFilter.OrderFilterType;
import org.cmdbuild.elements.interfaces.ICard;
import org.cmdbuild.elements.interfaces.ITable;
import org.cmdbuild.elements.proxy.CardForwarder;
import org.cmdbuild.elements.report.ReportFactory.ReportType;
import org.cmdbuild.exception.NotFoundException;
import org.cmdbuild.exception.ORMException;
import org.cmdbuild.services.auth.UserContext;

public class ReportCard extends CardForwarder {
	
	public static final String REPORT_CLASS_NAME = "Report";
	private static final ITable reportClass = UserContext.systemContext().tables().get(REPORT_CLASS_NAME);

	private static final long serialVersionUID = 1L;		

	// number of subreport elements (administration side)
	private int subreportsNumber = -1;
	
	// jasper design created from uploaded file (administration side)
    private JasperDesign jd = null;
    
    // id of the report we're editing, "-1" if it's a new one (administration side)
    private int originalId = -1;

	public ReportCard() throws NotFoundException {
		super(reportClass.cards().create());
	}

	public ReportCard(ICard card) throws NotFoundException {
		super(card);
	}

	public static List<ReportCard> findAll() throws NotFoundException, ORMException {
		List<ReportCard> list = new ArrayList<ReportCard>();
		for(ICard card : UserContext.systemContext().tables().get(REPORT_CLASS_NAME).cards().list())
			list.add(new ReportCard(card));
		return list;
	}	
	
	public static List<ReportCard> findReportsByType(ReportType type) throws ORMException {		
		List<ReportCard> list = new ArrayList<ReportCard>();
		for(ICard card : UserContext.systemContext().tables()
				.get(REPORT_CLASS_NAME).cards().list()
				.filter("Type", AttributeFilterType.EQUALS, type.toString().toLowerCase())
				.order(CardAttributes.Code.toString(), OrderFilterType.ASC))
			list.add(new ReportCard(card));
		return list;
	}
	
	public static ReportCard findReportById(int id) throws NotFoundException, ORMException {
		return new ReportCard(UserContext.systemContext().tables().get(REPORT_CLASS_NAME).cards().get(id));
	}

	public ReportType getType() {
		return ReportType.valueOf(getAttributeValue("Type").getString().toUpperCase());
	}
	
	public void setType(ReportType type) {
		getAttributeValue("Type").setValue(type.toString().toLowerCase());
	}
		
	public String getQuery(){
		return getAttributeValue("Query").getString();
	}	
	
	public void setQuery(String value){
		getAttributeValue("Query").setValue(value);
	}	
	
	public int getOriginalId() {
		return originalId;
	}
	
	public void setOriginalId(int originalId) {
		this.originalId = originalId;
	}

	public JasperDesign getJd() {
		return jd;
	}

	public void setJd(JasperDesign jd) {
		this.jd = jd;
	}
	
	public void setSelectedGroups(int[] newvalue) {
		getAttributeValue("Groups").setValue(IntArray.valueOf(newvalue));
	}

	public int[] getSelectedGroups() {		
		return getAttributeValue("Groups").getIntArrayValue();
	}
	
	/**
	 * Get rich report as byte array
	 */
	public byte[] getRichReportBA() {
		return getAttributeValue("RichReport").getBinary();
	}
	
	/**
	 * Get rich report as JasperReport objects array
	 */
	public JasperReport[] getRichReportJRA() throws ClassNotFoundException, IOException {		
		int parseLength = 0;
		byte[] singleBin = null;
		int[] length = getReportLength();
		JasperReport[] obj = new JasperReport[length.length];		
		byte[] bin = getRichReportBA();
		
		//splits the reports in master and subreports
		for(int i=0; i<length.length; i++){
			singleBin = new byte[length[i]];
			for(int j=0; j<length[i]; j++){
				singleBin[j]=bin[parseLength+j];
			}
			parseLength += length[i];
			if(singleBin!=null&&length[i]>0)
				obj[i] = (JasperReport) fromByte(singleBin);
		}
		
		return obj;
	}

	public void setRichReport(byte[] richReport) {		
		getAttributeValue("RichReport").setValue(ByteArray.valueOf(richReport));
	}

	public byte[] getSimpleReport() {
		return getAttributeValue("SimpleReport").getBinary();
	}

	public void setSimpleReport(byte[] simpleReport) {
		getAttributeValue("SimpleReport").setValue(ByteArray.valueOf(simpleReport));
	}

	public byte[] getWizard() {
		return getAttributeValue("Wizard").getBinary();
	}

	public void setWizard(byte[] wizard) {
		getAttributeValue("Wizard").setValue(ByteArray.valueOf(wizard));
	}

	/**
	 * Get report images as byte array
	 */
	public byte[] getImagesBA() {
		return getAttributeValue("Images").getBinary();
	}
	
	/**
	 * Get report images as input stream array
	 */
	public InputStream[] getImagesISA() {
		byte[] binary = getImagesBA();
		int[] imagesLength = getImagesLength();
		InputStream[] obj = new InputStream[0];
		if(imagesLength!=null) {
			obj = new InputStream[imagesLength.length];
			int parseLength = 0;
			byte[] singleBin = null;
			
			//splits the images
			for(int i=0; i<imagesLength.length; i++) {
				singleBin = new byte[imagesLength[i]];
				for(int j=0; j<imagesLength[i]; j++) {
					singleBin[j] = binary[parseLength+j];
				}
				parseLength += imagesLength[i];
				if(singleBin!=null && imagesLength[i]>0)
					obj[i] = new ByteArrayInputStream(singleBin);
			}
		}
		return obj;
	}

	public void setImages(byte[] images) {
		getAttributeValue("Images").setValue(ByteArray.valueOf(images));
	}

	public int[] getReportLength() {
		return getAttributeValue("ReportLength").getIntArrayValue();
	}

	public void setReportLength(int[] subLength) {
		getAttributeValue("ReportLength").setValue(IntArray.valueOf(subLength));
	}

	public int[] getImagesLength() {
		return getAttributeValue("ImagesLength").getIntArrayValue();
	}

	public void setImagesLength(int[] imagesLength) {
		getAttributeValue("ImagesLength").setValue(IntArray.valueOf(imagesLength));
	}

	public String[] getImagesName() {
		return getAttributeValue("ImagesName").getStringArrayValue();
	}
	
	public void setImagesName(String[] imagesNames) {
		getAttributeValue("ImagesName").setValue(StringArray.valueOf(imagesNames));
	}

	public void setSubreportsNumber(int subreportsNumber) {
		this.subreportsNumber = subreportsNumber;
	}

	public int getSubreportsNumber() {
		return subreportsNumber;
	}

	public boolean isUserAllowed(UserContext userCtx) {
		boolean allowed = false;
		if (userCtx.privileges().isAdmin()) {
			allowed = true;
		} else {
			int[] groupsAllowed = this.getSelectedGroups();
			if (groupsAllowed != null){
				for (int i = groupsAllowed.length -1; i>=0; --i) {
					if (userCtx.belongsTo(groupsAllowed[i])) {
						allowed = true;
						break;
					}
				}
			}
		}
		return allowed;
	}
}
