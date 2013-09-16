package org.cmdbuild.services.bim.connector;

import static org.cmdbuild.common.Constants.ID_ATTRIBUTE;
import static org.cmdbuild.bim.utils.BimConstants.GLOBALID;
import static org.cmdbuild.bim.utils.BimConstants.FK_COLUMN_NAME;
import static org.cmdbuild.bim.utils.BimConstants.BIM_SCHEMA_NAME;
import static org.cmdbuild.bim.utils.BimConstants.GEOMETRY_ATTRIBUTE;
import static org.cmdbuild.bim.utils.BimConstants.STORE_COORDINATES_QUERY_TEMPLATE;
import static org.cmdbuild.bim.utils.BimConstants.POINT_TEMPLATE;
import static org.cmdbuild.bim.utils.BimConstants.X_ATTRIBUTE_NAME;
import static org.cmdbuild.bim.utils.BimConstants.Y_ATTRIBUTE_NAME;
import static org.cmdbuild.bim.utils.BimConstants.Z_ATTRIBUTE_NAME;
import static org.cmdbuild.dao.query.clause.AnyAttribute.anyAttribute;
import static org.cmdbuild.dao.query.clause.QueryAliasAttribute.attribute;
import static org.cmdbuild.dao.query.clause.where.EqualsOperatorAndValue.eq;
import static org.cmdbuild.dao.query.clause.where.SimpleWhereClause.condition;

import java.util.Iterator;

import org.cmdbuild.bim.model.Entity;
import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entry.CardReference;
import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.CMDomain;
import org.cmdbuild.dao.entrytype.attributetype.LookupAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.ReferenceAttributeType;
import org.cmdbuild.dao.query.CMQueryResult;
import org.cmdbuild.dao.query.CMQueryRow;
import org.cmdbuild.dao.query.clause.alias.Alias;
import org.cmdbuild.dao.query.clause.alias.EntryTypeAlias;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.data.store.lookup.Lookup;
import org.cmdbuild.data.store.lookup.LookupType;
import org.cmdbuild.logic.data.lookup.LookupLogic;
import org.cmdbuild.services.bim.DefaultBimDataModelManager;
import org.cmdbuild.utils.bim.BimIdentifier;
import org.springframework.jdbc.core.JdbcTemplate;

class MapperSupport {

	private static final String BIM = BIM_SCHEMA_NAME;
	private final CMDataView dataView;
	private final LookupLogic lookupLogic;
	private final JdbcTemplate jdbcTemplate;

	public MapperSupport(CMDataView dataView, LookupLogic lookupLogic,
			JdbcTemplate jdbcTemplate) {
		this.dataView = dataView;
		this.lookupLogic = lookupLogic;
		this.jdbcTemplate = jdbcTemplate;
	}

	public CMCard fetchCardFromGlobalIdAndClassName(String key, String className) {
		CMCard matchingCard = null;

		CMClass theClass = dataView.findClass(BimIdentifier.newIdentifier()
				.withName(className));
		Alias CLASS_ALIAS = EntryTypeAlias.canonicalAlias(theClass);
		CMQueryResult result = dataView
				.select( //
				attribute(CLASS_ALIAS,
						DefaultBimDataModelManager.FK_COLUMN_NAME)) //
				.from(theClass) //
				.where(condition(attribute(theClass, GLOBALID), eq(key))) //
				.run();

		if (!result.isEmpty()) {
			CMQueryRow row = result.getOnlyRow();
			CMCard card = row.getCard(CLASS_ALIAS);
			CardReference reference = (CardReference) card
					.get(DefaultBimDataModelManager.FK_COLUMN_NAME);
			Long masterId = reference.getId();
			theClass = dataView.findClass(className);
			result = dataView.select( //
					anyAttribute(theClass)) //
					.from(theClass)
					//
					.where(condition(attribute(theClass, ID_ATTRIBUTE),
							eq(masterId))) //
					.run();
			if (!result.isEmpty()) {
				row = result.getOnlyRow();
				card = row.getCard(theClass);
				matchingCard = card;
			}
		}
		return matchingCard;
	}

	public String findReferencedClassNameFromReferenceAttribute(
			CMAttribute attribute) {
		String domainName = ((ReferenceAttributeType) attribute.getType())
				.getDomainName();
		CMDomain domain = dataView.findDomain(domainName);
		String referencedClass = "";
		String ownerClassName = attribute.getOwner().getName();
		if (domain.getClass1().getName().equals(ownerClassName)) {
			referencedClass = domain.getClass2().getName();
		} else {
			referencedClass = domain.getClass1().getName();
		}
		return referencedClass;
	}

	public Long findMasterIdFromGuid(String value, String className) {
		Long referencedId = null;
		CMClass theClass = dataView.findClass(BimIdentifier.newIdentifier()
				.withName(className));
		Alias CLASS_ALIAS = EntryTypeAlias.canonicalAlias(theClass);
		CMQueryResult result = dataView.select( //
				anyAttribute(CLASS_ALIAS)) //
				.from(theClass)
				//
				.where(condition(attribute(CLASS_ALIAS, GLOBALID), eq(value))) //
				.run();
		if (!result.isEmpty()) {
			CMCard card = result.getOnlyRow().getCard(CLASS_ALIAS);
			if (card.get(FK_COLUMN_NAME) != null) {
				CardReference reference = (CardReference) card
						.get(FK_COLUMN_NAME);
				referencedId = reference.getId();
			}

		}
		return referencedId;
	}

	public Long findLookupIdFromDescription(String newLookupValue,
			CMAttribute attribute) {
		Long lookupId = null;
		String lookupTypeName = ((LookupAttributeType) attribute.getType())
				.getLookupTypeName();

		Iterable<LookupType> allLookupTypes = lookupLogic.getAllTypes();
		LookupType theType = null;
		for (Iterator<LookupType> it = allLookupTypes.iterator(); it.hasNext();) {
			LookupType lt = it.next();
			if (lt.name.equals(lookupTypeName)) {
				theType = lt;
				break;
			}
		}
		Iterable<Lookup> allLookusOfType = lookupLogic.getAllLookup(theType,
				true, 0, 0);

		for (Iterator<Lookup> it = allLookusOfType.iterator(); it.hasNext();) {
			Lookup l = it.next();
			if (l.getDescription() != null
					&& l.getDescription().equals(newLookupValue)) {
				lookupId = l.getId();
				break;
			}
		}
		return lookupId;
	}

	public void storeCoordinates(CMCard bimCard, Entity source) {

		String x1 = source.getAttributeByName(X_ATTRIBUTE_NAME).getValue();
		String x2 = source.getAttributeByName(Y_ATTRIBUTE_NAME).getValue();
		String x3 = source.getAttributeByName(Z_ATTRIBUTE_NAME).getValue();

		final String updateCoordinatesQuery = String.format(STORE_COORDINATES_QUERY_TEMPLATE, //
				BIM, //
				bimCard.getType().getIdentifier().getLocalName(), //
				GEOMETRY_ATTRIBUTE, //
				String.format(POINT_TEMPLATE,x1,x2,x3),//
				ID_ATTRIBUTE, //
				bimCard.getId() //
				);

		jdbcTemplate.update(updateCoordinatesQuery);
	}

}
