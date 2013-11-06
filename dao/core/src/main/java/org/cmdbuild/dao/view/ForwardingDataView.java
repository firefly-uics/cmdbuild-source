package org.cmdbuild.dao.view;

import java.util.Map;

import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entry.CMCard.CMCardDefinition;
import org.cmdbuild.dao.entry.CMRelation;
import org.cmdbuild.dao.entry.CMRelation.CMRelationDefinition;
import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.CMClass.CMClassDefinition;
import org.cmdbuild.dao.entrytype.CMDomain;
import org.cmdbuild.dao.entrytype.CMDomain.CMDomainDefinition;
import org.cmdbuild.dao.entrytype.CMEntryType;
import org.cmdbuild.dao.entrytype.CMIdentifier;
import org.cmdbuild.dao.function.CMFunction;
import org.cmdbuild.dao.query.CMQueryResult;
import org.cmdbuild.dao.query.QuerySpecs;
import org.cmdbuild.dao.query.QuerySpecsBuilder;
import org.cmdbuild.dao.query.clause.where.WhereClause;

public class ForwardingDataView implements CMDataView {

	private final CMDataView inner;

	public ForwardingDataView(final CMDataView dataView) {
		this.inner = dataView;
	}

	@Override
	public CMClass findClass(final Long id) {
		return inner.findClass(id);
	}

	@Override
	public CMClass findClass(final String name) {
		return inner.findClass(name);
	}

	@Override
	public CMClass findClass(final CMIdentifier identifier) {
		return inner.findClass(identifier);
	}

	@Override
	public Iterable<? extends CMClass> findClasses() {
		return inner.findClasses();
	}

	@Override
	public CMClass create(final CMClassDefinition definition) {
		return inner.create(definition);
	}

	@Override
	public CMClass update(final CMClassDefinition definition) {
		return inner.update(definition);
	}

	@Override
	public CMAttribute createAttribute(final CMAttributeDefinition definition) {
		return inner.createAttribute(definition);
	}

	@Override
	public CMAttribute updateAttribute(final CMAttributeDefinition definition) {
		return inner.updateAttribute(definition);
	}

	@Override
	public void delete(final CMAttribute attribute) {
		inner.delete(attribute);
	}

	@Override
	public CMDomain findDomain(final Long id) {
		return inner.findDomain(id);
	}

	@Override
	public CMDomain findDomain(final String name) {
		return inner.findDomain(name);
	}
	
	@Override
	public CMDomain findDomain(final CMIdentifier identifier) {
		return inner.findDomain(identifier);
	}

	@Override
	public Iterable<? extends CMDomain> findDomains() {
		return inner.findDomains();
	}

	@Override
	public Iterable<? extends CMDomain> findDomainsFor(final CMClass type) {
		return inner.findDomainsFor(type);
	}

	@Override
	public CMDomain create(final CMDomainDefinition definition) {
		return inner.create(definition);
	}

	@Override
	public CMDomain update(final CMDomainDefinition definition) {
		return inner.update(definition);
	}

	@Override
	public CMFunction findFunctionByName(final String name) {
		return inner.findFunctionByName(name);
	}

	@Override
	public Iterable<? extends CMFunction> findAllFunctions() {
		return inner.findAllFunctions();
	}

	@Override
	public void delete(final CMEntryType entryType) {
		inner.delete(entryType);
	}

	@Override
	public CMCardDefinition createCardFor(final CMClass type) {
		return inner.createCardFor(type);
	}

	@Override
	public CMCardDefinition update(final CMCard card) {
		return inner.update(card);
	}

	@Override
	public void delete(final CMCard card) {
		inner.delete(card);
	}

	@Override
	public CMRelationDefinition createRelationFor(final CMDomain domain) {
		return inner.createRelationFor(domain);
	}

	@Override
	public CMRelationDefinition update(final CMRelation relation) {
		return inner.update(relation);
	}

	@Override
	public void delete(final CMRelation relation) {
		inner.delete(relation);
	}

	@Override
	public QuerySpecsBuilder select(final Object... attrDef) {
		return inner.select(attrDef);
	}

	@Override
	public CMQueryResult executeQuery(final QuerySpecs querySpecs) {
		return inner.executeQuery(querySpecs);
	}

	@Override
	public void clear(final CMEntryType type) {
		inner.clear(type);
	}

	@Override
	public CMClass getActivityClass() {
		return inner.getActivityClass();
	}

	@Override
	public CMClass getReportClass() {
		return inner.getReportClass();
	}

	@Override
	public Iterable<? extends WhereClause> getAdditionalFiltersFor(final CMEntryType classToFilter) {
		return inner.getAdditionalFiltersFor(classToFilter);
	}

	@Override
	public Map<String, String> getAttributesPrivilegesFor(final CMEntryType entryType) {
		return inner.getAttributesPrivilegesFor(entryType);
	}

}
