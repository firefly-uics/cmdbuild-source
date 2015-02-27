package org.cmdbuild.logic.report;

public interface ReportLogic {

	interface Report {

		int getId();

		String getTitle();

		String getType();

		String getDescription();

	}

	Iterable<Report> readAll();

}
