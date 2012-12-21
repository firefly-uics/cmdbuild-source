package utils;

import static org.cmdbuild.dao.query.clause.QueryAliasAttribute.attribute;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.cmdbuild.dao.entry.DBCard;
import org.cmdbuild.dao.entry.DBEntry;
import org.cmdbuild.dao.entry.DBRelation;
import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.CMEntryType;
import org.cmdbuild.dao.entrytype.DBClass;
import org.cmdbuild.dao.entrytype.DBDomain;
import org.cmdbuild.dao.entrytype.DBEntryType;
import org.cmdbuild.dao.entrytype.attributetype.CMAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.TextAttributeType;
import org.cmdbuild.dao.query.clause.QueryAliasAttribute;
import org.cmdbuild.dao.query.clause.alias.Alias;
import org.cmdbuild.dao.view.DBDataView.DBAttributeDefinition;
import org.cmdbuild.dao.view.DBDataView.DBClassDefinition;
import org.cmdbuild.dao.view.DBDataView.DBDomainDefinition;

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
		final DBClassDefinition definition = mock(DBClassDefinition.class);
		when(definition.getName()).thenReturn(name);
		when(definition.getParent()).thenReturn(parent);
		when(definition.isHoldingHistory()).thenReturn(true);
		return definition;
	}

	protected DBClassDefinition newSimpleClass(final String name) {
		final DBClassDefinition definition = mock(DBClassDefinition.class);
		when(definition.getName()).thenReturn(name);
		when(definition.isHoldingHistory()).thenReturn(false);
		return definition;
	}

	protected DBClassDefinition newSuperClass(final String name, final DBClass parent) {
		final DBClassDefinition definition = mock(DBClassDefinition.class);
		when(definition.getName()).thenReturn(name);
		when(definition.getParent()).thenReturn(parent);
		when(definition.isSuperClass()).thenReturn(true);
		when(definition.isHoldingHistory()).thenReturn(true);
		return definition;
	}

	protected DBDomainDefinition newDomain(final String name, final DBClass class1, final DBClass class2) {
		final DBDomainDefinition definition = mock(DBDomainDefinition.class);
		when(definition.getName()).thenReturn(name);
		when(definition.getClass1()).thenReturn(class1);
		when(definition.getClass2()).thenReturn(class2);
		return definition;
	}

	protected DBAttributeDefinition newTextAttribute(final String name, final DBEntryType owner) {
		final DBAttributeDefinition definition = mock(DBAttributeDefinition.class);
		when(definition.getName()).thenReturn(name);
		when(definition.getOwner()).thenReturn(owner);
		when(definition.getMode()).thenReturn(CMAttribute.Mode.WRITE);
		when(definition.isActive()).thenReturn(true);
		when(definition.getType()).thenReturn((CMAttributeType) new TextAttributeType());
		return definition;
	}

}
