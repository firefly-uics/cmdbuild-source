package org.cmdbuild.servlets.json.management.dataimport.csv;

import java.util.Map;

import org.cmdbuild.servlets.json.management.dataimport.csv.CsvImporter.CsvCard;

public class CsvData {

	private final Iterable<String> headers;
	private final Map<Long, CsvCard> tempIdToCsvCard;

	public CsvData(final Iterable<String> headers, final Map<Long, CsvCard> tempIdToCsvCard) {
		this.headers = headers;
		this.tempIdToCsvCard = tempIdToCsvCard;
	}

	public Iterable<String> getHeaders() {
		return headers;
	}

	public Iterable<CsvCard> getCards() {
		return tempIdToCsvCard.values();
	}

	public CsvCard getCard(final Long id) {
		return tempIdToCsvCard.get(id);
	}

}
