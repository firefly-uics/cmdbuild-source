package org.cmdbuild.servlets.json.management.dataimport.csv;

import static org.cmdbuild.common.Constants.CODE_ATTRIBUTE;
import static org.cmdbuild.dao.constants.Cardinality.CARDINALITY_1N;
import static org.cmdbuild.dao.constants.Cardinality.CARDINALITY_N1;
import static org.cmdbuild.dao.query.clause.AnyAttribute.anyAttribute;
import static org.cmdbuild.dao.query.clause.QueryAliasAttribute.attribute;
import static org.cmdbuild.dao.query.clause.where.EqualsOperatorAndValue.eq;
import static org.cmdbuild.dao.query.clause.where.SimpleWhereClause.condition;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.fileupload.FileItem;
import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entry.DBCard;
import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.CMDomain;
import org.cmdbuild.dao.entrytype.attributetype.LookupAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.ReferenceAttributeType;
import org.cmdbuild.dao.query.CMQueryResult;
import org.cmdbuild.dao.query.CMQueryRow;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.data.store.lookup.Lookup;
import org.cmdbuild.data.store.lookup.LookupStore;
import org.cmdbuild.data.store.lookup.LookupType;
import org.cmdbuild.exception.NotFoundException.NotFoundExceptionType;
import org.supercsv.io.CsvMapReader;
import org.supercsv.io.ICsvMapReader;
import org.supercsv.prefs.CsvPreference;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class CSVImporter {

	// a casual number from which start
	private static Long idCounter = 1000L;

	private final CsvPreference preferences;
	private final CMDataView view;
	private final CMClass importClass;
	private final LookupStore lookupStore;

	public CSVImporter( //
			final CMDataView view, //
			final LookupStore lookupStore, //
			final CMClass importClass, //
			final CsvPreference preferences //
		) {

		this.view = view;
		this.lookupStore = lookupStore;
		this.importClass = importClass;
		this.preferences = preferences;
	}

	public CSVData getCsvDataFrom(final FileItem csvFile) throws IOException {
		return new CSVData(getHeaders(csvFile), getCsvCardsFrom(csvFile), importClass.getName());
	}

	private Map<Long, CSVCard> getCsvCardsFrom(final FileItem csvFile) throws IOException {
		final Reader reader = new InputStreamReader(csvFile.getInputStream());
		final ICsvMapReader csvReader = new CsvMapReader(reader, preferences);
		return createCsvCards(csvReader);
	}

	private Map<Long, CSVCard> createCsvCards(final ICsvMapReader csvReader) throws IOException {
		final Map<Long, CSVCard> csvCards = Maps.newHashMap();
		try {
			final String[] headers = csvReader.getCSVHeader(true);
			Map<String, String> currentLine = csvReader.read(headers);

			while (currentLine != null) {
				final Long fakeId = getAndIncrementIdForCsvCard();
				final DBCard mutableCard = (DBCard) view.createCardFor(importClass);
				final CSVCard csvCard = new CSVCard(mutableCard, fakeId);
				for (final Entry<String, String> entry : currentLine.entrySet()) {
					final String attributeName = entry.getKey();
					Object attributeValue = entry.getValue();

					// if the attribute has no value do nothing
					if (attributeValue == null
							|| "".equals(attributeValue)) {

						continue;
					}

					final CMAttribute attribute = importClass.getAttribute(attributeName);

					if (attribute == null) {
						throw NotFoundExceptionType.ATTRIBUTE_NOTFOUND.createException(importClass.getDescription(), attributeName);
					}

					if (attribute.getType() instanceof ReferenceAttributeType) {

						// Use the Code attribute of the referenced card
						manageReferenceAttribute(mutableCard, csvCard,
								attributeName, attributeValue, attribute);

					} else if (attribute.getType() instanceof LookupAttributeType) {

						// For the lookup use the Description
						manageLookupAttribute(mutableCard, csvCard,
								attributeName, attributeValue, attribute);

					} else {

						try {
							mutableCard.set(attributeName, attributeValue);
						} catch (final Exception ex) {
							csvCard.addInvalidAttribute(attributeName, attributeValue);
						}

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

	private void manageLookupAttribute(final DBCard mutableCard,
			final CSVCard csvCard, final String attributeName,
			Object attributeValue, final CMAttribute attribute) {

		final LookupAttributeType type = (LookupAttributeType) attribute.getType();
		final String lookupTypeName = type.getLookupTypeName();
		final LookupType lookupType = LookupType.newInstance().withName(lookupTypeName).build();

		boolean set = false;
		for (Lookup lookup : lookupStore.listForType(lookupType)) {
			if (attributeValue.equals(lookup.description)) {
				mutableCard.set(attributeName, lookup.getId());
				set = true;
				break;
			}
		}

		if (!set) {
			csvCard.addInvalidAttribute(attributeName, attributeValue);
		}
	}

	private void manageReferenceAttribute(final DBCard mutableCard,
			final CSVCard csvCard, final String attributeName,
			Object attributeValue, final CMAttribute attribute) {

		final ReferenceAttributeType type = (ReferenceAttributeType) attribute.getType();
		final String domainName = type.getDomainName();
		final CMDomain domain = view.findDomain(domainName);
		if (domain != null) {

			// retrieve the destination
			final String cardinality = domain.getCardinality();
			CMClass destination = null;
			if (CARDINALITY_1N.value().equals(cardinality)) {
				destination = domain.getClass1();
			} else if (CARDINALITY_N1.value().equals(cardinality)) {
				destination = domain.getClass2();
			}

			if (destination != null) {
				final CMQueryResult queryResult = view.select(anyAttribute(destination)) //
				.from(destination) //
				.where(condition(attribute(destination, CODE_ATTRIBUTE), eq(attributeValue))) //
				.run();

				if (!queryResult.isEmpty()) {
					final CMQueryRow row = queryResult.iterator().next();
					final CMCard referredCard = row.getCard(destination);

					mutableCard.set(attributeName, referredCard.getId());
				} else {
					csvCard.addInvalidAttribute(attributeName, attributeValue);
				}

			} else {
				csvCard.addInvalidAttribute(attributeName, attributeValue);
			}
		}
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