package org.cmdbuild.services.store.report;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.cmdbuild.elements.report.ReportFactory.ReportType;
import org.cmdbuild.exception.NotFoundException;
import org.cmdbuild.exception.ORMException;
import org.cmdbuild.model.Report;
import org.cmdbuild.services.DBService;
import org.cmdbuild.services.store.report.ReportQuery.QueryConfiguration;
import org.postgresql.jdbc4.Jdbc4Array;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;

public class JDBCReportStore implements ReportStore {
	public static final String REPORT_CLASS_NAME = "Report";

	private final JdbcTemplate jdbcTemplate;

	private enum Attributes {
		Id,
		Code,
		Description,
		Status,
		User,
		BeginDate,
		Type,
		Query,
		SimpleReport,
		RichReport,
		Wizard,
		Images,
		ImagesLength,
		ReportLength,
		IdClass,
		ImagesName,
		Groups
	};

	public JDBCReportStore() {
		jdbcTemplate = new JdbcTemplate(DBService.getInstance().getDataSource());
	}

	@Override
	public List<Report> findReportsByType(final ReportType type) throws ORMException {
		final List<Report> reportList = new ArrayList<Report>();
		final QueryConfiguration configuration = ReportQuery.listByType(type);

		jdbcTemplate.query(configuration.query, configuration.arguments, new RowCallbackHandler() {
			@Override
			public void processRow(ResultSet rs) throws SQLException {
				reportList.add(fromResultSet(rs));
			}
		});

		return reportList;
	}

	@Override
	public Report findReportByTypeAndCode(final ReportType type, final String code) throws ORMException {
		for (final Report report: findReportsByType(type)) {
			if (report.getCode().equalsIgnoreCase(code)) {
				return report;
			}
		}

		return null;
	}

	private class FindReportByIdCallbackHandler implements RowCallbackHandler {
		private boolean founded = false;
		private Report report = null;
		
		@Override
		public void processRow(ResultSet rs) throws SQLException {
			if (!founded) {
				report = fromResultSet(rs);
			}
		}
		
		public Report getReport() {
			return report;
		}
	}

	@Override
	public Report findReportById(final int id) throws NotFoundException, ORMException {
		final QueryConfiguration configuration = ReportQuery.selectById(id);
		final FindReportByIdCallbackHandler callBackHandler = new FindReportByIdCallbackHandler();

		jdbcTemplate.query(configuration.query, configuration.arguments, callBackHandler);

		return callBackHandler.getReport();
	}

	@Override
	public void deleteReport(final int id) {
		final QueryConfiguration configuration = ReportQuery.delete(id);
		jdbcTemplate.update(configuration.query, configuration.arguments);
	}

	@Override
	public List<String> getReportTypes() {

		final QueryConfiguration configuration = ReportQuery.findTypes();
		final ArrayList<String> list = new ArrayList<String>();
		jdbcTemplate.query(configuration.query, configuration.arguments, new RowCallbackHandler() {
			@Override
			public void processRow(ResultSet rs) throws SQLException {
				list.add(rs.getString(Attributes.Type.toString()));
			}
		});

		return list;
	}

	@Override
	public void insertReport(Report report) throws SQLException, IOException {
		final QueryConfiguration configuration = ReportQuery.insert(report);
		jdbcTemplate.update(configuration.query, configuration.arguments);
	}

	@Override
	public void updateReport(Report report) throws SQLException, IOException {
		final QueryConfiguration configuration = ReportQuery.update(report);
		jdbcTemplate.update(configuration.query, configuration.arguments);
	}

	private Report fromResultSet(final ResultSet rs) throws SQLException {
		final Report report = new Report();
		report.setId(rs.getInt(Attributes.Id.toString()));
		report.setCode(rs.getString(Attributes.Code.toString()));
		report.setDescription(rs.getString(Attributes.Description.toString()));
		report.setStatus(rs.getString(Attributes.Status.toString()));
		report.setUser(rs.getString(Attributes.User.toString()));
		report.setBeginDate(rs.getDate(Attributes.BeginDate.toString()));
		String typeString = rs.getString(Attributes.Type.toString());
		report.setType(ReportType.valueOf(typeString.toUpperCase()));
		report.setQuery(rs.getString(Attributes.Query.toString()));
		report.setSimpleReport(rs.getBytes(Attributes.SimpleReport.toString()));
		report.setRichReport(rs.getBytes(Attributes.RichReport.toString()));
		report.setWizard(rs.getBytes(Attributes.Wizard.toString()));
		report.setImages(rs.getBytes(Attributes.Images.toString()));
		report.setImagesLength(toIntegerArray(rs.getObject((Attributes.ImagesLength.toString()))));
		report.setImagesName(toStringArray(rs.getObject(Attributes.ImagesName.toString())));
		report.setReportLength(toIntegerArray(rs.getObject((Attributes.ReportLength.toString()))));
		report.setGroups(toStringArray(rs.getObject((Attributes.Groups.toString()))));

		return report;
	} 

	private Integer[] toIntegerArray(final Object resultSetOutput) throws SQLException {
		final Jdbc4Array array = (Jdbc4Array) resultSetOutput;
		return (Integer[]) array.getArray();
	}

	private String[] toStringArray(final Object resultSetOutput) throws SQLException {
		final Jdbc4Array array = (Jdbc4Array) resultSetOutput;
		return (String[]) array.getArray();
	}
}
