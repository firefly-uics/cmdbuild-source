package org.cmdbuild.servlets.json.management;

import java.io.IOException;
import java.util.SortedSet;

import org.apache.commons.fileupload.FileItem;
import org.cmdbuild.csv.CSVCard;
import org.cmdbuild.csv.CSVData;
import org.cmdbuild.elements.interfaces.ICard;
import org.cmdbuild.elements.interfaces.ITable;
import org.cmdbuild.logger.Log;
import org.cmdbuild.services.SessionVars;
import org.cmdbuild.servlets.json.JSONBase;
import org.cmdbuild.servlets.json.serializers.Serializer;
import org.cmdbuild.servlets.utils.Parameter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ImportCSV extends JSONBase {

	private final static String DESCRIPTION_SUFFIX = "_description";

	@JSONExported
	public void uploadCSV(
			@Parameter("filecsv") FileItem file,
			@Parameter("separator") String separatorString,
			ITable table) throws IOException {
		int separator = separatorString.charAt(0);
		CSVData csvData = new CSVData(file, table, separator);
		new SessionVars().setCsvData(csvData);
	}

	@JSONExported
	public JSONObject getCSVRecords(
			JSONObject serializer) throws JSONException {
		CSVData csvData = new SessionVars().getCsvData();
		serializer.put("headers", csvData.getHeader());
		JSONArray rows = new JSONArray();
		for(ICard card : csvData.getCards()) {
			rows.put(serializeCSVCard(card));
		}
		serializer.put("rows", rows);
		return serializer;
	}

	@JSONExported
	public void updateCSVRecords(
			@Parameter("data") JSONArray jsonCards) throws JSONException {
		CSVData csvData = new SessionVars().getCsvData();
		ITable table = csvData.getTable();
		SortedSet<CSVCard> cards = csvData.getCards();
		for (int i = 0, ilen = jsonCards.length(); i<ilen; ++i) {
			CSVCard csvCard = CSVCard.create(table);
			JSONObject jsonCard = jsonCards.getJSONObject(i);
			csvCard.getAttributeValue(ICard.CardAttributes.Id.toString()).setValue((Integer)jsonCard.getInt(ICard.CardAttributes.Id.toString()));
			String[] header = csvData.getHeader();
			for (int j=0, jlen=header.length; j < jlen; ++j) {
				String attrName = header[j];
				if (jsonCard.has(attrName)) {
					String attrValue = jsonCard.getString(attrName);
					String attrDescription = null;
					try {
						attrDescription = jsonCard.getString(attrName+DESCRIPTION_SUFFIX);
					} catch (Exception e) {
						// do nothing
					}
					csvCard.setValidatedFromJSON(attrName, attrValue, attrDescription);
				}
			}

			if (!cards.remove(csvCard)) {
				Log.OTHER.error("CSV card not found!");
			}

			cards.add(csvCard);
		}
	}

	@JSONExported
	@Transacted
	public void storeCSVRecords() {
		CSVData csvData = new SessionVars().getCsvData();
		for (CSVCard card : csvData.getCards()) {
			card.save();
		}
		csvData.getCards().clear();
	}

	private JSONObject serializeCSVCard(ICard card) throws JSONException {
		JSONObject jsonCard = Serializer.serializeCard(card, false);
		JSONObject output = new JSONObject();
		output.put("card", jsonCard);
		output.put("not_valid_values", card.getExtendedProperties().get(CSVCard.invalidXpName));

		return output;
	}
};
