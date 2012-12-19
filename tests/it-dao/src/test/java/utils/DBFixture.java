package utils;

import static org.cmdbuild.dao.query.clause.QueryAliasAttribute.attribute;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.cmdbuild.dao.entry.DBCard;
import org.cmdbuild.dao.entry.DBEntry;
import org.cmdbuild.dao.entry.DBRelation;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.CMEntryType;
import org.cmdbuild.dao.entrytype.DBClass;
import org.cmdbuild.dao.entrytype.DBDomain;
import org.cmdbuild.dao.query.clause.QueryAliasAttribute;
import org.cmdbuild.dao.query.clause.alias.Alias;
import org.cmdbuild.dao.view.DBDataView.DBClassDefinition;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;

/**
 * Class containing some utility methods to create/delete cards, relations and
 * query them
 */
public abstract class DBFixture extends IntegrationTestBase {

	protected static String uniqueUUID() {
		return UUID.randomUUID().toString();
	}

	protected DBCard insertCardWithCode(final DBClass c, final Object value) {
		return insertCard(c, c.getCodeAttributeName(), value);
	}

	protected DBCard insertCard(final DBClass c, final String key, final Object value) {
		return DBCard.newInstance(dbDriver(), c).set(key, value).save();
	}

	protected void insertCards(final DBClass c, final int quantity) {
		for (long i = 0; i < quantity; ++i) {
			insertCardWithCode(c, String.valueOf(i));
		}
	}

	protected void insertCardsWithCodeAndDescription(final DBClass c, final int quantity) {
		for (long i = 0; i < quantity; ++i) {
			DBCard.newInstance(dbDriver(), c) //
					.setCode(String.valueOf(i)) //
					.setDescription(String.valueOf(i)) //
					.save();
		}
	}

	protected DBRelation insertRelation(final DBDomain d, final DBCard c1, final DBCard c2) {
		return DBRelation.newInstance(dbDriver(), d) //
				.setCard1(c1) //
				.setCard2(c2) //
				.save();
	}

	protected void deleteCard(final DBCard c) {
		deleteEntry(c);
	}

	protected void deleteRelation(final DBRelation r) {
		deleteEntry(r);
	}

	protected void deleteEntry(final DBEntry e) {
		dbDriver().delete(e);
	}

	protected Iterable<String> namesOf(final Iterable<? extends CMEntryType> entityTypes) {
		return Iterables.transform(entityTypes, new Function<CMEntryType, String>() {

			@Override
			public String apply(final CMEntryType input) {
				return input.getName();
			}

		});
	}

	protected QueryAliasAttribute keyAttribute(final CMEntryType et) {
		return attribute(et, et.getKeyAttributeName());
	}

	protected QueryAliasAttribute codeAttribute(final CMClass c) {
		return attribute(c, c.getCodeAttributeName());
	}

	protected QueryAliasAttribute codeAttribute(final Alias alias, final CMClass c) {
		return attribute(alias, c.getCodeAttributeName());
	}

	protected QueryAliasAttribute descriptionAttribute(final CMClass c) {
		return attribute(c, c.getDescriptionAttributeName());
	}

	protected DBClassDefinition newClass(final String name, final DBClass parent) {
		final DBClassDefinition classDefinition = mock(DBClassDefinition.class);
		when(classDefinition.getName()).thenReturn(name);
		when(classDefinition.getParent()).thenReturn(parent);
		when(classDefinition.isHoldingHistory()).thenReturn(true);
		return classDefinition;
	}

	protected DBClassDefinition newSuperClass(final String name, final DBClass parent) {
		final DBClassDefinition classDefinition = mock(DBClassDefinition.class);
		when(classDefinition.getName()).thenReturn(name);
		when(classDefinition.getParent()).thenReturn(parent);
		when(classDefinition.isSuperClass()).thenReturn(true);
		when(classDefinition.isHoldingHistory()).thenReturn(true);
		return classDefinition;
	}

}
