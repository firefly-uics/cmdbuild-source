package org.cmdbuild.csv;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Map.Entry;

import org.apache.commons.fileupload.FileItem;
import org.cmdbuild.elements.interfaces.ITable;
import org.cmdbuild.logger.Log;
import org.supercsv.io.CsvMapReader;
import org.supercsv.io.ICsvMapReader;
import org.supercsv.prefs.CsvPreference;

public class CSVData {
	static Integer currentId = 1000;

	private final ITable table;

	private SortedSet<CSVCard> cards;
	private final String[] header;

	public CSVData(FileItem fileItem, ITable table, int separator) throws IOException {
		this.table = table;
		this.cards = new TreeSet<CSVCard>();
		Reader isReader = new InputStreamReader(fileItem.getInputStream());
		CsvPreference csvPrefs = new CsvPreference('"', separator, "\n");
		ICsvMapReader csvReader = new CsvMapReader(isReader, csvPrefs);
		this.header = csvReader.getCSVHeader(true);
		readCsv(csvReader);
	}

	public SortedSet<CSVCard> getCards() {
		return cards;
	}

	public ITable getTable() {
		return table;
	}

	public final String[] getHeader() {
		return header;
	}

	private void readCsv(ICsvMapReader csvReader) throws IOException {
	    try {
	        Map<String, String> currentLine = csvReader.read(header);
	        
	        while( currentLine != null) {
	        	Log.OTHER.debug(String.format("CSV Line %d", csvReader.getLineNumber()));
	        	CSVCard csvCard = CSVCard.createWithFakeId(table);
	        	for (Entry<String, String> entry: currentLine.entrySet()) {
	        		String attributeName = entry.getKey();
	        		String attributeValue = entry.getValue();
	        		Log.OTHER.debug(String.format("   %s: %s", attributeName, attributeValue));
	        		csvCard.setValidatedFromCSV(attributeName, attributeValue);
	        	}
	        	cards.add(csvCard);
	        	currentLine = csvReader.read(header);
	        }
	      } finally {
	    	  csvReader.close();
	      }
	}

	public static synchronized Integer getNextId() {
		return ++currentId;
	}
}
