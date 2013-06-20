package org.cmdbuild.servlets.json.management.dataimport.csv;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.fileupload.FileItem;
import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entry.DBCard;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.view.CMDataView;
import org.supercsv.io.CsvMapReader;
import org.supercsv.io.ICsvMapReader;
import org.supercsv.prefs.CsvPreference;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class CsvImporter {

	// a casual number from which start
	private static Long idCounter = 1000L;

	public class CsvCard {

		private final Map<String, Object> invalidAttributes = Maps.newHashMap();
		private final CMCard card;
		private final Long fakeId;

		private CsvCard(final CMCard card, final Long fakeId) {
			this.card = card;
			this.fakeId = fakeId;
		}

		public Object get(final String attributeName) {
			return card.get(attributeName);
		}

		public Iterable<Entry<String, Object>> getValues() {
			return card.getValues();
		}

		public CMCard getCMCard() {
			return card;
		}

		public Long getFakeId() {
			return fakeId;
		}

		public boolean isInvalid() {
			return invalidAttributes.isEmpty();
		}

		public Map<String, Object> getInvalidAttributes() {
			return invalidAttributes;
		}

		public void addInvalidAttribute(final String name, final Object value) {
			invalidAttributes.put(name, value);
		}

	}

	private final CsvPreference preferences;
	private final CMDataView view;
	private final CMClass importClass;

	public CsvImporter(final CMDataView view, final CMClass importClass, final CsvPreference preferences) {
		this.preferences = preferences;
		this.view = view;
		this.importClass = importClass;
	}

	public CsvData getCsvDataFrom(final FileItem csvFile) throws IOException {
		return new CsvData(getHeaders(csvFile), getCsvCardsFrom(csvFile));
	}

	private Map<Long, CsvCard> getCsvCardsFrom(final FileItem csvFile) throws IOException {
		final Reader reader = new InputStreamReader(csvFile.getInputStream());
		final ICsvMapReader csvReader = new CsvMapReader(reader, preferences);
		return createCsvCards(csvReader);
	}

	private Map<Long, CsvCard> createCsvCards(final ICsvMapReader csvReader) throws IOException {
		final Map<Long, CsvCard> csvCards = Maps.newHashMap();
		try {
			final String[] headers = csvReader.getCSVHeader(true);
			Map<String, String> currentLine = csvReader.read(headers);
			while (currentLine != null) {
				final Long fakeId = getAndIncrementIdForCsvCard();
				final DBCard mutableCard = (DBCard) view.createCardFor(importClass);
				final CsvCard csvCard = new CsvCard(mutableCard, fakeId);
				for (final Entry<String, String> entry : currentLine.entrySet()) {
					final String attributeName = entry.getKey();
					final String attributeValue = entry.getValue();
					try {
						mutableCard.set(attributeName, attributeValue);
					} catch (final Exception ex) {
						csvCard.addInvalidAttribute(attributeName, attributeValue);
					}
				}
				csvCards.put(fakeId, csvCard);
				currentLine = csvReader.read(headers);
			}
		} finally {
			csvReader.close();
		}
		return csvCards;
	}

	private static synchronized Long getAndIncrementIdForCsvCard() {
		return idCounter++;
	}

	private Iterable<String> getHeaders(final FileItem csvFile) throws IOException {
		final Reader reader = new InputStreamReader(csvFile.getInputStream());
		final ICsvMapReader csvReader = new CsvMapReader(reader, preferences);
		return Lists.newArrayList(csvReader.getCSVHeader(true));
	}

}
