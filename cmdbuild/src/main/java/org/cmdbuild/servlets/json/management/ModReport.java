package org.cmdbuild.servlets.json.management;

import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.activation.DataHandler;
import javax.activation.DataSource;

import org.cmdbuild.common.annotations.OldDao;
import org.cmdbuild.common.utils.TempDataSource;
import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.elements.interfaces.IAttribute;
import org.cmdbuild.elements.interfaces.ITableFactory;
import org.cmdbuild.elements.report.ReportFactory;
import org.cmdbuild.elements.report.ReportFactory.ReportExtension;
import org.cmdbuild.elements.report.ReportFactory.ReportType;
import org.cmdbuild.elements.report.ReportFactoryDB;
import org.cmdbuild.elements.report.ReportFactoryTemplate;
import org.cmdbuild.elements.report.ReportFactoryTemplateDetail;
import org.cmdbuild.elements.report.ReportFactoryTemplateList;
import org.cmdbuild.elements.report.ReportParameter;
import org.cmdbuild.exception.ReportException.ReportExceptionType;
import org.cmdbuild.logger.Log;
import org.cmdbuild.logic.TemporaryObjectsBeforeSpringDI;
import org.cmdbuild.logic.data.QueryOptions;
import org.cmdbuild.model.Report;
import org.cmdbuild.services.SessionVars;
import org.cmdbuild.services.auth.UserContext;
import org.cmdbuild.services.store.report.ReportStore;
import org.cmdbuild.servlets.json.JSONBase;
import org.cmdbuild.servlets.json.serializers.AttributeSerializer;
import org.cmdbuild.servlets.json.serializers.ReportSerializer;
import org.cmdbuild.servlets.utils.Parameter;
import org.cmdbuild.utils.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ModReport extends JSONBase {

	private static final long serialVersionUID = 1L;


	@OldDao
	@JSONExported
	public JSONArray getReportTypesTree(final Map<String, String> params) throws JSONException {
		final ReportStore reportStore = TemporaryObjectsBeforeSpringDI.getReportStore();
		final JSONArray rows = new JSONArray();
		for (final String type: reportStore.getReportTypes()) {
			final JSONObject jsonObj = new JSONObject();
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

	@OldDao
	@JSONExported
	public JSONObject getReportsByType( //
			@Parameter(PARAMETER_TYPE) final String reportType, //
			@Parameter(PARAMETER_LIMIT) final int limit,
			@Parameter(PARAMETER_START) final int offset) throws JSONException {

		final ReportStore reportStore = TemporaryObjectsBeforeSpringDI.getReportStore();
		final JSONArray rows = new JSONArray();
		int numRecords = 0;
		for (final Report report : reportStore.findReportsByType(ReportType.valueOf(reportType.toUpperCase()))) {
			if (report.isUserAllowed()) {
				++numRecords;
				if (numRecords > offset && numRecords <= offset + limit)
					rows.put(ReportSerializer.toClient(report));
			}
		}

		JSONObject out = new JSONObject();
		out.put("rows", rows);
		out.put("results", numRecords);
		return out;
	}

	@OldDao
	@JSONExported
	public JSONObject createReportFactoryByTypeCode(
			final UserContext userCtx, //
			@Parameter(PARAMETER_TYPE) final String type, //
			@Parameter(PARAMETER_CODE) final String code //
			) throws Exception {

		final ReportStore reportStore = TemporaryObjectsBeforeSpringDI.getReportStore();
		final Report reportCard = reportStore.findReportByTypeAndCode(ReportType.valueOf(type.toUpperCase()), code);

		if (reportCard == null)
			throw ReportExceptionType.REPORT_NOTFOUND.createException(code);

		if (!reportCard.isUserAllowed()) {
			final String groups = StringUtils.join(userCtx.getGroups(), ", ");
			throw ReportExceptionType.REPORT_GROUPNOTALLOWED.createException(groups, reportCard.getCode());
		}

		JSONObject out = new JSONObject();
		ReportFactoryDB factory = null;
		if (type.equalsIgnoreCase(ReportType.CUSTOM.toString())) {
			factory = new ReportFactoryDB(reportCard.getId(), null);
			boolean filled = false;
			if (factory.getReportParameters().isEmpty()) {
				factory.fillReport();
				filled = true;
			} else {
				for (final ReportParameter reportParameter : factory.getReportParameters()) {
					final CMAttribute attribute = reportParameter.createCMDBuildAttribute();
					out.append("attribute", AttributeSerializer.toClient(attribute));
				}
			}

			out.put("filled", filled);
		}

		new SessionVars().setReportFactory(factory);
		return out;
	}

	/**
	 * Create report factory obj
	 */
	@JSONExported
	@OldDao
	public JSONObject createReportFactory( //
			@Parameter(PARAMETER_TYPE) final String type, //
			@Parameter(PARAMETER_ID) final int id,
			@Parameter(PARAMETER_EXTENSION) final String extension //
		) throws Exception { //

		ReportFactoryDB reportFactory = null;

		final JSONObject out = new JSONObject();
		if (ReportType.valueOf(type.toUpperCase()) == ReportType.CUSTOM) {
			final ReportExtension reportExtension = ReportExtension.valueOf(extension.toUpperCase());
			reportFactory = new ReportFactoryDB(id, reportExtension);

			// if zip extension, do not compile
			if (reportExtension == ReportExtension.ZIP) {
				out.put("filled", true);
			}

			else {
				// if no parameters
				if (reportFactory.getReportParameters().isEmpty()) {
					reportFactory.fillReport();
					out.put("filled", true);
				}

				// else, prepare required parameters
				else {
					out.put("filled", false);
					for (final ReportParameter reportParameter : reportFactory.getReportParameters()) {
						final CMAttribute attribute = reportParameter.createCMDBuildAttribute();
						out.append("attribute", AttributeSerializer.toClient(attribute));
					}
				}
			}
		}

		new SessionVars().setReportFactory(reportFactory);
		return out;
	}

	/**
	 * Set user-defined parameters and fill report
	 * 
	 * @throws Exception
	 */
	@JSONExported
	public void updateReportFactoryParams( //
			final Map<String, String> formParameters //
		) throws Exception {

		final ReportFactoryDB reportFactory = (ReportFactoryDB) new SessionVars().getReportFactory();
		if (formParameters.containsKey("reportExtension")) {
			reportFactory.setReportExtension(ReportExtension.valueOf(formParameters.get("reportExtension")
					.toUpperCase()));
		}

		for (final ReportParameter reportParameter : reportFactory.getReportParameters()) {
			// update parameter
			reportParameter.parseValue(formParameters.get(reportParameter.getFullName()));
			Log.REPORT.debug("Setting parameter " + reportParameter.getFullName() + ": " + reportParameter.getValue());
		}

		reportFactory.fillReport();
		new SessionVars().setReportFactory(reportFactory);
	}

	/**
	 * Print report to output stream
	 * 
	 * @param noDelete
	 *            this may be requested for wf server side processing
	 */
	@JSONExported
	public DataHandler printReportFactory( //
			@Parameter(value = "donotdelete", required = false) final boolean notDelete //
			)throws Exception {

		final ReportFactory reportFactory = new SessionVars().getReportFactory();
		// TODO: report filename should be always read from jasperPrint obj
		// get report filename
		String filename = "";
		if (reportFactory instanceof ReportFactoryDB) {
			final ReportFactoryDB reportFactoryDB = (ReportFactoryDB) reportFactory;
			filename = reportFactoryDB.getReportCard().getCode().replaceAll(" ", "");
		} else if (reportFactory instanceof ReportFactoryTemplate) {
			final ReportFactoryTemplate reportFactoryTemplate = (ReportFactoryTemplate) reportFactory;
			filename = reportFactoryTemplate.getJasperPrint().getName();
		}

		// add extension
		filename += "." + reportFactory.getReportExtension().toString().toLowerCase();

		// send to stream
		final DataSource dataSource = TempDataSource.create(filename, reportFactory.getContentType());
		final OutputStream outputStream = dataSource.getOutputStream();
		reportFactory.sendReportToStream(outputStream);
		outputStream.flush();
		outputStream.close();

		if (!notDelete) {
			new SessionVars().removeReportFactory();
		}

		return new DataHandler(dataSource);
	}

	/**
	 * Print cards on screen
	 */
	@JSONExported
	public void printCurrentView( //
			@Parameter("columns") final JSONArray columns, //
			@Parameter(PARAMETER_TYPE) final String type, //
			@Parameter(value = PARAMETER_CLASS_NAME) final String className, //
			@Parameter(PARAMETER_LIMIT) final int limit, //
			@Parameter(PARAMETER_START) final int offset, //
			@Parameter(value = PARAMETER_FILTER, required = false) final JSONObject filter, //
			@Parameter(value = PARAMETER_SORT, required = false) final JSONArray sorters, //
			@Parameter(value = PARAMETER_ATTRIBUTES, required = false) final JSONArray attributes) //
		throws Exception {

		final QueryOptions queryOptions = QueryOptions.newQueryOption() //
				.limit(limit) //
				.offset(offset) //
				.orderBy(sorters) //
				.filter(filter) //
				.build();

		final List<String> attributeOrder = jsonArrayToStringList(columns);
		final ReportFactoryTemplateList rft = new ReportFactoryTemplateList( //
				ReportExtension.valueOf(type.toUpperCase()), //
				queryOptions, //
				attributeOrder, //
				className //
			);

		rft.fillReport();
		new SessionVars().setReportFactory(rft);
	}

	private List<String> jsonArrayToStringList(final JSONArray columns) throws JSONException {
		final List<String> attributeOrder = new LinkedList<String>();
		for (int i = 0; i < columns.length(); ++i) {
			attributeOrder.add(columns.getString(i));
		}
		return attributeOrder;
	}

	@OldDao
	@JSONExported
	public void printCardDetails( //
			@Parameter(PARAMETER_FORMAT) final String format, //
			@Parameter(PARAMETER_CLASS_NAME) final String className, //
			@Parameter(PARAMETER_CARD_ID) final Long cardId, //
			final UserContext userCtx //
			) throws Exception {

		final ReportFactoryTemplateDetail rftd = new ReportFactoryTemplateDetail(
				className, cardId, userCtx,
				ReportExtension.valueOf(format.toUpperCase()));

		rftd.fillReport();
		new SessionVars().setReportFactory(rftd);
	}

}
