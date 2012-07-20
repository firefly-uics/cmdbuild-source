package org.cmdbuild.servlets.json.management;

import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.activation.DataHandler;
import javax.activation.DataSource;

import org.cmdbuild.common.utils.TempDataSource;
import org.cmdbuild.elements.interfaces.CardQuery;
import org.cmdbuild.elements.interfaces.IAttribute;
import org.cmdbuild.elements.interfaces.ICard;
import org.cmdbuild.elements.interfaces.ITableFactory;
import org.cmdbuild.elements.report.ReportFacade;
import org.cmdbuild.elements.report.ReportFactory;
import org.cmdbuild.elements.report.ReportFactory.ReportExtension;
import org.cmdbuild.elements.report.ReportFactory.ReportType;
import org.cmdbuild.elements.report.ReportFactoryDB;
import org.cmdbuild.elements.report.ReportFactoryTemplate;
import org.cmdbuild.elements.report.ReportFactoryTemplateDetail;
import org.cmdbuild.elements.report.ReportFactoryTemplateList;
import org.cmdbuild.elements.report.ReportParameter;
import org.cmdbuild.elements.wrappers.GroupCard;
import org.cmdbuild.elements.wrappers.ReportCard;
import org.cmdbuild.exception.ReportException.ReportExceptionType;
import org.cmdbuild.logger.Log;
import org.cmdbuild.services.SessionVars;
import org.cmdbuild.services.auth.UserContext;
import org.cmdbuild.servlets.json.JSONBase;
import org.cmdbuild.servlets.json.serializers.Serializer;
import org.cmdbuild.servlets.utils.Parameter;
import org.cmdbuild.utils.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ModReport extends JSONBase {
	
	private static final long serialVersionUID = 1L;

	private static ReportFacade reportFacade = new ReportFacade();

	@JSONExported
	public JSONArray getReportTypesTree(
			Map<String, String> params) throws JSONException {
		JSONArray rows = new JSONArray();
		for (String type : reportFacade.getReportTypes()) {
			JSONObject jsonObj = new JSONObject();
			jsonObj.put("id", type);
			jsonObj.put("text", type);
			jsonObj.put("type", "report");
			jsonObj.put("leaf", true);
			jsonObj.put("cls", "file");
			jsonObj.put("selectable", true);
			rows.put(jsonObj);
		}
		return rows;        	
	}
	
	@JSONExported
	public String getReportTypes(
			Map<String, String> params) throws JSONException {
		JSONObject serializer = new JSONObject();
		for (String type : reportFacade.getReportTypes()) {
			JSONObject jsonObj = new JSONObject();
			jsonObj.put("id", type);
    		serializer.append("rows", jsonObj);    		
		}
				
		return serializer.toString();        	
	}
	
	@JSONExported
	public JSONObject getReportsByType(
			JSONObject serializer,
			UserContext userCtx,
			@Parameter("type") String reportType,
			@Parameter("limit") int limit,
			@Parameter("start") int offset) throws JSONException {
		JSONArray rows = new JSONArray();
		int numRecords = 0;
		for (ReportCard report : ReportCard.findReportsByType(ReportType.valueOf(reportType.toUpperCase()))) {
			if (report.isUserAllowed(userCtx)) {
				++numRecords;
				if (numRecords > offset && numRecords <= offset+limit)
					rows.put(serializeReport(report));
			}
		}
		serializer.put("rows", rows);
		serializer.put("results", numRecords);
		return serializer;
	}

	@JSONExported
	public JSONObject getGroups(Map<String, String> params) throws JSONException {
		JSONObject serializer = new JSONObject();
       	for (GroupCard group : GroupCard.allActive()) {
			JSONObject jsonGroup = new JSONObject();
			jsonGroup.put("id", group.getAttributeValue("Id"));
			jsonGroup.put("description", group.getAttributeValue("Description"));
			serializer.append("rows", jsonGroup);
		}
       	return serializer;
	}
	
	@JSONExported
	public JSONObject createReportFactoryByTypeCode(
			UserContext userCtx,
			JSONObject serializer,
			@Parameter("type") String type,
			@Parameter("code") String code,
			ITableFactory tf) throws Exception {
		
		ReportCard reportCard = null;		
		for(ReportCard report : ReportCard.findReportsByType(ReportType.valueOf(type.toUpperCase()))) {
			if(report.getCode().equalsIgnoreCase(code)) {
				reportCard = report;
				break;
			}
		}
		
		if(reportCard == null)
			throw ReportExceptionType.REPORT_NOTFOUND.createException(code);
		
		if(!reportCard.isUserAllowed(userCtx)) {
			String groups = StringUtils.join(userCtx.getGroups(), ", ");
			throw ReportExceptionType.REPORT_GROUPNOTALLOWED.createException(groups, reportCard.getCode());
		}

		ReportFactoryDB factory = null;
		if(type.equalsIgnoreCase(ReportType.CUSTOM.toString())) {
			factory = new ReportFactoryDB(reportCard.getId(),null);
			boolean filled = false;
			if (factory.getReportParameters().isEmpty()) {
				factory.fillReport();
				filled = true;
			} else {
				for(ReportParameter reportParameter : factory.getReportParameters()) {
					IAttribute attribute = reportParameter.createCMDBuildAttribute(tf);
					serializer.append("attribute", Serializer.serializeAttribute(attribute));
				}
			}
			serializer.put("filled", filled);
		}
		new SessionVars().setReportFactory(factory);
		return serializer;
	}
	
	/**
	 * Create report factory obj
	 */
	@JSONExported
	public JSONObject createReportFactory(
			JSONObject serializer,
			@Parameter("type") String type,
			@Parameter("id") int id,
			@Parameter("extension") String extension,
			ITableFactory tf) throws Exception {
		ReportFactoryDB reportFactory=null;
		
		if(ReportType.valueOf(type.toUpperCase()) == ReportType.CUSTOM) {
			ReportExtension reportExtension = ReportExtension.valueOf(extension.toUpperCase());
			reportFactory = new ReportFactoryDB(id, reportExtension);

			// if zip extension, do not compile
			if(reportExtension == ReportExtension.ZIP) {
				serializer.put("filled", true);
			}
			
			else {
				// if no parameters
				if(reportFactory.getReportParameters().isEmpty()) {
					reportFactory.fillReport();
					serializer.put("filled", true);
				} 
				
				// else, prepare required parameters
				else {
					serializer.put("filled", false);
					for(ReportParameter reportParameter : reportFactory.getReportParameters()) {
						IAttribute attribute = reportParameter.createCMDBuildAttribute(tf);
						serializer.append("attribute", Serializer.serializeAttribute(attribute));
					}
				}
			}
		}
						
		new SessionVars().setReportFactory(reportFactory);
		return serializer;	
	}
		
	
	/**
	 * Set user-defined parameters and fill report
	 * @throws Exception 
	 */
	@JSONExported
	public JSONObject updateReportFactoryParams(
			JSONObject serializer,
			Map<String,String> formParameters,
			ITableFactory tf) throws Exception {
		ReportFactoryDB reportFactory = (ReportFactoryDB) new SessionVars().getReportFactory();
		if (formParameters.containsKey("reportExtension")) {
			reportFactory.setReportExtension(ReportExtension.valueOf(formParameters.get("reportExtension").toUpperCase()));
		}
		
		for(ReportParameter reportParameter : reportFactory.getReportParameters()) {
			// update parameter
			reportParameter.parseValue(formParameters.get(reportParameter.getFullName()));
			Log.REPORT.debug("Setting parameter "+reportParameter.getFullName()+": "+reportParameter.getValue());
		}
		
		reportFactory.fillReport();
		new SessionVars().setReportFactory(reportFactory);
		return serializer;	
	}
	
	
	/**
	 * Print report to output stream
	 */
	@JSONExported
	public DataHandler printReportFactory(
			@Parameter(value="donotdelete",required=false) boolean notDelete //this may be requested for wf server side processing
			) throws Exception {
		ReportFactory reportFactory = new SessionVars().getReportFactory();
		// TODO: report filename should be always read from jasperPrint obj
		// get report filename
		String filename = "";
		if (reportFactory instanceof ReportFactoryDB) {
			ReportFactoryDB reportFactoryDB = (ReportFactoryDB) reportFactory;
			filename = reportFactoryDB.getReportCard().getCode().replaceAll(" " , "");
		} else if(reportFactory instanceof ReportFactoryTemplate) {
			ReportFactoryTemplate reportFactoryTemplate = (ReportFactoryTemplate) reportFactory;
			filename = reportFactoryTemplate.getJasperPrint().getName();
		}
		
		// add extension
		filename += "." + reportFactory.getReportExtension().toString().toLowerCase();
		
		// send to stream
		DataSource dataSource = TempDataSource.create(filename, reportFactory.getContentType());
		OutputStream outputStream = dataSource.getOutputStream();
		reportFactory.sendReportToStream(outputStream);		
		outputStream.flush();
		outputStream.close();
		
		if (!notDelete) {
			new SessionVars().removeReportFactory();
		}
		
		return new DataHandler(dataSource);
	}

	@JSONExported
	public JSONObject deleteReport(
			JSONObject serializer,
			@Parameter("id") int id) throws JSONException {
		ReportCard report = ReportCard.findReportById(id);
		report.delete();
		return serializer;
	}
	
	/**
	 * Print cards on screen
	 */
    @JSONExported
    public void printCurrentView(
                    @Parameter("columns") JSONArray columns,
                    @Parameter("type") String type,
                    CardQuery cardQuery) throws Exception {
    	final List<String> attributeOrder = jsonArrayToStringList(columns);
    	final CardQuery reportQuery = ((CardQuery) cardQuery.clone()).limit(0);
		final ReportFactoryTemplateList rft = new ReportFactoryTemplateList(ReportExtension.valueOf(type.toUpperCase()), reportQuery, attributeOrder, cardQuery.getTable());
		rft.fillReport();
		new SessionVars().setReportFactory(rft);
    }

    private List<String> jsonArrayToStringList(JSONArray columns) throws JSONException {
            List<String> attributeOrder = new LinkedList<String>();
            for (int i=0; i<columns.length(); ++i) {
                    attributeOrder.add(columns.getString(i));
            }
            return attributeOrder;
    }
	
	private JSONObject serializeReport(ReportCard report) throws JSONException {
		JSONObject serializer = null;
		
		serializer = new JSONObject();
		serializer.put("id", report.getId());
		serializer.put("title", report.getCode());
		serializer.put("description", report.getDescription());
		serializer.put("type", report.getType());
		serializer.put("query", report.getQuery());		
		serializer.put("groups", report.getSelectedGroups());
		
		return serializer;
	}
	
	 @JSONExported
    public JSONObject printCardDetails(
    				@Parameter("format") String format,
    				UserContext userCtx,
                    JSONObject serializer,
                    ICard card) throws Exception {

		ReportFactoryTemplateDetail rftd = new ReportFactoryTemplateDetail(card, userCtx, ReportExtension.valueOf(format.toUpperCase()));
		rftd.fillReport();
		new SessionVars().setReportFactory(rftd);
		return serializer;
    }
	
}
