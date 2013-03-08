package org.cmdbuild.servlets.json.management;

import static org.cmdbuild.servlets.json.ComunicationConstants.FILE_CSV;
import static org.cmdbuild.servlets.json.ComunicationConstants.SEPARATOR;

import java.io.IOException;
import java.util.Map.Entry;

import org.apache.commons.fileupload.FileItem;
import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entry.DBCard;
import org.cmdbuild.logic.TemporaryObjectsBeforeSpringDI;
import org.cmdbuild.logic.data.access.CardStorableConverter;
import org.cmdbuild.logic.data.access.DataAccessLogic;
import org.cmdbuild.model.data.Card;
import org.cmdbuild.services.SessionVars;
import org.cmdbuild.servlets.json.JSONBase;
import org.cmdbuild.servlets.json.management.dataimport.csv.CsvData;
import org.cmdbuild.servlets.json.management.dataimport.csv.CsvImporter.CsvCard;
import org.cmdbuild.servlets.json.serializers.CardSerializer;
import org.cmdbuild.servlets.utils.Parameter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ImportCSV extends JSONBase {

	/**
	 * Stores in the session the records of the file that the user has uploaded
	 * 
	 * @param file
	 *            is the uploaded file
	 * @param separatorString
	 *            the separator of the csv file
	 * @param classId
	 *            the id of the class where the records will be stored
	 */
	@JSONExported
	public void uploadCSV(@Parameter(FILE_CSV) final FileItem file, //
			@Parameter(SEPARATOR) final String separatorString, //
			@Parameter("idClass") final Long classId) throws IOException {
		clearSession();
		final DataAccessLogic dataAccessLogic = TemporaryObjectsBeforeSpringDI.getDataAccessLogic();
		final CsvData importedCsvData = dataAccessLogic.importCsvFileFor(file, classId, separatorString);
		new SessionVars().setCsvData(importedCsvData);
	}

	/**
	 * 
	 * @return the serialization of the cards
	 */
	@JSONExported
	public JSONObject getCSVRecords(final JSONObject serializer) throws JSONException {
		final CsvData csvData = new SessionVars().getCsvData();
		serializer.put("headers", csvData.getHeaders());
		final JSONArray rows = new JSONArray();
		for (final CsvCard csvCard : csvData.getCards()) {
			rows.put(serializeCSVCard(csvCard));
		}
		serializer.put("rows", rows);
		return serializer;
	}

	@JSONExported
	public void updateCSVRecords(@Parameter("data") final JSONArray jsonCards) throws JSONException {
		final CsvData csvData = new SessionVars().getCsvData();
		for (int i = 0; i < jsonCards.length(); i++) {
			final JSONObject jsonCard = jsonCards.getJSONObject(i);
			final Long fakeId = jsonCard.getLong("Id");
			final CsvCard csvCard = csvData.getCard(fakeId);
			// ugly... it should not have knowledge of dao implementation
			final DBCard mutableCard = (DBCard) csvCard.getCMCard();
			for (final String attributeName : csvData.getHeaders()) {
				if (jsonCard.has(attributeName)) {
					final Object attributeValue = jsonCard.get(attributeName);
					try {
						mutableCard.set(attributeName, attributeValue);
						if (csvCard.getInvalidAttributes().containsKey(attributeName)) {
							csvCard.getInvalidAttributes().remove(attributeName);
						}
					} catch (final Exception ex) {
						csvCard.addInvalidAttribute(attributeName, attributeValue);
					}
				}
			}
		}
	}

	@JSONExported
	@Transacted
	public void storeCSVRecords() {
		final DataAccessLogic dataAccessLogic = TemporaryObjectsBeforeSpringDI.getDataAccessLogic();
		final CsvData csvData = new SessionVars().getCsvData();
		for (final CsvCard csvCard : csvData.getCards()) {
			final CMCard card = csvCard.getCMCard();
			final Card cardToCreate = Card.newInstance() //
					.withClassName(card.getType().getIdentifier().getLocalName()) //
					.withAllAttributes(card.getValues()) //
					.build();
			dataAccessLogic.createCard(cardToCreate);
		}
		clearSession();
	}

	private void clearSession() {
		new SessionVars().setCsvData(null);
	}

	private JSONObject serializeCSVCard(final CsvCard csvCard) throws JSONException {
		final Card card = CardStorableConverter.of(csvCard.getCMCard()) //
				.convert(csvCard.getCMCard());
		final JSONObject jsonCard = CardSerializer.toClient(card);
		jsonCard.put("Id", csvCard.getFakeId());
		jsonCard.put("IdClass_value", csvCard.getCMCard().getType().getIdentifier().getLocalName());
		final JSONObject output = new JSONObject();
		output.put("card", jsonCard);
		final JSONObject notValidValues = new JSONObject();
		for (final Entry<String, Object> entry : csvCard.getInvalidAttributes().entrySet()) {
			notValidValues.put(entry.getKey(), entry.getValue());
		}
		output.put("not_valid_values", notValidValues);
		return output;
	}
};
