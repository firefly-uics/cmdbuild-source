package org.cmdbuild.elements.report;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import org.cmdbuild.dao.backend.CMBackend;
import org.cmdbuild.elements.wrappers.ReportCard;
import org.springframework.beans.factory.annotation.Autowired;

public class ReportFacade {

	/*
	 * THE DAO LAYER SHOULD BE USED INSTEAD OF THE DATABASE BACKEND AFTER THE ARRAY AND BINARY ATTRIBUTE TYPES ARE SUPPORTED
	 */

	@Autowired
	private CMBackend backend = CMBackend.INSTANCE;

	public List<String> getReportTypes() {
		return backend.getReportTypes();
	}

	public boolean insertReport(ReportCard report) throws SQLException, IOException {
		return backend.insertReport(report);
	}
	
	public boolean updateReport(ReportCard report) throws SQLException, IOException {
		return backend.updateReport(report);
	}
}