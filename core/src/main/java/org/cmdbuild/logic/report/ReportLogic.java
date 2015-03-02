package org.cmdbuild.logic.report;

import java.util.Map;

import javax.activation.DataHandler;

import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.logic.Logic;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import com.google.common.base.Optional;

public interface ReportLogic extends Logic {

	Marker marker = MarkerFactory.getMarker(ReportLogic.class.getName());

	interface Report {

		int getId();

		String getTitle();

		String getType();

		String getDescription();

	}

	Iterable<Report> readAll();

	Optional<Report> read(int reportId);

	Iterable<CMAttribute> parameters(int id);

	DataHandler download(int reportId, String extension, Map<String, Object> parameters);

}
