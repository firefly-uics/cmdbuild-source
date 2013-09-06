package org.cmdbuild.services.bim.connector;

import static org.cmdbuild.common.Constants.ID_ATTRIBUTE;
import static org.cmdbuild.dao.query.clause.AnyAttribute.anyAttribute;
import static org.cmdbuild.dao.query.clause.QueryAliasAttribute.attribute;
import static org.cmdbuild.dao.query.clause.where.EqualsOperatorAndValue.eq;
import static org.cmdbuild.dao.query.clause.where.SimpleWhereClause.condition;
import static org.cmdbuild.services.bim.DefaultBimDataModelManager.FK_COLUMN_NAME;

import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entry.CardReference;
import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.CMDomain;
import org.cmdbuild.dao.entrytype.attributetype.ReferenceAttributeType;
import org.cmdbuild.dao.query.CMQueryResult;
import org.cmdbuild.dao.query.CMQueryRow;
import org.cmdbuild.dao.query.clause.alias.Alias;
import org.cmdbuild.dao.query.clause.alias.EntryTypeAlias;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.services.bim.DefaultBimDataModelManager;
import org.cmdbuild.utils.bim.BimIdentifier;

public class MapperSupport {

	private CMDataView dataView;

	public MapperSupport(CMDataView dataView) {
		this.dataView = dataView;
	}

	public CMCard fetchCardFromGlobalIdAndClassName(String key, String className) {
		CMCard matchingCard = null;

		CMClass theClass = dataView.findClass(BimIdentifier.newIdentifier().withName(className));
		Alias CLASS_ALIAS = EntryTypeAlias.canonicalAlias(theClass);
		CMQueryResult result = dataView.select( //
				attribute(CLASS_ALIAS, DefaultBimDataModelManager.FK_COLUMN_NAME)) //
				.from(theClass) //
				.run();

		if (!result.isEmpty()) {
			CMQueryRow row = result.getOnlyRow();
			CMCard card = row.getCard(CLASS_ALIAS);
			CardReference reference = (CardReference) card.get(DefaultBimDataModelManager.FK_COLUMN_NAME);
			Long masterId = reference.getId();
			theClass = dataView.findClass(className);
			result = dataView.select( //
					anyAttribute(theClass)) //
					.from(theClass) //
					.where(condition(attribute(theClass, ID_ATTRIBUTE), eq(masterId))) //
					.run();
			if (!result.isEmpty()) {
				row = result.getOnlyRow();
				card = row.getCard(theClass);
				matchingCard = card;
			}
		}
		return matchingCard;
	}

	public String findReferencedClassNameFromReferenceAttribute(CMAttribute attribute) {
		String domainName = ((ReferenceAttributeType) attribute.getType()).getDomainName();
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
		CMClass theClass = dataView.findClass(BimIdentifier.newIdentifier().withName(className));
		Alias CLASS_ALIAS = EntryTypeAlias.canonicalAlias(theClass);
		CMQueryResult result = dataView.select( //
				anyAttribute(CLASS_ALIAS)) //
				.from(theClass) //
				.where(condition(attribute(CLASS_ALIAS, DefaultBimDataModelManager.GLOBALID), eq(value))) //
				.run();
		if (!result.isEmpty()) {
			CMCard card = result.getOnlyRow().getCard(CLASS_ALIAS);
			if (card.get(FK_COLUMN_NAME) != null) {
				CardReference reference = (CardReference) card.get(FK_COLUMN_NAME);
				referencedId = reference.getId();
			}

		}
		return referencedId;
	}

}
