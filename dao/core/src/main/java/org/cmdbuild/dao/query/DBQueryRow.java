package org.cmdbuild.dao.query;

import java.util.HashMap;
import java.util.Map;

import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entry.CMValueSet;
import org.cmdbuild.dao.entry.DBCard;
import org.cmdbuild.dao.entry.DBEntry;
import org.cmdbuild.dao.entry.DBFunctionCallOutput;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.CMDomain;
import org.cmdbuild.dao.query.clause.QueryRelation;
import org.cmdbuild.dao.query.clause.alias.Alias;
import org.cmdbuild.dao.query.clause.alias.EntryTypeAlias;

/*
 * Note: Mutable classes because it is supposed to be used by driver
 *       implementations.
 *
 * TODO Consider named attributes not bounded to a class or a domain to
 *      allow functions or aliases for other attributes
 */
public class DBQueryRow implements CMQueryRow {

	private Long number;
	private final Map<Alias, DBCard> cards;
	private final Map<Alias, QueryRelation> relations;
	private final Map<Alias, DBFunctionCallOutput> other;

	// Should we have a reference to the QuerySpecs?
	public DBQueryRow() {
		cards = new HashMap<Alias, DBCard>();
		relations = new HashMap<Alias, QueryRelation>();
		other = new HashMap<Alias, DBFunctionCallOutput>();
	}

	@Override
	public Long getNumber() {
		return number;
	}

	public void setNumber(final Long number) {
		this.number = number;
	}

	public void setCard(final Alias alias, final DBCard card) {
		cards.put(alias, card);
	}

	public void setRelation(final Alias alias, final QueryRelation relation) {
		relations.put(alias, relation);
	}

	public void setFunctionCallOutput(final Alias alias, final DBFunctionCallOutput functionCallOutput) {
		other.put(alias, functionCallOutput);
	}

	public void setValue(final Alias alias, final String key, final Object value) {
		getEntry(alias).setOnly(key, value);
	}

	private DBEntry getEntry(final Alias alias) {
		if (cards.containsKey(alias)) {
			return cards.get(alias);
		} else if (relations.containsKey(alias)) {
			return relations.get(alias).getRelation();
		} else {
			throw missingAlias(alias);
		}
	}

	@Override
	public CMCard getCard(final CMClass type) {
		return getCard(EntryTypeAlias.canonicalAlias(type));
	}

	@Override
	public CMCard getCard(final Alias alias) {
		if (cards.containsKey(alias)) {
			return cards.get(alias);
		} else {
			throw missingAlias(alias);
		}
	}

	@Override
	public QueryRelation getRelation(final Alias alias) {
		if (relations.containsKey(alias)) {
			return relations.get(alias);
		} else {
			throw missingAlias(alias);
		}
	}

	@Override
	public QueryRelation getRelation(final CMDomain type) {
		return getRelation(EntryTypeAlias.canonicalAlias(type));
	}

	@Override
	public CMValueSet getValueSet(final Alias alias) {
		if (other.containsKey(alias)) {
			return other.get(alias);
		} else {
			return getEntry(alias);
		}
	}

	private RuntimeException missingAlias(final Alias alias) {
		return new IllegalArgumentException("missing alias " + alias);
	}

}
