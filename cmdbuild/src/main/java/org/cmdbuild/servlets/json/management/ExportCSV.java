package org.cmdbuild.servlets.json.management;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.activation.DataHandler;
import javax.mail.util.ByteArrayDataSource;

import org.cmdbuild.common.annotations.OldDao;
import org.cmdbuild.elements.interfaces.CardQuery;
import org.cmdbuild.elements.interfaces.IAttribute;
import org.cmdbuild.elements.interfaces.ICard;
import org.cmdbuild.elements.interfaces.ITable;
import org.cmdbuild.logger.Log;
import org.cmdbuild.servlets.json.JSONBase;
import org.cmdbuild.servlets.utils.Parameter;
import org.supercsv.io.CsvMapWriter;
import org.supercsv.io.ICsvMapWriter;
import org.supercsv.prefs.CsvPreference;

@SuppressWarnings("restriction")
public class ExportCSV extends JSONBase{
	
	@OldDao
	@JSONExported(contentType="text/csv")
	public DataHandler export (
			@Parameter("separator") String separatorString,
			ITable table) throws IOException {
		
		int separator = separatorString.charAt(0);
		final CsvPreference exportCsvPrefs = new CsvPreference('"', separator, "\n");
		String fileName = table.getName()+".csv";
		String dirName = System.getProperty("java.io.tmpdir");
		File file = new File(dirName, fileName);
		writeCsvDataToFile(table, file, exportCsvPrefs);
		return createDataHandler(file);
	}

	private DataHandler createDataHandler(File file)
			throws FileNotFoundException, IOException {
		FileInputStream in = new FileInputStream(file);
		ByteArrayDataSource ds = new ByteArrayDataSource(in,"text/csv");
		ds.setName(file.getName());
		return new DataHandler(ds);
	}
	
	private File writeCsvDataToFile(ITable table, File file, CsvPreference exportCsvPrefs) throws IOException{
		ICsvMapWriter writer = new CsvMapWriter(new FileWriter(file), exportCsvPrefs);
		Map<String, IAttribute> attributes = table.getAttributes();
		Set<String> filteredAttributeHeaders = getDisplayableAttributesName(attributes);
		String[] headers = filteredAttributeHeaders.toArray(new String[filteredAttributeHeaders.size()]);
    	CardQuery cards = table.cards().list();
	    try {
	    	writer.writeHeader(headers);
	     	final HashMap<String, ? super Object> cardCsv = new HashMap<String, Object>();
	     	for (ICard card : cards) {
	     		try {
		     		cardCsv.clear();
					cardCsv.putAll(card.getAttributeValueMap());
					writer.write(cardCsv, headers);
	     		} catch (RuntimeException e) {
	     			Log.PERSISTENCE.warn(String.format("Error exporting CSV for %s card %d", card.getSchema().getName(), card.getId()));
	     			throw e;
	     		}
	     	}
	    } finally {
	    	writer.close();
	    }
	    return file;
	};
	
	private Set<String> getDisplayableAttributesName(Map<String, IAttribute> attributes) {
		Set<String> filteredAttributeHeaders = new HashSet<String>();
		for (IAttribute attribute : attributes.values()) {
			if (attribute.isDisplayable())
				filteredAttributeHeaders.add(attribute.getName());			
		}
		return filteredAttributeHeaders;
	}
}
