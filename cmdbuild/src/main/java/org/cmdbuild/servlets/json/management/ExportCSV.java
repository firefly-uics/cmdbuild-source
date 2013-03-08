package org.cmdbuild.servlets.json.management;

import static org.cmdbuild.servlets.json.ComunicationConstants.PARAMETER_CLASS_NAME;
import static org.cmdbuild.servlets.json.ComunicationConstants.PARAMETER_SEPARATOR;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.activation.DataHandler;
import javax.mail.util.ByteArrayDataSource;

import org.cmdbuild.logic.TemporaryObjectsBeforeSpringDI;
import org.cmdbuild.logic.data.access.DataAccessLogic;
import org.cmdbuild.servlets.json.JSONBase;
import org.cmdbuild.servlets.utils.Parameter;

public class ExportCSV extends JSONBase {

	@JSONExported(contentType = "text/csv")
	public DataHandler export( //
			@Parameter(PARAMETER_SEPARATOR) final String separator, //
			@Parameter(PARAMETER_CLASS_NAME) final String className) //
			throws IOException {

		final DataAccessLogic dataAccessLogic = TemporaryObjectsBeforeSpringDI.getDataAccessLogic();
		final File csvFile = dataAccessLogic.exportClassAsCsvFile(className, separator);
		return createDataHandler(csvFile);
	}

	private DataHandler createDataHandler(final File file) throws FileNotFoundException, IOException {
		final FileInputStream in = new FileInputStream(file);
		final ByteArrayDataSource ds = new ByteArrayDataSource(in, "text/csv");
		ds.setName(file.getName());
		return new DataHandler(ds);
	}

}
