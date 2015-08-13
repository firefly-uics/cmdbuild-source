package org.cmdbuild.servlets.json.management.dataimport.csv;

import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;

import javax.activation.DataHandler;

import org.cmdbuild.dao.entry.DBCard;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.data.store.lookup.LookupStore;
import org.cmdbuild.servlets.json.management.dataimport.CardFiller;
import org.json.JSONException;

import com.google.common.collect.Maps;

public class CSVImporter {

	// a casual number from which start
	private static Long idCounter = 1000L;

	private final CsvReader csvReaded;
	private final CMDataView view;
	private final CMClass importClass;
	private final LookupStore lookupStore;

	public CSVImporter(final CsvReader csvReader, final CMDataView view, final LookupStore lookupStore,
			final CMClass importClass) {
		this.csvReaded = csvReader;
		this.view = view;
		this.lookupStore = lookupStore;
		this.importClass = importClass;
	}

	public CSVData getCsvDataFrom(final DataHandler csvFile) throws IOException, JSONException {
		final CsvReader.CsvData data = csvReaded.read(csvFile);
		return new CSVData(data.headers(), getCsvCardsFrom(data.lines()), importClass.getName());
	}

	private Map<Long, CSVCard> getCsvCardsFrom(final Iterable<CsvReader.CsvLine> lines) throws JSONException {
		final Map<Long, CSVCard> csvCards = Maps.newHashMap();
		for (final CsvReader.CsvLine line : lines) {
			final CardFiller cardFiller = new CardFiller(importClass, view, lookupStore);
			final Long fakeId = getAndIncrementIdForCsvCard();
			final DBCard mutableCard = (DBCard) view.createCardFor(importClass);
			final CSVCard csvCard = new CSVCard(mutableCard, fakeId);
			for (final Entry<String, String> entry : line.entries()) {
				try {
					cardFiller.fillCardAttributeWithValue( //
							mutableCard, //
							entry.getKey(), //
							entry.getValue() //
							);
				} catch (final CardFiller.CardFillerException ex) {
					csvCard.addInvalidAttribute(ex.attributeName, ex.attributeValue);
				}
			}
			csvCards.put(fakeId, csvCard);
		}

		return csvCards;
	}

	private static synchronized Long getAndIncrementIdForCsvCard() {
		return idCounter++;
	}

}